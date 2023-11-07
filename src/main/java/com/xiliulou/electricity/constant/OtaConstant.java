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
    
    /**
     * ota 六合一下载
     */
    public static final Integer OTA_SIX_IN_ONE_TYPE_DOWNLOAD = 4;
    
    /**
     * ota 六合一同步
     */
    public static final Integer OTA_SIX_IN_ONE_TYPE_SYNC = 5;
    
    /**
     * ota 六合一升级
     */
    public static final Integer OTA_SIX_IN_ONE_TYPE_UPGRADE = 6;
    
    
    public static final int OTA_VERSIONTYPE_OLD = 1;
    
    public static final int OTA_VERSIONTYPE_NEW = 2;
    
    public static final int OTA_VERSIONTYPE_SIX_IN_ONE = 3;
    
    
    public static final String SESSION_PREFIX_OLD = "OLD";
    
    public static final String SESSION_PREFIX_NEW = "NEW";
    
    public static final String SESSION_PREFIX_SIX_IN_ONE = "SIXINONE";
    
    public static final String OTA_CORE_FILE_URL = "coreFileUrl";
    
    public static final String OTA_OPERATE_TYPE = "operateType";
    
    public static final String OTA_USERID = "userid";
    
    public static final String OTA_USERNAME = "username";
    
    public static final String OTA_CONTENT_CELLNOS = "cellNos";
    
    public static final String OTA_CONTENT = "content";
    
    public static final String OTA_CORE_FILE_SHA256HEX = "coreFileSha256Hex";
    
    public static final String OTA_SUB_FILE_URL = "subFileUrl";
    
    public static final String OTA_SUB_FILE_SHA256HEX = "subFileSha256Hex";
    
}