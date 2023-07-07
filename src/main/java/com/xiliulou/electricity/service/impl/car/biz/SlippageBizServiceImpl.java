package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.SlippageBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 逾期业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class SlippageBizServiceImpl implements SlippageBizService {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    /**
     * 是否存在为支付的滞纳金<br />
     * 包含换电(单电)、租车(单车、车电一体)
     *
     * @param tenantId
     * @param uid
     * @return
     */
    @Override
    public Boolean isExitUnpaid(Integer tenantId, Long uid) {
        // TODO 找志龙提供接口，是否存在换电滞纳金
        Boolean batterySlippage = Boolean.TRUE;
        if (batterySlippage) {
            return Boolean.TRUE;
        }
        Boolean carSlippage = carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid);
        return carSlippage;
    }
}
