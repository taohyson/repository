package com.cai.common.domain;

/**
 * 游戏类型管理
 * 
 * @author Administrator
 *
 */
public class GameTypeDBModel {

	private long id;
	private int game_type_index;
	private int gold_type;
	private String description;
	private int gameID;
	private String goldIndex;
	private int game_big_type;
	private String app_name;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getGame_type_index() {
		return game_type_index;
	}

	public void setGame_type_index(int game_type_index) {
		this.game_type_index = game_type_index;
	}

	public int getGold_type() {
		return gold_type;
	}

	public void setGold_type(int gold_type) {
		this.gold_type = gold_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getGameID() {
		return gameID;
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public String getGoldIndex() {
		return goldIndex;
	}

	public void setGoldIndex(String goldIndex) {
		this.goldIndex = goldIndex;
	}

	public int getGame_big_type() {
		return game_big_type;
	}

	public void setGame_big_type(int game_big_type) {
		this.game_big_type = game_big_type;
	}

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

}
