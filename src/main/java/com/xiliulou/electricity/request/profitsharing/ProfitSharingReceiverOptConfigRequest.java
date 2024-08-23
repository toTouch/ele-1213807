package com.xiliulou.electricity.request.profitsharing;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账接收方配置表(TProfitSharingReceiverConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:27:48
 */
@Data
public class ProfitSharingReceiverOptConfigRequest implements Serializable {
    
    
    @NotNull(groups = UpdateGroup.class, message = "id不可为空")
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 分账方配置表id
     */
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "profitSharingConfigId不可为空")
    private Long profitSharingConfigId;
    
    /**
     * 分账接收方账户，接收方类型为商户则为商户id，接收方类型为个人则为openId，
     */
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "account不可为空")
    private String account;
    
    /**
     * 接收方类型：1-商户，2-个人
     */
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "receiverType不可为空")
    private Integer receiverType;
    
    /**
     * 接收方账户名
     */
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "receiverName不可为空")
    private String receiverName;
    
    /**
     * 比例
     */
    @NotNull(groups = {CreateGroup.class, UpdateGroup.class}, message = "scale不可为空")
    private BigDecimal scale;
    
    
}

