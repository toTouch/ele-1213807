package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.CarDepositOrder;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrderHistory;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.CarDepositOrderMapper;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.service.CarDepositOrderService;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderHistoryService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardFailureRecordService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarBatteryFreeDepositAlipayVo;
import com.xiliulou.electricity.vo.CarDepositOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.UserCarDepositOrderVo;
import com.xiliulou.electricity.vo.UserCarDepositVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (CarDepositOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:15:22
 */
@Service("carDepositOrderService")
@Slf4j
public class CarDepositOrderServiceImpl implements CarDepositOrderService {
    @Autowired
    private CarDepositOrderMapper carDepositOrderMapper;
    @Resource
    private EleDepositOrderMapper eleDepositOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserOauthBindService userOauthBindService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    EleRefundOrderHistoryService eleRefundOrderHistoryService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    ElectricityCarService electricityCarService;
    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarDepositOrder selectByIdFromDB(Long id) {
        return this.carDepositOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarDepositOrder selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Slave
    @Override
    public List<CarDepositOrderVO> selectByPage(RentCarDepositOrderQuery rentCarDepositOrderQuery) {
        List<CarDepositOrder> carDepositOrders = this.carDepositOrderMapper.selectByPage(rentCarDepositOrderQuery);
        if (CollectionUtils.isEmpty(carDepositOrders)) {
            return Collections.EMPTY_LIST;
        }

        return carDepositOrders.parallelStream().map(item -> {
            CarDepositOrderVO carDepositOrderVO = new CarDepositOrderVO();
            BeanUtils.copyProperties(item, carDepositOrderVO);

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            if (!Objects.isNull(store)) {
                carDepositOrderVO.setStoreName(store.getName());
            }

            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(item.getCarModelId().intValue());
            if (!Objects.isNull(electricityCarModel)) {
                carDepositOrderVO.setCarModelName(electricityCarModel.getName());
            }

            //是否已退押金
            carDepositOrderVO.setRefundDeposit(eleRefundOrderService
                    .checkDepositOrderIsRefund(item.getOrderId(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER));

            return carDepositOrderVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectPageCount(RentCarDepositOrderQuery rentCarDepositOrderQuery) {
        return this.carDepositOrderMapper.selectPageCount(rentCarDepositOrderQuery);
    }

    /**
     * 新增数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarDepositOrder insert(CarDepositOrder carDepositOrder) {
        this.carDepositOrderMapper.insertOne(carDepositOrder);
        return carDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarDepositOrder carDepositOrder) {
        return this.carDepositOrderMapper.update(carDepositOrder);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return this.carDepositOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.carDepositOrderMapper.deleteById(id) > 0;
    }

    @Override
    public CarDepositOrder selectByOrderId(String orderNo) {
        return this.carDepositOrderMapper.selectOne(new LambdaQueryWrapper<CarDepositOrder>().eq(CarDepositOrder::getOrderId, orderNo));
    }

    @Override
    public CarDepositOrder selectByOrderId(String orderNo, Integer tenantId) {
        return this.carDepositOrderMapper.selectOne(new LambdaQueryWrapper<CarDepositOrder>().eq(CarDepositOrder::getOrderId, orderNo).eq(CarDepositOrder::getTenantId, tenantId));
    }

    @Override
    public Triple<Boolean, String, Object> selectRentCarDeposit() {
        UserCarDepositVO userCarDepositVO = new UserCarDepositVO();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found userInfo,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found userCarDeposit! uid={}", user.getUid());
            return Triple.of(true, "", userCarDepositVO);
        }

        CarDepositOrder carDepositOrder = this.selectByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found carDepositOrder,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0015", "订单不存在");
        }

        BeanUtils.copyProperties(carDepositOrder, userCarDepositVO);

        Store store = storeService.queryByIdFromCache(carDepositOrder.getStoreId());
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carDepositOrder.getCarModelId().intValue());
        Integer status = eleRefundOrderService.queryStatusByOrderId(userCarDeposit.getOrderId());
        
        userCarDepositVO.setStoreName(Objects.isNull(store) ? null : store.getName());
        userCarDepositVO.setCarModelName(Objects.isNull(electricityCarModel) ? null : electricityCarModel.getName());
        userCarDepositVO.setReturnDepositStatus(status);
        userCarDepositVO.setDepositType(userCarDeposit.getDepositType());
        userCarDepositVO.setPayTime(carDepositOrder.getCreateTime());
    
        //判断用户是否有车辆
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(user.getUid());
        if (Objects.isNull(electricityCar)) {
            userCarDepositVO.setHasCarStatus(UserCarDepositVO.HAS_CAR_STATUS_NO);
        } else {
            userCarDepositVO.setHasCarStatus(UserCarDepositVO.HAS_CAR_STATUS_YES);
        }
    
        return Triple.of(true, "", userCarDepositVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public Triple<Boolean, String, Object> refundRentCarDeposit(HttpServletRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR REFUND ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CAR_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR REFUND ERROR! not found user,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR REFUND ERROR! user is disable,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR REFUND ERROR! user is not rent deposit,uid={}", user.getUid());
            return Triple.of(false, "100238", "未缴纳押金");
        }

        //是否归还车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("ELE CAR REFUND ERROR! user is rent car,uid={}", user.getUid());
            return Triple.of(false, "100250", "用户未归还车辆");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR REFUND ERROR! not found userCarDeposit! uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
        }

        //查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("ELE CAR REFUND ERROR! not found eleDepositOrder! uid={},orderId={}", user.getUid(), userCarDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit = userCarDeposit.getCarDeposit();
        if (eleDepositOrder.getPayAmount().compareTo(deposit) == 0) {
            log.error("ELE CAR REFUND ERROR! deposit not equals! uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0044", "退款金额不符");
        }

        BigDecimal payAmount = eleDepositOrder.getPayAmount();


        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.error("ELE CAR REFUND ERROR! have refunding order! uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0047", "请勿重复退款");
        }
    
        //获取退还金额
        BigDecimal refundAmount =
                getRefundAmount(eleDepositOrder).doubleValue() < 0 ? BigDecimal.ZERO : getRefundAmount(eleDepositOrder);

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT_REFUND, user.getUid());

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId)
                .payAmount(payAmount).refundAmount(refundAmount)
                .status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId())
                .franchiseeId(userInfo.getFranchiseeId())
                .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER).build();
    
        //零元直接退
        if (BigDecimal.valueOf(0).compareTo(refundAmount) == 0) {
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrder.setUpdateTime(System.currentTimeMillis());
        
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
        
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleDepositOrder.getOrderId());
            //车辆电池一起免押，退押金解绑用户电池信息
            if (Objects.nonNull(freeDepositOrder) && Objects
                    .equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                userBatteryService.deleteByUid(userInfo.getUid());
            
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }
            }
        
            userInfoService.updateByUid(updateUserInfo);
        
            userCarService.deleteByUid(userInfo.getUid());
        
            userCarDepositService.logicDeleteByUid(userInfo.getUid());
        
            userCarMemberCardService.deleteByUid(userInfo.getUid());
        
            //退押金解绑用户所属加盟商
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            //退押金成功通知前端
            return Triple.of(true, "", "SUCCESS");
        }
    
    
    
        eleRefundOrderService.insert(eleRefundOrder);

        //等到后台同意退款
        return Triple.of(true, "", "提交成功！");
    }
    
    private BigDecimal getRefundAmount(EleDepositOrder eleDepositOrder) {
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            return eleDepositOrder.getPayAmount();
        }
        
        BigDecimal refundAmount = eleDepositOrder.getPayAmount();
        FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService
                .queryByOrderId(eleDepositOrder.getOrderId());
        if (Objects.nonNull(freeDepositAlipayHistory)) {
            BigDecimal subtractAmount = eleDepositOrder.getPayAmount()
                    .subtract(freeDepositAlipayHistory.getAlipayAmount());
            refundAmount = subtractAmount.doubleValue() < 0 ? BigDecimal.ZERO : subtractAmount;
        }
        
        return refundAmount;
    }
    
    @Override
    public Triple<Boolean, String, Object> handleRentCarDeposit(Long franchiseeId ,Long carModelId, Long storeId, Integer memberCardId, UserInfo userInfo) {
        if (Objects.isNull(carModelId) || Objects.isNull(storeId)) {
            return Triple.of(true, "", null);
        }

        Store store = storeService.queryByIdFromCache(storeId);
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carModelId.intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", carModelId, userInfo.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        //租车押金和电池押金一起购买，校验换电套餐加盟商与车辆型号加盟商是否一致
        if (Objects.nonNull(franchiseeId) && !Objects.equals(franchiseeId, electricityCarModel.getFranchiseeId())) {
            log.error("ELE CAR DEPOSIT ERROR! car model franchiseeId not equals battery franchiseeId, franchiseeId1={},franchiseeId2={}", userInfo.getFranchiseeId(), electricityCarModel.getFranchiseeId());
            return Triple.of(false, "100255", "车辆型号加盟商与电池套餐加盟商不一致！");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setUid(userInfo.getUid());
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setDelFlag(CarDepositOrder.DEL_NORMAL);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_INIT);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(storeId);
        carDepositOrder.setPayType(CarDepositOrder.ONLINE_PAYTYPE);
        carDepositOrder.setCarModelId(carModelId);
        carDepositOrder.setRentBattery(Objects.isNull(memberCardId) ? CarDepositOrder.RENTBATTERY_NO : CarDepositOrder.RENTBATTERY_YES);

        return Triple.of(true, "", carDepositOrder);
    }

    /**
     * 线下退租车押金
     *
     * @param orderId
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleOffLineRefundCarDeposit(String orderId, Long uid, HttpServletRequest request) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE DEPOSIT ERROR! user is disable! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR REFUND ERROR! user is not rent deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }

        //是否归还车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("ELE CAR REFUND ERROR! user is rent car,uid={}", uid);
            return Triple.of(false, "100250", "用户未归还车辆");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR REFUND ERROR! not found userCarDeposit! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
        }

        //查找缴纳押金订单
        CarDepositOrder carDepositOrder = this.selectByOrderId(orderId, TenantContextHolder.getTenantId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("ELE CAR REFUND ERROR! not found carDepositOrder! uid={},orderId={}", uid, orderId);
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        //是否已退押金


        BigDecimal deposit = userCarDeposit.getCarDeposit();
        if (carDepositOrder.getPayAmount().compareTo(deposit) != 0) {
            log.error("ELE CAR REFUND ERROR! illegal deposit! userId={}", uid);
            return Triple.of(false, "ELECTRICITY.0044", "退款金额不符");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(carDepositOrder.getOrderId(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.error("ELE DEPOSIT ERROR! have refunding order,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0047", "请勿重复退款");
        }

        String refundOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT_REFUND, uid);

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(carDepositOrder.getOrderId())
                .refundOrderNo(refundOrderId)
                .payAmount(carDepositOrder.getPayAmount())
                .refundAmount(carDepositOrder.getPayAmount())
                .status(EleRefundOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(carDepositOrder.getTenantId())
                .franchiseeId(userInfo.getFranchiseeId())
                .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                .build();
        eleRefundOrderService.insert(eleRefundOrder);

        //插入修改记录
        EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
        eleRefundOrderHistory.setRefundOrderNo(eleRefundOrder.getRefundOrderNo());
        eleRefundOrderHistory.setRefundAmount(carDepositOrder.getPayAmount());
        eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
        eleRefundOrderHistory.setTenantId(eleRefundOrder.getTenantId());
        eleRefundOrderHistoryService.insert(eleRefundOrderHistory);


        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(uid);
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
        updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);

        //退押金时保存用户失效套餐记录
        //memberCardFailureRecordService.saveRentCarMemberCardFailureRecord(uid);

        userCarService.deleteByUid(uid);

        userCarDepositService.logicDeleteByUid(uid);

        userCarMemberCardService.deleteByUid(uid);

        userInfoService.unBindUserFranchiseeId(uid);

        return Triple.of(true, "", "操作成功");
    }

    @Slave
    @Override
    public BigDecimal queryDepositTurnOverByDepositType(Integer tenantId, Long todayStartTime, Integer depositType,
            List<Long> finalFranchiseeIds, Integer payType) {
        return Optional.ofNullable(carDepositOrderMapper
                .queryDepositTurnOverByDepositType(tenantId, todayStartTime, depositType, finalFranchiseeIds, payType))
                .orElse(BigDecimal.valueOf(0));
    }

    @Slave
    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(Integer tenantId, Integer depositType, List<Long> finalFranchiseeIds, Long beginTime, Long endTime) {
        return carDepositOrderMapper.queryDepositTurnOverAnalysisByDepositType(tenantId, depositType, finalFranchiseeIds, beginTime, endTime);
    }
    
    @Override
    public R payDepositOrderList(Long offset, Long size) {
        List<UserCarDepositOrderVo> voList = carDepositOrderMapper
                .payDepositOrderList(SecurityUtils.getUid(), TenantContextHolder.getTenantId(), offset, size);
        Optional.ofNullable(voList).orElse(new ArrayList<>()).parallelStream().forEachOrdered(item -> {
            Long refundTime = eleRefundOrderService
                    .queryRefundTime(item.getOrderId(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER);
            item.setRefundTime(refundTime);
        });
        return R.ok(voList);
    }

    @Override
    public BigDecimal queryFreeDepositAlipayTurnOver(Integer tenantId, Long time, Integer rentCarDeposit, List<Long> finalFranchiseeIds) {
        BigDecimal result = Optional.ofNullable(carDepositOrderMapper.queryFreeDepositAlipayTurnOver(tenantId, time, rentCarDeposit, finalFranchiseeIds)).orElse(BigDecimal.ZERO);

        List<CarBatteryFreeDepositAlipayVo> carBatteryFreeDepositAlipayVos = this.eleDepositOrderMapper.queryCarBatteryFreeDepositAlipay(tenantId, null, EleDepositOrder.ELECTRICITY_DEPOSIT, finalFranchiseeIds);
        BigDecimal eleAlipayAmount = BigDecimal.valueOf(0);
        BigDecimal totalAlipayAmount = BigDecimal.valueOf(0);
        if(!CollectionUtils.isEmpty(carBatteryFreeDepositAlipayVos)) {
            for(CarBatteryFreeDepositAlipayVo item : carBatteryFreeDepositAlipayVos) {
                if(item.getPayAmount().compareTo(item.getAlipayAmount()) < 0) {
                    eleAlipayAmount = eleAlipayAmount.add(item.getPayAmount());
                } else {
                    eleAlipayAmount = eleAlipayAmount.add(item.getAlipayAmount());
                }

                totalAlipayAmount = totalAlipayAmount.add(item.getAlipayAmount());
            }
        }

        result = result.add(totalAlipayAmount).subtract(eleAlipayAmount);
        return result;
    }

    @Override
    public CarDepositOrder queryLastPayDepositTimeByUid(Long uid, Long franchiseeId, Integer tenantId) {
        return carDepositOrderMapper.queryLastPayDepositTimeByUid(uid, franchiseeId, tenantId);
    }
}
