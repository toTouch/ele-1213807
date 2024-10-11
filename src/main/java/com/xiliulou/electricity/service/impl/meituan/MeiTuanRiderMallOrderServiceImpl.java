package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @date 2024/10/10 12:06:53
 */
@Service
public class MeiTuanRiderMallOrderServiceImpl implements MeiTuanRiderMallOrderService {
    
    @Resource
    private MeiTuanRiderMallOrderMapper meiTuanRiderMallOrderMapper;
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderId(String orderId, Long uid, Integer tenantId) {
        return meiTuanRiderMallOrderMapper.selectByOrderId(orderId, uid, tenantId);
    }
    
    @Override
    public Integer updateStatusByOrderId(MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        return meiTuanRiderMallOrderMapper.updateStatusByOrderId(meiTuanRiderMallOrder);
    }
}
