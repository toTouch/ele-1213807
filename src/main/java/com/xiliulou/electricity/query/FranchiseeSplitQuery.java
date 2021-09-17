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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FranchiseeSplitQuery {
	private Long id;
	private Integer percent;
	//1--加盟商  2--门店
	private Integer type;

	public static final Integer TYPE_FRANCHISEE = 1;
	public static final Integer TYPE_STORE = 2;

}
