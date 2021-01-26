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
	//业务操作
	//订单开旧门
	public static final String ELE_COMMAND_ORDER_OPEN_OLD_DOOR = "order_open_old_door";
	//订单开新门
	public static final String ELE_COMMAND_ORDER_OPEN_NEW_DOOR = "order_open_new_door";
	//物理操作
	public static final String ELE_COMMAND_CELL_OPEN_DOOR = "cell_open_door";
	public static final String ELE_COMMAND_CORE_OPEN_DOOR = "core_open_door";
	public static final String ELE_COMMAND_CELL_ALL_OPEN_DOOR = "cell_all_open_door";
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
	//禁用可用命令
	public static final String ELE_COMMAND_CELL_UPDATE = "cell_update";
	//物理操作回调结果
	public static final String ELE_COMMAND_OPERATE = "operate_result";

	//业务操作
	//旧门开门 order_old_door_open_rsp
	public static final String ELE_COMMAND_ORDER_OLD_DOOR_OPEN = "order_open_old_door_rsp";
	//旧门检测 order_old_door_check_battery_rsp
	public static final String ELE_COMMAND_ORDER_OLD_DOOR_CHECK = "order_old_door_check_battery_rsp";
	//新门开门 order_new_door_open_rsp
	public static final String ELE_COMMAND_ORDER_NEW_DOOR_OPEN = "order_open_new_door_rsp";
	//新门检测 order_new_door_check_battery_rsp
	public static final String ELE_COMMAND_ORDER_NEW_DOOR_CHECK = "order_new_door_check_battery_rsp";
	//物理操作
	//仓门上报 cell_report_info
	public static final String ELE_COMMAND_CELL_REPORT_INFO = "cell_report_info";
	//电池上报 cell_battery_report_info
	public static final String ELE_COMMAND_BATTERY_REPORT_INFO = "battery_report_info";
	//电柜版本上报
	public static final String EXCHANGE_CABINET = "exchange_cabinet";
	//配柜子
	public static final String ELE_COMMAND_CELL_CONFIG = "cell_config";

	public static final Map<String, String> ELE_COMMAND_MAPS = Maps.newHashMap();

	static {
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OPEN_OLD_DOOR, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OPEN_NEW_DOOR, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_DOOR, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_DOOR, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_LIGHT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_LIGHT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_LIGHT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CORE_CLOSE_LIGHT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_HEAT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_HEAT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_OPEN_FAN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CLOSE_FAN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CORE_OPEN_FAN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CORE_CLOSE_FAN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CHARGE_OPEN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CHARGE_CLOSE, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_SET_VOLTAGE, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_SET_CURRENT, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_UPDATE, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_OPERATE, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OLD_DOOR_OPEN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OLD_DOOR_CHECK, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_NEW_DOOR_OPEN, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_ORDER_NEW_DOOR_CHECK, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_REPORT_INFO, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_BATTERY_REPORT_INFO, "OK");
		ELE_COMMAND_MAPS.put(EXCHANGE_CABINET, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_ALL_OPEN_DOOR, "OK");
		ELE_COMMAND_MAPS.put(ELE_COMMAND_CELL_CONFIG, "OK");
	}
}
