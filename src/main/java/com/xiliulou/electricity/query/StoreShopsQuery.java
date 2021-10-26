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
public class StoreShopsQuery {
	private Long size;
	private Long offset;
	/**
	 * 商品名称
	 */
	private String name;
	private Long storeId;

	private Long beginTime;

	private Long endTime;

}
