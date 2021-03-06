package com.cai.common.domain.log;

import java.util.Date;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Copyright@2015-2016 Hunan Qisheng Network Technology Co. Ltd.[SHEN-ZHEN]
 * <p>
 * 玩家亲友圈福卡变动日志
 *
 * @author zhanglong 2018/9/14 17:32
 */
@Document(collection = "club_welfare_change_log")
@CompoundIndexes({ @CompoundIndex(name = "index_account_id", def = "{'accountId': 0}", background = true),
		@CompoundIndex(name = "index_gameTypeIndex", def = "{'gameTypeIndex': 0}", background = true),
		@CompoundIndex(name = "index_type", def = "{'type': 0}", background = true),
		@CompoundIndex(name = "index_clubId", def = "{'clubId': 0}", background = true), })
public class ClubMemberWelfareChangeLogModel extends BaseLogModel {
	private static final long serialVersionUID = -8800238054788044387L;

	/**
	 * 日志时间
	 */
	@Indexed(direction = IndexDirection.DESCENDING, expireAfterSeconds = 60 * 60 * 24 * 15, background = true)
	private Date create_time;

	private long accountId;
	private int clubId;
	private int type;
	private int costNum;
	private int gameTypeIndex;
	private String subName;
	private long operatorId;
	private String operatorName;
	private long oldValue;
	private long newValue;

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public int getClubId() {
		return clubId;
	}

	public void setClubId(int clubId) {
		this.clubId = clubId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCostNum() {
		return costNum;
	}

	public void setCostNum(int costNum) {
		this.costNum = costNum;
	}

	public int getGameTypeIndex() {
		return gameTypeIndex;
	}

	public void setGameTypeIndex(int gameTypeIndex) {
		this.gameTypeIndex = gameTypeIndex;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(long operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public long getOldValue() {
		return oldValue;
	}

	public void setOldValue(long oldValue) {
		this.oldValue = oldValue;
	}

	public long getNewValue() {
		return newValue;
	}

	public void setNewValue(long newValue) {
		this.newValue = newValue;
	}
}
