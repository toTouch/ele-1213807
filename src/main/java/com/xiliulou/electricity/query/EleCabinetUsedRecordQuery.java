package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/3 10:20
 * @Description: 柜机使用记录查询
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EleCabinetUsedRecordQuery {

    private Long size;

    private Long offset;

    /**
     * 换电柜ID
     */

    private Long id;

    /**
     * 用户名
     */
    private String userName;

    private Long uid;

    private Long createTime;

    private Long beginTime;

    private Long endTime;

}
