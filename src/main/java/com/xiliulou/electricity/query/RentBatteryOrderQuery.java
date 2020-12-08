package com.xiliulou.electricity.query;
import lombok.Builder;
import lombok.Data;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class RentBatteryOrderQuery {
	private Integer size;
	private Integer offset;
	/**
	 * 用户名字
	 */
	private String name;
	private String phone;
	/**
	 * 门店Id
	 */
	private Integer batteryStoreId;
	private Long beginTime;
	private Long endTime;
	private Integer status;
}
