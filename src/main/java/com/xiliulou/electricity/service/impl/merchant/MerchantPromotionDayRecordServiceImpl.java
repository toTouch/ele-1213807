package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.mapper.merchant.MerchantPromotionDayRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDayRecordQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPromotionDayRecordService;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMonthDetailVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description saas端商户推广费日结统计
 * @date 2024/2/23 20:51:02
 */
@Service
public class MerchantPromotionDayRecordServiceImpl implements MerchantPromotionDayRecordService {
    
    @Resource
    private MerchantPromotionDayRecordMapper merchantPromotionDayRecordMapper;
    
    @Slave
    @Override
    public List<MerchantPromotionMonthDetailVO> listByTenantId(MerchantPromotionDayRecordQueryModel queryModel) {
        return merchantPromotionDayRecordMapper.selectListByTenantId(queryModel);
    }
    
}
