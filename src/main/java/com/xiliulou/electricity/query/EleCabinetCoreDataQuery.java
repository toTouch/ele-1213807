package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-07-14-18:24
 */
@Data
@Builder
public class EleCabinetCoreDataQuery {

    private Integer id;
    private Long size;
    private Long offset;

}
