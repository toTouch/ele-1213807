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
public class StoreQuery {
	private Integer size;
	private Integer offset;
	/**
	 * 门店名称
	 */
	private String name;
	/**
	 * 门店地区Id
	 */
	private Integer areaId;
	private Long beginTime;
	private Long endTime;
}
