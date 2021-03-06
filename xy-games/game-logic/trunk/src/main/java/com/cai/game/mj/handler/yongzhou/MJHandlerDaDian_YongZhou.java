package com.cai.game.mj.handler.yongzhou;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.cai.common.constant.GameConstants;
import com.cai.common.constant.MsgConstants;
import com.cai.future.GameSchedule;
import com.cai.game.mj.handler.AbstractMJHandler;

import protobuf.clazz.Protocol.Int32ArrayResponse;
import protobuf.clazz.Protocol.RoomResponse;
import protobuf.clazz.Protocol.TableResponse;
import protobuf.clazz.Protocol.WeaveItemResponse;
import protobuf.clazz.Protocol.WeaveItemResponseArrayResponse;

/**
 * 打点选癞子牌
 * 
 * @author Administrator
 *
 */
public class MJHandlerDaDian_YongZhou extends AbstractMJHandler<MJTable_YongZhou> {

	protected int _da_dian_card;

	protected int _banker;

	public void reset_status(int banker) {
		_banker = banker;
	}

	@Override
	public void exe(MJTable_YongZhou table) {

		// 从牌堆拿出一张牌
		table._send_card_count++;
		int index = table._all_card_len - table.GRR._left_card_count;
		_da_dian_card = table._repertory_card[index];
		while (true) {
			if (table._logic.get_card_color(_da_dian_card) < 3) {
				break;
			}
			_da_dian_card = table._repertory_card[++index];
		}
		// 显示
		table.operate_show_card(_banker, GameConstants.Show_Card_Center, 1, new int[] { _da_dian_card }, GameConstants.INVALID_SEAT);
		--table.GRR._left_card_count;

		if (table.DEBUG_CARDS_MODE)
			_da_dian_card = 0x07;//

		int card_next = _da_dian_card;
		int cur_data = table._logic.get_card_value(_da_dian_card);
		if (cur_data == 1) {
			card_next = _da_dian_card + 1;
		} else if (cur_data == 9) {
			card_next = _da_dian_card - 8;
		} else {
			card_next = _da_dian_card + 1;
		}
		int card_data[] = { 0 };

		// 添加鬼
		table._logic.clean_magic_cards();
		table._logic.add_magic_card_index(table._logic.switch_to_card_index(card_next));
		card_data[0] = card_next;
		table._logic.sort_card_list(card_data);

		table.GRR._especial_card_count = 1;
		table.GRR._especial_show_cards[0] = card_next + GameConstants.CARD_ESPECIAL_TYPE_WANG_BA;

		// 延迟调度打点结束
		GameSchedule.put(new Runnable() {

			@Override
			public void run() {
				ReentrantLock lock = table.getRoomLock();
				try {
					lock.lock();
					table.operate_show_card(_banker, GameConstants.Show_Card_Center, 0, null, GameConstants.INVALID_SEAT);

					// 刷新有癞子的牌
					for (int i = 0; i < GameConstants.GAME_PLAYER; i++) {
						// 刷新自己手牌
						Tuple<Integer, List<Integer>> t = table.switch_to_cards_data(i);
						int hand_card_count = t.getLeft();
						Integer[] temp = t.getRight().toArray(new Integer[t.getRight().size()]);

						int cards[] = new int[GameConstants.MAX_COUNT];
						for (int j = 0; j < GameConstants.MAX_COUNT; j++) {
							cards[j] = temp[j];
						}
						table.operate_player_cards(i, hand_card_count, cards, 0, null);
					}

					// 检测听牌
					for (int i = 0; i < table.getTablePlayerNumber(); i++) {
						table._playerStatus[i]._hu_card_count = table.get_ting_card(table._playerStatus[i]._hu_cards, table.GRR._cards_index[i],
								table.GRR._weave_items[i], table.GRR._weave_count[i], i);
						if (table._playerStatus[i]._hu_card_count > 0) {
							table.operate_chi_hu_cards(i, table._playerStatus[i]._hu_card_count, table._playerStatus[i]._hu_cards);
						}
					}

					boolean is_qishou_hu = false;
					// 起手4个红中
					if (table.GRR._cards_index[_banker][table._logic.get_magic_card_index(0)] == 4) {

						table._playerStatus[_banker].add_action(GameConstants.WIK_ZI_MO);
						table._playerStatus[_banker].add_zi_mo(table._logic.switch_to_card_data(table._logic.get_magic_card_index(0)), _banker);
						table.GRR._chi_hu_rights[_banker].opr_or(GameConstants.CHR_ZI_MO);
						table.GRR._chi_hu_rights[_banker].opr_or(GameConstants.CHR_HUNAN_PRIVATE_QI_SHOU_HU);
						table.exe_qishou_hun(_banker);
						is_qishou_hu = true;
					}

					if (is_qishou_hu == false) {
						table.exe_dispatch_card(_banker, GameConstants.WIK_NULL, GameConstants.DELAY_SEND_CARD_DELAY);
					}
				} finally {
					lock.unlock();
				}
			}
		}, 3, TimeUnit.SECONDS);
		// table.exe_dispatch_card(_banker, GameConstants.WIK_NULL, 0);
	}

