package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 16:05
 */

@Data
public class ChannelEmployeeVO {
    
    private Long id;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    private Long areaId;
    
    private String areaName;
    
    private Integer status;
    
    private Integer merchantTotal;
    
    private Integer delFlag;
    
    private Integer tenantId;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String openId;
}
