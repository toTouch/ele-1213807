package com.xiliulou.electricity.reqparam.opt.carpackage;

import lombok.Data;

import java.util.List;

/**
 * description: 过期套餐处理请求
 *
 * @author caobotao.cbt
 * @date 2024/11/25 16:08
 */
@Data
public class ExpirePackageOrderReq {
    
    /**
     * 租户id集合
     */
    private List<Integer> tenantIds;
    
    /**
     * 处理数量
     */
    private Integer size = 500;
    
}
