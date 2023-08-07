package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.NumberUtil;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePo;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 逾期业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageSlippageBizServiceImpl implements CarRenalPackageSlippageBizService {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    /**
     * 根据用户ID查询车辆租赁套餐订单未支付的滞纳金金额
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 滞纳金金额，保留两位小数，四舍五入
     */
    @Override
    public BigDecimal queryCarPackageUnpaidAmountByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        List<CarRentalPackageOrderSlippagePo> slippageEntityList = carRentalPackageOrderSlippageService.selectUnPayByByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(slippageEntityList)) {
            return null;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        long now = System.currentTimeMillis();
        for (CarRentalPackageOrderSlippagePo slippageEntity : slippageEntityList) {
            // 结束时间，不为空
            if (ObjectUtils.isNotEmpty(slippageEntity.getLateFeeEndTime())) {
                now = slippageEntity.getLateFeeEndTime();
            }

            // 时间比对
            long lateFeeStartTime = slippageEntity.getLateFeeStartTime().longValue();

            // 没有滞纳金产生
            if (lateFeeStartTime < now) {
                continue;
            }

            // 转换天
            long diffDay = DateUtils.diffDay(now, lateFeeStartTime);
            // 计算滞纳金金额
            BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee());
            totalAmount.add(amount);
        }

        if (BigDecimal.ZERO.compareTo(totalAmount) == 0) {
            return null;
        }

        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 是否存在未支付的滞纳金<br />
     * 租车(单车、车电一体)
     *
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return true(存在)、false(不存在)
     */
    @Override
    public boolean isExitUnpaid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid);
    }
}
