package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import com.xiliulou.electricity.mapper.FreeServiceFeeOrderMapper;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : renhang
 * @description FreeServiceFeeOrderServiceImpl
 * @date : 2025-03-27 10:27
 **/
@Service
public class FreeServiceFeeOrderServiceImpl implements FreeServiceFeeOrderService {

    @Resource
    private FreeServiceFeeOrderMapper freeServiceFeeOrderMapper;


    @Override
    @Slave
    public Integer existsPaySuccessOrder(String freeDepositOrderId, Long uid) {
        return freeServiceFeeOrderMapper.existsPaySuccessOrder(freeDepositOrderId, uid);
    }

    @Override
    public void insertOrder(FreeServiceFeeOrder freeServiceFeeOrder) {
        freeServiceFeeOrderMapper.insert(freeServiceFeeOrder);
    }
}
