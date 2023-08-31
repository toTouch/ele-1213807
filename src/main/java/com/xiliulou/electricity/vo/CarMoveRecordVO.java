package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/15 14:51
 * @Description:
 */

@Data
public class CarMoveRecordVO {

    private Integer id;

    private Long tenantId;

    private Long carId;

    private String carSn;

    private Long carModelId;

    private String carModelName;

    private Long oldFranchiseeId;

    private String oldFranchiseeName;

    private Long oldStoreId;

    private String oldStoreName;

    private Long newFranchiseeId;

    private String newFranchiseeName;

    private Long newStoreId;

    private String newStoreName;

    private Long operator;

    private String operatorName;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

}
