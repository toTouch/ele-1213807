package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.CarMemberCardOrderMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarGpsVo;
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
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    CarDepositOrderService carDepositOrderService;
    
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;

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
        List<CarMemberCardOrderVO> carMemberCardOrders = this.carMemberCardOrderMapper.selectByPage(memberCardOrderQuery);
        if (CollectionUtils.isEmpty(carMemberCardOrders)) {
            return Collections.EMPTY_LIST;
        }

        return carMemberCardOrders.parallelStream().peek(item -> {
//            CarMemberCardOrderVO carMemberCardOrderVO = new CarMemberCardOrderVO();
//            BeanUtils.copyProperties(item, carMemberCardOrderVO);

//            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
//            if (Objects.nonNull(userInfo)) {
//                carMemberCardOrderVO.setPhone(userInfo.getPhone());
//            }

            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(item.getCarModelId().intValue());
            if (Objects.nonNull(electricityCarModel)) {
                item.setCarModelName(electricityCarModel.getName());
            }

//            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(item.getUid());
//            if (Objects.nonNull(userCarMemberCard)) {
//                carMemberCardOrderVO.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
//            }
            //            //计算过期时间
            //            if (ElectricityCarModel.RENT_TYPE_WEEK.equals(item.getMemberCardType())) {
            //                item.setMemberCardExpireTime(item.getUpdateTime() + item.getValidDays() * 7 * 24 * 60 * 60 * 1000L);
            //            } else if (ElectricityCarModel.RENT_TYPE_MONTH.equals(item.getMemberCardType())) {
            //                item.setMemberCardExpireTime(item.getUpdateTime() + item.getValidDays() * 30 * 24 * 60 * 60 * 1000L);
            //            }

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
            //押金时间
            CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(userCarDeposit.getOrderId());
            if (Objects.nonNull(carDepositOrder)) {
                userCarMemberCardVO.setPayDepositTime(carDepositOrder.getCreateTime());
            }
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
    
        //车辆经纬度
        CarAttr carAttr = electricityCarService.queryLastReportPointBySn(userCar.getSn());
        if (Objects.nonNull(carAttr)) {
            userCarMemberCardVO.setLongitude(carAttr.getLongitude());
            userCarMemberCardVO.setLatitude(carAttr.getLatitude());
            userCarMemberCardVO.setPointUpdateTime(carAttr.getCreateTime().getTime());
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
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
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
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, carMemberCardOrderQuery.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
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
    public Triple<Boolean, String, Object> freeDepositPayCarMemberCard(FreeDepositCarMemberCardOrderQuery freeDepositCarMemberCardOrderQuery, HttpServletRequest request) {
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
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found electricityCarModel id={},uid={}", userCar.getCarModel(), user.getUid());
            return Triple.of(false, "100009", "未找到车辆型号!");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", userCar.getCarModel(), user.getUid());
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCar.getCarModel(), electricityCarModel.getId().longValue())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(freeDepositCarMemberCardOrderQuery.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", user.getUid());
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, freeDepositCarMemberCardOrderQuery.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
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
        carMemberCardOrder.setCardName(getCardName(freeDepositCarMemberCardOrderQuery.getRentType()));
        carMemberCardOrder.setMemberCardType(freeDepositCarMemberCardOrderQuery.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(freeDepositCarMemberCardOrderQuery.getRentTime());
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
    public Triple<Boolean, String, Object> handleRentCarMemberCard(Long storeId, Long carModelId, Integer rentTime, String rentType, UserInfo userInfo) {

        if (Objects.isNull(carModelId) || Objects.isNull(storeId)) {
            return Triple.of(true, "", null);
        }

        Store store = storeService.queryByIdFromCache(storeId);
        if (Objects.isNull(store)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found store,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }


        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carModelId.intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR MEMBER CARD ERROR! not find carMode, carModelId={},uid={}", carModelId, userInfo.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", carModelId, userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && Objects.nonNull(userCarMemberCard.getMemberCardExpireTime())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCarMemberCard.getCardId(), electricityCarModel.getId().longValue())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(rentType);
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, rentTime, rentCarPriceRule);
        if (Boolean.FALSE.equals(calcSavePrice.getLeft())) {
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
        carMemberCardOrder.setCardName(getCardName(rentType));
        carMemberCardOrder.setMemberCardType(rentType);
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(rentTime);
        carMemberCardOrder.setPayType(CarMemberCardOrder.ONLINE_PAYTYPE);
        carMemberCardOrder.setStoreId(storeId);
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
    
    @Override
    public BigDecimal queryTurnOver(Integer tenantId, Long id) {
        return Optional.ofNullable(carMemberCardOrderMapper.queryTurnOver(tenantId, id)).orElse(BigDecimal.valueOf(0));
    }
    
    @Override
    public CarMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId) {
        return carMemberCardOrderMapper.queryLastPayMemberCardTimeByUid(uid, franchiseeId, tenantId);
    }
    
    @Override
    public R editUserMemberCard(CarMemberCardOrderAddAndUpdate carMemberCardOrderAddAndUpdate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin editUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(carMemberCardOrderAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("admin editUserMemberCard ERROR! not found user! uid={}",
                    carMemberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("admin editUserMemberCard ERROR! not pay deposit,uid={}",
                    carMemberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.warn("HOME WARN! user haven't carMemberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            log.error("admin editUserMemberCard ERROR! user haven't userCar uid={}", userInfo.getUid());
            return R.failMsg("未找到用户信息!");
        }
        
        ElectricityCarModel bindCarModel = electricityCarModelService
                .queryByIdFromCache(carMemberCardOrderAddAndUpdate.getCarModelId());
        if (Objects.isNull(bindCarModel) || !Objects
                .equals(bindCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100258", "未找到车辆型号");
        }
        
        ElectricityCarModel userCarModel = electricityCarModelService
                .queryByIdFromCache(Objects.isNull(userCar.getCarModel()) ? null : userCar.getCarModel().intValue());
        if (Objects.isNull(userCarModel) || !Objects
                .equals(userCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100258", "未找到车辆型号");
        }
        
        String oldOrderId = userCarMemberCard.getOrderId();
        Long memberCardExpireTime = carMemberCardOrderAddAndUpdate.getMemberCardExpireTime();
        
        if (!Objects.equals(bindCarModel.getId(), userCarModel.getId())) {
            if (Objects.isNull(carMemberCardOrderAddAndUpdate.getValidDays())) {
                return R.failMsg("请填写租赁周期");
            }
            
            //获取租车套餐计费规则
            Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(bindCarModel);
            if (ObjectUtil.isEmpty(rentCarPriceRule)) {
                log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", bindCarModel.getId(),
                        user.getUid());
                return R.fail("100237", "车辆租赁方式不存在!");
            }
            
            EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory
                    .getInstance(carMemberCardOrderAddAndUpdate.getRentType());
            if (Objects.isNull(calcRentCarPriceInstance)) {
                log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", user.getUid());
                return R.fail("100237", "车辆租赁方式不存在!");
            }
            
            Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance
                    .getRentCarPrice(userInfo, carMemberCardOrderAddAndUpdate.getValidDays(), rentCarPriceRule);
            if (!calcSavePrice.getLeft()) {
                return R.fail("100237", "车辆租赁方式不存在!");
            }
    
            if (Objects.nonNull(userCar.getCid()) || StringUtils.isNotBlank(userCar.getSn()) || Objects
                    .equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
                return R.fail("100253", "用户已绑定车辆，请先解绑");
            }
            
            
            BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();
            String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, userInfo.getUid());
            
            CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
            carMemberCardOrder.setUid(userInfo.getUid());
            carMemberCardOrder.setOrderId(orderId);
            carMemberCardOrder.setCreateTime(System.currentTimeMillis());
            carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
            carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_SUCCESS);
            carMemberCardOrder.setCarModelId(bindCarModel.getId().longValue());
            carMemberCardOrder.setUid(userInfo.getUid());
            carMemberCardOrder.setCardName(getCardName(carMemberCardOrderAddAndUpdate.getRentType()));
            carMemberCardOrder.setMemberCardType(carMemberCardOrderAddAndUpdate.getRentType());
            carMemberCardOrder.setPayAmount(rentCarPrice);
            carMemberCardOrder.setUserName(userInfo.getName());
            carMemberCardOrder.setValidDays(carMemberCardOrderAddAndUpdate.getValidDays());
            carMemberCardOrder.setPayType(CarMemberCardOrder.OFFLINE_PAYTYPE);
            carMemberCardOrder.setStoreId(bindCarModel.getStoreId());
            carMemberCardOrder.setFranchiseeId(bindCarModel.getFranchiseeId());
            carMemberCardOrder.setTenantId(userInfo.getTenantId());
            insert(carMemberCardOrder);
            
            UserCar updateUserCar = new UserCar();
            updateUserCar.setUid(userInfo.getUid());
            updateUserCar.setCarModel(bindCarModel.getId().longValue());
            updateUserCar.setUpdateTime(System.currentTimeMillis());
            userCarService.updateByUid(updateUserCar);
            
            oldOrderId = orderId;
            memberCardExpireTime = calculationOrderMemberCardExpireTime(carMemberCardOrder.getMemberCardType(),
                    carMemberCardOrder.getValidDays());
        }
        
        UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
        updateUserCarMemberCard.setUid(userInfo.getUid());
        updateUserCarMemberCard.setOrderId(oldOrderId);
        updateUserCarMemberCard.setCardId(bindCarModel.getId().longValue());
        updateUserCarMemberCard.setMemberCardExpireTime(memberCardExpireTime);
        updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());
        userCarMemberCardService.updateByUid(updateUserCarMemberCard);
        
        Double oldCardDay = 0.0;
        Long now = System.currentTimeMillis();
        if (userCarMemberCard.getMemberCardExpireTime() > now) {
            oldCardDay = Math.ceil((userCarMemberCard.getMemberCardExpireTime() - now) / 3600000 / 24.0);
        }
        
        Double carDayTemp = 0.0;
        if (memberCardExpireTime > now) {
            carDayTemp = Math.ceil((memberCardExpireTime - now) / 3600000 / 24.0);
        }
    
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.CAR_MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CAR_MEMBER_CARD_EXPIRE_CONTENT).operateUid(user.getUid())
                .uid(userInfo.getUid()).name(user.getUsername()).oldValidDays(oldCardDay.intValue())
                .newValidDays(carDayTemp.intValue()).tenantId(TenantContextHolder.getTenantId())
                .oldMemberCard(userCarModel.getName()).newMemberCard(bindCarModel.getName())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        //用户是否有绑定了车辆
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.nonNull(electricityCar) && Objects.equals(electricityCar.getLockType(), ElectricityCar.TYPE_LOCK)
                && now < memberCardExpireTime) {
            electricityCarService.carLockCtrl(electricityCar, ElectricityCar.TYPE_UN_LOCK);
        }
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> firstEditUserMemberCard(UserCarMemberCardQuery userCarMemberCardQuery) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(userCarMemberCardQuery.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin editUserMemberCard ERROR! not found user! uid={}", userCarMemberCardQuery.getUid());
            return Triple.of(false,"ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("admin editUserMemberCard ERROR! not pay deposit,uid={}", userCarMemberCardQuery.getUid());
            return Triple.of(false,"100238", "未缴纳租车押金");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found userCarDeposit! uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
        }

        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if(Objects.isNull(userCar)){
            log.error("ELE CAR DEPOSIT CARD ERROR! not found userCar! uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
        }

        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(userCarDeposit.getOrderId());
        if(Objects.isNull(carDepositOrder)){
            log.error("ELE CAR DEPOSIT CARD ERROR! not found carDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0015", "订单不存在");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT CARD ERROR! not found electricityCarModel,uid={}", userInfo.getUid());
            return Triple.of(false,"100258", "未找到车辆型号");
        }

        if (!Objects.equals(electricityCarModel.getId().longValue(), userCar.getCarModel())) {
            log.error("ELE CAR ERROR! user bind CarModel not equals current CarModel,uid={}", userInfo.getUid());
            return Triple.of(false, "100239", "用户所属加盟商与车辆加盟商不符");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", electricityCarModel.getId(), userInfo.getUid());
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(userCarMemberCardQuery.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, userCarMemberCardQuery.getValidDays(), rentCarPriceRule);
        if (Boolean.FALSE.equals(calcSavePrice.getLeft())) {
            log.error("ELE CAR MEMBER CARD ERROR! calcSavePrice is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100237", "车辆租赁方式不存在!");
        }

        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, userInfo.getUid());

        CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setOrderId(orderId);
        carMemberCardOrder.setCreateTime(System.currentTimeMillis());
        carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_SUCCESS);
        carMemberCardOrder.setCarModelId(electricityCarModel.getId().longValue());
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setCardName(getCardName(userCarMemberCardQuery.getRentType()));
        carMemberCardOrder.setMemberCardType(userCarMemberCardQuery.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(userCarMemberCardQuery.getValidDays());
        carMemberCardOrder.setPayType(CarMemberCardOrder.OFFLINE_PAYTYPE);
        carMemberCardOrder.setStoreId(electricityCarModel.getStoreId());
        carMemberCardOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        carMemberCardOrder.setTenantId(userInfo.getTenantId());
        this.insert(carMemberCardOrder);

        Long memberCardExpireTime=calculationOrderMemberCardExpireTime(carMemberCardOrder.getMemberCardType(), carMemberCardOrder.getValidDays());

        UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
        updateUserCarMemberCard.setUid(userInfo.getUid());
        updateUserCarMemberCard.setOrderId(orderId);
        updateUserCarMemberCard.setCardId(electricityCarModel.getId().longValue());
        updateUserCarMemberCard.setMemberCardExpireTime(memberCardExpireTime);
        updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());
        userCarMemberCardService.insertOrUpdate(updateUserCarMemberCard);

        //用户是否有绑定了车辆
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.nonNull(electricityCar) && Objects.equals(electricityCar.getLockType(), ElectricityCar.TYPE_LOCK)
                && System.currentTimeMillis() < memberCardExpireTime) {
            electricityCarService.carLockCtrl(electricityCar, ElectricityCar.TYPE_UN_LOCK);
        }
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }

        return Triple.of(true, "", "操作成功!");
    }

    private Long calculationOrderMemberCardExpireTime(String memberCardType, Integer validDays) {
        long memberCardExpireTime = 0L;
        if (ElectricityCarModel.RENT_TYPE_MONTH.equals(memberCardType)) {
            memberCardExpireTime = System.currentTimeMillis() + validDays * 30 * 24 * 60 * 60 * 1000L;
        }
        
        if (ElectricityCarModel.RENT_TYPE_WEEK.equals(memberCardType)) {
            memberCardExpireTime = System.currentTimeMillis() + validDays * 7 * 24 * 60 * 60 * 1000L;
        }
        return memberCardExpireTime;
    }
    
    @Override
    public R renewalUserMemberCard(CarMemberCardRenewalAddAndUpdate carMemberCardRenewalAddAndUpdate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin renewalUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(carMemberCardRenewalAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("admin renewalUserMemberCard ERROR! not found user! uid={}",
                    carMemberCardRenewalAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("admin renewalUserMemberCard ERROR! not pay deposit,uid={}",
                    carMemberCardRenewalAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.warn("HOME WARN! user haven't carMemberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            log.error("admin renewalUserMemberCard ERROR! user haven't userCar uid={}", userInfo.getUid());
            return R.failMsg("未找到用户信息!");
        }
        
        ElectricityCarModel userCarModel = electricityCarModelService
                .queryByIdFromCache(Objects.isNull(userCar.getCarModel()) ? null : userCar.getCarModel().intValue());
        if (Objects.isNull(userCarModel) || !Objects
                .equals(userCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("100258", "未找到车辆型号");
        }
    
        if (Objects.isNull(carMemberCardRenewalAddAndUpdate.getValidDays())) {
            return R.failMsg("请填写租赁周期");
        }
        
        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(userCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! renewalUserMemberCard not found rentCarPriceRule id={},uid={}",
                    userCarModel.getId(), user.getUid());
            return R.fail("100237", "车辆租赁方式不存在!");
        }
        
        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory
                .getInstance(carMemberCardRenewalAddAndUpdate.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! renewalUserMemberCard calcRentCarPriceInstance is null,uid={}",
                    user.getUid());
            return R.fail("100237", "车辆租赁方式不存在!");
        }
        
        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance
                .getRentCarPrice(userInfo, carMemberCardRenewalAddAndUpdate.getValidDays(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return R.fail("100237", "车辆租赁方式不存在!");
        }
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, userInfo.getUid());
        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();
        
        CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setOrderId(orderId);
        carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_SUCCESS);
        carMemberCardOrder.setCarModelId(userCarModel.getId().longValue());
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(carMemberCardRenewalAddAndUpdate.getValidDays());
        carMemberCardOrder.setPayType(CarMemberCardOrder.OFFLINE_PAYTYPE);
        carMemberCardOrder.setCardName(getCardName(carMemberCardRenewalAddAndUpdate.getRentType()));
        carMemberCardOrder.setMemberCardType(carMemberCardRenewalAddAndUpdate.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setStoreId(userCarModel.getStoreId());
        carMemberCardOrder.setFranchiseeId(userCarModel.getFranchiseeId());
        carMemberCardOrder.setTenantId(userInfo.getTenantId());
        carMemberCardOrder.setCreateTime(System.currentTimeMillis());
        carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        insert(carMemberCardOrder);
        
        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(userInfo.getUid());
        updateUserCar.setCarModel(userCarModel.getId().longValue());
        updateUserCar.setUpdateTime(System.currentTimeMillis());
        userCarService.updateByUid(updateUserCar);
        
        Long memberCardExpireTime = electricityMemberCardOrderService
                .calcRentCarMemberCardExpireTime(carMemberCardOrder.getMemberCardType(),
                        carMemberCardOrder.getValidDays(), userCarMemberCard);
        
        UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
        updateUserCarMemberCard.setOrderId(orderId);
        updateUserCarMemberCard.setUid(userInfo.getUid());
        updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());
        updateUserCarMemberCard.setCardId(userCarModel.getId().longValue());
        updateUserCarMemberCard.setMemberCardExpireTime(memberCardExpireTime);
        userCarMemberCardService.updateByUid(updateUserCarMemberCard);
        
        Long now = System.currentTimeMillis();
        Double oldCardDay = 0.0;
        
        Double carDayTemp = 0.0;
        if (memberCardExpireTime > now) {
            carDayTemp = Math.ceil((memberCardExpireTime - now) / 3600000 / 24.0);
        }
        
        if (userCarMemberCard.getMemberCardExpireTime() > now) {
            oldCardDay = Math.ceil((userCarMemberCard.getMemberCardExpireTime() - now) / 3600000 / 24.0);
        }
    
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().uid(userInfo.getUid())
                .name(user.getUsername()).oldValidDays(oldCardDay.intValue())
                .operateModel(EleUserOperateRecord.CAR_MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CAR_MEMBER_CARD_EXPIRE_CONTENT).operateUid(user.getUid())
                .newValidDays(carDayTemp.intValue()).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        //用户是否有绑定了车辆
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.nonNull(electricityCar) && Objects.equals(electricityCar.getLockType(), ElectricityCar.TYPE_LOCK)) {
            electricityCarService.carLockCtrl(electricityCar, ElectricityCar.TYPE_UN_LOCK);
        }
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        return R.ok();
    }
}
