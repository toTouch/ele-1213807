package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonth;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 11:41
 * @desc
 */
@Service
@Slf4j
public class MerchantPlaceFeeMonthServiceImpl implements MerchantPlaceFeeMonthService {
    @Resource
    private MerchantPlaceFeeMonthMapper merchantPlaceFeeMonthMapper;
    @Override
    public Integer batchInsert(List<MerchantPlaceFeeMonth> item) {
        return merchantPlaceFeeMonthMapper.batchInsert(item);
    }
    
    @Slave
    @Override
    public List<Long> selectCabinetIdByMerchantId(Long merchantId) {
        return merchantPlaceFeeMonthMapper.selectCabinetIdByMerchantId(merchantId);
    }
    
    @Slave
    @Override
    public Integer existPlaceFeeByMerchantId(Long merchantId) {
        return merchantPlaceFeeMonthMapper.existPlaceFeeByMerchantId(merchantId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeMonth> queryListByMonth(Long placeId, Long cabinetId, List<String> xDataList, Long merchantId) {
        return merchantPlaceFeeMonthMapper.selectListByMonth(placeId, cabinetId, xDataList, merchantId);
    }
    
    /**
     * 根据商户id查询历史数据
     * @param merchantId
     * @param cabinetId
     * @param placeId
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceFeeMonth> queryListByMerchantId(Long merchantId, Long cabinetId, Long placeId) {
        return merchantPlaceFeeMonthMapper.selectListByMerchantId(merchantId, cabinetId, placeId);
    }
    
    @Slave
    @Override
    public BigDecimal sumFeeByTime(Long merchantId, Long placeId, Long cabinetId, Long time) {
        return merchantPlaceFeeMonthMapper.sumFeeByTime(merchantId, placeId, cabinetId, time);
    }
}
