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

    //下单加锁缓存
    String ORDER_ELE_ID = "order_lock_ele_id:";

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


    //换电柜平台修改缓存
    String ELE_CONFIG_EDIT_UID = "ele_config_edit_uid:";


    //实名认证资料项缓存key
    String ELE_CACHE_AUTH_ENTRY = "ele_cache_auth_entry:";

    //用户实名认证资料锁
   String ELE_CACHE_USER_AUTH_LOCK_KEY = "ele_cache_user_auth_lock_key:";

    //用户缴纳押金
    String ELE_CACHE_USER_DEPOSIT_LOCK_KEY = "ele_cache_user_deposit_lock_key:";



    //下单周期限制
    String ORDER_TIME_UID = "order_time_uid:";

    //电池异常锁住换电柜
    String UNLOCK_CABINET_CACHE = "unlock_cabinet_cache:";

    //电柜上报数据
    String OTHER_CONFIG_CACHE = "other_config_cache:";

    String ELE_ORDER_WARN_MSG_CACHE_KEY = "ele_order_warn_msg:";

    String ELE_BIG_POWER_CELL_NO_CACHE_KEY = "big_power_cell_no:";

    //加盟商缓存
    String CACHE_FRANCHISEE = "franchisee:";

    /**
     * 角色名称和code
     */
    String OPERATE_NAME = "OPERATE_USER";
    String OPERATE_CODE = "运营商";
    String FRANCHISEE_NAME = "FRANCHISEE_USER";
    String FRANCHISEE_CODE = "加盟商";
    String STORE_NAME = "STORE_USER";
    String STORE_CODE = "门店";

    String CACHE_TENANT_ID = "tenant_cache:";


    //优惠券缓存
    String COUPON_CACHE = "coupon_cache:";

    //邀请活动缓存
    String SHARE_ACTIVITY_CACHE = "share_activity_cache:";


    //邀请分享缓存
    String SHARE_ACTIVITY_UID = "share_activity__lock_uid:";

    /**
     * 加盟商金额缓存
     */
    String CACHE_FRANCHISEE_AMOUNT = "cache_franchisee_amount:";
    /**
     * 门店金额缓存
     */
    String CACHE_STORE_AMOUNT = "cache_store_amount:";

    String CACHE_APP_INFO_LIMIT = "cache_app_info_limit:";
    String CACHE_APP_INFO_BASE = "cache_app_info_base:";
    String CACHE_APP_INFO = "cache_app_info:";
    String CACHE_THIRD_CALL_BACK_URL = "cache_third_call_back_url:";
    String CACHE_TENANT_ID_OPERATE = "cache_tenant_id_operate:";



    //新用户活动缓存
    String NEW_USER_ACTIVITY_CACHE = "new_user_activity_cache:";

    //老用户活动缓存
    String OLD_USER_ACTIVITY_CACHE = "old_user_activity_cache:";
}
