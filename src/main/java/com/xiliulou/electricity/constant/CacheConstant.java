package com.xiliulou.electricity.constant;

public interface CacheConstant {
    
    Long CACHE_EXPIRE_MONTH = 30 * 24 * 3600000L;
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET = "electricity_cabinet:";
    //换电柜缓存
    String CACHE_ELECTRICITY_CABINET_DEVICE = "electricity_cabinet_sn:";
    //换电柜型号缓存
    String CACHE_ELECTRICITY_CABINET_MODEL = "electricity_cabinet_model:";
    //换电柜GEO缓存
    String CACHE_ELECTRICITY_CABINET_GEO = "electricity_cabinet_geo:";
    //加载换电柜GEO缓存锁
    String CACHE_RELOAD_ELECTRICITY_CABINET_GEO = "reload_electricity_cabinet_geo:";
    
    //换电柜扩充信息缓存
    String CACHE_ELECTRICITY_CABINET_EXTRA = "electricity_cabinet_extra:";
    
    String ADMIN_OPERATE_LOCK_KEY = "admin_operate_lock_key:";
    String CACHE_PAY_PARAMS = "cache_pay_params";
    
    /**
     * 微信支付参数缓存key
     */
    String ELE_PAY_PARAMS_KEY = "saas_ele:pay_params:%d:%d";
    
    /**
     * 微信支付证书
     */
    String PAYMENT_CERTIFICATE_KEY = "saas_ele:pay_payment_certificate:%d:%d";
    
    
    /**
     * 微信支付证书缓存（调用微信接口获取）
     */
    String WECHAT_CERTIFICATE_KEY = "saas_ele:wechat_certificate:v3:%d:%d";
    
    //用户缓存
    String CACHE_USER_UID = "user_uid:";
    String CACHE_USER_PHONE = "user_phone:";
    String CACHE_USER_EXTRA = "user_extra:";
    String CACHE_MEMBER_CARD = "CACHE_MEMBER_CARD:";
    String CACHE_USER_INFO_UID = "user_info_uid:";
    //最后一个操作格挡缓存
    String ELECTRICITY_CABINET_DEVICE_LAST_CELL = "electricity_cabinet_cell_device_last:";
    //占用仓门缓存
    String ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY = "electricity_cabinet_cell_occupy:";
    
    String CACHE_LAST_ALLOCATE_FULLY_BATTERY_CELL = "last_allocate_fully_battery_cell:";
    
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
    //新增设备加锁
    String CACHE_DEVICE_CODE_LOCK = "device_code_lock:";
    
    //新增换电柜缓存
    String ELE_SAVE_UID = "ele_save_uid:";
    
    String ELE_BATCH_IMPORT = "ele_batch_import_uid:";
    
    String ELE_BATCH_OUT_WAREHOUSE = "ele_batch_out_warehouse:";
    
    String ELE_BATTERY_BATCH_EXIT_WAREHOUSE = "ele_battery_batch_exit_warehouse:";
    String ELE_CABINET_BATCH_EXIT_WAREHOUSE = "ele_cabinet_batch_exit_warehouse:";
    String ELE_CAR_BATCH_EXIT_WAREHOUSE = "ele_car_batch_exit_warehouse:";
    
    //修改换电柜缓存
    String ELE_EDIT_UID = "ele_edit_uid:";
    
    String ELE_OPERATOR_CACHE_KEY = "ele_oper:";
    
    String ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY = "ele_ope_self_open_cell:";
    
    //换电柜开门的前缀
    String ELE_OPERATOR_SESSION_PREFIX = "ele";
    
    String OPEN_FULL_CELL = "open_full_cell";
    
    
    String ELE_RECEIVER_CACHE_KEY = "ele_receiver:";
    
    //离线换电
    String OFFLINE_ELE_RECEIVER_CACHE_KEY = "offline_ele_receiver";
    
    //自助开仓
    String SELF_OPEN_CALL_CACHE_KEY = "self_open_cell_cache_key";
    
    // 换电柜与电池加盟商不一致开门 幂等锁
    String FRANCHISEES_NOT_SAME_OPEN_DOOR_LOCK = "franchisees_not_same_lock_cache_key:";
    
    // 换电柜与电池加盟商不一致开门
    String FRANCHISEES_NOT_SAME_OPEN_DOOR = "franchisees_not_same_cache_key:";
    
