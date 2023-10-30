package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * app配置类
 *
 * @author zhangyongbo
 * @since 2023-10-11
 */
@Data
public class ElectricityAppConfigQuery {
    
    private Long id;
    
    /**
     * 用户id
     */
    private Integer uid;
    
    /**
     * 是否开启选仓换电 (0--关闭，1--开启)
     */
    @NotNull(message = "选仓换电配置不能为空!")
    private Integer isSelectionExchange;
}
