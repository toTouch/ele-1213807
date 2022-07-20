package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class UserInfoQuery {
    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;
    private Long beginTime;
    private Long endTime;
    private Integer authStatus;
    private Integer tenantId;

    private Integer serviceStatus;

    private String batterySn;
    private Long franchiseeId;

    private Long memberCardExpireTimeBegin;
    private Long memberCardExpireTimeEnd;

    /**
     * 套餐id
     */
    private Long memberCardId;

    private String cardName;

    private Long uid;
}
