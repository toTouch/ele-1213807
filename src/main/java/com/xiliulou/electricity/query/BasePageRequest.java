package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 基本分页
 * @Author: renhang
 * @Date 2024/3/19 14:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasePageRequest {
    
    private Long size;
    
    private Long offset;
    
    /**
     * 默认是0: 换电保险
     * 1: 租车or车电一体
     */
    private Integer type = 0;
}
