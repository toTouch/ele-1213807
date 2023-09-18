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
    String MAINTAIN_NAME="MAINTAIN_USER";
    String MAINTAIN_CODE="运维";

    String APP_ID = "wx76159ea6aa7a64bc";
    String TENANT_ID = "tenantId";
    Integer TENANT_ID_DEFAULT = 0;
    //在线
    String STATUS_ONLINE="online";
    //离线
    String STATUS_OFFLINE="offline";

    String BUCKET_NAME="bucketName";
    String FILE_NAME="fileName";
    
    /**
     * 满仓消息队列名称
     */
    String  FULL_BATTERY_DELY_QUEUE="full_battery_dely_queue";

    String INNER_HEADER_APP = "APP";

    String INNER_HEADER_TIME = "TIME";

    String INNER_HEADER_INNER_TOKEN = "INNER-TOKEN";

    String INNER_TENANT_ID = "TENANT-CODE";

    String APP_SAAS = "SAAS";

    String APP_SAAS_AES_KEY = "123abc*@saasxab$";

    /**
     * 删除状态 0：正常，1：删除
     */
    Integer DEL_N = 0;

    Integer DEL_Y = 1;

    /**
     * 版本
     */
    String SWITCH_VERSION = "v3";


    /**
     * 链路ID
     */
    String TRACE_ID = "traceId";

    /** 飞书预警机器人地址 */
    String FEISHU_WARNING_ROBOT_WEB_HOOK_URL = "https://open.feishu.cn/open-apis/bot/v2/hook/5f47012b-b311-444e-b7c4-c433b1c100f3";
}
