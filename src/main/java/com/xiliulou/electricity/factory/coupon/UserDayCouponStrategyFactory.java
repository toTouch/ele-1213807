package com.xiliulou.electricity.factory.coupon;


import com.xiliulou.electricity.service.DayCouponStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Description: This class is UserDayCouponStrategyFactory!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/13
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDayCouponStrategyFactory implements CommandLineRunner {
    
    private final List<DayCouponStrategy> strategyList = new ArrayList<>();
    
    private final ApplicationContext applicationContext;
    @Override
    public void run(String... args) throws Exception {
        Map<String, DayCouponStrategy> beans = applicationContext.getBeansOfType(DayCouponStrategy.class);
        strategyList.addAll(beans.values());
    }
    
    private void init() {
        try {
            this.run();
        }catch (Exception e){
            log.warn("UserDayCouponStrategyFactory.init FAILED! Exception:", e);
        }
    }
    
    public DayCouponStrategy getDayCouponStrategy(final Integer tenantId,final Long uid) {
        if (Objects.isNull(uid) || Objects.isNull(tenantId)){
            log.warn("UserDayCouponStrategyFactory.getDayCouponStrategy FAILED! uid is null!");
            return null;
        }
        if (CollectionUtils.isEmpty(strategyList)){
            this.init();
        }
        if (CollectionUtils.isEmpty(strategyList)){
            log.warn("UserDayCouponStrategyFactory.getDayCouponStrategy FAILED! strategyList is empty!");
            return null;
        }
        return strategyList.stream().filter(strategy -> strategy.isPackageInUse(tenantId,uid))
                .findFirst().orElse( null);
    }
}
