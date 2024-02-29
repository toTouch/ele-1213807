package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailPro;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetPowerMonthDetailProMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailProService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序月度电费详情
 * @date 2024/2/21 20:11:40
 */
@Service
public class MerchantCabinetPowerMonthDetailProServiceImpl implements MerchantCabinetPowerMonthDetailProService {
    
    @Resource
    private MerchantCabinetPowerMonthDetailProMapper merchantCabinetPowerMonthDetailProMapper;
    
    @Slave
    @Override
    public Long queryLatestReportTime(Long cabinetId, List<String> monthList) {
        return merchantCabinetPowerMonthDetailProMapper.selectLatestReportTime(cabinetId, monthList);
    }
    
    @Slave
    @Override
    public List<MerchantCabinetPowerMonthDetailPro> listByMonth(Long cabinetId, List<String> monthList) {
        return merchantCabinetPowerMonthDetailProMapper.selectListByMonth(cabinetId, monthList);
    }
}
