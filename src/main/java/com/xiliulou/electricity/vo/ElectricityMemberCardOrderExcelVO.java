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
public class ElectricityMemberCardOrderExcelVO {
	@ExcelProperty("序号")
    private Integer id;
	@ExcelProperty("订单编号")
	private String orderId;
	@ExcelProperty("手机号")
    private String phone;
	@ExcelProperty("套餐类型")
	private String memberCardType;
	@ExcelProperty("状态")
	private String status;
	@ExcelProperty("支付金额")
	private BigDecimal payAmount;
	@ExcelProperty("购买时间")
	private String beginningTime;
	@ExcelProperty("到期时间")
	private String endTime;

}