	@Override
	public boolean handler_player_be_in_room(MJTable_YongZhou table, int seat_index) {
		RoomResponse.Builder roomResponse = RoomResponse.newBuilder();
		roomResponse.setType(MsgConstants.RESPONSE_RECONNECT_DATA);

		TableResponse.Builder tableResponse = TableResponse.newBuilder();

		table.load_room_info_data(roomResponse);
		table.load_player_info_data(roomResponse);
		table.load_common_status(roomResponse);

		// 游戏变量
		tableResponse.setBankerPlayer(table.GRR._banker_player);
		tableResponse.setCurrentPlayer(table.GRR._banker_player);
		tableResponse.setCellScore(0);

		// 状态变量
		tableResponse.setActionCard(0);
		// tableResponse.setActionMask((_response[seat_index] == false) ?
		// _player_action[seat_index] : MJGameConstants.WIK_NULL);

		// 历史记录
		tableResponse.setOutCardData(0);
		tableResponse.setOutCardPlayer(0);

		for (int i = 0; i < GameConstants.GAME_PLAYER; i++) {
			tableResponse.addTrustee(false);// 是否托管
			// 剩余牌数
			tableResponse.addDiscardCount(table.GRR._discard_count[i]);
			Int32ArrayResponse.Builder int_array = Int32ArrayResponse.newBuilder();
			for (int j = 0; j < 55; j++) {
				if (table._logic.is_magic_card(table.GRR._discard_cards[i][j])) {
					// 癞子
					int_array.addItem(table.GRR._discard_cards[i][j] + GameConstants.CARD_ESPECIAL_TYPE_GUI);
				} else {
					int_array.addItem(table.GRR._discard_cards[i][j]);
				}

			}
			tableResponse.addDiscardCards(int_array);

			// 组合扑克
			tableResponse.addWeaveCount(table.GRR._weave_count[i]);
			WeaveItemResponseArrayResponse.Builder weaveItem_array = WeaveItemResponseArrayResponse.newBuilder();
			for (int j = 0; j < GameConstants.MAX_WEAVE; j++) {
				WeaveItemResponse.Builder weaveItem_item = WeaveItemResponse.newBuilder();
				if (table._logic.is_magic_card(table.GRR._weave_items[i][j].center_card)) {
					weaveItem_item.setCenterCard(table.GRR._weave_items[i][j].center_card + GameConstants.CARD_ESPECIAL_TYPE_GUI);
				} else {
					weaveItem_item.setCenterCard(table.GRR._weave_items[i][j].center_card);
				}
				weaveItem_item.setProvidePlayer(table.GRR._weave_items[i][j].provide_player);
				weaveItem_item.setPublicCard(table.GRR._weave_items[i][j].public_card);
				weaveItem_item.setWeaveKind(table.GRR._weave_items[i][j].weave_kind);
				weaveItem_array.addWeaveItem(weaveItem_item);
			}
			tableResponse.addWeaveItemArray(weaveItem_array);

			//
			tableResponse.addWinnerOrder(0);

			// 牌
			tableResponse.addCardCount(table._logic.get_card_count_by_index(table.GRR._cards_index[i]));
		}

		// 数据
		tableResponse.setSendCardData(0);
		Tuple<Integer, List<Integer>> t = table.switch_to_cards_data(seat_index);
		int hand_card_count = t.getLeft();
		Integer[] temp = t.getRight().toArray(new Integer[t.getRight().size()]);
		int cards[] = new int[GameConstants.MAX_COUNT];
		for (int i = 0; i < GameConstants.MAX_COUNT; i++) {
			cards[i] = temp[i];
		}

		for (int i = 0; i < GameConstants.MAX_COUNT; i++) {
			tableResponse.addCardsData(cards[i]);
		}

		roomResponse.setTable(tableResponse);

		table.send_response_to_player(seat_index, roomResponse);

		// 显示
		table.operate_show_card(_banker, GameConstants.Show_Card_Center, 1, new int[_da_dian_card], seat_index);

		return true;
	}
}
