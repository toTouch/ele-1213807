package com.xiliulou.electricity.entity.faq;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
public class FaqCategoryV2 {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 分类
     */
    private String type;
    
    /**
     * 排序
     */
    private BigDecimal sort;
    
    /**
     * 操作人
     */
    private Long opUser;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
}
