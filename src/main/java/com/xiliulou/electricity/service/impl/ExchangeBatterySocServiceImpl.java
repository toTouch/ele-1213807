package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ExchangeBatterySoc;
import com.xiliulou.electricity.mapper.ExchangeBatterySocMapper;
import com.xiliulou.electricity.service.ExchangeBatterySocService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName: ExchangeBatterySocServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-04-10 10:11
 */
@Service
public class ExchangeBatterySocServiceImpl implements ExchangeBatterySocService {
    
    @Resource
    private ExchangeBatterySocMapper exchangeBatterySocMapper;
    
    @Override
    public int insertOne(ExchangeBatterySoc exchangeBatterySoc) {
        return exchangeBatterySocMapper.insertOne(exchangeBatterySoc);
    }
    
    @Override
    public int update(ExchangeBatterySoc exchangeBatterySoc) {
        return exchangeBatterySocMapper.update(exchangeBatterySoc);
    }
    
    @Override
    public ExchangeBatterySoc queryOneByUidAndSn(String sn) {
        return exchangeBatterySocMapper.selectOneByUidAndSn(sn);
    }
}
