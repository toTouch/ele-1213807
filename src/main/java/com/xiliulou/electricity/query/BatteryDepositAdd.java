package com.xiliulou.electricity.query;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class BatteryDepositAdd {
    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商id不能为空!", groups = {UpdateGroup.class})
    private Long franchiseeId;

    /**
     * 缴纳金额
     */
    @NotNull(message = "缴纳金额不能为空!", groups = {CreateGroup.class})
    private BigDecimal payAmount;

    /**
     * 缴纳金额
     */
    @NotNull(message = "用户id不能为空!", groups = {CreateGroup.class})
    private Long uid;

    private Integer model;

    /**
     * 加盟商押金类型 1--老（不分型号） 2--新（分型号）
     */
    private Integer modelType;

    /**
     * 门店Id
     */
    @NotNull(message = "门店不能为空!", groups = {CreateGroup.class})
    private Long storeId;
}
