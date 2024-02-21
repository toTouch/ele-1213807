package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:37
 * @desc
 */
public interface MerchantUserAmountService {
    
    Integer save(MerchantUserAmount merchantUserAmount);
    
    List<MerchantUserAmount> queryList(MerchantUserAmountQueryMode joinRecordQueryMode);
    
    /**
     * 返现时给商户增加金额
     * @param amount
     * @param uid
     * @return
     */
    Integer addAmount(BigDecimal amount, Long uid, Long tenantId);
    
    /**
     * 退款时从商户账户表中扣除金额
     * @param amount
     * @param uid
     * @return
     */
    Integer reduceAmount(BigDecimal amount, Long uid, Long tenantId);
}
