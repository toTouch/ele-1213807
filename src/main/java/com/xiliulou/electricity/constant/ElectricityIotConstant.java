package com.xiliulou.electricity.constant;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 13:34
 * @Description:
 */
public class ElectricityIotConstant {
    /**
     * 命令映射处理的handler
     */
    private static final Map<String, String> COMMAND_HANDLER_MAPS = Maps.newHashMap();

    public static final String ICID_COMMAND_IOT_HANDLER = "icIdCommandIotHandler";
    public static final String NORMAL_API_EXCHANGE_HANDLER = "normalApiExchangeHandler";
    public static final String NORMAL_API_RENT_HANDLER = "normalApiRentHandler";
    public static final String NORMAL_API_RETURN_HANDLER = "normalApiReturnHandler";
    /**
     * 电池上报
     */
    public static final String NORMAL_ELE_BATTERY_HANDLER = "normalEleBatteryHandler";
    /**
     * 电池变化
     */
    public static final String NORMAL_ELE_BATTERY_CHANGE_HANDLER = "normalEleBatteryChangeHandler";
    /**
     * 邮箱上报告警
     */
    public static final String NORMAL_ELE_EMAIL_WARN_MSG_HANDLER = "normalEleEmailWarnMsgHandler";
    public static final String NORMAL_ELE_CELL_HANDLER = "normalEleCellHandler";
    public static final String NORMAL_ELE_EXCHANGE_HANDLER = "normalEleExchangeHandler";
    public static final String NORMAL_ELE_OPERATE_HANDLER = "normalEleOperateHandler";
    public static final String NORMAL_ELE_ORDER_HANDLER = "normalEleOrderHandler";
    public static final String NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER = "normalEleOrderSelfOpenCellHandler";
    public static final String NORMAL_ELE_ORDER_OPERATE_HANDLER = "normalEleOrderOperateHandler";
    public static final String NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER = "normalOffLineEleExchangeHandler";
    public static final String NORMAL_OTHER_CONFIG_HANDLER = "normalOtherConfigHandler";
    public static final String NORMAL_POWER_CONSUMPTION_HANDLER = "normalPowerConsumptionHandler";
    public static final String NORMAL_WARN_HANDLER = "normalWarnHandler";
    public static final String NORMAL_NEW_EXCHANGE_ORDER_HANDLER = "newExchangeOrderHandler";
    /**
     * 核心板上报数据处理
     */
    public static final String NORMAL_CABINET_CORE_DATA_HANDLER = "normalCabinetCoreDataHandler";


    public static String acquireChargeHandlerName(String command) {
        return COMMAND_HANDLER_MAPS.get(command);
    }

