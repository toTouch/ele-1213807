package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 套餐购买订单过期处理<br />
     * 用于定时任务
     *
     * @param offset 偏移量
     * @param size 取值数量
     */
    @Override
    public void expirePackageOrder(Integer offset, Integer size) {
        // 初始化定义
        offset = ObjectUtils.isEmpty(offset) ? 0: offset;
        size = ObjectUtils.isEmpty(size) ? 500: size;

        boolean lookFlag = true;

        while (lookFlag) {

        }

        // 1. 查询会员套餐表中，套餐购买订单已过期的数据（不限制，时间到 或者 限制次数，次数为 0）
        // 2. 若有续接的套餐订单，直接覆盖，同时将原订单设置为已失效
        // 3. 若没有续接订单，查询是否存在设备，若存在，生成逾期订单，若不存在，结束
    }
}
