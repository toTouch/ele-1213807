package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

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

        long nowTime = System.currentTimeMillis();
        while (lookFlag) {
            // 1. 查询会员套餐表中，套餐购买订单已过期的数据
            List<CarRentalPackageMemberTermPO> memberTermEntityList = carRentalPackageMemberTermService.pageExpire(offset, size, nowTime);
            if (CollectionUtils.isEmpty(memberTermEntityList)) {
                lookFlag = false;
                break;
            }
            List<Long> uidList = memberTermEntityList.stream().map(CarRentalPackageMemberTermPO::getUid).collect(Collectors.toList());

            // 2. 根据UID查询名下的未使用的订单第一条订单









            offset += size;
        }

        // 2. 若有续接的套餐订单，直接覆盖，同时将原订单设置为已失效
        // 3. 若没有续接订单，查询是否存在设备，若存在，生成逾期订单，若不存在，结束
    }
}