    //换电柜平台修改缓存
    String ELE_CONFIG_EDIT_UID = "ele_config_edit_uid:";
    
    //换电柜租户地图key
    String ELE_TENANT_MAP_EDIT_UID = "ele_tenant_map_edit_uid:";
    
    
    //实名认证资料项缓存key
    String ELE_CACHE_AUTH_ENTRY = "ele_cache_auth_entry:";
    
    //用户实名认证资料锁
    String ELE_CACHE_USER_AUTH_LOCK_KEY = "ele_cache_user_auth_lock_key:";
    
    //用户缴纳押金
    String ELE_CACHE_USER_DEPOSIT_LOCK_KEY = "ele_cache_user_deposit_lock_key:";
    
    String ELE_CACHE_SERVICE_FEE_LOCK_KEY = "ele_cache_service_fee_lock_key:";
    //用户还车审核
    String CACHE_USER_RETURN_CAR_LOCK = "cache_user_return_car_lock:";
    
    String ELE_CACHE_USER_BATTERY_SERVICE_FEE_LOCK_KEY = "ele_cache_user_battery_service_fee_lock_key";
    
    //用户集成支付
    String ELE_CACHE_USER_INTEGRATED_PAYMENT_LOCK_KEY = "ele_cache_user_integrated_payment_lock_key:";
    
    String ELE_CACHE_USER_BATTERY_MEMBERCARD_REFUND_LOCK_KEY = "ele_battery_membercard_refund_lock_key:";
    
    //下单周期限制
    String ORDER_TIME_UID = "order_time_uid:";
    
    String EXCHANGE_ORDER_HANDLE_LIMIT = "exchange_order_handle_limit:";
    
    String RENT_BATTERY_ORDER_HANDLE_LIMIT = "rent_battery_order_handle_limit:";
    
    //电池异常锁住换电柜
    String UNLOCK_CABINET_CACHE = "unlock_cabinet_cache:";
    
    //电柜上报数据
    //    String OTHER_CONFIG_CACHE = "other_config_cache:";
    String OTHER_CONFIG_CACHE_V_2 = "other_config_cache_v2:";
    
    String ELE_ORDER_WARN_MSG_CACHE_KEY = "ele_order_warn_msg:";
    
    String ELE_BIG_POWER_CELL_NO_CACHE_KEY = "big_power_cell_no:";
    
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
    /**
     * 微信模板通知管理员缓存
     */
    String CACHE_ADMIN_NOTIFICATION = "cache_admin_notification:";
    String CACHE_ADMIN_ALREADY_NOTIFICATION = "cache_admin_already_notification:";
    /**
     * 租户模板通知缓存
     */
    String CACHE_TEMPLATE_CONFIG = "cache_template_config:";
    /**
     * 低电量通知用户缓存
     */
    String CACHE_LOW_BATTERY_NOTIFICATION = "cache_low_battery_notification:";
    
    String CACHE_APP_INFO_LIMIT = "cache_app_info_limit:";
    String CACHE_APP_INFO_BASE = "cache_app_info_base:";
    String CACHE_APP_INFO = "cache_app_info:";
    String CACHE_THIRD_CALL_BACK_URL = "cache_third_call_back_url:";
    String CACHE_TENANT_ID_OPERATE = "cache_tenant_id_operate:";
    
    
    //新用户活动缓存
    String NEW_USER_ACTIVITY_CACHE = "new_user_activity_cache:";
    
    //老用户活动缓存
    String OLD_USER_ACTIVITY_CACHE = "old_user_activity_cache:";
    
    //邀请返现活动缓存
    String SHARE_MONEY_ACTIVITY_CACHE = "share_money_activity_cache:";
    
    String CACHE_ELE_OTHER_CONFIG = "ele_other_config:";
    
    String CACHE_FEISHU_ACCESS_TOKEN = "cache_feishu_access_token";
    
    String CLIENT_ID = "xiliulou-ele:";
    
    String MERCHANT_CLIENT_ID = "xiliulou-merchant:";
    
    String CACHE_TENANT_MAINTENANCE_USER_CONFIG = "cache_maintenance_config:";
    String CACHE_TENANT_MAINTENANCE_USER_CONFIG_TEST = "cache_maintenance_config_test:";
    
    String ELE_CACHE_USER_DISABLE_MEMBER_CARD_LOCK_KEY = "ele-cache_user_disable_member_card_lock_key";
    
