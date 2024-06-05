package com.xiliulou.electricity.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 活动表(ElectricityConfig)实体类
 *
 * @author makejava
 * @since 2022-07-05 09:27:12
 */
@Data
public class ElectricityConfigWxCustomerQuery {
    @NotNull(message = "微信客服是否开启不能为空")
    @Range(min = 0, max = 1, message = "微信客服是否开启只能为0或1")
    private Integer enableWxCustomer;
}


