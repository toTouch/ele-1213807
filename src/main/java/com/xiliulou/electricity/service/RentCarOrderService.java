package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import org.apache.commons.lang3.tuple.Triple;

public interface RentCarOrderService {
    Triple<Boolean, String, Object> rentCarOrder(RentCarHybridOrderQuery query);
}