    //换电柜车辆型号缓存
    String CACHE_ELECTRICITY_CAR_MODEL = "electricity_car_model:";
    
    //新增换电柜车辆缓存
    String CAR_SAVE_UID = "car_save_uid:";
    //修改换电柜车辆缓存
    String CAR_EDIT_UID = "car_edit_uid:";
    
    //换电柜车辆缓存
    String CACHE_ELECTRICITY_CAR = "electricity_car:";
    
    //新增租户缓存
    String ELE_ADD_TENANT_CACHE = "ele_add_tenant__lock_id:";
    
    //用户缴纳租车押金
    String ELE_CACHE_USER_CAR_DEPOSIT_LOCK_KEY = "ele_cache_user_car_deposit_lock_key:";
    //用户退车押金
    String CACHE_USER_CAR_RETURN_DEPOSIT_LOCK = "cache_user_car_return_deposit_lock:";
    //用户购买租车套餐
    String ELE_CACHE_USER_CAR_CARD_LOCK_KEY = "ele_cache_user_car_card_lock_key:";
    //租车订单
    String ELE_CACHE_USER_RENT_CAR_LOCK_KEY = "ele_cache_user_rent_car_lock_key:";
    
    //操作银行卡 用户锁
    String BIND_BANK_OPER_USER_LOCK = "bind_Bank_oper_user_lock:";
    //提现密码缓存
    String CACHE_WITHDRAW_PASSWORD = "withdraw_password";
    
    // 批量提现审核 用户锁
    String CACHE_BATCH_WITHDRAW_OPER_USER_LOCK = "batch_withdraw_operation_user_lock:";
    
    // 门店修改
    String STORE_SAVE_UID = "store_save_uid:";
    
    //提现 用户锁
    String CACHE_WITHDRAW_USER_UID = "withdraw_user_uid:";
    /**
     * userInfo的缓存
     */
    String CACHE_USER_INFO = "user_info:";
    String CACHE_ELE_SET_CONFIG = "ele_set_config:";
    String CACHE_ELE_APP_SET_CONFIG = "ele_app_set_config:";
    
    /**
     * saas选仓换电开关是否有更新
     */
    String CACHE_ELE_SELECTION_EXCHANGE_UPDATE_TIME = "ele_selection_exchange_update_time:";
    
    String CACHE_ELE_SET_MAP_KEY = "ele_set_map_key:";
    String CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LAST_TIME = "cache_ele_battery_member_card_expired_last_time:";
    String CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LAST_TIME = "cache_ele_car_member_card_expired_last_time:";
    String CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LOCK = "cache_ele_battery_member_card_expired_lock:";
    String CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LOCK = "cache_ele_car_member_card_expired_lock:";
    String CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_BREAK_POWER_LOCK = "cache_ele_car_member_card_expired_break_power_lock:";
    String CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_BREAK_POWER_LAST_TIME = "cache_ele_car_member_card_expired_break_power_last_time:";
    
    String MEMBER_CARD_EXPIRING_SOON = "member_card_expiring_soon_cache:";
    String OTA_PROCESS_CACHE = "ota_process_cache";
    
    /**
     * 满仓柜机缓存
     */
    String FULL_BOX_ELECTRICITY_CACHE = "full_box_electricity_cache:";
    
    String CHECK_FULL_BATTERY_CACHE = "check_full_battery_cache:";
    
    String OTA_OPERATE_CACHE = "ota_operate_cache:";
    String NEW_OTA_OPERATE_CACHE = "new_ota_operate_cache:";
    /**
     * 异常告警导出缓存
     */
    String WARN_MESSAGE_EXPORT_CACHE = "warn_message_export_cache:";
    
    /**
     * 异常告警导出缓存
     */
    String USER_NOTIFY_SAVE_CACHE_UID = "user_notify_save_cache_uid:";
    
    
    /**
     * 保险缓存
     */
    String CACHE_FRANCHISEE_INSURANCE = "CACHE_FRANCHISEE_INSURANCE:";
    
    /**
     * insuranceUserInfo的缓存
     */
    String CACHE_INSURANCE_USER_INFO = "insurance_user_info:";
    
    /**
     * 用户电池缓存
     */
    String CACHE_USER_BATTERY = "user_battery:";
    
    /**
     * 用户电池套餐缓存
     */
    String CACHE_USER_BATTERY_MEMBERCARD = "user_battery_membercard:";
    
