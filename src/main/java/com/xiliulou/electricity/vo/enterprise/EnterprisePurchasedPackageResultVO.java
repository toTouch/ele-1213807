package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/8 20:17
 */

@Data
public class EnterprisePurchasedPackageResultVO {
    
    /**
     * 订单代付类型
     * 1- 代付到期， 2-已代付， 3-未代付
     * @see EnterprisePaymentStatusEnum
     */
    private Integer paymentStatus;
    
    
    private Integer recordSize;



}
