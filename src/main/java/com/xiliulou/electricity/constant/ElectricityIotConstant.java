package com.xiliulou.electricity.constant;

import com.google.api.client.util.Sets;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

public class ElectricityIotConstant {
    /**
     * 命令映射处理的handler
     */
    private static final Map<String, String> COMMAND_HANDLER_MAPS = Maps.newHashMap();
    private static final Set<String> SEND_COMMAND_SETS = Sets.newHashSet();

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
    public static final String NORMAL_ELE_CELL_HANDLER = "normalEleCellHandler";
    public static final String NORMAL_ELE_EXCHANGE_HANDLER = "normalEleExchangeHandler";
    
    public static final String NORMAL_ELE_CABINET_SIGNAL_HANDLER = "normalEleCabinetSignalHandler";
    
    public static final String NORMAL_ELE_OPERATE_HANDLER = "normalEleOperateHandler";
    public static final String NORMAL_ELE_ORDER_HANDLER = "normalEleOrderHandler";
    public static final String NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER = "normalEleOrderSelfOpenCellHandler";
    public static final String NORMAL_ELE_ORDER_OPERATE_HANDLER = "normalEleOrderOperateHandler";
    public static final String NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER = "normalOffLineEleExchangeHandler";
    public static final String NORMAL_OTHER_CONFIG_HANDLER = "normalOtherConfigHandler";
    public static final String NORMAL_POWER_CONSUMPTION_HANDLER = "normalPowerConsumptionHandler";
    public static final String NORMAL_NEW_EXCHANGE_ORDER_HANDLER = "newExchangeOrderHandler";
    public static final String NORMAL_ELE_WARN_MSG_HANDLER = "normalEleWarnMsgHandler";
    public static final String NORMAL_CUPBOARD_OPERATING_RECORD_HANDLER = "normalCupboardOperatingRecordHandler";
    public static final String NORMAL_HIGH_TEMPERATURE_ALARM_HANDLER = "highTemperatureAlarmHandler";

    /**
     * 核心板上报数据处理
     */
    public static final String NORMAL_CABINET_CORE_DATA_HANDLER = "normalCabinetCoreDataHandler";

    public static final String NORMAL_OTA_OPERATE_HANDLER = "normalOtaOperateHandler";


    public static final String NORMAL_BATTERY_SNAPSHOT_HANDLER = "normalBatterySnapShotHandler";


    /**
     * 电池充电设置
     */
    public static final String NORMAL_BATTERY_MULTI_SETTING_HANDLER = "normalEleBatteryMultiSettingHandler";

    public static final String NORMAL_OTHER_SETTING_PARAM_TEMPLATE_HANDLER = "normalOtherSettingParamTemplateHandler";

    /**
     * 电池满仓提醒
     */
    public static final String FULL_BATTERY_WARNING_HANDLER = "fullBatteryWarningHandler";


    public static final String NORMAL_ELE_CHARGE_POWER_HANDLER = "normalEleChargePowerHandler";

    public static final String NEW_HARDWARE_WARN_MSG_HANDLER = "hardwareWarnMsgHandler";
    
    public static final String HARDWARE_FAILURE_WARN_MSG_HANDLER = "hardwareFailureWarnMsgHandler";
    
    
    
    
    /**
     * 离线换电密码设置
     */
    public static final String NORMAL_OFFLINE_EXCHANGE_PASSWORD_HANDLER = "normalOfflineExchangePasswordHandler";


    public static String acquireChargeHandlerName(String command) {
        return COMMAND_HANDLER_MAPS.get(command);
    }

