package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/3/13 18:28
 * @mood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Valid
public class RentCarDepositQuery {
    
    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商id不能为空!", groups = {CreateGroup.class})
    private Long franchiseeId;
    
    /**
     * uid
     */
    @NotNull(message = "用户不能为空!", groups = {CreateGroup.class})
    private Long uid;
    
    /**
     * 缴纳金额
     */
    @NotNull(message = "押金不能为空!", groups = {CreateGroup.class})
    private BigDecimal payAmount;
    
    /**
     * 车辆型号Id
     */
    @NotNull(message = "车辆型号不能为空!", groups = {CreateGroup.class})
    private Integer carModelId;
    
    private Integer tenantId;
    
}
