package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.Map;

/**
 * @author: eclair
 * @Date: 2019/12/1 14:31
 * @Description:
 */
@Data
public class EleOuterCommandQuery {
	/**
	 * 命令
	 */
	private String command;
	/**
	 * 设备名称
	 */
	private String deviceName;
	/**
	 * 设备产品
	 */
	private String productKey;
	/**
	 * 是否需要等待
	 */
	private Boolean isWait;
	/**
	 * 会话id
	 */
	private String sessionId;
	/**
	 * 格子信息
	 */
	private Map<String, Object> data;
}
