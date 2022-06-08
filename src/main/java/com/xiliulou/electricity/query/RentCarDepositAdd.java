package com.xiliulou.electricity.query;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单表(TElectricityDeposit)实体类
 *
 * @author makejava
 * @since 2022-06-08 16:00:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentCarDepositAdd {


    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商id不能为空!", groups = {UpdateGroup.class})
    private Long franchiseeId;

    /**
     * 用户名
     */
    @NotNull(message = "用户名不能为空!", groups = {UpdateGroup.class})
    private String name;

    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空!", groups = {UpdateGroup.class})
    private String phone;

    /**
     * 缴纳金额
     */
    @NotNull(message = "缴纳金额不能为空!", groups = {UpdateGroup.class})
    private BigDecimal payAmount;

    private Integer tenantId;


}
