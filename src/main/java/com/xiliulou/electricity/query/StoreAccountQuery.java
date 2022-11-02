package com.xiliulou.electricity.query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreAccountQuery {
	private Long startTime;
	private Long endTime;

	private Long size;
	private Long offset;

	private Integer tenantId;

	private Long storeId;

	private String storeName;

	private String orderId;

	private List<Long> storeIdList;

}
