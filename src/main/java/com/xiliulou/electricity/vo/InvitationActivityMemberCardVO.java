package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 活动及关联的套餐列表
 * @date 2023/11/13 09:59:25
 */

@Data
public class InvitationActivityMemberCardVO {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动名称
     */
    private String name;
    
    /**
     * 套餐列表
     */
    private List<Long> memberCardIdList;
    
}
