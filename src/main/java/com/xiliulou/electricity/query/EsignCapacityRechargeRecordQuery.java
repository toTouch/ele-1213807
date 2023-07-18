package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 19:35
 * @Description:
 */

@Data
public class EsignCapacityRechargeRecordQuery {

    private Long id;

    private Integer esignCapacity;

    private Long operator;

    private String operatorName;

    private Long tenantId;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private Long size;

    private Long offset;

}
