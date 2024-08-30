package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.enums.FreeDepositServiceWayEnums;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.handler.BaseFreeDepositService;
import com.xiliulou.electricity.service.handler.FreeDepositFactory;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: FreeDepositServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-21 19:11
 */

@Service
@Slf4j
public class FreeDepositServiceImpl implements FreeDepositService {
    
    @Resource
    UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private FreeDepositOrderMapper freeDepositOrderMapper;
    
    @Resource
    FreeDepositDataService freeDepositDataService;
    
    @Resource
    private FreeDepositFactory freeDepositFactory;
    
    @Resource
    private ApplicationContext applicationContext;
    
    
    @Override
    public FreeDepositOrderStatusBO getFreeDepositOrderStatus(FreeDepositOrderStatusQuery query) {
        if (Objects.isNull(query)) {
            log.warn("FreeDeposit WARN! getFreeDepositOrderStatus.params is null");
            return null;
        }
        log.info("FreeDeposit INFO! getFreeDepositOrderStatus.channel is {}, orderId is {}", query.getChannel(), query.getOrderId());
        
        // 免押查询
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(query.getChannel()), BaseFreeDepositService.class);
        return service.queryFreeDepositOrderStatus(query);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> checkExistSuccessFreeDepositOrder(FreeDepositUserDTO freeDepositUserDTO) {
        if (Objects.isNull(freeDepositUserDTO)) {
            log.warn("FreeDeposit WARN! checkExistSuccessFreeDepositOrder.freeDepositUserDTO is null");
            return Triple.of(false, null, null);
        }
        
        Long uid = freeDepositUserDTO.getUid();
        // 获取换电套餐已存在的免押订单信息. 如果不存在或者押金类型为缴纳押金类型则返回
        UserBatteryDeposit batteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(batteryDeposit) || UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT.equals(batteryDeposit.getDepositType())) {
            log.warn("FreeDeposit WARN! checkExistSuccessFreeDepositOrder.batteryDeposit is null, uid is {}", uid);
            return Triple.of(false, null, null);
        }
        
        // 获取押金订单记录
        FreeDepositOrder freeDepositOrder = freeDepositOrderMapper.queryByOrderId(batteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FreeDeposit WARN! checkExistSuccessFreeDepositOrder.freeDepositOrder is null, orderId is {}", batteryDeposit.getOrderId());
            return Triple.of(false, null, null);
        }
        
        // 如果都一样，查询是否免押过； 只要有一个不一样，继续新的免押
        if (!Objects.equals(freeDepositOrder.getRealName(), freeDepositUserDTO.getRealName()) || !Objects.equals(freeDepositOrder.getIdCard(), freeDepositUserDTO.getIdCard())
                || !Objects.equals(batteryDeposit.getDid(), freeDepositUserDTO.getPackageId())) {
            log.warn("FreeDeposit WARN! checkExistSuccessFreeDepositOrder.userInfo is new, go on new Free, uid is {}", uid);
            return Triple.of(false, null, freeDepositOrder);
        }
        
        log.info("FreeDeposit INFO! checkExistSuccessFreeDepositOrder.channel is {}, orderId is {}", freeDepositOrder.getChannel(), batteryDeposit.getOrderId());
        
        // 是否免押
        FreeDepositOrderStatusQuery query = FreeDepositOrderStatusQuery.builder().orderId(batteryDeposit.getOrderId()).tenantId(freeDepositUserDTO.getTenantId()).uid(uid).build();
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(freeDepositOrder.getChannel()), BaseFreeDepositService.class);
        FreeDepositOrderStatusBO bo = service.queryFreeDepositOrderStatus(query);
        
        if (Objects.nonNull(bo) && PxzQueryOrderRsp.AUTH_FROZEN.equals(bo.getAuthStatus())) {
            log.info("query free deposit status from pxz success! uid = {}, orderId = {}", freeDepositUserDTO.getUid(), batteryDeposit.getOrderId());
            return Triple.of(true, "100400", "免押已成功，请勿重复操作");
        }
        return Triple.of(false, null, null);
    }
    
    
    /**
     * 免押确认接口，根据次数区分使用拍小租还是蜂云
     */
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositOrderRequest request) {
        if (Objects.isNull(request)) {
            return Triple.of(false, "100419", "系统异常，稍后再试");
        }
        // 获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(request.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.warn("FREE DEPOSIT WARN! freeDepositData is null,uid={}", request.getUid());
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }
        
        // 实现免押
        BaseFreeDepositService freeDepositWay = freeDepositFactory.getFreeDepositWay(freeDepositData.getFreeDepositCapacity(), freeDepositData.getFyFreeDepositCapacity());
        return freeDepositWay.freeDepositOrder(request);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> unFreezeDeposit(UnFreeDepositOrderQuery query) {
        if (Objects.isNull(query)) {
            log.warn("unFreezeDeposit WARN! getFreeDepositOrderStatus.params is null");
            return Triple.of(false, "100419", "系统异常，稍后再试");
        }
        log.info("FreeDeposit INFO! unFreezeDeposit.channel is {}, orderId is {}", query.getChannel(), query.getOrderId());
        // 免押解冻
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(query.getChannel()), BaseFreeDepositService.class);
        return service.unFreezeDeposit(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> authToPay(FreeDepositAuthToPayQuery query) {
        if (Objects.isNull(query)) {
            log.warn("authToPay WARN! authToPay.query is null");
            return Triple.of(false, "100419", "系统异常，稍后再试");
        }
        log.info("FreeDeposit INFO! authToPay.channel is {}, orderId is {}", query.getChannel(), query.getOrderId());
        // 代扣
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(query.getChannel()), BaseFreeDepositService.class);
        return service.authToPay(query);
    }
    
    @Override
    public AuthPayStatusBO queryAuthToPayStatus(FreeDepositAuthToPayStatusQuery query) {
        if (Objects.isNull(query)) {
            log.warn("queryAuthToPayStatus WARN! authToPay.query is null");
            return null;
        }
        log.info("QueryAuthToPayStatus INFO! queryAuthToPayStatus.channel is {}, orderId is {}", query.getChannel(), query.getOrderId());
        // 代扣状态
        BaseFreeDepositService service = applicationContext.getBean(FreeDepositServiceWayEnums.getClassStrByChannel(query.getChannel()), BaseFreeDepositService.class);
        return service.queryAuthToPayStatus(query);
    }
}

