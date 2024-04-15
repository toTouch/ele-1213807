package com.xiliulou.electricity.entity;

import lombok.Builder;
import lombok.Data;

@Data
//@Builder
public class ExchangeBatterySoc {
    
    private Long id;
    
    private Long uid;
    /**
    * sn码
    */
    private String sn;
    
    private Integer tenantId;
    /**
    * 加盟商id
    */
    private Long franchiseeId;
    /**
    * 门店id
    */
    private Long storeId;
    /**
    * 取走电池电量
    */
    private Double takeAwayPower;
    /**
    * 归还电池电量
    */
    private Double returnPower;
    /**
    * 差值电池电量
    */
    private Double poorPower;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;


}
