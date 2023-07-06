package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-02-14:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognizeUserRecordQuery {
    private Long size;

    private Long offset;

    private Integer tenantId;

    private String userName;

    private String phone;
    /**
     * 状态
     */
    private Integer status;

    private Long startTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
