package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

    /**
     * 套餐购买订单过期处理<br />
     * 用于定时任务
     *
     * @param offset
     * @param size
     */
    @Override
    public void expirePackageOrder(Integer offset, Integer size) {
        // 1. 查询会员套餐表中，套餐购买订单已过期的数据（不限制，时间到 或者 限制次数，次数为 0）
        // 2. 若有续接的套餐订单，直接覆盖，同时将原订单设置为已失效
        // 3. 若没有续接订单，查询是否存在设备，若存在，生成逾期订单，若不存在，结束
    }
}