    /**
     * 用户电池服务费缓存
     */
    String CACHE_USER_BATTERY_SERVICE_FEE = "user_battery_service_fee:";
    
    /**
     * 用户电池押金缓存
     */
    String CACHE_USER_DEPOSIT = "user_deposit:";
    /**
     * 用户电租车押金缓存
     */
    String CACHE_USER_CAR_DEPOSIT = "user_car_deposit:";
    /**
     * 用户租车套餐缓存
     */
    String CACHE_USER_CAR_MEMBERCARD = "user_car_membercard:";
    /**
     * 用户车辆缓存
     */
    String CACHE_USER_CAR = "user_car:";
    
    /**
     * 电池电压电流变化记录缓存
     */
    String CACHE_VOLTAGE_CURRENT_CHANGE = "voltage_current_change:";
    
    String SERVICE_FEE_USER_INFO = "service_fee_user_info:";
    
    /**
     * 区/县缓存
     */
    String CACHE_REGION_CODE = "region_code:";
    String CACHE_REGION_ID = "region_id:";
    
    /**
     * 城市缓存
     */
    String CACHE_CITY_CODE = "city_code:";
    
    /**
     * 用户扩展信息缓存
     */
    String CACHE_USER_INFO_EXTRA = "user_info_extra:";
    
    /**
     * 加盟商缓存
     */
    String CACHE_FRANCHISEE = "franchisee:";
    
    /**
     * 第三方配置缓存
     */
    String CACHE_FACEID_CONFIG = "faceidConfig:";
    
    /**
     * 电子签署配置缓存
     */
    String CACHE_ELE_CABINET_ESIGN_CONFIG = "electricity_cabinet_esign_config:";
    
    /**
     * 电子签署个人认证缓存锁
     */
    String CACHE_ELE_CABINET_ESIGN_AUTH_LOCK_KEY = "electricity_cabinet_esign_auth_lock_key:";
    
    /**
     * 电子签署根据模板创建文件缓存锁
     */
    String CACHE_ELE_CABINET_ESIGN_CREATE_FILE_LOCK_KEY = "electricity_cabinet_esign_create_file_lock_key:";
    
    /**
     * 电子签署个人签署缓存锁
     */
    String CACHE_ELE_CABINET_ESIGN_SIGN_LOCK_KEY = "electricity_cabinet_esign_sign_lock_key:";
    
    /**
     * 购买套餐分账缓存锁
     */
    String CACHE_DIVISION_ACCOUNT_PACKAGE_PURCHASE_KEY = "cache_division_account_package_purchase_key:";
    
    /**
     * 退租套餐分账缓存锁
     */
    String CACHE_DIVISION_ACCOUNT_PACKAGE_REFUND_KEY = "cache_division_account_package_refund_key:";
    
    /**
     * 购买套餐是处理活动信息缓存锁
     */
    String CACHE_HANDLE_ACTIVITY_PACKAGE_PURCHASE_KEY = "cache_handle_activity_package_purchase_key:";
    
    /**
     * 实名认证后处理活动信息缓存锁
     */
    String CACHE_HANDLE_ACTIVITY_REAL_NAME_AUTH_KEY = "cache_handle_activity_real_name_auth_key:";
    
    /**
     * 用户登录注册后处理活动信息缓存锁
     */
    String CACHE_HANDLE_ACTIVITY_USER_REGISTER_KEY = "cache_handle_activity_user_register_key:";
    
    /**
     * 购买套餐后发放优惠券给用户缓存锁
     */
    String CACHE_SEND_COUPON_PACKAGE_PURCHASE_KEY = "cache_send_coupon_package_purchase_key:";
    
    /**
     * 购买套餐下单缓存锁
     */
    String ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY = "user_battery_member_card_lock_key:";
    
    /**
     * 企业渠道用户购买套餐缓存锁
     */
    String ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_LOCK_KEY = "enterprise_user_purchase_package_lock_key:";
    
    /**
     * 企业渠道用户购买套餐+押金
     */
    String ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITH_DEPOSIT_LOCK_KEY = "enterprise_user_purchase_package_with_deposit_lock_key:";
    
    /**
     * 企业渠道用户免押购买套餐
     */
    String ELE_CACHE_ENTERPRISE_USER_PURCHASE_PACKAGE_WITHOUT_DEPOSIT_LOCK_KEY = "enterprise_user_purchase_package_without_deposit_lock_key:";
    
