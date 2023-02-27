package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-31-17:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognizeRechargeRecordVO {
    private Long id;
    /**
     * 人脸核身充值次数
     */
    private Integer faceRecognizeCapacity;
    /**
     * 操作人
     */
    private Long operator;

    private String operatorName;

    private Integer tenantId;

    private String tenantName;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;
}
