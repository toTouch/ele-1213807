package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.CarMemberCardOrderMapper;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarMemberCardOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.UserCarMemberCardVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐订单表(CarMemberCardOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:25
 */
@Service("carMemberCardOrderService")
@Slf4j
public class CarMemberCardOrderServiceImpl implements CarMemberCardOrderService {
    @Autowired
    private CarMemberCardOrderMapper carMemberCardOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserOauthBindService userOauthBindService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    CalcRentCarPriceFactory calcRentCarPriceFactory;
    @Autowired
    UserCarService userCarService;
    @Autowired
    UserCarDepositService userCarDepositService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarMemberCardOrder selectByIdFromDB(Long id) {
        return this.carMemberCardOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarMemberCardOrder selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<CarMemberCardOrderVO> selectByPage(RentCarMemberCardOrderQuery memberCardOrderQuery) {

        List<CarMemberCardOrder> carMemberCardOrders = this.carMemberCardOrderMapper.selectByPage(memberCardOrderQuery);
        if (CollectionUtils.isEmpty(carMemberCardOrders)) {
            return Collections.EMPTY_LIST;
        }

        return carMemberCardOrders.parallelStream().map(item -> {
            CarMemberCardOrderVO carMemberCardOrderVO = new CarMemberCardOrderVO();
            BeanUtils.copyProperties(item, carMemberCardOrderVO);

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo)) {
                carMemberCardOrderVO.setPhone(userInfo.getPhone());
            }

            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(item.getCarModelId().intValue());
            if (Objects.nonNull(electricityCarModel)) {
                carMemberCardOrderVO.setCarModelName(electricityCarModel.getName());
            }

            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(item.getUid());

            if (Objects.nonNull(userCarMemberCard)) {
                carMemberCardOrderVO.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
            }

            return carMemberCardOrderVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(RentCarMemberCardOrderQuery memberCardOrderQuery) {
        return this.carMemberCardOrderMapper.selectByPageCount(memberCardOrderQuery);
    }

    /**
     * 新增数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarMemberCardOrder insert(CarMemberCardOrder carMemberCardOrder) {
        this.carMemberCardOrderMapper.insertOne(carMemberCardOrder);
        return carMemberCardOrder;
    }

    /**
     * 修改数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarMemberCardOrder carMemberCardOrder) {
        return this.carMemberCardOrderMapper.update(carMemberCardOrder);

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
        return this.carMemberCardOrderMapper.deleteById(id) > 0;
    }

    @Override
    public CarMemberCardOrder selectByOrderId(String orderNo) {
        return this.carMemberCardOrderMapper.selectOne(new LambdaQueryWrapper<CarMemberCardOrder>().eq(CarMemberCardOrder::getOrderId, orderNo));
    }

    /**
     * 查询用户套餐详情
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> userCarMemberCardInfo() {
        UserCarMemberCardVO userCarMemberCardVO = new UserCarMemberCardVO();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE CAR MEMBER CARD ERROR! not found userInfo,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found userCarMemberCard! uid={}", user.getUid());
            return Triple.of(true, "", null);
        }

        //获取用户当前租车套餐订单
        CarMemberCardOrder carMemberCardOrder = this.selectByOrderId(userCarMemberCard.getOrderId());
        if (Objects.isNull(carMemberCardOrder)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found carMemberCardOrder,uid={}", user.getUid());
            return Triple.of(true, "", null);
        }

        userCarMemberCardVO.setCarModelId(carMemberCardOrder.getCarModelId());
        userCarMemberCardVO.setCardName(carMemberCardOrder.getCardName());
        userCarMemberCardVO.setPayAmount(carMemberCardOrder.getPayAmount());
        userCarMemberCardVO.setValidDays(carMemberCardOrder.getValidDays());
        userCarMemberCardVO.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());

        //车辆押金
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if(Objects.nonNull(userCarDeposit)){
            userCarMemberCardVO.setCarDeposit(userCarDeposit.getCarDeposit());
        }

        //车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carMemberCardOrder.getCarModelId().intValue());
        if (Objects.nonNull(electricityCarModel)) {
            userCarMemberCardVO.setCarModelName(electricityCarModel.getName());
        }

        //用户车辆SN码
        UserCar userCar = userCarService.selectByUidFromCache(user.getUid());
        if (Objects.nonNull(userCar)) {
            userCarMemberCardVO.setCarSN(StringUtils.isBlank(userCar.getSn()) ? null : userCar.getSn());
        }

        //门店名称
        Store store = storeService.queryByIdFromCache(carMemberCardOrder.getStoreId());
        if (Objects.nonNull(store)) {
            userCarMemberCardVO.setStoreName(store.getName());
        }

        return Triple.of(true, "", userCarMemberCardVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> payRentCarMemberCard(CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CAR_CARD_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE CAR MEMBER CARD ERROR!not found pay params,uid={}", user.getUid());
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), TenantContextHolder.getTenantId());
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE CAR MEMBER CARD ERROR!not found userOauthBind or thirdId is null,uid={}", user.getUid());
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found userInfo,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR MEMBER CARD ERROR! user is disable,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE CAR MEMBER CARD ERROR! user not auth,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR MEMBER CARD ERROR! not pay deposit,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳租车押金");
        }

        UserCar userCar = userCarService.selectByUidFromCache(user.getUid());
        if(Objects.isNull(userCar)){
            log.error("ELE CAR MEMBER CARD ERROR! not found user ");
            return Triple.of(false,"ELECTRICITY.0001", "未找到用户");
        }

        //获取车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carMemberCardOrderQuery.getCarModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found electricityCarModel id={},uid={}", carMemberCardOrderQuery.getCarModelId(), user.getUid());
            return Triple.of(false, "100009", "未找到车辆型号!");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", carMemberCardOrderQuery.getCarModelId(), user.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCar.getCarModel(), electricityCarModel.getId().longValue())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(carMemberCardOrderQuery.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", user.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, carMemberCardOrderQuery.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();


        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, user.getUid());

        CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
        carMemberCardOrder.setOrderId(orderId);
        carMemberCardOrder.setCreateTime(System.currentTimeMillis());
        carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_INIT);
        carMemberCardOrder.setCarModelId(electricityCarModel.getId().longValue());
        carMemberCardOrder.setUid(user.getUid());
        carMemberCardOrder.setCardName(getCardName(carMemberCardOrderQuery.getRentType()));
        carMemberCardOrder.setMemberCardType(carMemberCardOrderQuery.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(carMemberCardOrderQuery.getRentTime());
        carMemberCardOrder.setPayType(CarMemberCardOrder.ONLINE_PAYTYPE);
        carMemberCardOrder.setTenantId(userInfo.getTenantId());
        carMemberCardOrder.setStoreId(electricityCarModel.getStoreId());
        carMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        this.insert(carMemberCardOrder);

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(carMemberCardOrder.getOrderId())
                    .uid(user.getUid())
                    .payAmount(carMemberCardOrder.getPayAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_RENT_MEMBER_CARD)
                    .attach(ElectricityTradeOrder.ATTACH_RENT_CAR_MEMBER_CARD)
                    .description("租车套餐收费")
                    .tenantId(TenantContextHolder.getTenantId()).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, "", resultDTO);
        } catch (WechatPayException e) {
            log.error("ELE CAR MEMBER CARD ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return Triple.of(false, "购买失败", null);
    }

    @Override
    public Triple<Boolean, String, Object> handleRentCarMemberCard(RentCarHybridOrderQuery query, UserInfo userInfo) {

        if (Objects.isNull(query.getCarModelId()) || Objects.isNull(query.getStoreId())) {
            return Triple.of(true, "", null);
        }

        Store store = storeService.queryByIdFromCache(query.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }


        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(query.getCarModelId().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", query.getCarModelId(), userInfo.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", query.getCarModelId(), userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCarMemberCard.getCardId(), electricityCarModel.getId().longValue())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(query.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, query.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, userInfo.getUid());

        CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setOrderId(orderId);
        carMemberCardOrder.setCreateTime(System.currentTimeMillis());
        carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_INIT);
        carMemberCardOrder.setCarModelId(electricityCarModel.getId().longValue());
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setCardName(getCardName(query.getRentType()));
        carMemberCardOrder.setMemberCardType(query.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(query.getRentTime());
        carMemberCardOrder.setPayType(CarMemberCardOrder.ONLINE_PAYTYPE);
        carMemberCardOrder.setStoreId(query.getStoreId());
        carMemberCardOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        carMemberCardOrder.setTenantId(userInfo.getTenantId());

        return Triple.of(true, "", carMemberCardOrder);

    }

    @Override
    public String getCardName(String rentType) {

        if (ElectricityCarModel.RENT_TYPE_WEEK.equals(rentType)) {
            return "周租";
        } else if (ElectricityCarModel.RENT_TYPE_MONTH.equals(rentType)) {
            return "月租";
        }

        return "其它";
    }

    @Override
    public BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> finalFranchiseeIds) {
        return Optional.ofNullable(carMemberCardOrderMapper.queryCarMemberCardTurnOver(tenantId, todayStartTime, finalFranchiseeIds)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> finalFranchiseeIds, Long beginTime, Long endTime) {
        return carMemberCardOrderMapper.queryCarMemberCardTurnOverByCreateTime(tenantId, finalFranchiseeIds, beginTime, endTime);
    }
}
