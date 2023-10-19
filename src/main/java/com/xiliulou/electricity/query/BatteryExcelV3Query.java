package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @description 导入电池excel接口参数
 * @date 2023/10/18 17:46:42
 * @author HeYafeng
 */
@Data
public class BatteryExcelV3Query {
	
	private Long franchiseeId;
	
	private List<String> batteryList;
}
