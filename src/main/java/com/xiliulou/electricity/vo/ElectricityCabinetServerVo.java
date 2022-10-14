package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/9/27 16:18
 * @mood
 */
@Data public class ElectricityCabinetServerVo {
    private Long id;
    /**
     * 绑定的柜机id
     */
    private Integer electricityCabinetId;

    private String productKey;

    private String deviceName;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 服务开始时间
     */
    private Long serverBeginTime;
    /**
     * 服务结束时间
     */
    private Long serverEndTime;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private String eleName;

    private String tenantName;
}
