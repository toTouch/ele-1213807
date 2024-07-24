package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: Miss.Li
 * @Date: 2022/11/07 09:07
 * @Description:
 */
@Data
public class InsuranceOrderAdd {

	//保险
	@NotNull(message = "保险不能为空!", groups = {CreateGroup.class})
	private Integer insuranceId;

	//保险
	private Integer franchiseeId;
	
	/**
	 * 支付渠道 WECHAT-微信支付,ALIPAY-支付宝
	 */
	private String paymentChannel = ChannelEnum.WECHAT.getCode();
}