    public static boolean isLegalCommand(String command) {
        return Objects.equals(command, CUPBOARD_COMMAND_RESET_PASSWORD)
                || Objects.equals(command, ELE_COMMAND_ORDER_OPEN_OLD_DOOR)
                || Objects.equals(command, ELE_COMMAND_ORDER_OPEN_NEW_DOOR)
                || Objects.equals(command, ELE_COMMAND_RENT_OPEN_DOOR)
                || Objects.equals(command, ELE_COMMAND_RETURN_OPEN_DOOR)
                || Objects.equals(command, ELE_COMMAND_CELL_OPEN_DOOR)
                || Objects.equals(command, SELF_OPEN_CELL)
                || Objects.equals(command, ELE_COMMAND_CORE_OPEN_DOOR)
                || Objects.equals(command, ELE_COMMAND_CELL_ALL_OPEN_DOOR)
                || Objects.equals(command, ELE_COMMAND_CELL_OPEN_LIGHT)
                || Objects.equals(command, ELE_COMMAND_CELL_CLOSE_LIGHT)
                || Objects.equals(command, ELE_COMMAND_CORE_OPEN_LIGHT)
                || Objects.equals(command, ELE_COMMAND_CORE_CLOSE_LIGHT)
                || Objects.equals(command, ELE_COMMAND_CELL_OPEN_HEAT)
                || Objects.equals(command, ELE_COMMAND_CELL_CLOSE_HEAT)
                || Objects.equals(command, ELE_COMMAND_CELL_OPEN_FAN)
                || Objects.equals(command, ELE_COMMAND_CELL_CLOSE_FAN)
                || Objects.equals(command, ELE_COMMAND_CORE_OPEN_FAN)
                || Objects.equals(command, ELE_COMMAND_CORE_CLOSE_FAN)
                || Objects.equals(command, ELE_COMMAND_CELL_CHARGE_OPEN)
                || Objects.equals(command, ELE_COMMAND_CELL_CHARGE_CLOSE)
                || Objects.equals(command, ELE_COMMAND_CELL_SET_VOLTAGE)
                || Objects.equals(command, ELE_COMMAND_CELL_SET_CURRENT)
                || Objects.equals(command, ELE_COMMAND_CELL_UPDATE)
                || Objects.equals(command, ELE_COMMAND_OPERATE)
                || Objects.equals(command, ELE_COMMAND_ORDER_OLD_DOOR_OPEN)
                || Objects.equals(command, ELE_COMMAND_ORDER_OLD_DOOR_CHECK)
                || Objects.equals(command, ELE_COMMAND_ORDER_NEW_DOOR_OPEN)
                || Objects.equals(command, ELE_COMMAND_ORDER_NEW_DOOR_CHECK)
                || Objects.equals(command, ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)
                || Objects.equals(command, ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)
                || Objects.equals(command, ELE_COMMAND_RENT_ORDER_RSP)
                || Objects.equals(command, ELE_COMMAND_RETURN_ORDER_RSP)
                || Objects.equals(command, ELE_COMMAND_SELF_OPEN_CELL_RSP)
                || Objects.equals(command, ELE_COMMAND_CELL_REPORT_INFO)
                || Objects.equals(command, ELE_COMMAND_BATTERY_REPORT_INFO)
                || Objects.equals(command, EXCHANGE_CABINET)
                || Objects.equals(command, ELE_COMMAND_CELL_CONFIG)
                || Objects.equals(command, ELE_COMMAND_CUPBOARD_UPDATE_APPLICATION)
                || Objects.equals(command, ELE_COMMAND_POWER_CONSUMPTION)
                || Objects.equals(command, ELE_COMMAND_POWER_CONSUMPTION_RSP)
                || Objects.equals(command, ELE_COMMAND_CUPBOARD_REQUEST_LOG)
                || Objects.equals(command, ELE_COMMAND_OTHER_CONFIG)
                || Objects.equals(command, ELE_COMMAND_BATTERY_SYNC_INFO)
                || Objects.equals(command, ELE_COMMAND_CUPBOARD_RESTART)
                || Objects.equals(command, ELE_COMMAND_WARN_MSG_RSP)
                || Objects.equals(command, ELE_COMMAND_UNLOCK_CABINET)
                || Objects.equals(command, ELE_COMMAND_OTHER_CONFIG_READ)
                || Objects.equals(command, ELE_COMMAND_OTHER_CONFIG_RSP)
                || Objects.equals(command, ELE_COMMAND_START_OPEN_CHECK_CELL)
                || Objects.equals(command, API_RENT_ORDER)
                || Objects.equals(command, API_RENT_ORDER_RSP)
                || Objects.equals(command, API_RETURN_ORDER)
                || Objects.equals(command, API_RETURN_ORDER_RSP)
                || Objects.equals(command, API_EXCHANGE_ORDER)
                || Objects.equals(command, API_EXCHANGE_ORDER_RSP)
                || Objects.equals(command, API_ORDER_OPER_HISTORY)
                || Objects.equals(command, GET_CARD_NUM_ICCID)
                || Objects.equals(command, ELE_COMMAND_ICCID_GET_RSP)
                || Objects.equals(command, OFFLINE_ELE_EXCHANGE_ORDER_RSP)
                || Objects.equals(command, OFFLINE_EXCHANGE_ORDER_ACK_RSP)
                || Objects.equals(command, OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS)
                || Objects.equals(command, ELE_OTHER_SETTING)
                || Objects.equals(command, ELE_COMMAND_NEW_EXCHANGE_ORDER)
                || Objects.equals(command, ELE_COMMAND_NEW_EXCHANGE_ORDER_RSP)
                || Objects.equals(command, ELE_CELL_OPEN_CHARGE)
                || Objects.equals(command, ELE_CELL_CLOSE_CHARGE);

    }


