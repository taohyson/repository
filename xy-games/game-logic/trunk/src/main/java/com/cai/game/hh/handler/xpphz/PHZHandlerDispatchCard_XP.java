package com.cai.game.hh.handler.xpphz;

import java.util.concurrent.TimeUnit;

import com.cai.common.constant.GameConstants;
import com.cai.common.constant.MsgConstants;
import com.cai.common.constant.game.mj.GameConstants_XiangXiang;
import com.cai.common.domain.ChiHuRight;
import com.cai.common.domain.PlayerStatus;
import com.cai.common.domain.SysParamModel;
import com.cai.dictionary.SysParamDict;
import com.cai.future.GameSchedule;
import com.cai.future.runnable.GameFinishRunnable;
import com.cai.future.runnable.HuPaiRunnable;
import com.cai.future.runnable.OutCardRunnable;
import com.cai.game.hh.handler.HHHandlerDispatchCard;

import protobuf.clazz.Protocol.Int32ArrayResponse;
import protobuf.clazz.Protocol.RoomResponse;
import protobuf.clazz.Protocol.TableResponse;
import protobuf.clazz.Protocol.WeaveItemResponse;
import protobuf.clazz.Protocol.WeaveItemResponseArrayResponse;

/**
 * 摸牌
 * 
 * @author admin
 *
 */
public class PHZHandlerDispatchCard_XP extends HHHandlerDispatchCard<HHTable_XP> {

	@SuppressWarnings("static-access")
	@Override
	public void exe(HHTable_XP table) {
		XPPHZUtils.cleanPlayerStatus(table);
		if (XPPHZUtils.endHuangZhuang(table, true)) {
			return;
		}

		PlayerStatus curPlayerStatus = table._playerStatus[_seat_index];
		curPlayerStatus.reset();

		int temp[] = new int[GameConstants.MAX_HH_INDEX];
		for (int i = 0; i < GameConstants.MAX_HH_INDEX; i++) {
			temp[i] = table.GRR._cards_index[_seat_index][i] + table.GRR._cards_index[_seat_index][i + 20];
		}

		int an_long_Index[] = new int[5];
		int an_long_count = 0;
		// 玩家出牌 响应判断,是否有提 暗龙
		for (int i = 0; i < GameConstants.MAX_HH_INDEX; i++) {
			if (temp[i] == 4) {
				an_long_Index[an_long_count++] = i;
			}
		}
		if (an_long_count > 0) {
			int _action = GameConstants.WIK_AN_LONG;
			// 效果
			table.operate_effect_action(_seat_index, GameConstants.EFFECT_ACTION_TYPE_ACTION, 1, new long[] { _action },
					1, GameConstants.INVALID_SEAT);

			for (int i = 0; i < an_long_count; i++) {
				int cbWeaveIndex = table.GRR._weave_count[_seat_index];
				table.GRR._weave_items[_seat_index][cbWeaveIndex].public_card = 1;
				table.GRR._weave_items[_seat_index][cbWeaveIndex].center_card = table._logicXP
						.switch_to_card_data(an_long_Index[i]);
				table.GRR._weave_items[_seat_index][cbWeaveIndex].weave_kind = GameConstants.WIK_AN_LONG;
				table.GRR._weave_items[_seat_index][cbWeaveIndex].provide_player = _seat_index;
				table.GRR._weave_items[_seat_index][cbWeaveIndex].hu_xi = table._logicXP
						.get_weave_hu_xi(table.GRR._weave_items[_seat_index][cbWeaveIndex]);

				for (int k = 0; k < 4; k++) {
					table.GRR._weave_items[_seat_index][cbWeaveIndex].weave_card[k] = table._logicXP
							.switch_to_card_data(an_long_Index[i]);
					table.GRR._weave_items[_seat_index][cbWeaveIndex].weave_card[++k] = table._logicXP
							.switch_to_card_data(an_long_Index[i]) + 32;
				}
				table.GRR._weave_count[_seat_index]++;
				table._long_count[_seat_index]++;
				// 删除手上的牌
				table.GRR._cards_index[_seat_index][an_long_Index[i]] = 0;
				table.GRR._cards_index[_seat_index][an_long_Index[i] + 20] = 0;

				table.GRR._card_count[_seat_index] = table._logicXP
						.get_card_count_by_index(table.GRR._cards_index[_seat_index]);

				table.GRR._card_count[_seat_index] = table._logicXP.get_card_count_by_index(temp);

			}
			// 刷新手牌包括组合
			int cards[] = new int[GameConstants.MAX_HH_COUNT];
			int hand_card_count = table._logicXP.switch_to_cards_data(table.GRR._cards_index[_seat_index], cards);
			table.operate_player_cards(_seat_index, hand_card_count, cards, table.GRR._weave_count[_seat_index],
					table.GRR._weave_items[_seat_index]);
		}

		if (an_long_count >= 2) {
			table._ti_two_long[_seat_index] = true;
		}
		XPPHZUtils.ting_basic(table, _seat_index);
		table._current_player = _seat_index;// 轮到操作的人是自己

		// 从牌堆拿出一张牌
		table._send_card_count++;
		_send_card_data = table._repertory_card[table._all_card_len - table.GRR._left_card_count];
		if (table.DEBUG_CARDS_MODE) {
			_send_card_data = 0x1a;
		}

		--table.GRR._left_card_count;
		table._last_card = _send_card_data;
		// 用户是否可以提扫
		int card_type = GameConstants.CHR_ZI_MO;
		int ti_sao = table.estimate_player_ti_wei_respond_phz(_seat_index, _send_card_data);

		if (ti_sao != GameConstants.WIK_NULL) {
			table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, GameConstants.INVALID_SEAT,
					false);
			table.cards_has_wei[table._logicXP.switch_to_card_index(_send_card_data) % 20] = 1; // 统一用20以下的就可以表示了
			return;
		}
		// 发牌处理,判断发给的这个人有没有胡牌或杠牌
		// 胡牌判断
		ChiHuRight chr[] = new ChiHuRight[table.getTablePlayerNumber()];
		for (int i = 0; i < table.getTablePlayerNumber(); i++) {
			chr[i] = table.GRR._chi_hu_rights[i];
			chr[i].set_empty();
		}
		int bHupai = 0;

