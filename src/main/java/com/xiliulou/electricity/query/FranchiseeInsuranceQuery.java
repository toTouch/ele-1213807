package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-10:36
 */
@Data
@Builder
public class FranchiseeInsuranceQuery {

    private Long size;
    private Long offset;
    private Integer tenantId;

    private String name;
    private Long franchiseeId;
    private Integer insuranceType;
    private Integer status;
    private Long storeId;

    /**
     * 车辆型号
     */
    private Long carModelId;
    /**
     * 电池型号
     */
    private String simpleBatteryType;

}
