package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.query.BatteryServiceFeeOrderQuery;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    
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
        List<EleBatteryServiceFeeOrder> list = eleBatteryServiceFeeOrderMapper.selectByPage(query);
        return R.ok(list);
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
            
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getStatus(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS)
                    && BigDecimal.valueOf(0).compareTo(eleBatteryServiceFeeOrderVo.getBatteryServiceFee()) != 0) {
                eleBatteryServiceFeeOrderVo.setBatteryGenerateDay(
                        (eleBatteryServiceFeeOrderVo.getPayAmount().divide(eleBatteryServiceFeeOrderVo.getBatteryServiceFee(), 2, RoundingMode.DOWN)).intValue());
            }
            
            if (StringUtils.isNotBlank(eleBatteryServiceFeeOrderVo.getBatteryType())) {
                eleBatteryServiceFeeOrderVo.setBatteryTypeList(JsonUtil.fromJsonArray(eleBatteryServiceFeeOrderVo.getBatteryType(), String.class));
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
        return eleBatteryServiceFeeOrderMapper.selectOne(new LambdaQueryWrapper<EleBatteryServiceFeeOrder>().eq(EleBatteryServiceFeeOrder::getOrderId, orderNo));
    }
    
    @Override
    public Integer updateByOrderNo(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder) {
        return eleBatteryServiceFeeOrderMapper.updateByOrderNo(eleBatteryServiceFeeOrder);
    }
    
    @Override
    public void membercardExpireGenerateServiceFeeOrder(String s) {
        List<Integer> tenantIds = null;
        if (StringUtils.isNotBlank(s)) {
            tenantIds = JsonUtil.fromJsonArray(s, Integer.class);
        }

        Long userBatteryMemberCardId = 0L;
        int size = 200;
        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectUseableListByTenantIds(userBatteryMemberCardId, size, tenantIds);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }

            userBatteryMemberCardId = userBatteryMemberCardList.get(userBatteryMemberCardList.size() - 1).getId();

            userBatteryMemberCardList.parallelStream().forEach(item -> {
                
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                    return;
                }
                
                // 获取过期滞纳金起算时间
                Integer expiredProtectionTime = getExpiredProtectionTime(null, userInfo.getTenantId());
                
                if (Objects.isNull(item.getMemberCardExpireTime()) || item.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L > System.currentTimeMillis()) {
                    return;
                }
                
                Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! not found user,uid={}", item.getUid());
                    return;
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.isNull(batteryMemberCard) || Objects.equals(batteryMemberCard.getBusinessType(),
                        BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode())) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! memberCard is not exit,uid={},memberCardId={}", item.getUid(), item.getMemberCardId());
                    return;
                }
                
                if (BigDecimal.ZERO.compareTo(batteryMemberCard.getServiceCharge()) >= 0) {
                    log.info("BATTERY SERVICE FEE ORDER INFO! The serviceCharge of batteryMemberCard is zero,uid={},memberCardId={}", item.getUid(), item.getMemberCardId());
                    return;
                }
                
                ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.isNull(serviceFeeUserInfo)) {
                    log.warn("BATTERY SERVICE FEE ORDER WARN! not found serviceFeeUserInfo,uid={}", item.getUid());
                    return;
                }
                
                // 用户当前是否绑定的有套餐过期滞纳金订单
                if (StringUtils.isNotBlank(serviceFeeUserInfo.getExpireOrderNo())) {
                    return;
                }
                
                // 套餐过期生成滞纳金订单
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                
                // 用户绑定的电池型号
                List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
                
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                        .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid())).uid(item.getUid()).phone(userInfo.getPhone())
                        .name(userInfo.getName()).payAmount(BigDecimal.ZERO).status(EleDepositOrder.STATUS_INIT).batteryServiceFeeGenerateTime(item.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000)
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                        .source(EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE).franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId())
                        .modelType(franchisee.getModelType()).batteryType(CollectionUtils.isEmpty(userBatteryTypes) ? "" : JsonUtil.toJson(userBatteryTypes))
                        .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn()).batteryServiceFee(batteryMemberCard.getServiceCharge())
                        .expiredProtectionTime(expiredProtectionTime).build();
                eleBatteryServiceFeeOrderMapper.insert(eleBatteryServiceFeeOrder);
                
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
                serviceFeeUserInfoUpdate.setExpireOrderNo(eleBatteryServiceFeeOrder.getOrderId());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            });
        }
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(BatteryServiceFeeQuery batteryServiceFeeQuery) {
        List<EleBatteryServiceFeeOrderVo> eleBatteryServiceFeeOrders = eleBatteryServiceFeeOrderMapper.selectListSuperAdminPage(batteryServiceFeeQuery);
        
        for (EleBatteryServiceFeeOrderVo eleBatteryServiceFeeOrderVo : eleBatteryServiceFeeOrders) {
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getModelType(), Franchisee.NEW_MODEL_TYPE) && Objects.nonNull(eleBatteryServiceFeeOrderVo.getTenantId())) {
                Integer model = batteryModelService.acquireBatteryModel(eleBatteryServiceFeeOrderVo.getBatteryType(), eleBatteryServiceFeeOrderVo.getTenantId());
                eleBatteryServiceFeeOrderVo.setModel(model);
            }
            
            if (Objects.equals(eleBatteryServiceFeeOrderVo.getStatus(), EleBatteryServiceFeeOrderVo.STATUS_SUCCESS)
                    && BigDecimal.valueOf(0).compareTo(eleBatteryServiceFeeOrderVo.getBatteryServiceFee()) != 0) {
                eleBatteryServiceFeeOrderVo.setBatteryGenerateDay(
                        (eleBatteryServiceFeeOrderVo.getPayAmount().divide(eleBatteryServiceFeeOrderVo.getBatteryServiceFee(), 2, RoundingMode.DOWN)).intValue());
            }
            
            if (StringUtils.isNotBlank(eleBatteryServiceFeeOrderVo.getBatteryType())) {
                eleBatteryServiceFeeOrderVo.setBatteryTypeList(JsonUtil.fromJsonArray(eleBatteryServiceFeeOrderVo.getBatteryType(), String.class));
            }
            
            if (Objects.nonNull(eleBatteryServiceFeeOrderVo.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(eleBatteryServiceFeeOrderVo.getTenantId());
                eleBatteryServiceFeeOrderVo.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
            }
            
        }
        return R.ok(eleBatteryServiceFeeOrders);
    }
    
    @Override
    @Slave
    public R countTotalForSuperAdmin(BatteryServiceFeeQuery batteryServiceFeeQuery) {
        return R.ok(eleBatteryServiceFeeOrderMapper.countTotalForSuperAdmin(batteryServiceFeeQuery));
    }
    
    @Override
    public Integer getExpiredProtectionTime(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder, Integer tenantId) {
        // 获取滞纳金起算时间的时候，若产生了滞纳金订单，直接取滞纳金订单中的快照记录，若快照记录中没有，又需要从租户配置中实时获取
        // 没有滞纳金订单时需要直接从租户配置中取，无论何种场景从租户配置中取值，取不到的时候都需要使用默认值24小时
        Integer expiredProtectionTime = null;
        if (!Objects.isNull(eleBatteryServiceFeeOrder)) {
            // 已经生成滞纳金订单的从订单中取起算时间，取不到在使用24小时
            expiredProtectionTime = eleBatteryServiceFeeOrder.getExpiredProtectionTime();
        }
        
        if (Objects.isNull(expiredProtectionTime)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            expiredProtectionTime =
                    Objects.isNull(electricityConfig) || Objects.isNull(electricityConfig.getExpiredProtectionTime()) ? ElectricityConfig.EXPIRED_PROTECTION_TIME_DEFAULT
                            : electricityConfig.getExpiredProtectionTime();
        }
        
        return expiredProtectionTime;
    }
}