    public static final String CUPBOARD_COMMAND_RESET_PASSWORD = "cupboard_reset";

    //业务操作
    //订单开旧门
    public static final String ELE_COMMAND_ORDER_OPEN_OLD_DOOR = "order_open_old_door";
    //订单开新门
    public static final String ELE_COMMAND_ORDER_OPEN_NEW_DOOR = "order_open_new_door";
    //租电池开门
    public static final String ELE_COMMAND_RENT_OPEN_DOOR = "rent_open_door";
    //还电池开门
    public static final String ELE_COMMAND_RETURN_OPEN_DOOR = "return_open_door";
    //操作记录
    public static final String ELE_EXCHANGE_ORDER_OPERATE_RECORD = "exchange_order_operate_record";
    //自助开仓
    public static final String SELF_OPEN_CELL = "self_open_cell";

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


    //新命令
    //旧电池
    @Deprecated
    public static final String ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP = "init_exchange_order_rsp";
    //新电池
    @Deprecated
    public static final String ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP = "complete_exchange_order_rsp";
    //租电池
    public static final String ELE_COMMAND_RENT_ORDER_RSP = "rent_order_rsp";
    //还电池
    public static final String ELE_COMMAND_RETURN_ORDER_RSP = "return_order_rsp";

    public static final String ELE_COMMAND_SELF_OPEN_CELL_RSP = "self_open_cell_rsp";
    /**
     * 新换电命令
     */
    public static final String ELE_COMMAND_NEW_EXCHANGE_ORDER = "exchange_order";
    public static final String ELE_COMMAND_NEW_EXCHANGE_ORDER_RSP = "exchange_order_rsp";


    //物理操作
    //仓门上报 cell_report_info
    public static final String ELE_COMMAND_CELL_REPORT_INFO = "cell_report_info";
    //电池上报 cell_battery_report_info
    public static final String ELE_COMMAND_BATTERY_REPORT_INFO = "battery_report_info";
    //电柜版本上报
    public static final String EXCHANGE_CABINET = "exchange_cabinet";
    //配柜子
    public static final String ELE_COMMAND_CELL_CONFIG = "cell_config";
    //远程更新
    public static final String ELE_COMMAND_CUPBOARD_UPDATE_APPLICATION = "cupboard_update_application";

    public static final String ELE_COMMAND_POWER_CONSUMPTION = "power_consumption";

    public static final String ELE_COMMAND_POWER_CONSUMPTION_RSP = "power_consumption_rsp";

    public static final String ELE_COMMAND_CUPBOARD_REQUEST_LOG = "cupboard_request_log";

    //重新上报电池
    public static final String ELE_COMMAND_BATTERY_SYNC_INFO = "battery_sync_info";

    //租电池开门通知
    public static final String ELE_COMMAND_RENT_OPEN_DOOR_RSP = "rent_open_door_rsp";
    //租电池检测
    public static final String ELE_COMMAND_RENT_CHECK_BATTERY_RSP = "rent_check_battery_rsp";
    //还电池开门通知
    public static final String ELE_COMMAND_RETURN_OPEN_DOOR_RSP = "return_open_door_rsp";
    //还电池检测
    public static final String ELE_COMMAND_RETURN_CHECK_BATTERY_RSP = "return_check_battery_rsp";

    //重启app
    public static final String ELE_COMMAND_CUPBOARD_RESTART = "cupboard_restart";

