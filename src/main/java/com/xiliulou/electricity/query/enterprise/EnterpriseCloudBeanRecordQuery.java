package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-10:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCloudBeanRecordQuery {
    private Long size;
    private Long offset;



}
