package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.query.BatteryServiceFeeOrderQuery;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserBatteryTypeService userBatteryTypeService;

    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Override
    public EleBatteryServiceFeeOrder queryEleBatteryServiceFeeOrderByOrderId(String orderNo) {
        return eleBatteryServiceFeeOrderMapper.selectOne(new LambdaQueryWrapper<EleBatteryServiceFeeOrder>().eq(EleBatteryServiceFeeOrder::getOrderId, orderNo));
    }

    @Override
    public Integer insert(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        return eleBatteryServiceFeeOrderMapper.insert(eleBatteryServiceFeeOrder);
    }

    @Override
    public void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        eleBatteryServiceFeeOrderMapper.updateById(eleBatteryServiceFeeOrder);
    }

    @Override
    public R queryList(BatteryServiceFeeOrderQuery query) {

        return R.ok();
    }

    @Slave
    @Override
    public R queryListForAdmin(Long offset, Long size, Long startTime, Long endTime, Long uid, Integer status, Integer tenantId) {

        return R.ok();

//        List<EleBatteryServiceFeeOrderVo> eleBatteryServiceFeeOrders = eleBatteryServiceFeeOrderMapper.queryListForAdmin(uid, offset, size, startTime, endTime, status, tenantId);
//
//        for (EleBatteryServiceFeeOrderVo eleBatteryServiceFeeOrderVo : eleBatteryServiceFeeOrders) {
//            if (Objects.equals(eleBatteryServiceFeeOrderVo.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
//                Integer model = batteryModelService.acquireBatteryModel(eleBatteryServiceFeeOrderVo.getBatteryType(), tenantId);
//                eleBatteryServiceFeeOrderVo.setModel(model);
//            }
//
//            if (Objects.equals(eleBatteryServiceFeeOrderVo.getStatus(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS) && BigDecimal.valueOf(0).compareTo(eleBatteryServiceFeeOrderVo.getBatteryServiceFee()) != 0) {
//                eleBatteryServiceFeeOrderVo.setBatteryGenerateDay((eleBatteryServiceFeeOrderVo.getPayAmount().divide(eleBatteryServiceFeeOrderVo.getBatteryServiceFee())).intValue());
//            }
//
//        }
//        return R.ok(eleBatteryServiceFeeOrders);
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

            if(StringUtils.isNotBlank(eleBatteryServiceFeeOrderVo.getBatteryType())){
                eleBatteryServiceFeeOrderVo.setBatteryTypeList(JsonUtil.fromJsonArray(eleBatteryServiceFeeOrderVo.getBatteryType(),String.class));
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

    @Override
    public EleBatteryServiceFeeOrder selectByOrderNo(String orderNo) {
        return eleBatteryServiceFeeOrderMapper.selectOne(new LambdaQueryWrapper<EleBatteryServiceFeeOrder>().eq(EleBatteryServiceFeeOrder::getOrderId,orderNo));
    }

    @Override
    public Integer updateByOrderNo(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        return eleBatteryServiceFeeOrderMapper.updateByOrderNo(eleBatteryServiceFeeOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void membercardExpireGenerateServiceFeeOrder() {
        int offset = 0;
        int size = 200;

        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectUseableList(offset, size);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }

            userBatteryMemberCardList.parallelStream().forEach(item -> {

                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                    return;
                }

                if (item.getMemberCardExpireTime() + 24 * 60 * 60 * 1000L > System.currentTimeMillis()) {
                    return;
                }

                Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! not found user,uid={}", item.getUid());
                    return;
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! memberCard is not exit,uid={},memberCardId={}", item.getUid(), item.getMemberCardId());
                    return;
                }

                ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.isNull(serviceFeeUserInfo)) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! not found serviceFeeUserInfo,uid={}", item.getUid());
                    return;
                }

                //用户当前是否绑定的有套餐过期滞纳金订单
                if(StringUtils.isNotBlank(serviceFeeUserInfo.getExpireOrderNo())){
                    return;
                }

                //套餐过期生成滞纳金订单
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());

                //用户绑定的电池型号
                List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());

                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                        .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid()))
                        .uid(item.getUid())
                        .phone(userInfo.getPhone())
                        .name(userInfo.getName())
                        .payAmount(BigDecimal.ZERO)
                        .status(EleDepositOrder.STATUS_INIT)
                        .batteryServiceFeeGenerateTime(System.currentTimeMillis())
                        .createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis())
                        .tenantId(userInfo.getTenantId())
                        .source(EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE)
                        .franchiseeId(userInfo.getFranchiseeId())
                        .storeId(userInfo.getStoreId())
                        .modelType(franchisee.getModelType())
                        .batteryType(CollectionUtils.isEmpty(userBatteryTypes) ? "" : JsonUtil.toJson(userBatteryTypes))
                        .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn())
                        .batteryServiceFee(batteryMemberCard.getServiceCharge()).build();
                eleBatteryServiceFeeOrderMapper.insert(eleBatteryServiceFeeOrder);

                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
                serviceFeeUserInfoUpdate.setExpireOrderNo(eleBatteryServiceFeeOrder.getOrderId());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            });

            offset += size;
        }
    }
}
