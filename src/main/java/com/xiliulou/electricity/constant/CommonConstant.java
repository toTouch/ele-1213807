package com.xiliulou.electricity.constant;

/**
 * @author : eclair
 * @date : 2022/7/29 15:55
 */
public interface CommonConstant {
    /**
     * 角色名称和code
     */
    String OPERATE_NAME = "OPERATE_USER";
    String OPERATE_CODE = "运营商";
    String FRANCHISEE_NAME = "FRANCHISEE_USER";
    String FRANCHISEE_CODE = "加盟商";
    String STORE_NAME = "STORE_USER";
    String STORE_CODE = "门店";

    String APP_ID = "wx76159ea6aa7a64bc";
    String TENANT_ID = "tenantId";
    Integer TENANT_ID_DEFAULT = 0;
    //在线
    String STATUS_ONLINE="online";
    //离线
    String STATUS_OFFLINE="offline";
    
    /**
     * 满仓消息队列名称
     */
    String  FULL_BATTERY_DELY_QUEUE="full_battery_dely_queue";
}