    /**
     * 根据手机号码添加企业渠道用户缓存锁
     */
    String ELE_CACHE_ENTERPRISE_USER_SAVE_BY_PHONE_LOCK_KEY = "enterprise_user_save_by_phone_lock_key:";
    
    /**
     * 扫码添加企业渠道用户缓存锁
     */
    String ELE_CACHE_ENTERPRISE_USER_UPDATE_AFTER_SCAN_LOCK_KEY = "enterprise_user_update_after_scan_lock_key:";
    
    
    String ELE_CACHE_USER_CLOUD_BEAN_RECHARGE_LOCK_KEY = "user_cloud_bean_recharge_lock_key:";
    
    /**
     * 迁移加盟商缓存锁
     */
    String ELE_CACHE_USER_MOVE_FRANCHISEE_LOCK_KEY = "user_move_franchisee_lock_key:";
    /**
     * 电池套餐取消支付缓存锁
     */
    String ELE_CACHE_BATTERY_CANCELL_PAYMENT_LOCK_KEY = "battery_cancell_payment_lock_key:";
    
    /**
     * 已分配过的格挡缓存
     */
    String CACHE_DISTRIBUTION_CELL = "cache_distribution_cell:";
    /**
     * 上一次取格挡缓存
     */
    String CACHE_PRE_TAKE_CELL = "pre_take_cell:";
    String CACHE_BT_ATTR = "bt_attr:";
    
    /**
     * 人脸核身token缓存锁
     */
    String ELE_CACHE_FACEID_TOKEN_LOCK_KEY = "faceid_token_lock_key:";
    
    /**
     * 人脸核身结果缓存锁
     */
    String ELE_CACHE_FACEID_RESULT_LOCK_KEY = "faceid_result_lock_key:";
    String CACHE_PXZ_CONFIG = "cache_pxz_config:";
    
    /**
     * 免押订单查询缓存锁
     */
    String ELE_CACHE_FREE_BATTERY_DEPOSIT_LOCK_KEY = "free_battery_deposit_lock_key:";
    String ELE_CACHE_FREE_CAR_DEPOSIT_LOCK_KEY = "free_car_deposit_lock_key:";
    String ELE_CACHE_FREE_DEPOSIT_CAPACITY_LOCK_KEY = "free_deposit_capacity_lock_key:";
    
    /**
     * 免押电池套餐押金支付缓存锁
     */
    String ELE_CACHE_FREE_DEPOSIT_MEMBERCARD_LOCK_KEY = "free_deposit_membercard_lock_key:";
    
    /**
     * 换电套餐免押订单链接缓存
     */
    String ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY = "battery_free_deposit_order_generate_lock_key:";
    /**
     * 换电套餐免押订单链接缓存V2
     */
    String ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2 = "battery_free_deposit_order_generate_lock_key:%s:%s";
    /**
     * 租车套餐免押订单链接缓存
     */
    String ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY = "car_rental_free_deposit_order_generate_lock_key:";
    /**
     * 租车套餐免押订单链接缓存V2
     */
    String ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2 = "car_rental_free_deposit_order_generate_lock_key:%s:%s";
    
    /**
     * 企业渠道换电套餐免押订单链接缓存
     */
    String ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY = "enterprise_battery_free_deposit_order_generate_lock_key:";
    /**
     * 企业渠道换电套餐免押订单链接缓存V2
     */
    String ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2 = "enterprise_battery_free_deposit_order_generate_lock_key:%s:%s";
    
    /**
     * 用户活跃缓存
     */
    String USER_ACTIVE_INFO_CACHE = "user_active_info_cache:";
    /**
     * 渠道人缓存
     */
    String CACHE_USER_CHANNEL = "cache_user_channel:";
    
    String CACHE_SCAN_INTO_ACTIVITY_LOCK = "CACHE_SCAN_INTO_ACTIVITY_LOCK:";
    /**
     * 电池型号缓存
     */
    String CACHE_BATTERY_MODEL = "battery_model:";
    String CACHE_BATTERY_MATERIAL = "battery_material:";
    
    /**
     * 车辆锁状态
     */
    String CACHE_CAR_LOCK_STATUS = "cache_car_lock_status:";
    /**
     * 分帐配置缓存
     */
    String CACHE_DIVISION_ACCOUNT_CONFIG = "division_account_config:";
    
    
    String CACHE_RECEIVE_COUPON_LOCK = "receive_coupon_lock:";
    
