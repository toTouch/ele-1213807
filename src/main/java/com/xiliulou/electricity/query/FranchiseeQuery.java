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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FranchiseeQuery {
	private Long id;
	private Long size;
	private Long offset;
	/**
	 * 名称
	 */
	private String name;
	private Long beginTime;
	private Long endTime;
	private Integer tenantId;

	private Long uid;
	private List<Long> ids;

}
