package com.xiliulou.electricity.query;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class StoreQuery {
	private Long size;
	private Long offset;
	/**
	 * 门店名称
	 */
	private String name;
	private Long beginTime;
	private Long endTime;
	private Double distance;
	private Double lon;
	private Double lat;
	/**
	 * 可用状态(0--启用，1--禁用)
	 */
	private Integer usableStatus;
	/**
	 * 门店地址
	 */
	private String address;

	private List<Long> storeIdList;

	private Integer tenantId;

	private Long franchiseeId;

	private Long uid;

}
