package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.CreateFreeServiceFeeOrderDTO;
import com.xiliulou.electricity.dto.IsSupportFreeServiceFeeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.FreeServiceFeeOrderMapper;
import com.xiliulou.electricity.query.FreeServiceFeePageQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeServiceFeeOrderPageVO;
import com.xiliulou.electricity.vo.UserFreeServiceFeeStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author : renhang
 * @description FreeServiceFeeOrderServiceImpl
 * @date : 2025-03-27 10:27
 **/
@Service
@Slf4j
public class FreeServiceFeeOrderServiceImpl implements FreeServiceFeeOrderService {

    @Resource
    private FreeServiceFeeOrderMapper freeServiceFeeOrderMapper;

    @Resource
    private EleDepositOrderService eleDepositOrderService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    ApplicationContext applicationContext;

    @Resource
    private FreeDepositOrderService freeDepositOrderService;

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Resource
    private UserInfoServiceImpl userInfoService;

    @Resource
    private UserBatteryDepositServiceImpl userBatteryDepositService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;


    @Override
    public void update(FreeServiceFeeOrder freeServiceFeeOrder) {
        freeServiceFeeOrderMapper.update(freeServiceFeeOrder);
    }

    @Override
    @Slave
    public Integer existsPaySuccessOrder(String freeDepositOrderId, Long uid) {
        return freeServiceFeeOrderMapper.existsPaySuccessOrder(freeDepositOrderId, uid);
    }

    @Override
    public void insertOrder(FreeServiceFeeOrder freeServiceFeeOrder) {
        freeServiceFeeOrderMapper.insert(freeServiceFeeOrder);
    }

    @Override
    public IsSupportFreeServiceFeeDTO isSupportFreeServiceFee(UserInfo userInfo, String depositOrderId) {
        IsSupportFreeServiceFeeDTO dto = new IsSupportFreeServiceFeeDTO().setSupportFreeServiceFee(false);

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(depositOrderId);
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("isSupportFreeServiceFee Warn! EleDepositOrder is null, uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0049", "未缴纳押金");
        }

