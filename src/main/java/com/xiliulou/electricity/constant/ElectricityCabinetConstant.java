package com.xiliulou.electricity.constant;

public interface ElectricityCabinetConstant {
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET = "electricity_cabinet:";
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET_DEVICE = "electricity_cabinet_device:";
    //换电柜型号缓存
    String CACHE_ELECTRICITY_CABINET_MODEL = "electricity_cabinet_model:";
    //电池型号
    String CACHE_ELECTRICITY_BATTERY_MODEL = "ELECTRICITY_BATTERY_MODEL:";
    String ADMIN_OPERATE_LOCK_KEY = "admin_operate_lock_key:";
    String CACHE_PAY_PARAMS = "cache_pay_params";
    //用户缓存
    String CACHE_USER_UID = "user_uid:";
    String CACHE_USER_PHONE = "user_phone:";
    String CACHE_MEMBER_CARD = "CACHE_MEMBER_CARD:";
    String CACHE_USER_INFO_UID = "user_info_uid:";
    //最后一个操作格挡缓存
    String ELECTRICITY_CABINET_DEVICE_LAST_CELL = "electricity_cabinet_cell_device_last:";
    //占用仓门缓存
    String ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY = "electricity_cabinet_cell_occupy:";

    //执行订单取消定时任务
    String CACHE_ELECTRICITY_CABINET_ORDER_CANCEL = "electricity_cabinet_order_cancel";


    String WX_MIN_PRO_AUTHORIZATION_CODE_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    //第三方授权的限制频率的key
    String CAHCE_THIRD_OAHTH_KEY = "third_auth_key";
    //城市缓存，不变
    String CACHE_CITY = "city:";
    String CACHE_SUBSCRIPTION_MESSAGE = "CACHE_SUBSCRIPTION_MESSAGE_";
	String CACHE_USER_ROLE_RELATION = "user_role_relation:";
	String CACHE_ROLE_PERMISSION_RELATION = "role_permission_relation:";
	String CACHE_PERMISSION = "permission_id:";

    //门店缓存
    String CACHE_STORE = "store:";

    String CACHE_SERVICE_PHONE = "CACHE_SERVICE_PHONE:";
    //下单缓存
    String ORDER_UID = "order_uid:";
    //新增换电柜缓存
    String ELE_SAVE_UID = "ele_save_uid:";
    //修改换电柜缓存
    String ELE_EDIT_UID = "ele_edit_uid:";
}
