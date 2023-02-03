package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-02-14:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognizeUserRecordVO {

    private Long id;
    /**
     * 使用人
     */
    private Long uid;

    private String userName;
    /**
     * 审核结果
     */
    private String authResult;
    /**
     * 状态
     */
    private Integer status;

    private Integer tenantId;

    private Long createTime;


}
