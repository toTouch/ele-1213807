package com.xiliulou.electricity.query.exchange;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName: QuickExchangeQuery
 * @description:
 * @author: renhang
 * @create: 2024-11-18 17:16
 */
@Data
public class QuickExchangeQuery {
    
    /**
     * 柜机id
     */
    @NotNull(message = "柜机id不能为空")
    private Integer eid;
    
    /**
     * 租借在仓的仓门号
     */
    @NotNull(message = "仓门号不能为空")
    private Integer cellNo;
    
    
    /**
     * 用户id
     */
    @NotNull(message = "快捷换电用户不能为空")
    private Long uid;
    
    
    
    public static final String QUICK_EXCHANGE_VERSION = "2.3.8";
}