		int action_hu[] = new int[table.getTablePlayerNumber()];
		int action_pao[] = new int[table.getTablePlayerNumber()];
		int pao_type[][] = new int[table.getTablePlayerNumber()][1];

		int loop = 0;
		while (loop < table.getTablePlayerNumber()) {
			int i = (_seat_index + loop) % table.getTablePlayerNumber();
			loop++;
			int hu_xi_chi[] = new int[1];
			hu_xi_chi[0] = 0;
			PlayerStatus tempPlayerStatus = table._playerStatus[i];
			tempPlayerStatus.reset();
			boolean canHu = true;// 箍臭不能胡牌
			if (table.guoHu[i] && i != _seat_index) {
				canHu = false;
			}
			if (table.gu[i]) { // 箍臭不能胡牌
				canHu = false;
			}
			if (canHu) {
				action_hu[i] = table.analyse_chi_hu_card(table.GRR._cards_index[i], table.GRR._weave_items[i],
						table.GRR._weave_count[i], i, _seat_index, _send_card_data, chr[i], card_type, hu_xi_chi, true);// 分析胡牌
			}
			action_pao[i] = table.estimate_player_respond_phz(i, _seat_index, _send_card_data, pao_type[i], true);
			if (table._is_xiang_gong[i] == true) {
				action_hu[i] = GameConstants.WIK_NULL;
			}
			if (action_hu[i] != GameConstants.WIK_NULL) {
				tempPlayerStatus.add_action(GameConstants.WIK_ZI_MO);
				tempPlayerStatus.add_zi_mo(_send_card_data, i);

				if (action_pao[i] != GameConstants.WIK_PAO) {
					tempPlayerStatus.add_action(GameConstants.WIK_NULL);
					tempPlayerStatus.add_pass(_send_card_data, i);
				} else {
					tempPlayerStatus.add_action(GameConstants.WIK_PAO);
					tempPlayerStatus.add_pao(_send_card_data, i);
				}
				if (bHupai == 0) {
					table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data },
							GameConstants.INVALID_SEAT, false);
				}

