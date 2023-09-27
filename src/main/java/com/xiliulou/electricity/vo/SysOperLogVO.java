package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-12-17:41
 */
@Data
public class SysOperLogVO {

    private Long id;

    /**
     * 操作内容
     */
    private String title;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 操作人员id
     */
    private Long operatorUid;

    /**
     * 操作人员
     */
    private String operatorUserName;

    /**
     * 请求IP
     */
    private String operIp;

    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 其它信息
     */
    private String attr;

    /**
     * 操作时间
     */
    private Long operTime;
}
