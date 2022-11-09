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
	@ExcelProperty("加盟商")
	private String franchiseeName;
	@ExcelProperty("套餐名称")
	private String memberCardName;
	@ExcelProperty("套餐次数")
	private String maxUseCount;
	@ExcelProperty("有效天数")
	private Integer validDays;
	@ExcelProperty("状态")
	private String status;
	@ExcelProperty("支付金额")
	private BigDecimal payAmount;
	@ExcelProperty("购买时间")
	private String beginningTime;

}
