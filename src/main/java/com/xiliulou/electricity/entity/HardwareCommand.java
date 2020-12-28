package com.xiliulou.electricity.entity;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author: lxc
 * @Date: 2020/12/28 13:34
 * @Description:
 */
public class HardwareCommand {

	//开门命令：
	public static final String ELE_COMMAND_CELL_OPEN_DOOR = "cell_open_door";
	public static final String ELE_COMMAND_CORE_OPEN_DOOR = "core_open_door";
	//开灯命令
	public static final String ELE_COMMAND_CELL_OPEN_LIGHT = "cell_open_light";
	public static final String ELE_COMMAND_CELL_CLOSE_LIGHT = "cell_close_light";
	public static final String ELE_COMMAND_CORE_OPEN_LIGHT = "core_open_light";
	public static final String ELE_COMMAND_CORE_CLOSE_LIGHT = "core_close_light";
	//加热命令
	public static final String ELE_COMMAND_CELL_OPEN_HEAT = "cell_open_heat";
	public static final String ELE_COMMAND_CELL_CLOSE_HEAT = "cell_close_heat";
	//风扇命令
	public static final String ELE_COMMAND_CELL_OPEN_FAN = "cell_open_fan";
	public static final String ELE_COMMAND_CELL_CLOSE_FAN = "cell_close_fan";
	public static final String ELE_COMMAND_CORE_OPEN_FAN = "core_open_fan";
	public static final String ELE_COMMAND_CORE_CLOSE_FAN = "core_close_fan";
	//充电命令
	public static final String ELE_COMMAND_CELL_CHARGE_OPEN = "cell_charge_open";
	public static final String ELE_COMMAND_CELL_CHARGE_CLOSE = "cell_charge_close";
	public static final String ELE_COMMAND_CELL_SET_VOLTAGE = "cell_set_voltage";
	public static final String ELE_COMMAND_CELL_SET_CURRENT = "cell_set_current";


	public static final Map<String, String> CUPBOARD_COMMAND_MAPS = Maps.newHashMap();

	static {
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_DOOR, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_DOOR, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_LIGHT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_LIGHT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_LIGHT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CORE_CLOSE_LIGHT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_HEAT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_HEAT, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_FAN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_FAN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_FAN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CORE_CLOSE_FAN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_CHARGE_OPEN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_CHARGE_CLOSE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_SET_VOLTAGE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_SET_CURRENT, "OK");
	}
}
