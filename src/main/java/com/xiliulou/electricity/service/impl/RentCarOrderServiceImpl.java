package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.RentCarOrderMapper;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.query.UserRentCarOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.RentCarOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    CalcRentCarPriceFactory calcRentCarPriceFactory;
    @Autowired
    EleBindCarRecordService eleBindCarRecordService;
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;
    
    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;

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
     * @return 对象列表
     */
    @Override
    public List<RentCarOrderVO> selectByPage(RentCarOrderQuery rentCarOrderQuery) {
        List<RentCarOrder> rentCarOrders = this.rentCarOrderMapper.selectByPage(rentCarOrderQuery);
        if (CollectionUtils.isEmpty(rentCarOrders)) {
            return Collections.EMPTY_LIST;
        }

        return rentCarOrders.parallelStream().map(item -> {
            RentCarOrderVO rentCarOrderVO = new RentCarOrderVO();
            BeanUtils.copyProperties(item, rentCarOrderVO);

            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(item.getCarModelId().intValue());
            if (Objects.nonNull(electricityCarModel)) {
                rentCarOrderVO.setCarModelName(electricityCarModel.getName());
            }

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo)) {
                rentCarOrderVO.setRentBattery(userInfo.getBatteryRentStatus());
            }

            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(item.getUid());
            if (Objects.nonNull(userCarMemberCard)) {
                CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByIdFromDB(userCarMemberCard.getCardId());
                if (Objects.nonNull(carMemberCardOrder)) {
                    rentCarOrderVO.setRentType(carMemberCardOrder.getCardName());
                }
            }

            return rentCarOrderVO;

        }).collect(Collectors.toList());

    }

    @Override
    public Integer selectPageCount(RentCarOrderQuery rentCarOrderQuery) {
        return this.rentCarOrderMapper.selectPageCount(rentCarOrderQuery);
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
        return rentCarOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
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
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(RentCarOrderQuery rentCarOrderQuery) {

        UserInfo userInfo = userInfoService.queryUserInfoByPhone(rentCarOrderQuery.getPhone(), TenantContextHolder.getTenantId());
        if (Objects.isNull(userInfo) || !rentCarOrderQuery.getUsername().equals(userInfo.getName())) {
            log.error("ELE RENT CAR ERROR! not found user,phone={}", rentCarOrderQuery.getPhone());
            return Triple.of(false, "100001", "用户不存在");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE RENT CAR ERROR! user is disable!uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE RENT CAR ERROR! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR DEPOSIT ERROR! user already rent deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0049", "已缴纳押金");
        }

        //门店
        Store store = storeService.queryByIdFromCache(rentCarOrderQuery.getStoreId());
        if (Objects.isNull(store) || !Objects.equals(store.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,storeId={}", rentCarOrderQuery.getStoreId());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }


        //车辆是否可用
//        ElectricityCar electricityCar = electricityCarService.selectBySn(rentCarOrderQuery.getSn());
//        if (Objects.isNull(electricityCar) || !Objects.equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
//            log.error("ORDER ERROR! not found electricityCar,sn={},uid={}", rentCarOrderQuery.getSn(), userInfo.getUid());
//            return Triple.of(false, "100007", "车辆不存在");
//        }
//        if (Objects.equals(electricityCar.getStatus(), ElectricityCar.STATUS_IS_RENT)) {
//            log.error("ORDER ERROR! this car has been bound others,sn={},uid={}", rentCarOrderQuery.getSn(), userInfo.getUid());
//            return Triple.of(false, "100231", "车辆已绑定其它用户");
//        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(rentCarOrderQuery.getCarModelId().intValue());
        if (Objects.isNull(electricityCarModel) || !Objects.equals(electricityCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE RENT CAR ERROR! electricityCarModel is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100009", "车辆型号不存在");
        }

        //若用户已绑定加盟商，判断车辆加盟商与用户加盟商是否一致
        if (Objects.nonNull(electricityCarModel) && Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
            if (!Objects.equals(userInfo.getFranchiseeId(), electricityCarModel.getFranchiseeId())) {
                log.error("ELE CAR ERROR! user bind franchisee not equals car franchisee,uid={}", userInfo.getUid());
                return Triple.of(false, "100239", "用户所属加盟商与车辆加盟商不符");
            }
        }

        //生成租车押金订单
        CarDepositOrder carDepositOrder = buildRentCarDepositOrder(userInfo, electricityCarModel, rentCarOrderQuery, store);

        //生成租车套餐订单
        Triple<Boolean, String, Object> rentCarMemberCardOrderTriple = buildRentCarMemberCardOrder(userInfo, electricityCarModel, rentCarOrderQuery, store);
        if (!rentCarMemberCardOrderTriple.getLeft()) {
            return rentCarMemberCardOrderTriple;
        }
        CarMemberCardOrder carMemberCardOrder = (CarMemberCardOrder) rentCarMemberCardOrderTriple.getRight();

//        //生成租车订单
//        RentCarOrder rentCarOrder = buildRentCarOrder(userInfo, electricityCarModel, rentCarOrderQuery);
//
//        RentCarOrder insert = this.insert(rentCarOrder);

        carMemberCardOrderService.insert(carMemberCardOrder);

        carDepositOrderService.insert(carDepositOrder);

        //更新用户租车状态
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setFranchiseeId(electricityCarModel.getFranchiseeId());
        updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);

        //更新用户押金
        UserCarDeposit userCarDeposit = new UserCarDeposit();
        userCarDeposit.setUid(userInfo.getUid());
        userCarDeposit.setDid(carDepositOrder.getId());
        userCarDeposit.setOrderId(carDepositOrder.getOrderId());
        userCarDeposit.setCarDeposit(carDepositOrder.getPayAmount());
        userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
        userCarDeposit.setApplyDepositTime(System.currentTimeMillis());
        userCarDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
        userCarDeposit.setTenantId(userInfo.getTenantId());
        userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
        userCarDeposit.setCreateTime(System.currentTimeMillis());
        userCarDeposit.setUpdateTime(System.currentTimeMillis());
        userCarDepositService.insertOrUpdate(userCarDeposit);

        //保存用户车辆型号
        UserCar userCar = new UserCar();
        userCar.setUid(userInfo.getUid());
        userCar.setCarModel(carDepositOrder.getCarModelId());
        userCar.setDelFlag(UserCar.DEL_NORMAL);
        userCar.setTenantId(userInfo.getTenantId());
        userCar.setCreateTime(System.currentTimeMillis());
        userCar.setUpdateTime(System.currentTimeMillis());
        userCarService.insertOrUpdate(userCar);

        //更新用户车辆套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(carMemberCardOrder.getUid());
        UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
        updateUserCarMemberCard.setUid(userInfo.getUid());
        updateUserCarMemberCard.setOrderId(carMemberCardOrder.getOrderId());
        updateUserCarMemberCard.setCardId(carMemberCardOrder.getCarModelId());
        updateUserCarMemberCard.setMemberCardExpireTime(electricityMemberCardOrderService.calcRentCarMemberCardExpireTime(carMemberCardOrder.getMemberCardType(), carMemberCardOrder.getValidDays(), userCarMemberCard));
        updateUserCarMemberCard.setDelFlag(UserCarMemberCard.DEL_NORMAL);
        updateUserCarMemberCard.setCreateTime(System.currentTimeMillis());
        updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());
        userCarMemberCardService.insertOrUpdate(updateUserCarMemberCard);
    
        //用户是否有绑定了车辆
        //        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        //        if (Objects.nonNull(electricityCar) && Objects.nonNull(electricityConfig) && Objects
        //                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)
        //                && System.currentTimeMillis() < updateUserCarMemberCard.getMemberCardExpireTime()) {
        //            electricityCarService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
        //        }
    
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityCar) && Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)
                && System.currentTimeMillis() < updateUserCarMemberCard.getMemberCardExpireTime()) {
            boolean boo = electricityCarService
                    .retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
    
            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory.setStatus(
                    boo ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_MEMBER_CARD_UN_LOCK);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
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

    private RentCarOrder buildRentCarOrder(UserInfo userInfo, ElectricityCarModel electricityCarModel, RentCarOrderQuery rentCarOrderQuery) {

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, userInfo.getUid());
        RentCarOrder rentCarOrder = new RentCarOrder();
        rentCarOrder.setOrderId(orderId);
        rentCarOrder.setCarModelId(electricityCarModel.getId().longValue());
        rentCarOrder.setCarDeposit(electricityCarModel.getCarDeposit().doubleValue());
        rentCarOrder.setStatus(RentCarOrder.STATUS_SUCCESS);
        rentCarOrder.setCarSn(rentCarOrderQuery.getSn());
        rentCarOrder.setType(RentCarOrder.TYPE_RENT);
        rentCarOrder.setUid(userInfo.getUid());
        rentCarOrder.setName(userInfo.getName());
        rentCarOrder.setPhone(userInfo.getPhone());
        rentCarOrder.setTransactionType(RentCarOrder.TYPE_TRANSACTION_OFFLINE);
        rentCarOrder.setStoreId(electricityCarModel.getStoreId());
        rentCarOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
        rentCarOrder.setCreateTime(System.currentTimeMillis());
        rentCarOrder.setUpdateTime(System.currentTimeMillis());

        return rentCarOrder;
    }

    private Triple<Boolean, String, Object> buildRentCarMemberCardOrder(UserInfo userInfo, ElectricityCarModel electricityCarModel, RentCarOrderQuery rentCarOrderQuery, Store store) {
        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", rentCarOrderQuery.getCarModelId(), userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCarMemberCard.getCardId(), electricityCarModel.getId().longValue())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(rentCarOrderQuery.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, rentCarOrderQuery.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return Triple.of(false, "100237", "租车套餐计费规则不存在!");
        }

        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_MEMBERCARD, userInfo.getUid());

        CarMemberCardOrder carMemberCardOrder = new CarMemberCardOrder();
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setOrderId(orderId);
        carMemberCardOrder.setCreateTime(System.currentTimeMillis());
        carMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        carMemberCardOrder.setStatus(CarMemberCardOrder.STATUS_SUCCESS);
        carMemberCardOrder.setCarModelId(electricityCarModel.getId().longValue());
        carMemberCardOrder.setUid(userInfo.getUid());
        carMemberCardOrder.setCardName(carMemberCardOrderService.getCardName(rentCarOrderQuery.getRentType()));
        carMemberCardOrder.setMemberCardType(rentCarOrderQuery.getRentType());
        carMemberCardOrder.setPayAmount(rentCarPrice);
        carMemberCardOrder.setUserName(userInfo.getName());
        carMemberCardOrder.setValidDays(rentCarOrderQuery.getRentTime());
        carMemberCardOrder.setPayType(CarMemberCardOrder.OFFLINE_PAYTYPE);
        carMemberCardOrder.setStoreId(rentCarOrderQuery.getStoreId());
        carMemberCardOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        carMemberCardOrder.setTenantId(userInfo.getTenantId());

        return Triple.of(true, "", carMemberCardOrder);
    }

    private CarDepositOrder buildRentCarDepositOrder(UserInfo userInfo, ElectricityCarModel electricityCarModel, RentCarOrderQuery rentCarOrderQuery, Store store) {
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setUid(userInfo.getUid());
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setDelFlag(CarDepositOrder.DEL_NORMAL);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_SUCCESS);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(rentCarOrderQuery.getStoreId());
        carDepositOrder.setPayType(CarDepositOrder.OFFLINE_PAYTYPE);
        carDepositOrder.setCarModelId(rentCarOrderQuery.getCarModelId());
        carDepositOrder.setRentBattery(CarDepositOrder.RENTBATTERY_NO);

        return carDepositOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> rentCarOrder(UserRentCarOrderQuery query) {
    
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
            return Triple.of(false, "100238", "未缴纳押金");
        }
    
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE RENT CAR ERROR! not pay deposit,uid={}", user.getUid());
            return Triple.of(false, "100238", "未缴纳押金");
        }
    
        //是否购买套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.error("ELE RENT CAR ERROR! not pay rent car memberCard,uid={}", user.getUid());
            return Triple.of(false, "100232", "未购买租车套餐");
        }
    
        //套餐是否过期
        if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("ELE RENT CAR ERROR! rent car memberCard expired,uid={}", user.getUid());
            return Triple.of(false, "100233", "租车套餐已过期");
        }
    
        //车电关联是否可租车
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                    .selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, String, Object> checkUserBatteryMemberCardResult = checkUserBatteryMemberCard(
                    userBatteryMemberCard, userInfo);
            if (!checkUserBatteryMemberCardResult.getLeft()) {
                return checkUserBatteryMemberCardResult;
            }
        }
    
        //车辆是否可用
        ElectricityCar electricityCar = electricityCarService.selectBySn(query.getSn(), TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityCar) || !Objects.equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ELE RENT CAR ERROR! not found electricityCar,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100007", "车辆不存在");
        }
    
        if (Objects.equals(electricityCar.getStatus(), ElectricityCar.STATUS_IS_RENT)) {
            log.error("ELE RENT CAR ERROR! this car has been bound others,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100231", "车辆已绑定其它用户");
        }
    
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE RENT CAR ERROR! electricityCarModel is null,uid={}", user.getUid());
            return Triple.of(false, "100009", "车辆型号不存在");
        }
    
        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            log.error("ELE RENT CAR ERROR! this user not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "未找到用户信息");
        }
    
        if (!Objects.equals(userCar.getCarModel(), electricityCar.getModelId().longValue())) {
            log.error("ELE RENT CAR ERROR! this user bind car model not equals this car model,uid={}", userInfo.getUid());
            return Triple.of(false, "100236", "车辆型号不匹配");
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
        rentCarOrder.setTransactionType(RentCarOrder.TYPE_TRANSACTION_ONLINE);
        rentCarOrder.setStoreId(electricityCarModel.getStoreId());
        rentCarOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
        rentCarOrder.setCreateTime(System.currentTimeMillis());
        rentCarOrder.setUpdateTime(System.currentTimeMillis());
    
        int insert = rentCarOrderMapper.insertOne(rentCarOrder);
    
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //更新用户车辆租赁状态
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
        
            UserCar updateUserCar = new UserCar();
            updateUserCar.setUid(user.getUid());
            updateUserCar.setSn(query.getSn());
            updateUserCar.setUpdateTime(System.currentTimeMillis());
            userCarService.updateByUid(updateUserCar);
        
            ElectricityCar updateElectricityCar = new ElectricityCar();
            updateElectricityCar.setId(electricityCar.getId());
            updateElectricityCar.setStatus(ElectricityCar.STATUS_IS_RENT);
            updateElectricityCar.setUid(userInfo.getUid());
            updateElectricityCar.setPhone(userInfo.getPhone());
            updateElectricityCar.setUserInfoId(userInfo.getId());
            updateElectricityCar.setUserName(userInfo.getName());
            updateElectricityCar.setUpdateTime(System.currentTimeMillis());
            electricityCarService.update(updateElectricityCar);
    
            //ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
            //ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
            if (Objects.nonNull(electricityCar) && Objects.nonNull(electricityConfig) && Objects
                    .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
                boolean boo = electricityCarService
                        .retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
        
                CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
                carLockCtrlHistory.setUid(userInfo.getUid());
                carLockCtrlHistory.setName(userInfo.getName());
                carLockCtrlHistory.setPhone(userInfo.getPhone());
                carLockCtrlHistory.setStatus(
                        boo ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_BIND_USER_UN_LOCK);
                carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
                carLockCtrlHistory.setCarModel(electricityCar.getModel());
                carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
                carLockCtrlHistory.setCarSn(electricityCar.getSn());
                carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
                carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
                carLockCtrlHistory.setTenantId(userInfo.getTenantId());
                carLockCtrlHistoryService.insert(carLockCtrlHistory);
            }
        
            return null;
        });
    
        return Triple.of(true, "车辆绑定成功", null);
    }
    
    private Triple<Boolean, String, Object> checkUserBatteryMemberCard(UserBatteryMemberCard userBatteryMemberCard,
            UserInfo userInfo) {
        //用户没缴纳押金可直接租车
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(true, "", "");
        }
        
        //用户未开通套餐可直接租车
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            return Triple.of(false, "100210", "用户未购买套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "用户电池套餐已暂停");
        }
        
        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
            log.warn("ORDER WARN! user's member card is expire! uid={} cardId={}", userInfo.getUid(),
                    userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "100212", "用户电池套餐已过期");
        }
        
        //如果用户不是送的套餐
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService
                .queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.LIMITED_COUNT_TYPE)
                    && userBatteryMemberCard.getRemainingNumber() < 0) {
                log.warn("ORDER ERROR! user's count < 0 ,uid={},cardId={}", userInfo.getUid(),
                        electricityMemberCard.getType());
                return Triple.of(false, "100213", "用户电池套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }
}