        if (!Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)) {
            return dto;
        }

        // 如果押金类型不是免押，走正常的支付
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.warn("isSupportFreeServiceFee WARN! User not free order ,uid is {} ", userInfo.getUid());
            dto.setErrorMsg("不是免押，不需要支付服务费");
            return dto;
        }

        // 是否新购买套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && StrUtil.isNotBlank(userBatteryMemberCard.getOrderId())) {
            log.warn("isSupportFreeServiceFee WARN! User is renew member, don't need pay freeServiceFee, uid is {}", userInfo.getUid());
            dto.setErrorMsg("续费套餐，不需要支付服务费");
            return dto;
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || Objects.isNull(franchisee.getFreeServiceFeeSwitch()) || Objects.equals(franchisee.getFreeServiceFeeSwitch(), Franchisee.FREE_SERVICE_FEE_SWITCH_CLOSE)) {
            log.warn("isSupportFreeServiceFee WARN! FreeServiceFeeSwitch is close , franchisee is {} ", userInfo.getFranchiseeId());
            dto.setErrorMsg("加盟商未开启服务费，不需要支付服务费");
            return dto;
        }

        // 用户是否已经支付过免押服务费
        Integer existsPaySuccessOrder = applicationContext.getBean(FreeServiceFeeOrderService.class).existsPaySuccessOrder(eleDepositOrder.getOrderId(), userInfo.getUid());
        if (Objects.nonNull(existsPaySuccessOrder)) {
            log.info("isSupportFreeServiceFee Info! Current User Payed FreeServiceFee, freeDepositOrderId is {} , uid is {} ", eleDepositOrder.getOrderId(), userInfo.getUid());
            dto.setErrorMsg("已支付过服务费，不需要支付服务费");
            return dto;
        }

        return dto.setSupportFreeServiceFee(true).setFreeServiceFee(franchisee.getFreeServiceFee());
    }

    @Override
    public IsSupportFreeServiceFeeDTO isSupportFreeServiceFeeCar(UserInfo userInfo, String depositOrderId) {
        IsSupportFreeServiceFeeDTO dto = new IsSupportFreeServiceFeeDTO().setSupportFreeServiceFee(false);
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee) || Objects.isNull(franchisee.getFreeServiceFeeSwitch()) || Objects.equals(franchisee.getFreeServiceFeeSwitch(), Franchisee.FREE_SERVICE_FEE_SWITCH_CLOSE)) {
            log.warn("isSupportFreeServiceFeeCar WARN! freeServiceFeeSwitch is close , franchisee is {} ", userInfo.getFranchiseeId());
            return dto;
        }

        // 用户是否已经支付过免押服务费
        Integer existsPaySuccessOrder = applicationContext.getBean(FreeServiceFeeOrderService.class).existsPaySuccessOrder(depositOrderId, userInfo.getUid());
        if (Objects.nonNull(existsPaySuccessOrder)) {
            log.info("isSupportFreeServiceFeeCar Info! current User Payed FreeServiceFee, freeDepositOrderId is {} , uid is {} ", depositOrderId, userInfo.getUid());
            return dto;
        }

        return dto.setSupportFreeServiceFee(true).setFreeServiceFee(franchisee.getFreeServiceFee());
    }


    @Override
    public FreeServiceFeeOrder createFreeServiceFeeOrder(CreateFreeServiceFeeOrderDTO dto) {
        UserInfo userInfo = dto.getUserInfo();
        String freeServiceFeeOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.FREE_SERVICE_FEE, userInfo.getUid());

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(dto.getDepositOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            throw new BizException("402061", "不存在的免押订单");
        }
        return FreeServiceFeeOrder.builder().uid(userInfo.getUid()).name(userInfo.getName()).phone(userInfo.getPhone()).orderId(freeServiceFeeOrderId)
                .freeDepositOrderId(dto.getDepositOrderId()).payAmount(dto.getFreeServiceFee()).status(dto.getStatus()).depositType(freeDepositOrder.getDepositType())
                .tenantId(userInfo.getTenantId()).franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).paymentChannel(StrUtil.isNotBlank(dto.getPaymentChannel()) ? dto.getPaymentChannel() : "offline")
                .payTime(Objects.nonNull(dto.getPayTime()) ? dto.getPayTime() : null)
                .build();
    }


    @Override
    @Slave
    public List<FreeServiceFeeOrderPageVO> pageList(FreeServiceFeePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        query.setFranchiseeIds(pair.getRight());

        List<FreeServiceFeeOrder> list = freeServiceFeeOrderMapper.selectPageList(query);
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }

        return list.stream().map(order -> {
            FreeServiceFeeOrderPageVO vo = BeanUtil.copyProperties(order, FreeServiceFeeOrderPageVO.class);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(order.getFranchiseeId());
            vo.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    @Slave
    public Long count(FreeServiceFeePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()) {
            return NumberConstant.ZERO_L;
        }
        query.setFranchiseeIds(pair.getRight());
        return freeServiceFeeOrderMapper.selectCount(query);
    }

    @Override
    @Slave
    public FreeServiceFeeOrder queryByOrderId(String orderId) {
        return freeServiceFeeOrderMapper.selectByOrderId(orderId);
    }

    @Override
    public Pair<Boolean, Object> notifyOrderHandler(String orderId, Integer tradeOrderStatus, UserInfo userInfo) {
        FreeServiceFeeOrder freeServiceFeeOrder = queryByOrderId(orderId);
        if (ObjectUtil.isEmpty(freeServiceFeeOrder)) {
            log.error("FreeServiceFeeOrderService NotifyOrderHandler Error! freeServiceFeeOrder is null , orderId is {} ", orderId);
            return Pair.of(Boolean.FALSE, "未找到免押服务费订单");
        }

        if (!ObjectUtil.equal(freeServiceFeeOrder.getStatus(), FreeServiceFeeStatusEnum.STATUS_UNPAID.getStatus())) {
            log.warn("FreeServiceFeeOrderService NotifyOrderHandler Warn! freeServiceFeeOrder is notNeed Update , orderId is {} , status is {}", orderId, freeServiceFeeOrder.getStatus());
            return Pair.of(Boolean.FALSE, "免押服务费订单已处理");
        }

        FreeServiceFeeOrder updateOrder = new FreeServiceFeeOrder();
        updateOrder.setOrderId(freeServiceFeeOrder.getOrderId());
        updateOrder.setUpdateTime(System.currentTimeMillis());
        updateOrder.setPayTime(System.currentTimeMillis());
        updateOrder.setStatus(tradeOrderStatus);
        this.update(updateOrder);
        return Pair.of(Boolean.TRUE, null);
    }

    @Override
    public FreeServiceFeeOrder queryByFreeDepositOrderId(String freeDepositOrderId) {
        return freeServiceFeeOrderMapper.selectByFreeDepositOrderId(freeDepositOrderId);
    }


    @Override
    public UserFreeServiceFeeStatusVO getFreeServiceFeeStatus(Long uid) {
        UserFreeServiceFeeStatusVO vo = UserFreeServiceFeeStatusVO.builder().batteryFreeServiceFeeStatus(0).carFreeServiceFeeStatus(0).build();

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return vo;
        }
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.queryByUid(uid);
            if (Objects.nonNull(userBatteryDeposit)) {
                EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(eleDepositOrder) && Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)
                        && Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
                    vo.setBatteryFreeServiceFeeStatus(Objects.nonNull(this.existsPaySuccessOrder(eleDepositOrder.getOrderId(), uid)) ? 1 : 0);
                }
            }
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            // 租车会员信息
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(userInfo.getTenantId(), uid);
            if (Objects.nonNull(memberTermEntity)) {
                // 押金缴纳信息
                CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(memberTermEntity.getDepositPayOrderNo());
                if (Objects.nonNull(depositPayEntity) && Objects.equals(depositPayEntity.getPayState(), PayStateEnum.SUCCESS.getCode())
                        && Objects.equals(depositPayEntity.getPayType(), PayTypeEnum.EXEMPT.getCode())) {
                    vo.setCarFreeServiceFeeStatus(Objects.nonNull(this.existsPaySuccessOrder(depositPayEntity.getOrderNo(), uid)) ? 1 : 0);
                }
            }
        }
        return vo;
    }
}