				ti_sao = GameConstants.WIK_ZI_MO;
				bHupai = 1;
			} else {
				chr[i].set_empty();
			}
		}

		if (ti_sao != GameConstants.WIK_NULL && ti_sao != GameConstants.WIK_ZI_MO) {
			return;
		}

		for (int i = 0; i < table.getTablePlayerNumber(); i++) {
			if ((action_pao[i] != GameConstants.WIK_NULL) && (bHupai == 0)) {
				ti_sao = GameConstants.WIK_PAO;
				table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, GameConstants.INVALID_SEAT,
						false);
				table.exe_gang(i, _seat_index, _send_card_data, action_pao[i], pao_type[i][0], true, true, false, 1000);
				return;
			} else if (action_pao[i] != GameConstants.WIK_NULL) {
				ti_sao = GameConstants.WIK_PAO;
			}
		}

		// 玩家出牌 响应判断,是否有吃,碰,胡
		boolean bAroseAction = false;
		if (ti_sao != GameConstants.WIK_PAO) {
			bAroseAction = table.estimate_player_out_card_respond_xpphz(_seat_index, _send_card_data, true,
					table._logicXP);
			if (bHupai == 0) {
				table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, GameConstants.INVALID_SEAT,
						false);
			}
		}

		if ((bAroseAction == false) && (ti_sao == GameConstants.WIK_NULL)) {
			table.operate_player_action(_seat_index, true);
		} else {
			// WalkerGeek 检测是否需要胡按钮
			boolean needAction = true;
			for (int i = 0; i < table.getTablePlayerNumber(); i++) {
				if (table._playerStatus[i].has_action()) {
					if (table.has_rule(GameConstants.GAME_RULE_XP_ZI_MO_QIANG_ZHI_HU) && i == _seat_index
							&& table._playerStatus[i].has_zi_mo()) {
						needAction = false;
					}
				}

			}
			if (needAction) {
				// 等待别人操作这张牌
				for (int i = 0; i < table.getTablePlayerNumber(); i++) {
					if (table._playerStatus[i].has_action()) {
						table._playerStatus[i].set_status(GameConstants.Player_Status_OPR_CARD);
						// 操作状态
						table.operate_player_action(i, false);
						// WalkerGeek 自摸强制胡牌
						if (table.has_rule(GameConstants.GAME_RULE_XP_ZI_MO_QIANG_ZHI_HU) && i == _seat_index
								&& table._playerStatus[i].has_zi_mo()) {
							GameSchedule.put(
									new HuPaiRunnable(table.getRoom_id(), _seat_index, GameConstants.WIK_ZI_MO,
											_send_card_data),
									GameConstants.DELAY_AUTO_OUT_CARD_TRUTESS, TimeUnit.MILLISECONDS);
						}
					}
				}
			}
		}

		// 托管状态
		if (table.isTrutess(_seat_index)) {
			GameSchedule.put(new OutCardRunnable(table.getRoom_id(), _seat_index, _send_card_data),
					GameConstants.DELAY_AUTO_OUT_CARD_TRUTESS, TimeUnit.MILLISECONDS);
			return;
		}
		if (curPlayerStatus.has_action()) {// 有动作
			if (table.has_rule(GameConstants.GAME_RULE_XP_ZI_MO_QIANG_ZHI_HU) && curPlayerStatus.has_zi_mo()) {
				GameSchedule.put(
						new HuPaiRunnable(table.getRoom_id(), _seat_index, GameConstants.WIK_ZI_MO, _send_card_data),
						GameConstants.DELAY_AUTO_OUT_CARD_TRUTESS, TimeUnit.MILLISECONDS);
			} else {
				curPlayerStatus.set_status(GameConstants.Player_Status_OPR_CARD);// 操作状态
				table.operate_player_action(_seat_index, false);
			}
		} else {
			// 不能换章,自动出牌
			if (table._playerStatus[_seat_index].lock_huan_zhang()) {
				GameSchedule.put(new OutCardRunnable(table.getRoom_id(), _seat_index, _send_card_data),
						GameConstants.DELAY_AUTO_OUT_CARD, TimeUnit.MILLISECONDS);
			} else if (ti_sao == GameConstants.WIK_NULL) {
				if (bAroseAction == false) {
					table.operate_player_get_card(_seat_index, 0, null, GameConstants.INVALID_SEAT, false);
					// 没有人要就加入到牌堆
					int discard_time = 2000;
					int gameId = table.getGame_id() == 0 ? 5 : table.getGame_id();
					SysParamModel sysParamModel1104 = SysParamDict.getInstance()
							.getSysParamModelDictionaryByGameId(gameId).get(1104);
					if (sysParamModel1104 != null && sysParamModel1104.getVal4() > 0
							&& sysParamModel1104.getVal4() < 10000) {
						discard_time = sysParamModel1104.getVal4();
					}

					if (table._last_card != 0) {
						table.exe_add_discard(_seat_index, 1, new int[] { table._last_card }, true, discard_time);
					}

					// 显示出牌
					int next_player = (_seat_index + table.getTablePlayerNumber() + 1) % table.getTablePlayerNumber();
					// 过张的牌都不可以
					table._cannot_chi[_seat_index][table._cannot_chi_count[_seat_index]++] = _send_card_data;
					table._cannot_chi[next_player][table._cannot_chi_count[next_player]++] = _send_card_data;

					table._current_player = next_player;
					_seat_index = next_player;
					// 延时5秒发牌
					int dispatch_time = 3000;
					if (sysParamModel1104 != null && sysParamModel1104.getVal5() > 0
							&& sysParamModel1104.getVal5() < 10000) {
						dispatch_time = sysParamModel1104.getVal5();
					}
					table.exe_dispatch_card(next_player, GameConstants.WIK_NULL, dispatch_time);
					table._last_card = _send_card_data;
					table._last_player = table._current_player;
				} else {
					// 过张的牌都不可以
					table._cannot_chi[_seat_index][table._cannot_chi_count[_seat_index]++] = _send_card_data;
					table._cannot_peng[_seat_index][table._cannot_peng_count[_seat_index]++] = _send_card_data;
				}
			}
		}

		return;
	}

	@Override
	public boolean handler_player_out_card(HHTable_XP table, int seat_index, int card) {
		// TODO Auto-generated method stub
		if (table.is_card_has_wei(card)) {
			table.has_shoot[seat_index] = true;
		}
		// 错误断言
		card = table.get_real_card(card);

		if (table._logicXP.is_valid_card(card) == false) {
			table.log_error("出牌,牌型出错");
			return false;
		}

		// 效验参数
		if (seat_index != _seat_index) {
			table.log_error("出牌,没到出牌");
			return false;
		}
		if (table._playerStatus[_seat_index].get_status() != GameConstants.Player_Status_OUT_CARD) {
			table.log_error("状态不对不能出牌");
			return false;
		}
		if ((table.GRR._cards_index[_seat_index][table._logicXP.switch_to_card_index(table._logicXP.toLowCard(card))]
				+ table.GRR._cards_index[_seat_index][table._logicXP
						.switch_to_card_index(table._logicXP.toUpCard(card))]) >= 3) {
			// 刷新手牌
			int cards[] = new int[GameConstants.MAX_HH_COUNT];
			// 显示出牌
			table.operate_out_card(_seat_index, 0, null, GameConstants.OUT_CARD_TYPE_MID, GameConstants.INVALID_SEAT);
			// 刷新自己手牌
			int hand_card_count = table._logicXP.switch_to_cards_data(table.GRR._cards_index[_seat_index], cards);
			table.operate_player_cards(_seat_index, hand_card_count, cards, table.GRR._weave_count[_seat_index],
					table.GRR._weave_items[_seat_index]);

			table.log_error(_seat_index + "出牌出错 HHHandlerDispatchCard " + _seat_index);
			return true;
		}
		// 删除扑克
		if (table._logicXP.remove_card_by_index(table.GRR._cards_index[_seat_index], card) == false) {
			table.log_error("出牌删除出错");
			return false;
		}

		// 出牌
		table.exe_out_card(_seat_index, card, GameConstants.WIK_NULL);

		return true;
	}

	/***
	 * //用户操作
	 * 
	 * @param seat_index
	 * @param operate_code
	 * @param operate_card
	 * @return
	 */
	@Override
	public boolean handler_operate_card(HHTable_XP table, int seat_index, int operate_code, int operate_card,
			int luoCode) {
		PlayerStatus playerStatus = table._playerStatus[seat_index];

		// 校验操作
		if ((operate_code != GameConstants.WIK_NULL) && (playerStatus.has_action_by_code(operate_code) == false)) {
			return false;
		}
		table.record_effect_action(seat_index, GameConstants.EFFECT_ACTION_TYPE_ACTION, 1, new long[] { operate_code },
				1);
		if (operate_code == GameConstants.WIK_NULL) {
			if (playerStatus.has_zi_mo() == true) {
				int index = -1;
				for (int i = 0; i < table._guo_hu_pai_count[seat_index]; i++) {
					if (table._guo_hu_pai_cards[seat_index][i] == table._logicXP.toLowCard(operate_card)) {
						index = i;
					}
				}
				if (index == -1) {
					index = table._guo_hu_pai_count[seat_index]++;
				}
				table._guo_hu_pai_cards[seat_index][index] = table._logicXP.toLowCard(operate_card);

				int all_hu_xi = 0;
				for (int i = 0; i < table._hu_weave_count[seat_index]; i++) {
					all_hu_xi += table._hu_weave_items[seat_index][i].hu_xi;
				}
				table._guo_hu_xi[seat_index][index] = all_hu_xi;
			}
		}
		// 是否已经响应
		if (playerStatus.has_action() == false) {
			table.log_player_error(seat_index, "DispatchCard 出牌,玩家操作已失效");
			return true;
		}
		// 是否已经响应
		if (playerStatus.is_respone()) {
			table.log_player_error(seat_index, "DispatchCard 出牌,玩家已操作");
			return true;
		}
		if (operate_card != this._send_card_data) {
			table.log_player_error(seat_index, "DispatchCard 操作牌，与当前牌不一样");
			return true;
		}

		if (table._playerStatus[seat_index].has_action_by_code(GameConstants.WIK_ZI_MO)) {
			if (_seat_index != seat_index) {
				table.guoHu[seat_index] = true;
			}
		}

		// 记录玩家的操作
		playerStatus.operate(operate_code, operate_card);
		playerStatus.clean_status();

		if (operate_code == GameConstants.WIK_NULL) {
			boolean flag = false;
			for (int i = 0; i < playerStatus._action_count; i++) {
				switch (playerStatus._action[i]) {
				case GameConstants.WIK_LEFT:
				case GameConstants.WIK_CENTER:
				case GameConstants.WIK_RIGHT:
				case GameConstants.WIK_XXD:
				case GameConstants.WIK_DDX:
				case GameConstants.WIK_EQS:
					if (!flag) {
						table._cannot_chi[seat_index][table._cannot_chi_count[seat_index]++] = table._logicXP
								.toLowCard(operate_card);
						playerStatus.set_exe_pass(true);
						flag = true;
					}
					break;
				case GameConstants.WIK_PENG:
					table._cannot_peng[seat_index][table._cannot_peng_count[seat_index]++] = table._logicXP
							.toLowCard(operate_card);
					playerStatus.set_exe_pass(true);
					break;
				case GameConstants.WIK_ZI_MO:
					if (_seat_index != seat_index) {
						table.guoHu[seat_index] = true;
					}
					break;
				}
			}

		}
		// 吃操作后，是否有落
		switch (operate_code) {
		case GameConstants.WIK_LEFT:
		case GameConstants.WIK_CENTER:
		case GameConstants.WIK_RIGHT:
		case GameConstants.WIK_XXD:
		case GameConstants.WIK_DDX:
		case GameConstants.WIK_EQS:
			if (luoCode != -1) {
				playerStatus.set_lou_pai_kind(luoCode);
			}
		}

		// 变量定义 优先级最高操作的玩家和操作--不通炮的算法
		int target_player = seat_index;
		int target_action = operate_code;
		int target_lou_code = luoCode;
		int target_p = 0;
		for (int p = 0; p < table.getTablePlayerNumber(); p++) {
			int i = (_seat_index + p) % table.getTablePlayerNumber();
			if (i == target_player) {
				target_p = table.getTablePlayerNumber() - p;
			}
		}
		int cbActionRank[] = new int[3];
		for (int p = 0; p < table.getTablePlayerNumber(); p++) {
			int i = (_seat_index + p) % table.getTablePlayerNumber();
			int cbUserActionRank = 0; // 获取动作
			int cbTargetActionRank = 0; // 优先级别
			if (table._playerStatus[i].has_action()) {
				if (table._playerStatus[i].is_respone()) {
					cbUserActionRank = table._logicXP.get_action_rank(table._playerStatus[i].get_perform())
							+ table.getTablePlayerNumber() - p;
					cbActionRank[i] = cbUserActionRank; // 获取已经执行的动作的优先级
				} else {
					cbUserActionRank = table._logicXP.get_action_list_rank(table._playerStatus[i]._action_count,
							table._playerStatus[i]._action) + table.getTablePlayerNumber() - p; // 获取最大的动作的优先级
				}

				// 目标玩家
				if (table._playerStatus[target_player].is_respone()) {
					cbTargetActionRank = table._logicXP
							.get_action_rank(table._playerStatus[target_player].get_perform()) + target_p;
					cbActionRank[i] = cbUserActionRank;
				} else {
					cbTargetActionRank = table._logicXP.get_action_list_rank(
							table._playerStatus[target_player]._action_count,
							table._playerStatus[target_player]._action) + target_p;
				}

				// 优先级别 动作判断 优先级最高的人和动作
				if (cbUserActionRank > cbTargetActionRank) {
					target_player = i;// 最高级别人
					target_action = table._playerStatus[i].get_perform();
					target_lou_code = table._playerStatus[i].get_lou_kind();
					target_p = table.getTablePlayerNumber() - p;
				}
			}
		}

		// 优先级最高的人还没操作
		if (table._playerStatus[target_player].is_respone() == false) {
			return true;
		}

		// 变量定义
		int target_card = table._playerStatus[target_player]._operate_card;

		// 判断可不可以吃的上家用户
		int eat_type = GameConstants.WIK_LEFT | GameConstants.WIK_CENTER | GameConstants.WIK_RIGHT
				| GameConstants.WIK_DDX | GameConstants.WIK_XXD | GameConstants.WIK_EQS | GameConstants.WIK_YWS;
		if (target_action == GameConstants.WIK_NULL) {
			// 显示出牌
			int next_player = (_seat_index + table.getTablePlayerNumber() + 1) % table.getTablePlayerNumber();
			// 过张的牌都不可以吃
			table._cannot_chi[_seat_index][table._cannot_chi_count[_seat_index]++] = _send_card_data;
			table._cannot_chi[next_player][table._cannot_chi_count[next_player]++] = _send_card_data;
			for (int i = 0; i < table.getTablePlayerNumber(); i++) {
				for (int j = 0; j < table._playerStatus[i]._action_count; j++) {
					if (table._playerStatus[i]._action[j] == GameConstants.WIK_PENG) {
						table._cannot_peng[i][table._cannot_peng_count[i]++] = _send_card_data;
					}
				}
			}
		} else if ((target_action & eat_type) != GameConstants_XiangXiang.WIK_NULL) {
			for (int i = 0; i < table.getTablePlayerNumber(); i++) {
				for (int j = 0; j < table._playerStatus[i]._action_count; j++) {
					if (table._playerStatus[i]._action[j] == GameConstants.WIK_PENG) {
						table._cannot_peng[i][table._cannot_peng_count[i]++] = _send_card_data;
					}
				}
			}
			if (_seat_index != target_player)
				table._cannot_chi[_seat_index][table._cannot_chi_count[_seat_index]++] = _send_card_data;
		}
		// 用户状态
		for (int i = 0; i < table.getTablePlayerNumber(); i++) {

			table._playerStatus[i].clean_action();
			table._playerStatus[i].clean_status();

			table.operate_player_action(i, true);
		}

		// 判断可不可以吃的上家用户
		/*
		 * int last_player = (target_player + table.getTablePlayerNumber() + 1)
		 * % table.getTablePlayerNumber(); boolean flag = false;
		 */
		/*
		 * for (int j = 0; j < table._playerStatus[last_player]._action_count;
		 * j++) { switch (table._playerStatus[last_player]._action[j]) { case
		 * GameConstants.WIK_LEFT: case GameConstants.WIK_CENTER: case
		 * GameConstants.WIK_RIGHT: case GameConstants.WIK_XXD: case
		 * GameConstants.WIK_DDX: case GameConstants.WIK_EQS: if (target_action
		 * == GameConstants.WIK_NULL) { continue; } if (flag == false) { if
		 * (table._playerStatus[last_player].get_exe_pass() == true) { //
		 * table._cannot_chi[last_player][table._cannot_chi_count[last_player]--
		 * ] // = 0;
		 * table._cannot_chi[last_player][table._cannot_chi_count[seat_index]++]
		 * = _send_card_data; flag = true;
		 * table._playerStatus[last_player].set_exe_pass(false); } } break; } }
		 */

		// 用户状态
		for (int i = 0; i < table.getTablePlayerNumber(); i++) {
			boolean flag_temp = false;

			if (table._playerStatus[i].has_action()) {
				for (int j = 0; j < table._playerStatus[i]._action_count; j++) {
					switch (table._playerStatus[i]._action[j]) {
					case GameConstants.WIK_LEFT:
					case GameConstants.WIK_CENTER:
					case GameConstants.WIK_RIGHT:
					case GameConstants.WIK_XXD:
					case GameConstants.WIK_DDX:
					case GameConstants.WIK_EQS:
						if (!((target_action == GameConstants.WIK_PENG)
								|| (target_action == GameConstants.WIK_ZI_MO))) {
							continue;
						}
						if (flag_temp == false) {
							if (table._playerStatus[i].get_exe_pass() == true) {
								// table._cannot_chi[i][table._cannot_chi_count[i]--]
								// = 0;
								flag_temp = true;
							}
						}
						break;
					case GameConstants.WIK_PENG:
						if (!((target_action == GameConstants.WIK_NULL)
								|| (target_action & eat_type) != GameConstants.WIK_NULL)) {
							continue;
						}
						if (table._playerStatus[i].get_exe_pass() == false) {
							table._cannot_peng[i][table._cannot_peng_count[i]++] = table._logicXP
									.toLowCard(operate_card);
						}
						break;
					}
				}
			}

			table._playerStatus[i].clean_action();
			table._playerStatus[i].clean_status();

			table.operate_player_action(i, true);
		}

		// 执行动作
		switch (target_action) {
		case GameConstants.WIK_NULL: {
			// 清除用户状态
			table._playerStatus[_seat_index].clean_action();
			table._playerStatus[_seat_index].clean_status();

			if (table._playerStatus[_seat_index].lock_huan_zhang()) {
				// 显示胡牌
				for (int i = 0; i < table.getTablePlayerNumber(); i++) {
					int cards[] = new int[GameConstants.MAX_HH_COUNT];
					int hand_card_count = table._logicXP.switch_to_cards_data(table.GRR._cards_index[i], cards);

					table.operate_show_card(i, GameConstants.Show_Card_HU, hand_card_count, cards,
							table.GRR._weave_items[i], table.GRR._weave_count[i], GameConstants.INVALID_SEAT);

				}
				GameSchedule.put(new OutCardRunnable(table.getRoom_id(), _seat_index, _send_card_data),
						GameConstants.DELAY_AUTO_OUT_CARD, TimeUnit.MILLISECONDS);
			} else {
				for (int i = 0; i < table.getTablePlayerNumber(); i++) {
					int pao_type[] = new int[1];
					int action = table.estimate_player_respond_phz(i, _seat_index, _send_card_data, pao_type, true);
					if (action != GameConstants.WIK_NULL) {
						table.exe_gang(i, _seat_index, _send_card_data, action, pao_type[0], true, true, false, 1000);
						return true;
					}
				}
				table.operate_player_get_card(_seat_index, 0, null, GameConstants.INVALID_SEAT, false);

				// 要出牌，但是没有牌出设置成相公 下家用户发牌
				int pai_count = 0;
				for (int i = 0; i < GameConstants.MAX_HH_INDEX; i++) {
					if ((table.GRR._cards_index[_seat_index][i] + table.GRR._cards_index[_seat_index][i + 20]) < 3) {
						pai_count += table.GRR._cards_index[_seat_index][i]
								+ table.GRR._cards_index[_seat_index][i + 20];
					}
				}
				if (pai_count == 0) {
					table._is_xiang_gong[_seat_index] = true;
					table.operate_player_xiang_gong_flag(_seat_index, table._is_xiang_gong[_seat_index]);
					int next_player = (_seat_index + table.getTablePlayerNumber() + 1) % table.getTablePlayerNumber();
					// 用户状态
					table._playerStatus[_seat_index].clean_action();
					table._playerStatus[_seat_index].clean_status();
					table._current_player = next_player;
					table._last_player = next_player;

					table.exe_dispatch_card(next_player, GameConstants.WIK_NULL, 1000);
					return true;
				}
				table.exe_add_discard(_seat_index, 1, new int[] { _send_card_data }, true, 0);
				XPPHZUtils.setNextPlay(table, _seat_index, 0, _send_card_data,
						_seat_index + "发牌" + _seat_index + "  " + _seat_index);
			}
			return true;

		}
		case GameConstants.WIK_LEFT: { // 上吃操作
			// 删除扑克
			int cbRemoveCard[] = new int[] { target_card + 1, target_card + 2 };
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][0]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(target_player, "吃牌删除出错");
				return false;
			}

			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_RIGHT: { // 下吃操作
			// 删除扑克
			int cbRemoveCard[] = new int[] { target_card - 1, target_card - 2 };
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][2]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "吃牌删除出错");
				return false;
			}
			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_CENTER: { // 中吃操作
			// 删除扑克
			int cbRemoveCard[] = new int[] { target_card - 1, target_card + 1 };
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][1]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "吃牌删除出错");
				return false;
			}
			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_XXD: { // 吃小
			// 删除扑克
			int target_card_color = table._logicXP.get_card_color(target_card);

			int cbRemoveCard[] = new int[2];
			if (target_card_color == 0) {
				cbRemoveCard[0] = target_card;
				cbRemoveCard[1] = target_card + 16;
			} else {
				cbRemoveCard[0] = target_card - 16;
				cbRemoveCard[1] = target_card - 16;
			}
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][4]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "吃牌删除出错");
				return false;
			}
			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_DDX: { // 吃大
			// 删除扑克
			int target_card_color = table._logicXP.get_card_color(target_card);

			int cbRemoveCard[] = new int[2];
			if (target_card_color == 0) {
				cbRemoveCard[0] = target_card + 16;
				cbRemoveCard[1] = target_card + 16;
			} else {
				cbRemoveCard[0] = target_card - 16;
				cbRemoveCard[1] = target_card;
			}
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][5]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "吃牌删除出错");
				return false;
			}

			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_EQS: { // 吃二七十
			// 删除扑克
			int cbRemoveCard[] = new int[] { target_card, target_card };
			int target_card_value = table._logicXP.get_card_value(target_card);
			switch (target_card_value) {
			case 2:
				cbRemoveCard[0] = target_card + 5;
				cbRemoveCard[1] = target_card + 8;
				break;
			case 7:
				cbRemoveCard[0] = target_card - 5;
				cbRemoveCard[1] = target_card + 3;
				break;
			case 10:
				cbRemoveCard[0] = target_card - 8;
				cbRemoveCard[1] = target_card - 3;
				break;
			default:
				break;
			}
			table.add_lou_weave_xpphz(target_lou_code, target_player, target_card, _seat_index,
					table._lou_weave_item[target_player][3]);
			table.setWeaveCard(target_player, target_card, cbRemoveCard);
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "吃牌删除出错");
				return false;
			}
			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_PENG: { // 碰牌操作
			// 删除扑克
			int wIndex = table.GRR._weave_count[target_player];
			int k = 0;
			if (table.GRR._cards_index[target_player][table._logicXP
					.switch_to_card_index(table._logicXP.toLowCard(target_card))] > 1) {
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toLowCard(target_card);
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toLowCard(target_card);
			} else if (table.GRR._cards_index[target_player][table._logicXP
					.switch_to_card_index(table._logicXP.toUpCard(target_card))] > 1) {
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toLowCard(target_card);
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toLowCard(target_card);
			} else {
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toLowCard(target_card);
				table.GRR._weave_items[target_player][wIndex].weave_card[k++] = table._logicXP.toUpCard(target_card);
			}
			table.GRR._weave_items[target_player][wIndex].weave_card[k++] = target_card;

			int cbRemoveCard[] = new int[] { target_card, target_card };
			if (!table._logicXP.remove_cards_by_index(table.GRR._cards_index[target_player], cbRemoveCard, 2)) {
				table.log_player_error(seat_index, "碰牌删除出错");
				return false;
			}

			table.exe_chi_peng(target_player, _seat_index, target_action, target_card,
					GameConstants.CHI_PENG_TYPE_DISPATCH, target_lou_code);
			return true;
		}
		case GameConstants.WIK_PAO: { // 杠牌操作
			int pao_type[] = new int[1];
			int action = table.estimate_player_respond_phz(target_player, _seat_index, _send_card_data, pao_type, true);
			if (action != GameConstants.WIK_NULL) {
				table.exe_gang(target_player, _seat_index, _send_card_data, action, pao_type[0], true, true, false,
						1000);
			}
			return true;
		}

		case GameConstants.WIK_ZI_MO: { // 自摸
			table.GRR._chi_hu_rights[target_player].set_valid(true);
			table.GRR._chi_hu_card[target_player][0] = operate_card;
			table._cur_banker = target_player;
			table._shang_zhuang_player = target_player;
			table.process_chi_hu_player_operate(target_player, operate_card, true);
			table.process_chi_hu_player_score_phz(target_player, _seat_index, operate_card, true);

			// 记录
			if (table.GRR._chi_hu_rights[target_player].da_hu_count > 0) {
				table._player_result.da_hu_zi_mo[target_player]++;
			} else {
				table._player_result.xiao_hu_zi_mo[target_player]++;
			}
			table.countChiHuTimes(target_player, true);

			int delay = GameConstants.GAME_FINISH_DELAY_FLS;
			if (table.GRR._chi_hu_rights[target_player].type_count > 2) {
				delay += table.GRR._chi_hu_rights[target_player].type_count - 2;
			}
			GameSchedule.put(new GameFinishRunnable(table.getRoom_id(), target_player, GameConstants.Game_End_NORMAL),
					delay, TimeUnit.SECONDS);
			return true;
		}
		}

		return true;
	}

	@Override
	public boolean handler_player_be_in_room(HHTable_XP table, int seat_index) {
		RoomResponse.Builder roomResponse = RoomResponse.newBuilder();
		roomResponse.setType(MsgConstants.RESPONSE_RECONNECT_DATA);

		TableResponse.Builder tableResponse = TableResponse.newBuilder();

		table.load_room_info_data(roomResponse);
		table.load_player_info_data(roomResponse);
		table.load_common_status(roomResponse);

		// 游戏变量
		tableResponse.setBankerPlayer(table.GRR._banker_player);
		tableResponse.setCurrentPlayer(_seat_index);
		tableResponse.setCellScore(0);

		// 状态变量
		tableResponse.setActionCard(0);
		// 历史记录
		tableResponse.setOutCardData(0);
		tableResponse.setOutCardPlayer(0);

		for (int i = 0; i < table.getTablePlayerNumber(); i++) {
			tableResponse.addTrustee(false);// 是否托管
			// 剩余牌数
			tableResponse.addDiscardCount(table.GRR._discard_count[i]);
			Int32ArrayResponse.Builder int_array = Int32ArrayResponse.newBuilder();
			for (int j = 0; j < 55; j++) {
				int_array.addItem(table.GRR._discard_cards[i][j]);
			}
			tableResponse.addDiscardCards(int_array);

			// 组合扑克
			tableResponse.addWeaveCount(table.GRR._weave_count[i]);
			WeaveItemResponseArrayResponse.Builder weaveItem_array = WeaveItemResponseArrayResponse.newBuilder();
			for (int j = 0; j < GameConstants.MAX_WEAVE_HH; j++) {
				WeaveItemResponse.Builder weaveItem_item = WeaveItemResponse.newBuilder();
				weaveItem_item.setProvidePlayer(table.GRR._weave_items[i][j].provide_player);
				weaveItem_item.setPublicCard(table.GRR._weave_items[i][j].public_card);
				weaveItem_item.setWeaveKind(table.GRR._weave_items[i][j].weave_kind);
				weaveItem_item.setHuXi(table.GRR._weave_items[i][j].hu_xi);
				for (int k = 0; k < 4; k++) {
					if (table.GRR._weave_items[i][j].weave_card[k] > 0) {
						weaveItem_item.addWeaveCard(table.GRR._weave_items[i][j].weave_card[k]);
					}
				}
				if (seat_index != i) {
					if ((table.GRR._weave_items[i][j].weave_kind == GameConstants.WIK_TI_LONG
							|| table.GRR._weave_items[i][j].weave_kind == GameConstants.WIK_AN_LONG
							|| table.GRR._weave_items[i][j].weave_kind == GameConstants.WIK_AN_LONG_LIANG)
							&& table.GRR._weave_items[i][j].public_card == 0) {
						weaveItem_item.setCenterCard(0);
					} else {
						if (table.is_mj_type(GameConstants.GAME_TYPE_PHZ_XT)
								&& table.has_rule(GameConstants.GAME_RULE_DI_AN_WEI)
								&& table._xt_display_an_long[i] == true)
							weaveItem_item.setCenterCard(0);
						else
							weaveItem_item.setCenterCard(table.GRR._weave_items[i][j].center_card);
					}
				} else {
					weaveItem_item.setCenterCard(table.GRR._weave_items[i][j].center_card);
				}
				weaveItem_array.addWeaveItem(weaveItem_item);
			}
			tableResponse.addWeaveItemArray(weaveItem_array);

			//
			tableResponse.addWinnerOrder(0);
			tableResponse.addHuXi(table._hu_xi[i]);

			// 牌

			if (i == _seat_index) {
				tableResponse.addCardCount(table._logicXP.get_card_count_by_index(table.GRR._cards_index[i]) - 1);
			} else {
				tableResponse.addCardCount(table._logicXP.get_card_count_by_index(table.GRR._cards_index[i]));
			}
		}

		// 数据
		tableResponse.setSendCardData(0);
		int hand_cards[] = new int[GameConstants.MAX_HH_COUNT];
		int hand_card_count = table._logicXP.switch_to_cards_data(table.GRR._cards_index[seat_index], hand_cards);
		boolean flag = false;
		if (table.getTablePlayerNumber() == GameConstants.GAME_PLAYER_HH) {
			if (table.GRR._left_card_count == 19) {
				if (seat_index == _seat_index) {
					table._logicXP.remove_card_by_data(hand_cards, _send_card_data);
				}
				if (table.has_rule(GameConstants.GAME_RULE_DI_ERZI_LIANG_PAI)) {
					if (_send_card_data != 0)
						table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, seat_index, false);

				} else {
					if (seat_index == _seat_index) {
						if (_send_card_data != 0) {
							table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, seat_index,
									false);
						}
					} else {
						if (_send_card_data != 0) {
							table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, seat_index,
									true);
						}
					}
				}
				flag = true;
			}
		}

		// 先注释掉，等客户端一起联调
		for (int x = 0; x < hand_card_count; x++) {
			if (table.is_card_has_wei(hand_cards[x])) { // 如果是偎的牌
				hand_cards[x] += GameConstants.CARD_ESPECIAL_TYPE_CAN_SHOOT;
			}
		}
		for (int i = 0; i < GameConstants.MAX_HH_COUNT; i++) {
			tableResponse.addCardsData(hand_cards[i]);
		}

		roomResponse.setTable(tableResponse);
		for (int i = 0; i < table.getTablePlayerNumber(); i++) {
			roomResponse.addActions(table.chong[i]);
			roomResponse.addDouliuzi(table.gu[i] ? 1 : 0);
		}

		table.send_response_to_player(seat_index, roomResponse);
		// 摸牌
		if ((_send_card_data != 0) && (flag == false)) {
			table.operate_player_get_card(_seat_index, 1, new int[] { _send_card_data }, seat_index, false);
		}

		if (table._playerStatus[seat_index].has_action() && (table._playerStatus[seat_index].is_respone() == false)) {
			table.operate_player_action(seat_index, false);
		}
		if (table._is_xiang_gong[seat_index] == true) {
			table.operate_player_xiang_gong_flag(seat_index, table._is_xiang_gong[seat_index]);
		}
		table.istrustee[seat_index] = false;
		int ting_cards[] = table._playerStatus[seat_index]._hu_cards;
		int ting_count = table._playerStatus[seat_index]._hu_card_count;

		if (ting_count > 0) {
			table.operate_chi_hu_cards(seat_index, ting_count, ting_cards);
		}
		return true;
	}

}
