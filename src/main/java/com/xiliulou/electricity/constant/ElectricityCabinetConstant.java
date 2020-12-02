package com.xiliulou.electricity.constant;

public interface ElectricityCabinetConstant {
	//换电柜缓存
	String CACHE_ELECTRICITY_CABINET = "electricity_cabinet:";
	//换电柜型号缓存
	String CACHE_ELECTRICITY_CABINET_MODEL = "electricity_cabinet_model:";
    //电池型号
    public static final String CACHE_ELECTRICITY_BATTERY_MODEL = "ELECTRICITY_BATTERY_MODEL:";

	//用户缓存
	String CACHE_USER_UID = "user_uid:";
	String CACHE_USER_PHONE = "user_phone:";

	//最后一个操作格挡缓存
	String ELECTRICITY_CABINET_DEVICE_LAST_CELL = "electricity_cabinet_cell_device_last:";
	//占用仓门缓存
	String ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY = "electricity_cabinet_cell_occupy:";

	//执行订单取消定时任务
	String CACHE_ELECTRICITY_CABINET_ORDER_CANCEL = "electricity_cabinet_order_cancel";
}
