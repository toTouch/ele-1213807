package com.xiliulou.electricity.request.profitsharing;

import com.xiliulou.electricity.validator.QueryGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分账接收方配置表(TProfitSharingReceiverConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:27:48
 */
@Data
public class ProfitSharingReceiverConfigQryRequest implements Serializable {
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 分账方配置表id
     */
    @NotNull(message = "profitSharingConfigId不可为空")
    private Long profitSharingConfigId;
    
    @NotNull(groups = QueryGroup.class, message = "size不可为空")
    private Integer size;
    
    @NotNull(groups = QueryGroup.class, message = "offset不可为空")
    private Integer offset;
}