    //上报异常
    public static final String ELE_COMMAND_WARN_MSG_RSP = "warn_msg_rsp";

    //解锁换电柜
    public static final String ELE_COMMAND_UNLOCK_CABINET = "unlock_cabinet";
    /**
     * 修改柜机配置
     */
    public static final String ELE_COMMAND_OTHER_CONFIG = "other_config";

    //上报其他配置
    public static final String ELE_COMMAND_OTHER_CONFIG_RSP = "other_config_rsp";

    //开启循环检测电池和格挡
    public static final String ELE_COMMAND_START_OPEN_CHECK_CELL = "start_open_check_cell";
    /**
     * api租电
     */
    public static final String API_RENT_ORDER = "api_rent_battery";
    public static final String API_RENT_ORDER_RSP = "api_rent_battery_rsp";
    /**
     * api还电
     */
    public static final String API_RETURN_ORDER = "api_return_battery";
    public static final String API_RETURN_ORDER_RSP = "api_return_battery_rsp";
    /**
     * api换电
     */
    public static final String API_EXCHANGE_ORDER = "api_exchange_battery";
    public static final String API_EXCHANGE_ORDER_RSP = "api_exchange_battery_rsp";

    /**
     * api操作记录
     */
    public static final String API_ORDER_OPER_HISTORY = "api_exchange_order_operate_record";


    /**
     * 获取4G卡号
     */
    public static final String GET_CARD_NUM_ICCID = "get_card_num_iccid";

    //获取4G卡号
    public static final String ELE_COMMAND_ICCID_GET_RSP = "iccid_get_rsp";

    public static final String OFFLINE_ELE_EXCHANGE_ORDER_RSP = "offline_ele_exchange_order_rsp";

    public static final String OFFLINE_EXCHANGE_ORDER_ACK_RSP = "offline_exchange_order_ack_rsp";

    public static final String OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS = "offline_exchange_order_ack";
    /**
     * 核心板数据上报
     */
    public static final String EXCHANGE_CORE_REPORT = "exchange_core_report";

    /**
     * 柜机检测电池数据上报
     */
    public static final String BATTERY_CHANGE_REPORT = "battery_change_report";

    /**
     * 柜机其他设置（新）
     */
    public static final String ELE_OTHER_SETTING = "other_setting";
    /**
     * 读柜机其他设置
     */
    public static final String ELE_COMMAND_OTHER_CONFIG_READ = "other_config_read";
    /**
     * 读柜机其他设置上报
     */
    public static final String ELE_OTHER_CONFIG_RSP_V2 = "other_config_rsp_v2";
    /**
     * 打开充电器
     */
    public static final String ELE_CELL_OPEN_CHARGE = "cell_open_charge";
    /**
     * 关闭充电器
     */
    public static final String ELE_CELL_CLOSE_CHARGE = "cell_close_charge";
    /**
     * 邮箱上报告警
     */
    public static final String ELE_EMAIL_WARN_MSG = "email_warn_msg";


