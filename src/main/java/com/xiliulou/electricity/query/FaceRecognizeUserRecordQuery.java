package com.xiliulou.electricity.query;

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
public class FaceRecognizeUserRecordQuery {
    private Long size;

    private Long offset;

    private String userName;

    private String phone;

    private Long stareTime;

    private Long endTime;
}
