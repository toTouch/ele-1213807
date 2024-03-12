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
    
    /**
     * 商户申请提现时，扣减账户余额， 同时记录增加的提现金额
     * @param amount
     * @param uid
     * @param tenantId
     * @return
     */
    Integer withdrawAmount(BigDecimal amount, Long uid, Long tenantId);
    
    /**
     * 商户提现失败时，将扣除的提现金额返还给账户余额，同时减扣点申请时的提现金额
     * @param uid
     * @return
     */
    Integer rollBackWithdrawAmount(BigDecimal amount, Long uid, Long tenantId);
    
    MerchantUserAmount queryByUid(Long uid);
    
    List<MerchantUserAmount> queryUserAmountList(List<Long> uidList, Long tenantId);
}
