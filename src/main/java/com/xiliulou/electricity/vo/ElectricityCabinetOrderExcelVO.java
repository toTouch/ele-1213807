package com.xiliulou.electricity.vo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 *
 * @author lxc
 * @since 2021-05-18 11:13:45
 */
@Data
public class ElectricityCabinetOrderExcelVO {
	@ExcelProperty("序号")
    private Integer id;
	@ExcelProperty("订单编号")
	private String orderId;
	@ExcelProperty("手机号")
    private String phone;
	@ExcelProperty("支付方式")
	private String paymentMethod;
	@ExcelProperty("归还电池编号")
	private String oldElectricityBatterySn;
	@ExcelProperty("租借电池编号")
	private String newElectricityBatterySn;
	@ExcelProperty("状态")
	private String status;
	@ExcelProperty("开始时间")
	private String createTime;
	@ExcelProperty("结束时间")
	private String updateTime;

}
