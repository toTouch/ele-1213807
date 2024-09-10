package com.xiliulou.electricity.query.profitsharing;

import com.xiliulou.electricity.validator.QueryGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 分账接收方配置表(TProfitSharingReceiverConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:27:48
 */
@Data
public class ProfitSharingReceiverConfigModel implements Serializable {
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 分账方配置表id
     */
    private Long profitSharingConfigId;
    
    /**
     * 数据权限校验的加盟商id
     */
    private List<Long> dataPermissionFranchiseeIds;
    
    private Integer size;
    
    private Integer offset;
}

