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
}
