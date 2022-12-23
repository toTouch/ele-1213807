package com.xiliulou.electricity.service.strategy;

import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.service.EleCalcRentCarPriceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service(value = ElectricityCarModel.RENT_TYPE_MONTH)
public class EleMonthRentCarPriceService implements EleCalcRentCarPriceService {
    @Override
    public Pair<Boolean, Object> getRentCarPrice(UserInfo userInfo, Integer rentTime, Map<String, Double> rentCarPriceRuleMap) {
        Double price = rentCarPriceRuleMap.get(ElectricityCarModel.RENT_TYPE_MONTH);
        if (Objects.isNull(price)) {
            log.error("ELE ERROR! not found rent car price,uid={}", userInfo.getUid());
            return Pair.of(false, null);
        }

        BigDecimal totalPrice= BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(rentTime));

        return Pair.of(true, totalPrice);
    }


}
