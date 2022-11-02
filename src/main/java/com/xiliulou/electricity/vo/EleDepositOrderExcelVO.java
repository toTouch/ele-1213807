package com.xiliulou.electricity.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * @author lxc
 * @since 2021-05-18 11:13:45
 */
@Data
public class EleDepositOrderExcelVO {
	@ExcelProperty("序号")
    private Integer id;
	@ExcelProperty("门店") private String storeName;
	@ExcelProperty("订单编号")
	private String orderId;
	@ExcelProperty("用户名")
	private String name;
	@ExcelProperty("手机号")
    private String phone;
	@ExcelProperty("支付金额")
	private BigDecimal payAmount;
	@ExcelProperty("订单状态")
	private String status;
	@ExcelProperty("缴纳时间")
	private String creatTime;

}
