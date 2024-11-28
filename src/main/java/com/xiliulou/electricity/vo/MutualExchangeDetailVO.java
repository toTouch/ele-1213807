package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName: MutualExchangeDetailVO
 * @description:
 * @author: renhang
 * @create: 2024-11-27 17:39
 */
@Data
public class MutualExchangeDetailVO {
    
    private Long id;
    
    /**
     * 组合名称
     */
    private String combinedName;
    
    /**
     * 组合名称
     */
    private List<Item> combinedFranchiseeList;
    
    /**
     * 状态 0:禁用,1:启用
     */
    private Integer status;
    
    
    private Integer tenantId;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    
    @Data
    public static class Item {
        
        private Long franchiseeId;
        
        private String franchiseeName;
    }
}
