package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2025/3/17 10:38
 */
@Data
public class CabinetLocationVO {
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 租户id
     */
    private Integer tenantId;

    private String tenantName;

}
