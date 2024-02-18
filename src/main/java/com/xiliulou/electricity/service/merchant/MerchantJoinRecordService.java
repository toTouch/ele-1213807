package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;

import java.util.List;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 17:53:17
 */
public interface MerchantJoinRecordService {
    
    /**
     * 扫码参与
     */
    R joinScanCode(String code);
    
    /**
     * 是否已过期
     */
    Integer existsIfExpired(Long merchantId, Long joinUid);
    
    /**
     * 修改参与状态
     */
    Integer updateStatus(Long merchantId, Long joinUid, Integer status);
    
    /**
     * 参与人是否存在保护期内的记录
     */
    Integer existsInProtectionTimeByJoinUid(Long joinUid);
    
    /**
     * 根据商户id和参与人uid查询记录
     */
    MerchantJoinRecord queryByMerchantIdAndJoinUid(Long merchantId, Long joinUid);
    
    /**
     * 根据商户id和参与状态查询记录
     */
    List<MerchantJoinRecord> listByMerchantIdAndStatus(Long merchantId, Integer status);
    
    /**
     * 定时任务：保护期状态和有效期状态
     */
    void handelProtectionAndStartExpired();
}
