package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface EleCalcRentCarPriceService {

    /**
     * 获取租车套餐费用
     * @param userInfo
     * @param rentTime
     * @param rentCarPriceRuleMap
     * @return
     */
    Pair<Boolean, Object> getRentCarPrice(UserInfo userInfo, Integer rentTime, Map<String, Double> rentCarPriceRuleMap);

}
