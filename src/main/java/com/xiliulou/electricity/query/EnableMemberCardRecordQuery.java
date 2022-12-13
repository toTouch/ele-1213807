package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: hrp
 * @Date: 2022/11/17 10:02
 * @Description:
 */
@Data
@Builder
public class EnableMemberCardRecordQuery {


    private Long size;
    private Long offset;

    private Long beginTime;

    private Long endTime;

    private Integer franchiseeId;

    private String userName;

    private Integer enableType;


    /**
     * 用户手机号
     */
    private String phone;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private Long uid;

}
