package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: HRP
 * @Date: 2022/6/2 10:02
 * @Description:
 */
@Data
@Builder
public class ElectricityMemberCardRecordQuery {
    private Long size;
    private Long offset;
    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 停卡审核状态
     */
    private Integer status;

    private Integer tenantId;

    private Long uid;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;

    private Long beginTime;

    private Long endTime;

}
