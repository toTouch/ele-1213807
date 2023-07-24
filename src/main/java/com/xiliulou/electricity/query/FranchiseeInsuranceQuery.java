package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

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

}
