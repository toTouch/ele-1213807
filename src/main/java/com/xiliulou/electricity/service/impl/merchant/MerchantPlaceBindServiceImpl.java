package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceBindMapper;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:50
 * @desc
 */
@Service("merchantPlaceBindService")
@Slf4j
public class MerchantPlaceBindServiceImpl implements MerchantPlaceBindService {
    
    @Resource
    private MerchantPlaceBindMapper placeBindMapper;
    
    @Override
    public int batchInsert(List<MerchantPlaceBind> merchantPlaceBindList) {
        return placeBindMapper.batchInsert(merchantPlaceBindList);
    }
    
    @Override
    public int batchUnBind(Set<Long> unBindList, Long merchantId, long updateTime) {
        return placeBindMapper.batchUnBind(unBindList, merchantId, updateTime);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceBind> listByMerchantId(Long merchantId, Integer status) {
        return placeBindMapper.selectListByMerchantId(merchantId, status);
    }
    
    @Slave
    @Override
    public Integer existPlaceFeeByMerchantId(Long merchantId) {
        return placeBindMapper.existPlaceFeeByMerchantId(merchantId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceBind> queryNoSettleByMerchantId(Long merchantId) {
        return placeBindMapper.queryNoSettleByMerchantId(merchantId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceBind> listBindRecord(MerchantPlaceConditionRequest request) {
        return placeBindMapper.selectListBindRecord(request);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceBind> listUnbindRecord(MerchantPlaceConditionRequest request) {
        return placeBindMapper.selectListUnbindRecord(request);
    }
    
}
