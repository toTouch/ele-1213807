package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    private Integer authType;
    private Integer tenantId;

    private Integer serviceStatus;
    private Integer memberCardExpireType;

    private String nowElectricityBatterySn;
    private Long batteryId;
    private Long franchiseeId;
    private List<Long> franchiseeIds;
    private List<Long> storeIds;

    private Long memberCardExpireTimeBegin;
    private Long memberCardExpireTimeEnd;

    private Integer carMemberCardExpireType;
    private Long carMemberCardExpireTimeBegin;
    private Long carMemberCardExpireTimeEnd;

    /**
     * 套餐id
     */
    private Long memberCardId;

    private String cardName;

    private Long uid;

    /**
     * 排序方式
     */
    private Integer sortType;
    
    /**
     * 套餐购买次数
     */
    private Integer cardPayCount;

    private Integer batteryRentStatus;

    private Integer batteryDepositStatus;
    
    private Long userCreateBeginTime;
    
    private Long userCreateEndTime;

    /**
     * 套餐购买次数(所有套餐类型的总次数，包含：换电、车、车电一体)
     */
    private Integer payCount;

    public static final Integer SORT_TYPE_EXPIRE_TIME = 0;
    public static final Integer SORT_TYPE_AUTH_TIME = 1;
    public static final Integer SORT_TYPE_CAR_EXPIRE_TIME = 2;

}
