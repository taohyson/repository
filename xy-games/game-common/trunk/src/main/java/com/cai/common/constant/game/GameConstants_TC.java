package com.cai.common.constant.game;

import com.cai.common.constant.GameConstants;

public class GameConstants_TC extends GameConstants {

	public static final int GAME_RULE_PLAYER_4 = 1; // 4人
	public static final int GAME_RULE_PLAYER_3 = 2; // 3人
	public static final int GAME_RULE_PLAYER_2 = 3; // 2人
	public static final int GAME_RULE_NON_LAI_ZI = 4; // 没有赖子
	public static final int GAME_RULE_LAI_ZI = 5; // 有赖子
	public static final int GAME_RULE_NON_PAO = 6; // 不跑
	public static final int GAME_RULE_PAO_1 = 7; // 跑1分
	public static final int GAME_RULE_DING_PAO_1 = 8; // 定跑1
	public static final int GAME_RULE_DING_PAO_2 = 9; // 定跑2
	public static final int GAME_RULE_DING_PAO_3 = 10; // 定跑3
	public static final int GAME_RULE_TIAN_HU = 11; // 天胡算大胡
	public static final int GAME_RULE_NO_TIAN_HU = 12; // 天胡算自摸
	public static final int GAME_RULE_THREE_MEN = 13; // 三门
	public static final int GAME_RULE_TWO_MEN = 14; // 两门
	public static final int GAME_RULE_ISTRUSTEE = 15; // 托管
	public static final int GAME_RULE_CAI_GANG = 16; // 猜杠
	public static final int GAME_RULE_PAO_2 = 17; // 跑2分
	public static final int GAME_RULE_PAO_3 = 18; // 跑3分
	public static final int GAME_RULE_FOUR_LAI_ZI_REWARD = 19; // 四赖子奖励
	public static final int GAME_RULE_HAI_DI_HU = 20; // 海底胡
	public static final int GAME_RULE_HAO_HUA_QI_DUI = 21; // 豪华七对
	public static final int GAME_RULE_DI_FEN_1 = 22; // 1底分
	public static final int GAME_RULE_DI_FEN_2 = 23; // 2底分
	public static final int GAME_RULE_DI_FEN_5 = 24; // 5底分

	public static final int HU_CARD_TYPE_ZI_MO = 0x00000001; // 自摸
	public static final int HU_CARD_TYPE_JIE_PAO = 0x00000002; // 接炮
	public static final int HU_CARD_TYPE_QIANG_GANG = 0x00000004; // 抢杠
	public static final int HU_CARD_TYPE_GANG_KAI_HUA = 0x00000008; // 杠开

	public static final int WIK_CAI_GANG = 0x400; // 猜杠
	public static final int WIK_CAI_GANG_FAILD = 0x400; // 猜杠失败
	public static final int WIK_CAI_GANG_SUCCESS = 0x800; // 猜杠成功
	public static final int WIK_HAI_DI_EFFECT = 0x1000; // 开启海底动画
	public static final int EFFECT_HAI_DI = 0x2000; // 海底动画1

	public static final int CHR_XIAO_QI_DUI = 0x00000001; // 七小对
	public static final int CHR_QING_YI_SE = 0x00000002; // 清一色
	public static final int CHR_QING_GANG_HU = 0x00000004; // 抢杠胡
	public static final int CHR_GNAG_KAI = 0x00000008; // 杠开
	public static final int CHR_HAI_DI_HU = 0x00000010; // 海底胡
	public static final int CHR_YING_HU = 0x00000020; // 硬胡
	public static final int CHR_JIANG_YI_SE = 0x00000040; // 将一色
	public static final int CHR_SI_LAI_ZI = 0x00000080; // 四赖子胡
	public static final int CHR_TIAN_HU = 0x00000100; // 天胡

	public final static int[] DEFAULT_CARDS = new int[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, // 索子
			0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, // 索子
			0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, // 索子
			0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, // 索子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29 // 同子
	};
	/**
	 * 万筒
	 */
	public static final int CARD_DATA_WAN_TONG[] = new int[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子
	};

	public static final int TC_GANG_TYPE_AN_GANG = 3; // 暗杠
	public static final int TC_GANG_TYPE_JIE_GANG = 5; // 明杠/接杠
	public static final int TC_GANG_TYPE_ADD_GANG = 7; // 碰杠/回头杠/续杠
}
