package com.xiliulou.electricity.query;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
@Valid
public class RentCarDepositAdd {


    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商id不能为空!", groups = {UpdateGroup.class})
    private Long franchiseeId;

    /**
     * 用户名
     */
    @Deprecated
    private String name;

    /**
     * 手机号
     */
    //@NotEmpty(message = "手机号不能为空!", groups = {CreateGroup.class})
    @Deprecated
    private String phone;
    
    /**
     * uid
     */
    @NotEmpty(message = "用户不能为空!", groups = {CreateGroup.class})
    private Long uid;

    /**
     * 缴纳金额
     */
    @NotNull(message = "押金不能为空!", groups = {CreateGroup.class})
    private BigDecimal payAmount;

    /**
     * 门店Id
     */
    @NotNull(message = "门店不能为空!", groups = {CreateGroup.class})
    private Long storeId;

    /**
     * 车辆型号Id
     */
    @NotNull(message = "车辆型号不能为空!", groups = {CreateGroup.class})
    private Integer carModelId;

    private Integer tenantId;


}
