package com.xiliulou.electricity.constant;

/**
 * @author HeYafeng
 * @description 柜机相关常量
 * @date 2023/10/27 15:05:50
 */
public class OtaConstant {
    
    /**
     * ota下载
     */
    public static final Integer OTA_TYPE_DOWNLOAD = 1;
    
    /**
     * ota同步
     */
    public static final Integer OTA_TYPE_SYNC = 2;
    
    /**
     * ota升级
     */
    public static final Integer OTA_TYPE_UPGRADE = 3;
    
    public static final int OTA_VERSION_TYPE_FOR_SYNC_UPGRADE = 0;
    public static final int OTA_VERSION_TYPE_OLD = 1;
    
    public static final int OTA_VERSION_TYPE_NEW = 2;
    
    public static final int OTA_VERSION_TYPE_SIX = 3;
    
    public static final int OTA_VERSION_TYPE_NEW_SIX = 4;
    
    public static final String SESSION_PREFIX_OLD = "OLD";
    
    public static final String SESSION_PREFIX_NEW = "NEW";
    
    public static final String SESSION_PREFIX_SIX = "SIX";
    
    public static final String SESSION_PREFIX_NEW_SIX = "NEWSIX";
    
    public static final String OTA_CORE_FILE_URL = "coreFileUrl";
    
    public static final String OTA_OPERATE_TYPE = "operateType";
    
    public static final String OTA_USERID = "userid";
    
    public static final String OTA_USERNAME = "username";
    
    public static final String OTA_CONTENT_CELL_NOS = "cellNos";
    
    public static final String OTA_CONTENT = "content";
    
    public static final String OTA_CORE_FILE_SHA256HEX = "coreFileSha256Hex";
    
    public static final String OTA_SUB_FILE_URL = "subFileUrl";
    
    public static final String OTA_SUB_FILE_SHA256HEX = "subFileSha256Hex";
    
}