package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 16:02
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChannelEmployeeRequest {
    
    private Long id;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Long franchiseeId;
    
    private Integer status;
    
    /**
     * 偏移量
     */
    private Integer offset;
    
    /**
     * 取值数量
     */
    private Integer size;
    
    private List<Long> idList;
    
}
