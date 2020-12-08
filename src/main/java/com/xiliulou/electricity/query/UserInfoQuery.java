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
public class UserInfoQuery {
	private Integer size;
	private Integer offset;
	/**
	 * 用户名字
	 */
	private String name;
	private String phone;
	/**
	 * 门店地区Id
	 */
	private Integer areaId;
	private Long beginTime;
	private Long endTime;
}
