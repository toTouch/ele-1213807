package com.xiliulou.electricity.constant;

public interface ElectricityCabinetConstant {
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET = "electricity_cabinet:";
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET_DEVICE = "electricity_cabinet_device:";
    //换电柜型号缓存
    String CACHE_ELECTRICITY_CABINET_MODEL = "electricity_cabinet_model:";

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

    String ELE_OPERATOR_CACHE_KEY = "ele_oper:";

    //换电柜开门的前缀
    String ELE_OPERATOR_SESSION_PREFIX = "ele";

    //订单同步订单响应
    String ELE_COMMAND_ORDER_SYNC_RSP = "new_order_sync_rsp";

    String ELE_RECEIVER_CACHE_KEY = "ele_receiver:";

    String ELE_ORDER_OPERATOR_CACHE_KEY = "ele_order_oper:";

    //换电柜平台修改缓存
    String ELE_CONFIG_EDIT_UID = "ele_config_edit_uid:";

    //实名认证审核资料锁
    String ELE_CACHE_AUTH_ENTRY_LOCK_KEY = "ele_cache_auth_entry_lock_key:";

    //实名认证资料项缓存key
    String ELE_CACHE_AUTH_ENTRY = "ele_cache_auth_entry:";

    //用户实名认证资料锁
   String ELE_CACHE_USER_AUTH_LOCK_KEY = "ele_cache_user_auth_lock_key:";

    //用户缴纳押金
    String ELE_CACHE_USER_DEPOSIT_LOCK_KEY = "ele_cache_user_deposit_lock_key:";

    //用户租电池
    String ELE_CACHE_USER_RENT_BATTERY_LOCK_KEY = "ele_cache_user_rent_battery_lock_key:";

    //用户还电池
    String ELE_CACHE_USER_RETURN_BATTERY_LOCK_KEY = "ele_cache_user_return_battery_lock_key:";

}
