package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/4/7 10:04
 * @desc
 */
@Data
public class CloudBeanSumVO {
    /**
     * 类型
     */
    private Integer type;
    
    /**
     * 云豆数量
     */
    private BigDecimal beanAmount;
}
