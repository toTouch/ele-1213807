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
public class RentBatteryOrderExcelVO {
	@ExcelProperty("序号")
    private Integer id;
	@ExcelProperty("换电柜")
	private String ElectricityCabinetName;
	@ExcelProperty("仓门编号")
	private Integer cellNo;
	@ExcelProperty("订单编号")
	private String orderId;
	@ExcelProperty("用户名")
	private String name;
	@ExcelProperty("手机号")
    private String phone;
	@ExcelProperty("电池编号")
	private String electricityBatterySn;
	@ExcelProperty("押金")
	private BigDecimal batteryDeposit;
	@ExcelProperty("订单类型")
	private String type;
	@ExcelProperty("订单状态")
	private String status;
	@ExcelProperty("租借时间")
	private String creatTime;

}
