package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCardOrderCoupon;
import com.xiliulou.electricity.mapper.BatteryMemberCardOrderCouponMapper;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (BatteryMemberCardOrderCoupon)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-02 14:52:19
 */
@Service("batteryMemberCardOrderCouponService")
@Slf4j
public class BatteryMemberCardOrderCouponServiceImpl implements BatteryMemberCardOrderCouponService {
    @Resource
    private BatteryMemberCardOrderCouponMapper batteryMemberCardOrderCouponMapper;

    @Override
    public List<Long> selectCouponIdsByOrderId(String orderId) {
        return this.batteryMemberCardOrderCouponMapper.selectCouponIdsByOrderId(orderId);
    }

    @Override
    public String selectOrderIdByCouponId(Long couponId) {
        return this.batteryMemberCardOrderCouponMapper.selectOrderIdByCouponId(couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<BatteryMemberCardOrderCoupon> list) {
        if (CollectionUtils.isEmpty(list)) {
            return NumberConstant.ZERO;
        }
        return this.batteryMemberCardOrderCouponMapper.batchInsert(list);
    }
}
