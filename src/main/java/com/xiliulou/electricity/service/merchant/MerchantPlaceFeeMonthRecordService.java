package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthRecordVO;

import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeMonthRecordService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
public interface MerchantPlaceFeeMonthRecordService {
    List<MerchantPlaceFeeMonthRecordVO> selectByMonthDate(String monthDate, Integer tenantId);
}
