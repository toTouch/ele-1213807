package com.xiliulou.electricity.vo.meituan;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 限制提单VO
 * @date 2024/8/28 13:02:16
 */
@Builder
@Data
public class LimitTradeVO {
    
    /**
     * 是否限制购买：true-限制 false-不限制
     */
    private Boolean limitResult;
    
    /**
     * 限制原因:1-老客限制
     */
    private Integer limitType;
    
}
