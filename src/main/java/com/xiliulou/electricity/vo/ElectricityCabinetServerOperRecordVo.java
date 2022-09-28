package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/9/28 17:23
 * @mood
 */
@Data public class ElectricityCabinetServerOperRecordVo {
    private Long id;

    private Long eleServerId;

    private Long createUid;
    /**
     * 以前的服务开始时间
     */
    private Long oldServerBeginTime;
    /**
     * 以前的服务结束时间
     */
    private Long oldServerEndTime;
    /**
     * 修改后服务开始时间
     */
    private Long newServerBeginTime;
    /**
     * 修改后服务结束时间
     */
    private Long newServerEndTime;

    private Long createTime;

    private String eleName;

    private String tenantName;
}