    public static boolean isLegalCommand(String command) {
        return SEND_COMMAND_SETS.contains(command);

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
    public static final String ELE_COMMAND_RENT_ORDER_RSP_ACK = "rent_order_rsp_ack";
    //还电池
    public static final String ELE_COMMAND_RETURN_ORDER_RSP = "return_order_rsp";
    public static final String ELE_COMMAND_RETURN_ORDER_RSP_ACK = "return_order_rsp_ack";

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
    //
    public static final String EXCHANGE_CABINET_SINAL = "net_signal_rsp";
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

    public static final String EXCHANGE_ORDER_MANAGE_SUCCESS = "exchange_order_rsp_ack";
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
     * 物理记录操作回调
     */
    public static final String CUPBOARD_OPERATING_RECORD = "cupboard_operating_record";

    /**
     * ota升级
     */
    public static final String OTA_OPERATE = "ota_operate";

    public static final String OTA_OPERATE_RSP = "ota_operate_rsp";

    public static final String RESTART_APP = "restart_app";
    /**
     * 重新开启App循环检测
     */
    public static final String CUPBOARD_RESUME_CYCLE = "cupboard_resume_cycle";

    /**
     * 故障上报
     */
    public static final String ELE_COMMAND_WARN_MSG_NOTIFY = "warn_msg_notify";
    /**
     * 修改电池充电设置
     */
    public static final String ELE_BATTERY_MULTI_PARAM_SETTING = "battery_multi_param_setting";
    /**
     * 读取电池充电设置
     */
    public static final String ELE_BATTERY_MULTI_PARAMS_READ = "battery_multi_params_read";
    public static final String ELE_BATTERY_MULTI_PARAMS_READ_RSP = "battery_multi_rsp";

    public static final String ELE_BATTERY_SNAPSHOT = "battery_snap_shot";

    /**
     * 电柜模板 app参数设置
     */
    public static final String OTHER_SETTING_PARAM_TEMPLATE = "other_setting_param_template";

    public static final String READ_OTHER_SETTING_PARAM_TEMPLATE = "read_other_setting_param_template";

    /**
     * 同步电池型号
     */
    public static final String TAKE_BATTERY_MODE = "take_battery_mode";

    /**
     * 高温告警
     */
    public static final String TEMPERATURE_WARNING = "temperature_warning";

    /**
     * 电费计算上报
     */
    public static final String CALC_ELE_POWER_REPORT = "calculate_consumption_rsp";
    /**
     * 电费计算上报响应
     */
    public static final String CALC_ELE_POWER_REPORT_ACK = "calculate_consumption_rsp_ack";

    /**
     * 满仓提醒
     */
    public static final String FULL_BATTERY_WARNING = "full_battery_warning";


    /**
     * 新硬件上报
     */
    public static final String NEW_HARDWARE_WARN_MSG = "hardware_warn_msg";
    public static final String NEW_HARDWARE_WARN_MSG_ACK = "hardware_warn_msg_ack";
    
    /**
     * 柜机故障告警上报
     */
    public static final String HARDWARE_FAILURE_WARN_MSG = "hardware_failure_warn_msg";
    public static final String HARDWARE_FAILURE_WARN_MSG_ACK = "hardware_failure_warn_msg_ack";
    
    /**
     * 离线换电
     */
    /*public static final String ELE_BATTERY_OFFLINE_EXCHANGE = "battery_offline_exchange";*/
    
    /**
     * 设置离线换电密码
     */
    public static final String  ELE_BATTERY_OFFLINE_PASSWORD_RESET = "offline_password_reset";


    static {

        SEND_COMMAND_SETS.add(CUPBOARD_COMMAND_RESET_PASSWORD);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_OPEN_OLD_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_OPEN_NEW_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RENT_OPEN_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RETURN_OPEN_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_OPEN_DOOR);
        SEND_COMMAND_SETS.add(SELF_OPEN_CELL);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CORE_OPEN_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_ALL_OPEN_DOOR);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_OPEN_LIGHT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CLOSE_LIGHT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CORE_OPEN_LIGHT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CORE_CLOSE_LIGHT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_OPEN_HEAT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CLOSE_HEAT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_OPEN_FAN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CLOSE_FAN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CORE_OPEN_FAN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CORE_CLOSE_FAN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CHARGE_OPEN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CHARGE_CLOSE);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_SET_VOLTAGE);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_SET_CURRENT);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_UPDATE);
        SEND_COMMAND_SETS.add(ELE_COMMAND_OPERATE);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_OLD_DOOR_OPEN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_OLD_DOOR_CHECK);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_NEW_DOOR_OPEN);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ORDER_NEW_DOOR_CHECK);
        SEND_COMMAND_SETS.add(ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RENT_ORDER_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RETURN_ORDER_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_SELF_OPEN_CELL_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RETURN_ORDER_RSP_ACK);
        SEND_COMMAND_SETS.add(ELE_COMMAND_RENT_ORDER_RSP_ACK);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_REPORT_INFO);
        SEND_COMMAND_SETS.add(ELE_COMMAND_BATTERY_REPORT_INFO);
        SEND_COMMAND_SETS.add(EXCHANGE_CABINET);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CELL_CONFIG);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CUPBOARD_UPDATE_APPLICATION);
        SEND_COMMAND_SETS.add(ELE_COMMAND_POWER_CONSUMPTION);
        SEND_COMMAND_SETS.add(ELE_COMMAND_POWER_CONSUMPTION_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CUPBOARD_REQUEST_LOG);
        SEND_COMMAND_SETS.add(ELE_COMMAND_OTHER_CONFIG);
        SEND_COMMAND_SETS.add(ELE_COMMAND_BATTERY_SYNC_INFO);
        SEND_COMMAND_SETS.add(ELE_COMMAND_CUPBOARD_RESTART);
        SEND_COMMAND_SETS.add(ELE_COMMAND_UNLOCK_CABINET);
        SEND_COMMAND_SETS.add(ELE_COMMAND_OTHER_CONFIG_READ);
        SEND_COMMAND_SETS.add(ELE_COMMAND_OTHER_CONFIG_RSP);
        SEND_COMMAND_SETS.add(ELE_COMMAND_START_OPEN_CHECK_CELL);
        SEND_COMMAND_SETS.add(API_RENT_ORDER);
        SEND_COMMAND_SETS.add(API_RENT_ORDER_RSP);
        SEND_COMMAND_SETS.add(API_RETURN_ORDER);
        SEND_COMMAND_SETS.add(API_RETURN_ORDER_RSP);
        SEND_COMMAND_SETS.add(API_EXCHANGE_ORDER);
        SEND_COMMAND_SETS.add(API_EXCHANGE_ORDER_RSP);
        SEND_COMMAND_SETS.add(API_ORDER_OPER_HISTORY);
        SEND_COMMAND_SETS.add(GET_CARD_NUM_ICCID);
        SEND_COMMAND_SETS.add(ELE_COMMAND_ICCID_GET_RSP);
        SEND_COMMAND_SETS.add(OFFLINE_ELE_EXCHANGE_ORDER_RSP);
        SEND_COMMAND_SETS.add(OFFLINE_EXCHANGE_ORDER_ACK_RSP);
        SEND_COMMAND_SETS.add(OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS);
        SEND_COMMAND_SETS.add(EXCHANGE_ORDER_MANAGE_SUCCESS);
        SEND_COMMAND_SETS.add(ELE_OTHER_SETTING);
        SEND_COMMAND_SETS.add(ELE_COMMAND_NEW_EXCHANGE_ORDER);
        SEND_COMMAND_SETS.add(ELE_COMMAND_NEW_EXCHANGE_ORDER_RSP);
        SEND_COMMAND_SETS.add(NEW_HARDWARE_WARN_MSG_ACK);
        SEND_COMMAND_SETS.add(CALC_ELE_POWER_REPORT_ACK);
        SEND_COMMAND_SETS.add(OTHER_SETTING_PARAM_TEMPLATE);
        SEND_COMMAND_SETS.add(ELE_BATTERY_MULTI_PARAMS_READ);
        SEND_COMMAND_SETS.add(ELE_BATTERY_MULTI_PARAM_SETTING);
        SEND_COMMAND_SETS.add(ELE_COMMAND_WARN_MSG_NOTIFY);
        SEND_COMMAND_SETS.add(RESTART_APP);
        SEND_COMMAND_SETS.add(OTA_OPERATE_RSP);
        SEND_COMMAND_SETS.add(OTA_OPERATE);
        SEND_COMMAND_SETS.add(CUPBOARD_OPERATING_RECORD);
        SEND_COMMAND_SETS.add(TAKE_BATTERY_MODE);
        SEND_COMMAND_SETS.add(ELE_CELL_CLOSE_CHARGE);
        SEND_COMMAND_SETS.add(CUPBOARD_RESUME_CYCLE);
        SEND_COMMAND_SETS.add(ELE_CELL_OPEN_CHARGE);
        SEND_COMMAND_SETS.add(ELE_BATTERY_OFFLINE_PASSWORD_RESET);


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
        COMMAND_HANDLER_MAPS.put(CUPBOARD_RESUME_CYCLE, NORMAL_ELE_OPERATE_HANDLER);


        COMMAND_HANDLER_MAPS.put(EXCHANGE_CABINET, NORMAL_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(EXCHANGE_CABINET_SINAL, NORMAL_ELE_CABINET_SIGNAL_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_POWER_CONSUMPTION, NORMAL_POWER_CONSUMPTION_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_POWER_CONSUMPTION_RSP, NORMAL_POWER_CONSUMPTION_HANDLER);
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
//        COMMAND_HANDLER_MAPS.put(OFFLINE_EXCHANGE_ORDER_ACK_RSP, NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(OFFLINE_ELE_EXCHANGE_ORDER_MANAGE_SUCCESS, NORMAL_OFFLINE_ELE_EXCHANGE_HANDLER);
        COMMAND_HANDLER_MAPS.put(EXCHANGE_ORDER_MANAGE_SUCCESS, NORMAL_NEW_EXCHANGE_ORDER_HANDLER);


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
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RENT_ORDER_RSP_ACK, NORMAL_ELE_ORDER_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_RETURN_ORDER_RSP_ACK, NORMAL_ELE_ORDER_HANDLER);

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
         * 物理记录操作回调
         */
        COMMAND_HANDLER_MAPS.put(CUPBOARD_OPERATING_RECORD, NORMAL_CUPBOARD_OPERATING_RECORD_HANDLER);

        /**
         * ota升级
         */
        COMMAND_HANDLER_MAPS.put(OTA_OPERATE, NORMAL_OTA_OPERATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(OTA_OPERATE_RSP, NORMAL_OTA_OPERATE_HANDLER);

        COMMAND_HANDLER_MAPS.put(ELE_COMMAND_WARN_MSG_NOTIFY, NORMAL_ELE_WARN_MSG_HANDLER);
        /**
         * 电池充电设置
         */
        COMMAND_HANDLER_MAPS.put(ELE_BATTERY_MULTI_PARAMS_READ, NORMAL_BATTERY_MULTI_SETTING_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_BATTERY_MULTI_PARAMS_READ_RSP, NORMAL_BATTERY_MULTI_SETTING_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_BATTERY_MULTI_PARAM_SETTING, NORMAL_BATTERY_MULTI_SETTING_HANDLER);
        COMMAND_HANDLER_MAPS.put(TAKE_BATTERY_MODE, NORMAL_BATTERY_MULTI_SETTING_HANDLER);
        COMMAND_HANDLER_MAPS.put(ELE_BATTERY_SNAPSHOT, NORMAL_BATTERY_SNAPSHOT_HANDLER);

        COMMAND_HANDLER_MAPS.put(OTHER_SETTING_PARAM_TEMPLATE, NORMAL_OTHER_SETTING_PARAM_TEMPLATE_HANDLER);
        COMMAND_HANDLER_MAPS.put(READ_OTHER_SETTING_PARAM_TEMPLATE, NORMAL_OTHER_SETTING_PARAM_TEMPLATE_HANDLER);

        /**
         * 高温告警
         */
        COMMAND_HANDLER_MAPS.put(TEMPERATURE_WARNING, NORMAL_HIGH_TEMPERATURE_ALARM_HANDLER);

        COMMAND_HANDLER_MAPS.put(CALC_ELE_POWER_REPORT, NORMAL_ELE_CHARGE_POWER_HANDLER);
        COMMAND_HANDLER_MAPS.put(CALC_ELE_POWER_REPORT_ACK, NORMAL_ELE_CHARGE_POWER_HANDLER);

        /**
         * 满仓提醒
         */
        COMMAND_HANDLER_MAPS.put(FULL_BATTERY_WARNING, FULL_BATTERY_WARNING_HANDLER);

        /**
         * 新警告故障通知
         */

        COMMAND_HANDLER_MAPS.put(NEW_HARDWARE_WARN_MSG, NEW_HARDWARE_WARN_MSG_HANDLER);
        COMMAND_HANDLER_MAPS.put(NEW_HARDWARE_WARN_MSG_ACK, NEW_HARDWARE_WARN_MSG_HANDLER);
    
        /**
         * 柜机故障告警上报
         */
    
        COMMAND_HANDLER_MAPS.put(HARDWARE_FAILURE_WARN_MSG, HARDWARE_FAILURE_WARN_MSG_HANDLER);
        COMMAND_HANDLER_MAPS.put(HARDWARE_FAILURE_WARN_MSG_ACK, HARDWARE_FAILURE_WARN_MSG_HANDLER);
    
        /**
         * 离线换电密码设置
         */
        COMMAND_HANDLER_MAPS.put(ELE_BATTERY_OFFLINE_PASSWORD_RESET, NORMAL_OFFLINE_EXCHANGE_PASSWORD_HANDLER);
        
    }
}
