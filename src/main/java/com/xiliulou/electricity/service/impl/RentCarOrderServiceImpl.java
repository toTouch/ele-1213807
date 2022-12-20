package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RentCarOrderServiceImpl implements RentCarOrderService {

    @Override
    public Triple<Boolean, String, Object> rentCarOrder(RentCarHybridOrderQuery query) {




        return Triple.of(true,"下单成功！",null);
    }
}