    /**
     * 邀请活动缓存
     */
    String CACHE_INVITATION_ACTIVITY = "invitation_activity:";
    
    String CACHE_GET_COUPON = "get_coupon:";
    
    String CACHE_BATTERY_MEMBERCARD = "battery_membercard:";
    
    String CACHE_CHARGE_POWER_CONFIG = "charge_p_conf:";
    String CACHE_CHARGE_POWER_CONFIG_NONE = "charge_p_conf_none:";
    String CACHE_CHARGE_CONFIG_OPERATE_LIMIT = "c_p_conf_op:";
    String CACHE_USER_EXPORT_LIMIT = "c_u_export_limit:";
    
    String CACHE_ELECTRICITY_CABINET_EXTEND_DATA = "electricity_cabinet_extends_data:";
    
    /**
     * 企业配置缓存
     */
    String CACHE_ENTERPRISE_INFO = "enterprise_Info:";
    
    /**
     * 商户扩展属性缓存
     */
    String CACHE_MERCHANT_ATTR = "merchantAttr:";
    String CACHE_MERCHANT_ATTR_CONFIG = "merchantAttrConfig:";
    /**
     * 返利配置
     */
    String CACHE_REBATE_CONFIG = "rebateConfig:";
    String CACHE_REBATE_CONFIG_SAVE_LOCK = "cache_rebate_config_save_lock:";
    String CACHE_REBATE_CONFIG_UPDATE_LOCK = "cache_rebate_config_update_lock:";
    String CACHE_REBATE_CONFIG_MODIFY_LOCK = "cache_rebate_config_modify_lock:";
    String CACHE_UPGRADE_CONDITION_UPDATE_LOCK = "cache_upgrade_condition_update_lock:";
    String CACHE_CHANNEL_SWITCH_STATE_UPDATE_LOCK = "cache_channel_switch_state_update_lock:";
    String CACHE_INVITATION_CONDITION_UPDATE_LOCK = "cache_invitation_condition_update_lock:";
    
    String CACHE_WECHAT_APPROVE = "cache_wechat_approve";
    
    String CACHE_INVITATION_ACTIVITY_USER_SAVE_LOCK = "cache_invitation_activity_user_save_lock:";
    String CACHE_ASSET_WAREHOUSE_LOCK = "cache_asset_warehouse_lock:";
    String CACHE_ASSET_INVENTORY_LOCK = "cache_asset_inventory_lock:";
    String CACHE_ASSET_EXIT_WAREHOUSE_RECORD_LOCK = "cache_asset_exit_warehouse_record_lock:";
    String CACHE_ASSET_WAREHOUSE_UPDATE_LOCK = "cache_asset_warehouse_update_lock:";
    String CACHE_ASSET_ALLOCATE_LOCK = "cache_asset_allocate_lock:";
    String CACHE_ASSET_BATCH_INVENTORY_LOCK = "cache_asset_batch_inventory_lock:";
    
    String CACHE_BATCH_SEND_COUPON = "cache_batch_send_coupon:";
    
    String CACHE_MERCHANT_AREA_SAVE_LOCK = "cache_merchant_area_save_lock:";
    
    String CACHE_MERCHANT_LEVEL_UPDATE_LOCK = "cache_merchant_level_update_lock:";
    
    String CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK = "cache_merchant_scan_into_activity_lock:";
    String CACHE_MERCHANT = "cache_merchant:";
    // 商家修改
    String MERCHANT_SAVE_UID = "merchant_save_uid:";
    String MERCHANT_PLACE_SAVE_UID = "merchant_place_save_uid:";
    
    String MERCHANT_PLACE_UPDATE_UID = "merchant_place_update_uid:";
    
    String MERCHANT_PLACE_DELETE_UID = "merchant_place_delete_uid:";
    
    String CACHE_MERCHANT_PLACE = "cache_merchant_place:";
    
    String MERCHANT_PLACE_CABINET_BIND_UID = "merchant_place_cabinet_bind_uid:";
    String MERCHANT_PLACE_CABINET_UNBIND_UID = "merchant_place_cabinet_unbind_uid:";
    
    String MERCHANT_PLACE_CABINET_DELETE_UID = "merchant_place_cabinet_delete_uid:";
    
