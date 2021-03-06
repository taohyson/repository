package com.cai.common.domain;

import java.io.Serializable;

public class SysGameType implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int game_type_index;

	private String desc;

	/**
	 * 收费索引
	 */
	private int gold_type;

	/**
	 * 游戏ID
	 */
	private int gameID;

	/**
	 * 收费的id索引
	 */
	private int[] goldIndex;
	/**
	 * 游戏大类型
	 */
	private int gameBigType;

	/**
	 * 游戏大名称
	 */
	private String appName;
	
	private String email;
	
	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getGame_type_index() {
		return game_type_index;
	}

	public void setGame_type_index(int game_type_index) {
		this.game_type_index = game_type_index;
	}

	public String getDesc() {
		if(desc == null){
			return "";
		}
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getGold_type() {
		return gold_type;
	}

	public void setGold_type(int gold_type) {
		this.gold_type = gold_type;
	}

	public int getGameID() {
		return gameID;
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public int[] getGoldIndex() {
		return goldIndex;
	}

	public void setGoldIndex(int[] goldIndex) {
		this.goldIndex = goldIndex;
	}

	public int getGameBigType() {
		return gameBigType;
	}

	public void setGameBigType(int gameBigType) {
		this.gameBigType = gameBigType;
	}

	public String getAppName() {
		if(appName == null){
			return "";
		}
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

}
