package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/26 13:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class EnterpriseRentBatteryOrderQuery {

    /**
     * 骑手UID
     */
    private Long uid;

    /**
     * 企业ID
     */
    private Long enterpriseId;

    /**
     * 起始时间
     */
    private Long beginTime;

    /**
     * 终止时间
     */
    private Long endTime;


}
