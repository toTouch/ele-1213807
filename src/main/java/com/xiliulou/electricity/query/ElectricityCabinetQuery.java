package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityCabinetQuery {
	private Integer size;
	private Integer offset;
	/**
	 * 电池编号
	 */
	private String sn;
	/**
	 * 换电柜名称
	 */
	private String name;
	/**
	 * 换电柜地区Id
	 */
	private Integer areaId;
	/**
	 * 换电柜地址
	 */
	private String address;
	/**
	 * 可用状态(0--启用，1--禁用)
	 */
	private Integer usableStatus;
	/**
	 * 电源状态(0--通电，1--断电)
	 */
	private Integer powerStatus;
	/**
	 * 物联网连接状态（0--连网，1--断网）
	 */
	private Integer onlineStatus;
}
