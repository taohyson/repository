/**
 * Copyright@2015-2016 Hunan Qisheng Network Technology Co. Ltd.[SHEN-ZHEN]
 *
 */
package com.xianyi.framework.core.transport;

import com.xianyi.framework.core.concurrent.IEventListener;

/**
 *
 * @author wu_hc
 */
public interface Connector {
	/**
	 * 发起链接
	 * 
	 * @return
	 */
	boolean connect();

	/**
	 * 重连
	 * 
	 * @return
	 */
	boolean reConnect();

	/**
	 * 初始化
	 * 
	 * @return
	 */
	boolean doInit();

	/**
	 * 登陆到目标服务器，主要用于校验连接合法性
	 * 
	 * @return
	 */
	boolean doLogin();

	/**
	 * 发送请求
	 * 
	 * @param request
	 */
	void send(Object request);

	/**
	 * 停止
	 */
	void shutdownGracefully();

	/**
	 * ip:port
	 * 
	 * @return
	 */
	String getConnectKey();

	/**
	 * 
	 * @return
	 */
	boolean isActive();

	/**
	 * 连接成功回调
	 * 
	 * @param callback
	 */
	void setConnectedCallback(final IEventListener callback);

	/**
	 * 描述
	 * 
	 * @return
	 */
	String description();
}
