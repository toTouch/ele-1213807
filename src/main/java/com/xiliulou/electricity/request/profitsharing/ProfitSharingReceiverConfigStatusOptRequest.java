package com.xiliulou.electricity.request.profitsharing;

import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverStatusEnum;
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
public class ProfitSharingReceiverConfigStatusOptRequest implements Serializable {
    
    
    @NotNull(message = "id不可为空")
    private Long id;
    
    
    /**
     * 接收方状态
     *
     * @see ProfitSharingConfigReceiverStatusEnum
     */
    @NotNull(message = "receiverStatus不可为空")
    private Integer receiverStatus;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
}

