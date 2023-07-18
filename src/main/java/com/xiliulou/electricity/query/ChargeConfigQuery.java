package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2023/7/18 10:25
 */
@Data
public class ChargeConfigQuery {
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Long id;
    /**
     * 电价名称
     */
    @NotNull(message = "电价名称不可以为空")
    private String name;

    private Long franchiseeId;

    private Long storeId;
    /**
     * 电柜id
     */
    private Long eid;

    @NotNull(message = "电费计算规则不可以为空")
    private String jsonRule;
}
