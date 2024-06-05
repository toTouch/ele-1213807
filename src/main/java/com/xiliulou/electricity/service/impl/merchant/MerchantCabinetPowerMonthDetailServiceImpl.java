package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetPowerMonthDetailMapper;
import com.xiliulou.electricity.query.merchant.MerchantPowerDetailQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailService;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthDetailVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description 电费月度详情
 * @date 2024/2/20 11:11:04
 */

@Service
public class MerchantCabinetPowerMonthDetailServiceImpl implements MerchantCabinetPowerMonthDetailService {
    
    @Resource
    private MerchantCabinetPowerMonthDetailMapper merchantCabinetPowerMonthDetailMapper;
    
    
    @Slave
    @Override
    public List<MerchantCabinetPowerMonthDetailVO> listByTenantId(MerchantPowerDetailQueryModel queryModel) {
        return merchantCabinetPowerMonthDetailMapper.selectListByTenantId(queryModel);
    }
}
