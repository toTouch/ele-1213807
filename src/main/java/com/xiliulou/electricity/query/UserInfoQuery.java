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
    
    /***********排期表内可快速实现的 P0 需求 15.1  实名用户列表（16条优化项）iii 20240311 start**************/
    /**
     * <p>
     * Description: 车辆押金缴纳状态，1已缴纳，0未缴纳
     * </p>
     */
    private Integer carDepositStatus;
    
    /**
     * <p>
     * Description: 套餐冻结状态，1正常，0冻结
     * </p>
     */
    private Integer freezeStatus;
    
    /***********排期表内可快速实现的 P0 需求 15.1  实名用户列表（16条优化项）iii 20240311 end**************/
    
    
    /**
     * 套餐id
     */
    private Long memberCardId;
    
    /**
     * 套餐冻结状态
     */
    private Integer memberCardStatus;
    
    
    private String cardName;
    
    private Long uid;
    
    /**
     * 排序方式
     */
    private Integer sortType;
    
    /**
     * 排序：升序 ASC， 降序 DESC
     */
    private String sortBy;
    
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
    
    /**
     * 关键字查询，手机号/真实名字模糊匹配
     */
    private String keywords;
    
    private List<Integer> userTypeList;
    
    /**
     * 身份证号
     */
    private String idNumber;
    
    private Integer delFlag;
    
    private Integer userStatus;
    
    public static final Integer SORT_TYPE_EXPIRE_TIME = 0;
    
    public static final Integer SORT_TYPE_AUTH_TIME = 1;
    
    public static final Integer SORT_TYPE_CAR_EXPIRE_TIME = 2;
    
}
