package com.xiliulou.electricity.constant;

import lombok.Data;

/**
 * @ClassName: BasePageQuery
 * @description:
 * @author: renhang
 * @create: 2024-10-14 09:12
 */
@Data
public class BasePageQuery {
    
    private Long size = 10L;
    
    private Long offset;
}
