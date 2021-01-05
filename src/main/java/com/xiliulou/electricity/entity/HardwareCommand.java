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
	//旧门开门 order_old_door_open
	public static final String ELE_COMMAND_ORDER_OLD_DOOR_OPEN = "order_old_door_open";
	//旧门关门 order_old_door_close
	public static final String ELE_COMMAND_ORDER_OLD_DOOR_CLOSE = "order_old_door_close";
	//旧门检测 order_old_door_check
	public static final String ELE_COMMAND_ORDER_OLD_DOOR_CHECK = "order_old_door_check";
	//新门开门 order_new_door_open
	public static final String ELE_COMMAND_ORDER_NEW_DOOR_OPEN = "order_new_door_open";
	//新门关门 order_new_door_close
	public static final String ELE_COMMAND_ORDER_NEW_DOOR_CLOSE = "order_new_door_close";
	//新门检测 order_new_door_check
	public static final String ELE_COMMAND_ORDER_NEW_DOOR_CHECK = "order_new_door_check";
	//物理操作
	//仓门上报 cell_report_info
	public static final String ELE_COMMAND_CELL_REPORT_INFO = "cell_report_info";
	//电池上报 cell_battery_report_info
	public static final String ELE_COMMAND_CELL_BATTERY_REPORT_INFO = "cell_battery_report_info";
	//电柜版本上报
	public static final String EXCHANGE_CABINET = "exchange_cabinet";

	public static final Map<String, String> CUPBOARD_COMMAND_MAPS = Maps.newHashMap();

	static {
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OPEN_OLD_DOOR, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OPEN_NEW_DOOR, "OK");
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
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_UPDATE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_OPERATE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OLD_DOOR_OPEN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OLD_DOOR_CLOSE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_OLD_DOOR_CHECK, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_NEW_DOOR_OPEN, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_NEW_DOOR_CLOSE, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_ORDER_NEW_DOOR_CHECK, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_REPORT_INFO, "OK");
		CUPBOARD_COMMAND_MAPS.put(ELE_COMMAND_CELL_BATTERY_REPORT_INFO, "OK");
		CUPBOARD_COMMAND_MAPS.put(EXCHANGE_CABINET, "OK");
	}
}
