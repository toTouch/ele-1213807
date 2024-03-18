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
public class RentBatteryOrderQuery {
    
    private Long size;
    
    private Long offset;
    
    /**
     * 用户UID
     */
    private Long uid;
    
    /**
     * 用户名字
     */
    private String name;
    
    private String phone;
    
    private Long beginTime;
    
    private Long endTime;
    
    private String status;
    
    private String orderId;
    
    private Integer type;
    
    private List<Integer> eleIdList;
    
    private Integer tenantId;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
    
    /**
     * 换电柜ID
     */
    private List<Integer> electricityCabinetIds;
    
    /**
     * 电柜名称
     */
    private String electricityCabinetName;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 电池编号
     */
    private String electricityBatterySn;
}
