package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-20-18:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeDepositRechargeRecordVO {
    private Long id;
    /**
     * 免押充值次数
     */
    private Integer freeRecognizeCapacity;
    /**
     * 操作人
     */
    private Long operator;

    private String operatorName;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;
}