    String CACHE_CHANNEL_EMPLOYEE_SAVE_LOCK = "cache_channel_employee_save_lock:";
    
    String CACHE_CHANNEL_EMPLOYEE_UPDATE_LOCK = "cache_channel_employee_update_lock:";
    
    String CACHE_MERCHANT_EMPLOYEE_SAVE_LOCK = "cache_merchant_employee_save_lock:";
    
    String CACHE_MERCHANT_EMPLOYEE_UPDATE_LOCK = "cache_merchant_employee_update_lock:";
    
    String CACHE_MERCHANT_WITHDRAW_APPLICATION = "cache_merchant_withdraw_application:";
    
    String CACHE_MERCHANT_WITHDRAW_APPLICATION_REVIEW = "cache_merchant_withdraw_application_review:";
    
    String CACHE_FAILURE_ALARM_LOCK = "cache_failure_alarm_lock:";
    
    String CACHE_TENANT_NOTE_RECHARGE_LOCK = "cache_tenant_note_recharge_lock:";
    
    String CACHE_FAILURE_ALARM_UPDATE_LOCK = "cache_failure_alarm_update_lock:";
    
    String CACHE_FAILURE_ALARM_DELETE_LOCK = "cache_failure_alarm_delete_lock:";
    
    String CACHE_FAILURE_ALARM_BATCH_SET_LOCK = "cache_failure_alarm_batch_set_lock:";
    
    String CACHE_FAILURE_ALARM = "cache_failure_alarm:";
    
    String CACHE_TENANT_NOTE = "cache_tenant_note:";
    
    String CACHE_USER_NOTICE_UPDATE_LOCK = "cache_user_notice_update_lock:";
    String CACHE_USER_DEPOSIT_PROTOCOL_UPDATE_LOCK = "cache_user_deposit_protocol_update_lock:";
    String CACHE_USER_ORDER_PROTOCOL_UPDATE_LOCK = "cache_user_order_protocol_update_lock:";
    String CACHE_USER_CAR_RENTAL_REFUND_PROTOCOL_UPDATE_LOCK = "cache_user_car_rental_refund_protocol_update_lock:";
    String CACHE_USER_MEMBER_CARD_DISABLE_LOCK = "cache_user_member_card_disable_lock:";
    String CACHE_USER_MEMBER_CARD_ENABLE_LOCK = "cache_user_member_card_enable_lock:";
    String CACHE_USER_CLEAN_BATTERY_SERVICE_FEE_LOCK = "cache_user_clean_battery_service_fee_lock:";
    String CACHE_USER_ADD_USER_DEPOSIT_MEMBER_CARD_LOCK = "cache_user_add_user_deposit_member_card_lock:";
    String CACHE_USER_MEMBER_CARD_EDIT_LOCK = "cache_user_member_card_edit_lock:";
    String CACHE_USER_BATTERY_MEMBER_CARD_RENEWAL_LOCK = "cache_user_battery_member_card_renewal_lock:";
    
    String CACHE_MERCHANT_MODIFY_INVITER_LOCK = "cache_merchant_modify_inviter_lock:";
    
    
    String CACHE_RECYCLE_CLOUD_BEAN_LOCK = "cache_recycle_cloud_bean_lock:";
    
    String CACHE_USER_GROUP_SAVE_LOCK = "cache_user_group_save_lock:";
    String CACHE_USER_GROUP_UPDATE_LOCK = "cache_user_group_update_lock:";
    String CACHE_USER_GROUP_BATCH_IMPORT = "cache_user_group_batch_import:";
    String CACHE_USER_GROUP_DETAIL_UPDATE_LOCK = "cache_user_group_detail_update_lock:";
    String CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK = "cache_user_group_detail_bind_group_lock:";
    String CACHE_USER_GROUP = "cache_user_group:";
    
    String CACHE_CHANNEL_USER_EXIT_LOCK = "cache_channel_user_exit_lock:";
    
    String CACHE_CHANNEL_USER_ADMIN_EXIT_LOCK = "cache_channel_user_admin_exit_lock:";
    
    String CACHE_CHANNEL_USER_EXIT_ALL_LOCK = "cache_channel_user_exit_all_lock:";
    
    String CACHE_BATTERY_BATCH_IMPORT_LOCK = "cache_battery_batch_import_lock:";
    
