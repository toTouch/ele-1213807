package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: eclair
 * @Date: 2020/7/1 09:32
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EleOpenDTO {
	private String sessionId;

	//type
	private String type;

	//订单Id
	private String orderId;
	//本次操作是否执行失败
	private Boolean isProcessFail;
	//是否需要结束订单
	private Boolean isNeedEndOrder;
	//订单状态序号
	private Double orderSeq;
	//orderStatus
	private String orderStatus;
	//msg
	private String msg;
}