    static {
        COMMAND_HANDLER_MAPS.put(CUPBOARD_COMMAND_RESET_PASSWORD, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_OPEN_DOOR, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_ALL_OPEN_DOOR, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CORE_OPEN_LIGHT, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CORE_CLOSE_LIGHT, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_OPEN_HEAT, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_CLOSE_HEAT, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_OPEN_FAN, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_CLOSE_FAN, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CORE_OPEN_FAN, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CORE_CLOSE_FAN, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_UPDATE, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_CONFIG, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CUPBOARD_UPDATE_APPLICATION, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CUPBOARD_REQUEST_LOG, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_OTHER_CONFIG, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CUPBOARD_RESTART, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_UNLOCK_CABINET, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_OPERATE, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_CELL_OPEN_CHARGE, NORMAL_ELE_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_CELL_CLOSE_CHARGE, NORMAL_ELE_OPERATE_HANDLER);


        COMMAND_HANDLER_MAPS.put(EXCHANGE_CABINET, NORMAL_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_POWER_CONSUMPTION, NORMAL_POWER_CONSUMPTION_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_POWER_CONSUMPTION_RSP, NORMAL_POWER_CONSUMPTION_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_WARN_MSG_RSP, NORMAL_WARN_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_OTHER_CONFIG_READ, NORMAL_OTHER_CONFIG_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_OTHER_SETTING, NORMAL_OTHER_CONFIG_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_OTHER_CONFIG_RSP_V2, NORMAL_OTHER_CONFIG_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_OTHER_CONFIG_RSP, NORMAL_OTHER_CONFIG_HANDLER);


        COMMAND_HANDLER_MAPS.put(API_RENT_ORDER, NORMAL_API_RENT_HANDLER);
        COMMAND_HANDLER_MAPS.put(API_RENT_ORDER_RSP, NORMAL_API_RENT_HANDLER);
        COMMAND_HANDLER_MAPS.put(API_RETURN_ORDER, NORMAL_API_RETURN_HANDLER);
        COMMAND_HANDLER_MAPS.put(API_RETURN_ORDER_RSP, NORMAL_API_RETURN_HANDLER);
        COMMAND_HANDLER_MAPS.put(API_EXCHANGE_ORDER, NORMAL_API_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(API_EXCHANGE_ORDER_RSP, NORMAL_API_EXCHANGE_HANDLER);


        COMMAND_HANDLER_MAPS.put(GET_CARD_NUM_ICCID, ICID_COMMAND_IOT_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_ICCID_GET_RSP, ICID_COMMAND_IOT_HANDLER);


        COMMAND_HANDLER_MAPS.put(OFFLINE_ELE_EXCHANGE_ORDER_RSP, NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(OFFLINE_EXCHANGE_ORDER_ACK_RSP, NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS, NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER);


        COMMAND_HANDLER_MAPS.put(API_ORDER_OPER_HISTORY, NORMAL_ELE_ORDER_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_EXCHANGE_ORDER_OPERATE_RECORD, NORMAL_ELE_ORDER_OPERATE_HANDLER);


        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_ORDER_OPEN_OLD_DOOR, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_ORDER_OPEN_NEW_DOOR, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RENT_OPEN_DOOR, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RENT_ORDER_RSP, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RETURN_OPEN_DOOR, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RETURN_ORDER_RSP, NORMAL_ELE_ORDER_HANDLER);

        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_SELF_OPEN_CELL_RSP, NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER);
        COMMAND_HANDLER_MAPS.put(SELF_OPEN_CELL, NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER);


        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_CELL_REPORT_INFO, NORMAL_ELE_CELL_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_START_OPEN_CHECK_CELL, NORMAL_ELE_CELL_HANDLER);


        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_BATTERY_SYNC_INFO, NORMAL_ELE_BATTERY_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_BATTERY_REPORT_INFO, NORMAL_ELE_BATTERY_HANDLER);
        /**
         * 柜机检测电池数据上报
         */
        COMMAND_HANDLER_MAPS.put(BATTERY_CHANGE_REPORT, NORMAL_ELE_BATTERY_CHANGE_HANDLER);
        /**
         * 核心板数据上报
         */
        COMMAND_HANDLER_MAPS.put(EXCHANGE_CORE_REPORT, NORMAL_CABINET_CORE_DATA_HANDLER);

        /**
         * 换电新命令，替代以前换电命令
         */
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_NEW_EXCHANGE_ORDER, NORMAL_NEW_EXCHANGE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_NEW_EXCHANGE_ORDER_RSP, NORMAL_NEW_EXCHANGE_ORDER_HANDLER);
        /**
         * 柜机上报告警发邮件
         */
        COMMAND_HANDLER_MAPS.put(ELE_EMAIL_WARN_MSG, NORMAL_ELE_EMAIL_WARN_MSG_HANDLER);


    }
}
