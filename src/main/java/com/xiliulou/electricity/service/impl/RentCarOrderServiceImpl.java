package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.RentCarOrderMapper;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 租车订单表(RentCarOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:57
 */
@Service("rentCarOrderService")
@Slf4j
public class RentCarOrderServiceImpl implements RentCarOrderService {
    @Autowired
    private RentCarOrderMapper rentCarOrderMapper;
    @Autowired
    private ElectricityCarService electricityCarService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserCarMemberCardService userCarMemberCardService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserOauthBindService userOauthBindService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    InsuranceOrderService insuranceOrderService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    CarDepositOrderService carDepositOrderService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder selectByIdFromDB(Long id) {
        return this.rentCarOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder selectByIdFromCache(Long id) {
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
    public List<RentCarOrder> selectByPage(int offset, int limit) {
        return this.rentCarOrderMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentCarOrder insert(RentCarOrder rentCarOrder) {
        this.rentCarOrderMapper.insertOne(rentCarOrder);
        return rentCarOrder;
    }

    /**
     * 修改数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(RentCarOrder rentCarOrder) {
        return this.rentCarOrderMapper.update(rentCarOrder);

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
        return this.rentCarOrderMapper.deleteById(id) > 0;
    }


    @Override
    public Triple<Boolean, String, Object> rentCarOrder(RentCarOrderQuery query) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE RENT CAR ERROR! not found user,sn={}", query.getSn());
            return Triple.of(false, "100001", "用户不存在");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE RENT CAR ERROR! not found user,uid={}", user.getUid());
            return Triple.of(false, "100001", "用户不存在");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE RENT CAR ERROR! user is disable!uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE RENT CAR ERROR! user not auth,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE RENT CAR ERROR! userCarDeposit is null,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE RENT CAR ERROR! not pay deposit,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }

        //是否购买套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.error("ELE RENT CAR ERROR! not pay rent car memberCard,uid={}", user.getUid());
            return Triple.of(false, "100232", "未购买租车");
        }

        //套餐是否过期
        if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("ELE RENT CAR ERROR! rent car memberCard expired,uid={}", user.getUid());
            return Triple.of(false, "100233", "租车套餐已过期");
        }

        //车辆是否可用
        ElectricityCar electricityCar = electricityCarService.selectBySn(query.getSn());
        if (Objects.isNull(electricityCar) || !Objects.equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ORDER ERROR! not found electricityCar,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100007", "车辆不存在");
        }

        if (Objects.equals(electricityCar.getStatus(), ElectricityCar.STATUS_IS_RENT)) {
            log.error("ORDER ERROR! this car has been bound others,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100231", "车辆已绑定其它用户");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE RENT CAR ERROR! electricityCarModel is null,uid={}", user.getUid());
            return Triple.of(false, "100009", "车辆型号不存在");
        }


        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, user.getUid());
        RentCarOrder rentCarOrder = new RentCarOrder();
        rentCarOrder.setOrderId(orderId);
        rentCarOrder.setCarModelId(electricityCar.getModelId().longValue());
        rentCarOrder.setCarDeposit(userCarDeposit.getCarDeposit().doubleValue());
        rentCarOrder.setStatus(RentCarOrder.STATUS_SUCCESS);
        rentCarOrder.setCarSn(query.getSn());
        rentCarOrder.setType(RentCarOrder.TYPE_RENT);
        rentCarOrder.setUid(user.getUid());
        rentCarOrder.setName(userInfo.getName());
        rentCarOrder.setPhone(userInfo.getPhone());
        rentCarOrder.setStoreId(electricityCarModel.getStoreId());
        rentCarOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
        rentCarOrder.setCreateTime(System.currentTimeMillis());
        rentCarOrder.setUpdateTime(System.currentTimeMillis());

        int insert = rentCarOrderMapper.insertOne(rentCarOrder);

        DbUtils.dbOperateSuccessThen(insert, () -> {

            UserCar updateUserCar = new UserCar();
            updateUserCar.setUid(user.getUid());
            updateUserCar.setSn(query.getSn());
            updateUserCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(updateUserCar);

            return null;
        });

        return Triple.of(true, "车辆绑定成功", null);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> rentCarHybridOrder(RentCarHybridOrderQuery query, HttpServletRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_RENT_CAR_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false)) {
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


        //处理租车押金
        Triple<Boolean, String, Object> rentCarDepositTriple = carDepositOrderService.handleRentCarDeposit(query, userInfo);
        if (!rentCarDepositTriple.getLeft()) {
            return rentCarDepositTriple;
        }

        //处理租车套餐订单
        Triple<Boolean, String, Object> rentCarMemberCardTriple = carMemberCardOrderService.handleRentCarMemberCard(query, userInfo);
        if (!rentCarMemberCardTriple.getLeft()) {
            return rentCarMemberCardTriple;
        }

        //处理电池押金相关
        Triple<Boolean, String, Object> rentBatteryDepositTriple = eleDepositOrderService.handleRentBatteryDeposit(query, userInfo);
        if (!rentBatteryDepositTriple.getLeft()) {
            return rentBatteryDepositTriple;
        }

        //处理电池套餐相关
        Triple<Boolean, String, Object> rentBatteryMemberCardTriple = electricityMemberCardOrderService.handleRentBatteryMemberCard(query, userInfo);
        if (!rentBatteryMemberCardTriple.getLeft()) {
            return rentBatteryMemberCardTriple;
        }

        //处理保险套餐相关
        Triple<Boolean, String, Object> rentBatteryInsuranceTriple = insuranceOrderService.handleRentBatteryInsurance(query, userInfo);
        if (!rentBatteryInsuranceTriple.getLeft()) {
            return rentBatteryInsuranceTriple;
        }

        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();

        BigDecimal totalPayAmount = BigDecimal.valueOf(0);


        //保存租车押金订单
        if (rentCarDepositTriple.getLeft() && Objects.nonNull(rentCarDepositTriple.getRight())) {
            CarDepositOrder carDepositOrder = (CarDepositOrder) rentCarDepositTriple.getRight();
            carDepositOrderService.insert(carDepositOrder);

            orderList.add(carDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_RENT_CAR_DEPOSIT);
            payAmountList.add(carDepositOrder.getPayAmount());

            totalPayAmount = totalPayAmount.add(carDepositOrder.getPayAmount());
        }

        //保存租车套餐订单
        if (rentCarMemberCardTriple.getLeft() && Objects.nonNull(rentCarMemberCardTriple.getRight())) {
            CarMemberCardOrder carMemberCardOrder = (CarMemberCardOrder) rentCarMemberCardTriple.getRight();
            carMemberCardOrderService.insert(carMemberCardOrder);

            orderList.add(carMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_RENT_CAR_MEMBER_CARD);
            payAmountList.add(carMemberCardOrder.getPayAmount());

            totalPayAmount = totalPayAmount.add(carMemberCardOrder.getPayAmount());
        }


        //保存电池押金订单
        if (rentBatteryDepositTriple.getLeft() && Objects.nonNull(rentBatteryDepositTriple.getRight())) {
            EleDepositOrder eleDepositOrder = (EleDepositOrder) rentBatteryDepositTriple.getRight();
            eleDepositOrderService.insert(eleDepositOrder);

            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            payAmountList.add(eleDepositOrder.getPayAmount());

            totalPayAmount = totalPayAmount.add(eleDepositOrder.getPayAmount());
        }


        //保存保险订单
        if (rentBatteryInsuranceTriple.getLeft() && Objects.nonNull(rentBatteryInsuranceTriple.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) rentBatteryInsuranceTriple.getRight();
            insuranceOrderService.insert(insuranceOrder);

            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());

            totalPayAmount = totalPayAmount.add(insuranceOrder.getPayAmount());
        }

        //保存套餐订单
        if (rentBatteryMemberCardTriple.getLeft() && Objects.nonNull(rentBatteryMemberCardTriple.getRight())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) rentBatteryMemberCardTriple.getRight();
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);

            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
            payAmountList.add(electricityMemberCardOrder.getPayAmount());

            totalPayAmount = totalPayAmount.add(electricityMemberCardOrder.getPayAmount());

//            electricityMemberCardOrderService.handleUserCouponAndActivity(userInfo,electricityMemberCardOrder);
        }

        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(payAmountList))
                    .payAmount(totalPayAmount)
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                    .description("集成支付收费")
                    .uid(user.getUid()).build();
            WechatJsapiOrderResultDTO resultDTO =
                    unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE UNION_INSURANCE_DEPOSIT_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
    }
}
