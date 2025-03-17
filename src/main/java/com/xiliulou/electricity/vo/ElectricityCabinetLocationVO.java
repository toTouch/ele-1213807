package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.constant.EleCabinetConstant;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2025/3/17 10:38
 */
@Data
public class ElectricityCabinetLocationVO {
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
