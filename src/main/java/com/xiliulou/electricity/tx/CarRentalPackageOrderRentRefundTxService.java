/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/14
 */

package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/14 19:19
 */
@Service
public class CarRentalPackageOrderRentRefundTxService {
    
    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;
    
    
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean update(CarRentalPackageOrderRentRefundPo carRentalPackageOrderRentRefund) {
        return carRentalPackageOrderRentRefundService.updateByOrderNo(carRentalPackageOrderRentRefund);
    }
    
}
