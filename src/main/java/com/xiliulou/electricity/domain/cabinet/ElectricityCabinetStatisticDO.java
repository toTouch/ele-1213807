package com.xiliulou.electricity.domain.cabinet;

import lombok.Data;

/**
 * 柜机统计
 *
 * @author zhangyongbo
 **/
@Data
public class ElectricityCabinetStatisticDO {
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 统计
     */
    private String statisticDate;
    
    /**
     * 换电柜id
     */
    private Integer electricityCabinetId;
    
    /**
     * 使用频次
     */
    private Integer useFrequency;
    
    
}
