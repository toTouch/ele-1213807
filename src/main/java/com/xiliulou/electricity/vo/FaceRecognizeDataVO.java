package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-31-15:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognizeDataVO {

    private Long id;
    /**
     * 人脸核身次数
     */
    private Integer faceRecognizeCapacity;
    /**
     * 充值时间
     */
    private Long rechargeTime;

    private Integer tenantId;

    private String tenantName;
}
