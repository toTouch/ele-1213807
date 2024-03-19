package com.xiliulou.electricity.vo.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName: InsuranceOrderIdsVo
 * @description: 保险订单orderids
 * @author: renhang
 * @create: 2024-03-19 14:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceOrderIdsVo {
    
    /**
     * orderId
     */
    private String orderId;
    
    /**
     * 保险名称
     */
    private String insuranceName;
}
