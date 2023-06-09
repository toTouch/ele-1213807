package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2022-04-20 10:21:24
 */
@Service("eleBatteryServiceFeeOrderService")
@Slf4j
public class EleBatteryServiceFeeOrderServiceImpl implements EleBatteryServiceFeeOrderService {

    @Resource
    EleBatteryServiceFeeOrderMapper eleBatteryServiceFeeOrderMapper;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    BatteryModelService batteryModelService;


    @Override
    public EleBatteryServiceFeeOrder queryEleBatteryServiceFeeOrderByOrderId(String orderNo) {
        return eleBatteryServiceFeeOrderMapper.selectOne(new LambdaQueryWrapper<EleBatteryServiceFeeOrder>().eq(EleBatteryServiceFeeOrder::getOrderId, orderNo));
    }

    @Override
    public void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        eleBatteryServiceFeeOrderMapper.updateById(eleBatteryServiceFeeOrder);
    }

    @Override
    public R queryList(Long offset, Long size, Long startTime, Long endTime) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return this.queryListForAdmin(offset, size, startTime, endTime, user.getUid(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS, user.getTenantId());
    }

    @Slave
    @Override
    public R queryListForAdmin(Long offset, Long size, Long startTime, Long endTime, Long uid, Integer status, Integer tenantId) {

        List<EleBatteryServiceFeeOrderVo> eleBatteryServiceFeeOrders = eleBatteryServiceFeeOrderMapper.queryListForAdmin(uid, offset, size, startTime, endTime, status, tenantId);

        for (EleBatteryServiceFeeOrderVo eleBatteryServiceFeeOrderVo : eleBatteryServiceFeeOrders) {
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                Integer model = batteryModelService.acquireBatteryModel(eleBatteryServiceFeeOrderVo.getBatteryType(), tenantId);
                eleBatteryServiceFeeOrderVo.setModel(model);
            }

            if (Objects.equals(eleBatteryServiceFeeOrderVo.getStatus(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS) && BigDecimal.valueOf(0).compareTo(eleBatteryServiceFeeOrderVo.getBatteryServiceFee()) != 0) {
                eleBatteryServiceFeeOrderVo.setBatteryGenerateDay((eleBatteryServiceFeeOrderVo.getPayAmount().divide(eleBatteryServiceFeeOrderVo.getBatteryServiceFee())).intValue());
            }

        }
        return R.ok(eleBatteryServiceFeeOrders);
    }

    @Slave
    @Override
    public R queryList(BatteryServiceFeeQuery batteryServiceFeeQuery) {
        List<EleBatteryServiceFeeOrderVo> eleBatteryServiceFeeOrders = eleBatteryServiceFeeOrderMapper.queryList(batteryServiceFeeQuery);

        for (EleBatteryServiceFeeOrderVo eleBatteryServiceFeeOrderVo : eleBatteryServiceFeeOrders) {
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                Integer model = batteryModelService.acquireBatteryModel(eleBatteryServiceFeeOrderVo.getBatteryType(), batteryServiceFeeQuery.getTenantId());
                eleBatteryServiceFeeOrderVo.setModel(model);
            }

            if (Objects.equals(eleBatteryServiceFeeOrderVo.getStatus(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS) && BigDecimal.valueOf(0).compareTo(eleBatteryServiceFeeOrderVo.getBatteryServiceFee()) != 0) {
                eleBatteryServiceFeeOrderVo.setBatteryGenerateDay((eleBatteryServiceFeeOrderVo.getPayAmount().divide(eleBatteryServiceFeeOrderVo.getBatteryServiceFee())).intValue());
            }

        }
        return R.ok(eleBatteryServiceFeeOrders);
    }

    @Slave
    @Override
    public R queryCount(BatteryServiceFeeQuery batteryServiceFeeQuery) {
        return R.ok(eleBatteryServiceFeeOrderMapper.queryCount(batteryServiceFeeQuery));
    }

    @Slave
    @Override
    public BigDecimal queryUserTurnOver(Integer tenantId, Long uid) {
        return Optional.ofNullable(eleBatteryServiceFeeOrderMapper.queryTurnOver(tenantId, uid)).orElse(new BigDecimal("0"));
    }

    @Slave
    @Override
    public BigDecimal queryTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeIds) {
        return Optional.ofNullable(eleBatteryServiceFeeOrderMapper.queryTenantTurnOver(tenantId, todayStartTime, franchiseeIds)).orElse(BigDecimal.valueOf(0));
    }

    @Slave
    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime) {
        return eleBatteryServiceFeeOrderMapper.queryTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Slave
    @Override
    public BigDecimal queryAllTurnOver(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime) {
        return eleBatteryServiceFeeOrderMapper.queryAllTurnOver(tenantId, franchiseeId, beginTime, endTime);
    }
}
