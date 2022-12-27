package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.CarDepositOrderMapper;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<CarDepositOrder> selectByPage(RentCarDepositOrderQuery rentCarDepositOrderQuery) {
        List<CarDepositOrder> carDepositOrders = this.carDepositOrderMapper.selectByPage(rentCarDepositOrderQuery);
        if(CollectionUtils.isEmpty(carDepositOrders)){
            return Collections.EMPTY_LIST;
        }

        return carDepositOrders;
    }

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
        return this.carDepositOrderMapper.selectOne(new LambdaQueryWrapper<CarDepositOrder>().eq(CarDepositOrder::getOrderId,orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> payRentCarDeposit(Long storeId, Integer carModelId, HttpServletRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CAR_DEPOSIT_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE CAR DEPOSIT ERROR!not found electricityPayParams,uid={}", user.getUid());
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE CAR DEPOSIT ERROR!not found userOauthBind,uid={}", user.getUid());
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE CAR DEPOSIT ERROR! user not auth,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR DEPOSIT ERROR! user already rent deposit,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
        }

        Store store = storeService.queryByIdFromCache(storeId);
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }
        if (Objects.equals(store.getPayType(), Store.OFFLINE_PAYMENT)) {
            log.error("ELE CAR DEPOSIT ERROR! not support online pay deposit,storeId={},uid={}", store.getId(), user.getUid());
            return Triple.of(false, "100008", "不支持线上缴纳租车押金");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carModelId);
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", carModelId, user.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, user.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_INIT);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(storeId);
        carDepositOrder.setPayType(CarDepositOrder.ONLINE_PAYTYPE);
        carDepositOrder.setCarModelId(carModelId.longValue());

        this.insert(carDepositOrder);

        // TODO: 2022/12/21 支付零元

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(payAmount)
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_RENT_CAR_DEPOSIT)
                    .attach(ElectricityTradeOrder.ATTACH_RENT_CAR_DEPOSIT)
                    .description("租车押金")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, "押金支付成功", resultDTO);
        } catch (WechatPayException e) {
            log.error("ELE CAR DEPOSIT ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return Triple.of(false, "", "押金支付失败！");
    }

    @Override
    public Triple<Boolean, String, Object> selectRentCarDeposit() {


        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }

        //是否归还车辆
        if(!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)){
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
            log.error("ELE CAR REFUND ERROR! not found eleDepositOrder! uid={},orderId={}", user.getUid(),userCarDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit = userCarDeposit.getCarDeposit();
        if (eleDepositOrder.getPayAmount().compareTo(deposit)==0) {
            log.error("ELE CAR REFUND ERROR! deposit not equals! uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0044", "退款金额不符");
        }

        BigDecimal payAmount = eleDepositOrder.getPayAmount();


        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
        if (refundCount > 0) {
            log.error("ELE CAR REFUND ERROR! have refunding order! uid={}", user.getUid());
            return Triple.of(false,"ELECTRICITY.0047", "请勿重复退款");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND,user.getUid());

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId)
                .payAmount(payAmount)
                .refundAmount(payAmount)
                .status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId())
                .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER).build();
        eleRefundOrderService.insert(eleRefundOrder);

        //等到后台同意退款
        return Triple.of(true, "", "提交成功！");
    }

    @Override
    public Triple<Boolean, String, Object> handleRentCarDeposit(RentCarHybridOrderQuery query, UserInfo userInfo) {
        if(Objects.isNull(query.getCarModelId()) || Objects.isNull(query.getStoreId())){
            return Triple.of(true, "", null);
        }

        Store store = storeService.queryByIdFromCache(query.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }
        if (Objects.equals(store.getPayType(), Store.OFFLINE_PAYMENT)) {
            log.error("ELE CAR DEPOSIT ERROR! not support online pay deposit,storeId={},uid={}", store.getId(), userInfo.getUid());
            return Triple.of(false, "100008", "不支持线上缴纳租车押金");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(query.getCarModelId().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", query.getCarModelId(), userInfo.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setUid(userInfo.getUid());
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_INIT);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(query.getStoreId());
        carDepositOrder.setPayType(CarDepositOrder.ONLINE_PAYTYPE);
        carDepositOrder.setCarModelId(query.getCarModelId());

        return Triple.of(true, "", carDepositOrder);
    }
}
