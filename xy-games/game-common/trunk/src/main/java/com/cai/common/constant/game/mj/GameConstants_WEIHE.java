package com.cai.common.constant.game.mj;


import com.cai.common.constant.GameConstants;

public class GameConstants_WEIHE extends GameConstants {

	public static final int GAME_RULE_WEINAN_ZHI_DING_QUE_MEN = 1; // 指定缺门
	public static final int GAME_RULE_WEINAN_PAO_1 = 2; //1炮
	public static final int GAME_RULE_WEINAN_PAO_2 = 3; //2炮
	public static final int GAME_RULE_WEINAN_PAO_3 = 4; // 3炮
	public static final int GAME_RULE_WEINAN_PAO_4 = 5; // 4炮
	public static final int GAME_RULE_WEINAN_PAO_FEN_SUI_FAN = 6; // 炮分随番
	public static final int GAME_RULE_WEINAN_PAO_FEN_DAN_SUAN = 7; // 炮分单算
	public static final int GAME_RULE_WEINAN_LOU_HU_ONE = 8; // 漏胡针对一张
	public static final int GAME_RULE_WEINAN_LOU_HU_ALL = 9; // 漏胡针对全部
	public static final int GAME_RULE_WEINAN_DI_FENG_1 = 10; //底分1
	public static final int GAME_RULE_WEINAN_DI_FENG_2 = 11; //底分2   
	public static final int GAME_RULE_WEINAN_DI_FENG_5 = 12; // 底分5

	public static final int Effect_Action_Pao_Hu_Fan  = 1001; // 硬缺点炮需要2番起胡值
	public static final int EFFECT_ACTION_TYPE  = 6; // 显示动画类型
	
	// 全部的麻将数据
	public static final int CARD_DATA_MAX[] = new int[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // 万子
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
			0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, // 同子

	};
}
