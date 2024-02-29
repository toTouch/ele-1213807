package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthDetail;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthDetailMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 13:35
 * @desc
 */
@Service("merchantPlaceFeeMonthDetailService")
@Slf4j
public class MerchantPlaceFeeMonthDetailServiceImpl implements MerchantPlaceFeeMonthDetailService {
    @Resource
    private MerchantPlaceFeeMonthDetailMapper merchantPlaceFeeMonthDetailMapper;
    @Override
    public Integer batchInsert(List<List<MerchantPlaceFeeMonthDetail>> partition) {
        return merchantPlaceFeeMonthDetailMapper.batchInsert(partition);
    }
    
    @Slave
    @Override
    public List<Long> queryMerchantIdList(Long startTime, Long endTime) {
        return merchantPlaceFeeMonthDetailMapper.selectMerchantIdList(startTime, endTime);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeMonthDetail> queryListByMerchantId(Long merchantId, Long startTime, Long endTime) {
        return merchantPlaceFeeMonthDetailMapper.selectListByMerchantId(merchantId, startTime, endTime);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeMonthDetail> queryListByMonth(Long cabinetId, Long placeId, List<String> monthList) {
        return merchantPlaceFeeMonthDetailMapper.selectListByMonth(cabinetId, placeId, monthList);
    }
}
