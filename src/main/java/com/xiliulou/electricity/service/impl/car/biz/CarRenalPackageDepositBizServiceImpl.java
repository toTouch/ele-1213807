package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 租车套餐押金业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageDepositBizServiceImpl implements CarRenalPackageDepositBizService {

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

}
