package com.xiliulou.electricity.model.car.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 租车套餐会员期限过期套餐查询参数
 **/
@Data
public class CarRentalPackageMemberTermExpiredQryModel implements Serializable {
    
    
    /**
     * 取值数量
     */
    private Integer size = 10;
    
    /**
     * 租户ID
     */
    private List<Integer> tenantIds;
    
    /**
     * 开始id
     */
    private Long startId;
    
    /**
     * 当前时间
     */
    private Long nowTime;
}