    //角色权限绑定操作
    String ROLE_PERMISSION_OPERATE_KEY = "saas_electricity:role_permission_opt:%s";
    
    //逾期用户备注重复提交锁
    String CACHE_USERINFO_OVERDUE_REMARK_SAVE_LOCK = "cache_userinfo_overdue_remark_save_lock:%d:%d";
    
    
    String OPEN_FULL_CELL_LIMIT = "open_full_cell_handle_limit:";
    
    
    String ALLOW_SELF_OPEN_CELL_START_TIME = "allow_self_open_cell_start_time:";
    
    String VEHICLE_BINDING_IDEMPOTENT_LOCK = "vehicle_binding_idempotent_lock:%s";
    
    String CACHE_USER_BIND_BATTERY_LOCK = "cache_user_bind_battery_lock:";
    
    String FREE_DEPOSIT_PAY_LOCK = "free_deposit_pay_lock:";
    
    String CACHE_FY_CONFIG = "cache_fy_config:";
    String CACHE_OFFLINE_KEY = "cache_offline_key:";
    String CACHE_OFFLINE_KEY_V2 = "cache_offline_key_v2:";
    
    /**
     * TCP设备session缓存
     */
    String CACHE_CABINET_SN_ONLINE = "device_report:cabinet_online:";
    
    
    /**
     * 分账配置缓存
     */
    String PROFIT_SHARING_PAY_PARAMS_ID_KEY = "saas_ele:profit_sharing:pay_params%d:%d";
    
    
    /**
     * 分账配置幂等
     */
    String PROFIT_SHARING_IDEMPOTENT_KEY = "saas_ele:profit_sharing:idempotent:%d";
    
    /**
     * 分账配置幂等
     */
    String PROFIT_SHARING_RECEIVER_IDEMPOTENT_KEY = "saas_ele:profit_sharing_receiver:idempotent:%d";
    
    
    
    /**
     * 分账统计分布式锁
     */
    String PROFIT_SHARING_STATISTICS_LOCK_KEY = "saas_ele:profit_sharing_statistics:lock:%d:%d";
    
    
    /**
     * 柜机仓门配置key
     */
    String CACHE_ELECTRICITY_CABINET_CELL_CONFIG = "electricity_cabinet_cell_config:";
    
    String CACHE_MEI_TUAN_RIDER_MALL_CONFIG = "mei_tuan_rider_mall_config:";
    
    String CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY = "cache_mei_tuan_create_battery_member_card_order_lock_key:";
    
    
    /**
     * 免押信息用户key
     */
    String FREE_DEPOSIT_USER_INFO_KEY = "free_deposit_user_info:%s";
    
    /**
     * 免押信息用户key
     */
    String CAR_FREE_DEPOSIT_USER_INFO_LOCK_KEY = "car_free_deposit_user_info_lock_key:%s:%s";
 
    
    String UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY = "un_free_deposit_user_lock_order:%s";
    
    /**
     * 购买分期套餐签约，缓存签约用的二维码
     */
    String CACHE_INSTALLMENT_FORM_BODY = "installment:form_Body:%s";
    
    /**
     * 分期套餐取消签约记录key
     */
    String CACHE_INSTALLMENT_CANCEL_SIGN = "installment:cancel_sign";
    
    /**
     * 签约取消互斥锁
     */
    String CACHE_INSTALLMENT_SIGN_CANCEL_LOCK = "installment:sign_cancel_lock:%d";
    
    /**
     * 代扣锁
     */
    String CACHE_INSTALLMENT_DEDUCT_LOCK = "installment:deduct_lock:%d";

    /**
     * 分期套餐混合支付锁
     */
    String CACHE_INSTALLMENT_PAYMENT_LOCK = "installment:payment_lock:%d";

    /**
     * 分期套餐签约成功后处理逻辑锁
     */
    String CACHE_INSTALLMENT_SIGN_NOTIFY_LOCK = "installment:sign_notify_lock:%d";

    /**
     * 分期套餐代扣后处理逻辑锁
     */
    String CACHE_INSTALLMENT_AGREEMENT_PAY_NOTIFY_LOCK = "installment:agreement_pay_notify_lock:%d";
    
    /**
     * 设备信息缓存
     */
    String CACHE_DEVICE_CODE = "cache_device_code:";
    String CACHE_DEVICE_CODE_SN = "cache_device_code_sn:";
}

