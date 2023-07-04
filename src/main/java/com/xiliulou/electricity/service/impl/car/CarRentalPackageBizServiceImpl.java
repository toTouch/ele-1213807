package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.car.CarRentalPackageBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 租车套餐业务聚合ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageBizServiceImpl implements CarRentalPackageBizService {

    /**
     * 购买租车套餐<br />
     * <pre>
     *     1、新增/修改套餐押金
     *     2、新增套餐购买订单记录
     *     3、新增/修改保险购买订单
     *     4、新增/修改用户信息的押金支付状态
     *     5、
     * </pre>
     *
     * @return
     */
    @Override
    public R buyCarRenalPackage() {
        return null;
    }
}
