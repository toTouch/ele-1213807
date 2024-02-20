package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthRecordMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthRecordService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthRecordVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : merchantPlaceFeeMonthRecordService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
@Service
public class MerchantPlaceFeeMonthRecordServiceImpl implements MerchantPlaceFeeMonthRecordService {
    
    @Resource
    private MerchantPlaceFeeMonthRecordMapper merchantPlaceFeeMonthRecordMapper;
    
    @Override
    public List<MerchantPlaceFeeMonthRecordVO> selectByMonthDate(String monthDate, Integer tenantId) {
        return merchantPlaceFeeMonthRecordMapper.selectListBySettlementTime(monthDate, tenantId);
    }
}
