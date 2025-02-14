package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawOldConfigInfo;

/**
 * 商户提现使用旧配置信息表(TMerchantWithdrawOldConfigInfo)表数据库访问层
 *
 * @author maxiaodong
 * @since 2025-02-13 17:43:56
 */
public interface MerchantWithdrawOldConfigInfoMapper {

    Integer existsMerchantOldWithdrawConfigInfo(Integer tenantId, Long franchiseeId);
}

