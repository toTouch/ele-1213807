package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 20:58
 * @Description:
 */
@Data
public class EsignCapacityDataQuery {

    private Long id;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    @NotNull(message = "签署次数不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer esignCapacity;

    private Long rechargeTime;

    private Long tenantId;

    private String tenantName;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private Long size;

    private Long offset;

}
