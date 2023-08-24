package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/8/19 14:10
 * @Description:
 */

@Data
@Builder
public class ChannelActivityHistoryQuery {

    private Long size;

    private Long offset;

    private Long id;

    private Long uid;

    private String phone;

    private Long inviteUid;

    private Long channelUid;

    /**
     * 邀请状态 1--已参与 2--邀请成功 3--已过期 4--被替换
     */
    private Integer status;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;

    private Long beginTime;

    private Long endTime;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;

}
