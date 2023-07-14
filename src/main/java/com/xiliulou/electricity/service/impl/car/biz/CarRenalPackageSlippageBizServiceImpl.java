package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.NumberUtil;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

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
    public String queryCarPackageUnpaidAmountByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageOrderSlippagePO slippageEntity = carRentalPackageOrderSlippageService.selectUnPayByByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(slippageEntity)) {
            return null;
        }

        // 时间比对
        long lateFeeStartTime = slippageEntity.getLateFeeStartTime().longValue();
        long now = System.currentTimeMillis();

        // 没有滞纳金产生
        if (lateFeeStartTime < now) {
            return null;
        }

        // 转换天
        int diffDay = DateUtils.diffDay(now, lateFeeStartTime);
        // 计算滞纳金金额
        BigDecimal amount = NumberUtil.mul(diffDay, slippageEntity.getLateFee());

        return String.format("%.2f", amount);
    }

    /**
     * 是否存在未支付的滞纳金<br />
     * 租车(单车、车电一体)
     *
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    @Override
    public Boolean isExitUnpaid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid);
    }
}
