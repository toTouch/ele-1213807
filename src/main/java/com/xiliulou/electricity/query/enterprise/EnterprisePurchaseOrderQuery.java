package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/8 14:58
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterprisePurchaseOrderQuery {
    
    private Long enterpriseId;
    
    private Long uid;
    
    private String userName;
    
    private String phone;
    
    private Long currentTime;
    
    /**
     * 订单代付类型
     * 1- 代付到期， 2-已代付， 3-未代付
     * @see EnterprisePaymentStatusEnum
     */
    private Integer paymentStatus;
    
    private Long tenantId;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    private Long size;
    
    private Long offset;
    
    
    
}
