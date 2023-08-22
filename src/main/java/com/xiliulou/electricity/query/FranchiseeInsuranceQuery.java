package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-10:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FranchiseeInsuranceQuery {

    private Long size;
    private Long offset;
    private Integer tenantId;

    private String name;
    private Long franchiseeId;
    private Integer insuranceType;
    private Integer type;
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
