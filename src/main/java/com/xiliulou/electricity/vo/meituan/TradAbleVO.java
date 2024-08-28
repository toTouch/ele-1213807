package com.xiliulou.electricity.vo.meituan;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 换电套餐限制提单VO
 * @date 2024/8/28 13:02:16
 */

@Data
public class TradAbleVO {
    
    /**
     * 是否限制购买：true-限制 false-不限制
     */
    private Boolean limitResult;
    
    /**
     * 限制原因
     */
    private Integer limitType;
    
    /**
     * 限制原因
     */
    private String limitReason;
}
