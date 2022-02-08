package com.xiliulou.electricity.query;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Auther: benyun
 * @Date: 2019/11/8 10:30
 * @Description:
 */
@Data
public class BatteryExcelQuery {
	/**
	 * 电池名称
	 */
	@ExcelProperty(index = 1)
	private String sn;

	/**
	 * 电池型号
	 */
	@ExcelProperty(index = 2)
	private String model;

	/**
	 * 电压
	 */
	@ExcelProperty(index = 3)
	private Integer voltage;

	/**
	 * 电池容量,单位(mah)
	 */
	@ExcelProperty(index = 4)
	private Integer capacity;

}
