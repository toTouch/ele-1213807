package com.xiliulou.electricity.manager;

import com.xiliulou.electricity.service.EleCalcRentCarPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CalcRentCarPriceFactory {
    @Autowired
    private Map<String, EleCalcRentCarPriceService> eleCalcRentCarPriceServiceMap;

    public EleCalcRentCarPriceService getInstance(String calcType) {
        return eleCalcRentCarPriceServiceMap.get(calcType);
    }

}
