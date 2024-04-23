package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ExchangeBatterySoc;

public interface ExchangeBatterySocService {
    
    int insertOne(ExchangeBatterySoc exchangeBatterySoc);
    
    int update(ExchangeBatterySoc exchangeBatterySoc);
    
    ExchangeBatterySoc queryOneByUidAndSn(String sn);
}
