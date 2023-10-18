package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CarRentalPackageExlConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalInfoExcelVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO;
import com.xiliulou.electricity.vo.userinfo.UserEleInfoVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户列表(TUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("userInfoService")
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    @Resource
    private CarRentalPackageOrderBizService carRentalPackageOrderBizService;

    @Value("${switch.version:v2}")
    private String switchVersion;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private UserInfoMapper userInfoMapper;
    @Autowired
    StoreService storeService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    UserService userService;
    @Autowired
    EleUserAuthService eleUserAuthService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    UserMoveHistoryService userMoveHistoryService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    OffLineElectricityCabinetService offLineElectricityCabinetService;
    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ElectricityCarService electricityCarService;

    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;

    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;

    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("DATA-SCREEN-THREAD-POOL", 4, "dataScreenThread:");

    @Autowired
    ElectricityCarModelService electricityCarModelService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    UserBatteryService userBatteryService;

    @Autowired
    UserCarMemberCardService userCarMemberCardService;

    @Autowired
    UserCarService userCarService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    UserCarDepositService userCarDepositService;

    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;

    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;

    @Autowired
    RentCarOrderService rentCarOrderService;

    @Autowired
    CarDepositOrderService carDepositOrderService;

    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;

    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;

    @Autowired
    FreeDepositOrderService freeDepositOrderService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Autowired
    CarRefundOrderService carRefundOrderService;

    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;

    @Autowired
    InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    BatteryModelService batteryModelService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    UserBatteryTypeService userBatteryTypeService;

    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    CarRentalPackageService carRentalPackageService;

    @Autowired
    CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    @Autowired
    UserAuthMessageService userAuthMessageService;
    
    @Autowired
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    EnterpriseRentRecordService enterpriseRentRecordService;
    
    /**
     * 分页查询
     *
     * @param userInfoQuery 查询条件
     * @return 用户集
     */
    @Slave
    @Override
    public List<UserInfo> page(UserInfoQuery userInfoQuery) {
        if (ObjectUtil.isEmpty(userInfoQuery)) {
            userInfoQuery = UserInfoQuery.builder().offset(0L).size(10L).build();
        }
        return userInfoMapper.page(userInfoQuery);
    }

    /**
     * 查询总数
     *
     * @param userInfoQuery 查询条件
     * @return 总数
     */
    @Slave
    @Override
    public Integer count(UserInfoQuery userInfoQuery) {
        return userInfoMapper.count(userInfoQuery);
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromDB(Long id) {
        return this.userInfoMapper.selectById(id);
    }

    /**
     * @param id 主键
     * @return
     */
    @Override
    public UserInfo selectUserByUid(Long id) {

        return queryByUidFromCache(id);
    }

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo insert(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
        return userInfo;
    }

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserInfo userInfo) {
        int result = this.userInfoMapper.update(userInfo);
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        return result;
    }

    @Override
    @Slave
    public R queryList(UserInfoQuery userInfoQuery) {

        List<UserBatteryInfoVO> userBatteryInfoVOS;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByCarMemberCardExpireTime(userInfoQuery);
        } else {
            userBatteryInfoVOS = userInfoMapper.queryListForBatteryService(userInfoQuery);
        }

        if (ObjectUtil.isEmpty(userBatteryInfoVOS)) {
            return R.ok(Collections.emptyList());
        }


/*
        CompletableFuture<Void> queryPayDepositTime = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {

                if (Objects.nonNull(item.getMemberCardExpireTime())) {
                    Long now = System.currentTimeMillis();
                    long carDays = 0;
                    if (item.getMemberCardExpireTime() > now) {
                        Double carDayTemp = Math.ceil((item.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24.0);
                        carDays = carDayTemp.longValue();
                    }
                    if (Objects.equals(item.getMemberCardDisableStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                        carDays = (item.getMemberCardExpireTime() - item.getDisableMemberCardTime()) / (24 * 60 * 60 * 1000L);
                    }
                    item.setCardDays(carDays);

                    if (!Objects.equals(item.getCardId().longValue(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
                        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectLatestByUid(item.getUid());
                        if (Objects.nonNull(electricityMemberCardOrder)) {
                            item.setMemberCardCreateTime(electricityMemberCardOrder.getCreateTime());
                        }
                    }
                } else {
                    item.setCardDays(null);
                    item.setCardId(null);
                    item.setCardName(null);
                }

                if (Objects.nonNull(item.getBatteryDepositStatus()) && Objects.equals(item.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                    EleDepositOrder eleDepositOrder = eleDepositOrderService.selectLatestByUid(item.getUid());
                    if (Objects.nonNull(eleDepositOrder)) {
                        item.setPayDepositTime(eleDepositOrder.getCreateTime());
                        item.setStoreId(eleDepositOrder.getStoreId());
                    }
                }

                //获取用户电池型号
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                if (Objects.nonNull(electricityBattery)) {
                    item.setModel(batteryModelService.analysisBatteryTypeByBatteryName(electricityBattery.getSn()));
                }

                //获取用户电池押金
                UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(item.getUid());
                if (Objects.nonNull(userBatteryDeposit)) {
                    item.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
                    item.setOrderId(userBatteryDeposit.getOrderId());
                }

            });
        }, threadPool).exceptionally(e -> {
            log.error("payDepositTime list ERROR! query memberCard error!", e);
            return null;
        });

        CompletableFuture<Void> queryMemberCard = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getCardId())) {
                    //获取用户套餐
                    BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getCardId().longValue());
                    item.setCardName(Objects.nonNull(batteryMemberCard)?batteryMemberCard.getName():"");

                }

                userBatteryMemberCardPackageService.batteryMembercardTransform(item.getUid());
            });
        }, threadPool).exceptionally(e -> {
            log.error("The member list ERROR! query memberCard error!", e);
            return null;
        });

        CompletableFuture<Void> queryElectricityCar = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getUid())) {
                    ElectricityCar electricityCar = electricityCarService.queryInfoByUid(item.getUid());
                    if (Objects.nonNull(electricityCar)) {
                        item.setCarSn(electricityCar.getSn());
                    }

                    UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(item.getUid());
                    if (Objects.nonNull(userCarMemberCard)) {
                        item.setRentCarMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
                    }

                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        CompletableFuture<Void> queryInsurance = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getUid())) {
                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(item.getUid());
                    if (Objects.nonNull(insuranceUserInfo)) {
                        item.setIsUse(insuranceUserInfo.getIsUse());
                        item.setInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime());
                    }
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        //用户邀请人
        CompletableFuture<Void> queryInviterUser = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.forEach(item -> {
                if (Objects.isNull(item.getUid())) {
                    return;
                }

                item.setInviterUserName(queryFinalInviterUserName(item.getUid(), item.getTenantId()));
            });
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryMemberCard, queryElectricityCar, queryPayDepositTime, queryInsurance, queryInviterUser);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }
        */

        return R.ok(userBatteryInfoVOS);
    }

    @Slave
    @Override
    public R queryCarRentalList(UserInfoQuery userInfoQuery) {

        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(userInfoQuery);
        if (ObjectUtil.isEmpty(userCarRentalPackageDOList)) {
            return R.ok(Collections.emptyList());
        }

        //处理租车/车店一体押金状态 和 当前套餐冻结状态
        List<UserCarRentalPackageVO> userCarRentalPackageVOList = Lists.newArrayList();
        for(UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList){
            UserCarRentalPackageVO userCarRentalPackageVO = new UserCarRentalPackageVO();
            BeanUtils.copyProperties(userCarRentalPackageDO, userCarRentalPackageVO);
            if(RentalPackageTypeEnum.CAR.getCode().equals(userCarRentalPackageDO.getPackageType())){
                userCarRentalPackageVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus());
            }else if(RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())){
                userCarRentalPackageVO.setDepositStatus(convertCarBatteryDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus()));
            }

            if(MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())){
                userCarRentalPackageVO.setPackageFreezeStatus(0);
            }else{
                userCarRentalPackageVO.setPackageFreezeStatus(1);
            }

            userCarRentalPackageVOList.add(userCarRentalPackageVO);
        }

        //获取用户电池相关信息
        CompletableFuture<Void> queryUserBatteryInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {

                //获取用户电池信息
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                item.setBatterySn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
                //item.setBusinessStatus(Objects.isNull(electricityBattery) ? null : electricityBattery.getBusinessStatus());
                item.setBatteryModel(Objects.isNull(electricityBattery) ? "" : electricityBattery.getModel());

                //获取用户所属加盟商
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

                //获取租车用户邀请人
                item.setInviterUserName(queryFinalInviterUserName(item.getUid(), userInfoQuery.getTenantId()));

            });
        }, threadPool).exceptionally(e -> {
            log.error("Query user battery info error for car rental.", e);
            return null;
        });

        //获取用户租车保险相关信息
        CompletableFuture<Void> queryUserInsuranceInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {

                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(item.getPackageId());
                if(Objects.nonNull(carRentalPackagePo)){
                    item.setPackageName(carRentalPackagePo.getName());
                }

                InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(item.getUid(), item.getPackageType());
                if (Objects.isNull(insuranceUserInfoVo)) {
                    return;
                }

                item.setInsuranceStatus(insuranceUserInfoVo.getIsUse());
                item.setInsuranceExpiredTime(insuranceUserInfoVo.getInsuranceExpireTime());
            });
        }, threadPool).exceptionally(e -> {
            log.error("Query user insurance info error for car rental.", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryInfo, queryUserInsuranceInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for car rental.", e);
        }

        return R.ok(userCarRentalPackageVOList);
    }

    @Slave
    @Override
    public R queryCarRentalCount(UserInfoQuery userInfoQuery) {
        Integer count = carRentalPackageMemberTermService.queryUserCarRentalPackageCount(userInfoQuery);
        return R.ok(count);
    }

    /**
     * 将车店一体押金状态，从0-已缴纳 1-未缴纳 转换为 0-未缴纳 1-已缴纳
     * @param depositStatus
     * @return
     */
    private Integer convertCarBatteryDepositStatus(Integer depositStatus){
        Integer status = 0;

        if(Objects.isNull(depositStatus)){
            return status;
        }

        if(YesNoEnum.YES.getCode().equals(depositStatus)){
            status = YesNoEnum.NO.getCode();
        }
        return status;
    }

    @Override
    @Transactional
    public R updateStatus(Long uid, Integer usableStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo oldUserInfo = queryByUidFromCache(uid);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
            return R.ok();
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(oldUserInfo.getId());
        userInfo.setUid(oldUserInfo.getUid());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(usableStatus);
        userInfo.setTenantId(tenantId);
        update(userInfo);
        return R.ok();
    }

    @Override
    public UserInfo queryByUidFromCache(Long uid) {
        UserInfo cache = redisService.getWithHash(CacheConstant.CACHE_USER_INFO + uid, UserInfo.class);

        if (Objects.nonNull(cache)) {
            return cache;
        }

        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
        if (Objects.isNull(userInfo)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_INFO + uid, userInfo);
        return userInfo;
    }

    @Override
    public UserInfo queryByUidFromDb(Long uid) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid));
    }

    @Slave
    @Override
    public Integer homeOne(Long first, Long now, Integer tenantId) {
        return userInfoMapper.homeOne(first, now, tenantId);
    }

    //    @Override
    //    public UserInfo queryByUidFromDb(Long uid) {
    //        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid));
    //    }
    //

    @Slave
    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, Integer tenantId) {
        return userInfoMapper.homeThree(startTimeMilliDay, endTimeMilliDay, tenantId);
    }

    /**
     * 获取用户套餐信息
     *
     * @param uid
     * @return
     */
    @Override
    public R getMemberCardInfo(Long uid) {
        UserInfo userInfo = queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID={}", uid);
            return R.failMsg("未找到用户信息!");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ELE WARN! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

//        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || Objects.equals(userBatteryMemberCard.getRemainingNumber().longValue(), UserBatteryMemberCard.MEMBER_CARD_ZERO_REMAINING)) {
//            log.warn("HOME WARN! user haven't memberCard uid={}", userInfo.getUid());
//            return R.fail("100210", "用户未开通套餐");
//        }

        Long validDays = null;
        Long memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime();



        /**
         * 为了兼容之前那人写的2.0小程序  看不懂完全看不懂，谨慎修改
         */
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ELE ERROR!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.ok();
        }

        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setName("体验卡");

        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {

            EleDisableMemberCardRecord eleDisableMemberCardRecord = null;
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                memberCardExpireTime = System.currentTimeMillis() + (memberCardExpireTime - userBatteryMemberCard.getDisableMemberCardTime());
                validDays = (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()) / (24 * 60 * 60 * 1000L);
                eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), userInfo.getTenantId());
            }

//            if (!Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
//
//                if ((Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
//                        System.currentTimeMillis() >= memberCardExpireTime) ||
//                        (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
//                                userBatteryMemberCard.getRemainingNumber() > 0 && System.currentTimeMillis() >= memberCardExpireTime) ||
//                        (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
//                                userBatteryMemberCard.getRemainingNumber() == 0) ||
//                        Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())) {
//                    return R.ok();
//                }
//
//                if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
//                    userBatteryMemberCard.setRemainingNumber(UserBatteryMemberCard.UN_LIMIT_COUNT_REMAINING_NUMBER);
//                }
//                if (Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() < 0) {
//                    memberCardExpireTime = System.currentTimeMillis();
//                }
//            } else {
                if (Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                        || System.currentTimeMillis() >= userBatteryMemberCard.getMemberCardExpireTime()) {
                    return R.ok();
                }
//            }
            ownMemberCardInfoVo.setName(batteryMemberCard.getName());
//            ownMemberCardInfoVo.setType(batteryMemberCard.getType());
            ownMemberCardInfoVo.setMaxUseCount(batteryMemberCard.getUseCount());
            if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)) {
                ownMemberCardInfoVo.setEndTime(userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * (24 * 60 * 60 * 1000L));
            }

            //兼容旧的小程序  送的次数卡 返回的套餐id必须为null
            ownMemberCardInfoVo.setCardId(userBatteryMemberCard.getMemberCardId().intValue());
        }

        ownMemberCardInfoVo.setMemberCardExpireTime(memberCardExpireTime);
//        ownMemberCardInfoVo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber().longValue());
//        ownMemberCardInfoVo.setMaxUseCount(userBatteryMemberCard.getRemainingNumber().longValue());
        ownMemberCardInfoVo.setDays((long) Math.round((memberCardExpireTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
//        ownMemberCardInfoVo.setCardId(userBatteryMemberCard.getMemberCardId().intValue());
        ownMemberCardInfoVo.setMemberCardDisableStatus(userBatteryMemberCard.getMemberCardStatus());
        ownMemberCardInfoVo.setValidDays(validDays);
        ownMemberCardInfoVo.setDisableMemberCardTime(userBatteryMemberCard.getDisableMemberCardTime());

        if (Objects.nonNull(userBatteryMemberCard.getRemainingNumber())) {
            ownMemberCardInfoVo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber().longValue());
            ownMemberCardInfoVo.setMaxUseCount(userBatteryMemberCard.getRemainingNumber().longValue());
        } else {
            ownMemberCardInfoVo.setRemainingNumber(0L);
            ownMemberCardInfoVo.setMaxUseCount(0L);
        }

        return R.ok(ownMemberCardInfoVo);
    }

    @Override
    public R getRentCarMemberCardInfo(Long uid) {

        UserInfo userInfo = queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID:{}", uid);
            return R.failMsg("未找到用户信息!");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.warn("ELE WARN! not pay deposit! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userCarMemberCard)) {
            log.error("not found userCarMemberCard,uid={}", uid);
            return R.failMsg("未找到用户信息!");
        }

        UserCar userCar = userCarService.selectByUidFromCache(uid);
        if (Objects.isNull(userCar)) {
            log.error("not found userCar,uid={}", uid);
            return R.failMsg("未找到用户信息!");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userCarMemberCard.getCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            return R.ok();
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELECTRICITY  ERROR! not found memberCard ! uid:{} ", userInfo.getUid());
            return R.fail("100005", "未找到车辆型号");
        }

        Long memberCardExpireTime = userCarMemberCard.getMemberCardExpireTime();

        if (Objects.isNull(memberCardExpireTime) || System.currentTimeMillis() >= memberCardExpireTime) {
            return R.ok();
        }
        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setMemberCardExpireTime(memberCardExpireTime);
        ownMemberCardInfoVo.setName(electricityMemberCard.getName());
        ownMemberCardInfoVo.setCarName(electricityCarModel.getName());
        ownMemberCardInfoVo.setType(electricityMemberCard.getType());
        ownMemberCardInfoVo.setDays((long) Math.round((memberCardExpireTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
        ownMemberCardInfoVo.setCardId(electricityMemberCard.getId());
        return R.ok(ownMemberCardInfoVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R verifyAuth(Long id, Integer authStatus, String msg) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
            return R.ok();
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setTenantId(tenantId);
        userInfo.setUid(oldUserInfo.getUid());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setAuthStatus(authStatus);
        userInfo.setAuthType(UserInfo.AUTH_TYPE_PERSON);
//        if (Objects.equals(authStatus, UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
//            userInfo.setServiceStatus(UserInfo.STATUS_IS_AUTH);
//        }
        update(userInfo);
        //修改资料项
        eleUserAuthService.updateByUid(oldUserInfo.getUid(), authStatus);

        if (Objects.equals(UserInfo.AUTH_STATUS_REVIEW_REJECTED, authStatus)) {
            UserAuthMessage userAuthMessage = new UserAuthMessage();
            userAuthMessage.setUid(userInfo.getUid());
            userAuthMessage.setAuthStatus(userInfo.getAuthStatus());
            userAuthMessage.setMsg(msg);
            userAuthMessage.setTenantId(userInfo.getTenantId());
            userAuthMessage.setCreateTime(System.currentTimeMillis());
            userAuthMessage.setUpdateTime(System.currentTimeMillis());
            userAuthMessageService.insert(userAuthMessage);
        }

        return R.ok();
    }

    @Override
    @Transactional
    public R updateAuth(UserInfo userInfo) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        userInfo.setTenantId(tenantId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = update(userInfo);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //实名认证数据修改
            UserInfo newUserInfo = this.queryByIdFromDB(userInfo.getId());
            //身份证
            if (Objects.nonNull(userInfo.getIdNumber())) {
                EleUserAuth eleUserAuth1 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(), EleAuthEntry.ID_ID_CARD);
                if (Objects.nonNull(eleUserAuth1)) {
                    eleUserAuth1.setUpdateTime(System.currentTimeMillis());
                    eleUserAuth1.setValue(userInfo.getIdNumber());
                    eleUserAuthService.update(eleUserAuth1);
                } else {
                    eleUserAuth1 = new EleUserAuth();
                    eleUserAuth1.setUid(userInfo.getUid());
                    eleUserAuth1.setEntryId(EleAuthEntry.ID_ID_CARD);
                    eleUserAuth1.setUpdateTime(System.currentTimeMillis());
                    eleUserAuth1.setValue(userInfo.getIdNumber());
                    eleUserAuth1.setCreateTime(System.currentTimeMillis());
                    eleUserAuth1.setTenantId(tenantId);
                    eleUserAuthService.insert(eleUserAuth1);
                }
            }

            //姓名
            if (Objects.nonNull(userInfo.getName())) {
                EleUserAuth eleUserAuth2 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(), EleAuthEntry.ID_NAME_ID);
                if (Objects.nonNull(eleUserAuth2)) {
                    eleUserAuth2.setUpdateTime(System.currentTimeMillis());
                    eleUserAuth2.setValue(userInfo.getName());
                    eleUserAuthService.update(eleUserAuth2);
                } else {
                    eleUserAuth2 = new EleUserAuth();
                    eleUserAuth2.setUid(userInfo.getUid());
                    eleUserAuth2.setEntryId(EleAuthEntry.ID_NAME_ID);
                    eleUserAuth2.setUpdateTime(System.currentTimeMillis());
                    eleUserAuth2.setValue(userInfo.getName());
                    eleUserAuth2.setCreateTime(System.currentTimeMillis());
                    eleUserAuth2.setTenantId(tenantId);
                    eleUserAuthService.insert(eleUserAuth2);
                }
            }
            return null;
        });
        return R.ok();
    }

    @Override
    public R queryUserAuthInfo(UserInfoQuery userInfoQuery) {
        List<UserInfo> userInfos = userInfoMapper.queryList(userInfoQuery);


        if (!DataUtil.collectionIsUsable(userInfos)) {
            return R.ok(Collections.emptyList());
        }

        List<UserAuthInfoVo> result = userInfos.stream().map(e -> {
            UserAuthInfoVo userAuthInfoVo = new UserAuthInfoVo();
            BeanUtils.copyProperties(e, userAuthInfoVo);

            if (Objects.equals(e.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_REJECTED)) {
                UserAuthMessage userAuthMessage = userAuthMessageService.selectLatestByUid(e.getUid());
                userAuthInfoVo.setMsg(Objects.isNull(userAuthMessage) ? "" : userAuthMessage.getMsg());
            }

            List<EleUserAuth> list = (List<EleUserAuth>) eleUserAuthService.selectCurrentEleAuthEntriesList(e.getUid()).getData();
            if (!DataUtil.collectionIsUsable(list)) {
                return userAuthInfoVo;
            }

            list.stream().forEach(auth -> {
                if (auth.getEntryId().equals(EleAuthEntry.ID_CARD_BACK_PHOTO)) {
                    userAuthInfoVo.setIdCardBackUrl(auth.getValue());
                }

                if (auth.getEntryId().equals(EleAuthEntry.ID_CARD_FRONT_PHOTO)) {
                    userAuthInfoVo.setIdCardFrontUrl(auth.getValue());
                }

                if (auth.getEntryId().equals(EleAuthEntry.ID_SELF_PHOTO)) {
                    userAuthInfoVo.setSelfPhoto(auth.getValue());
                }

            });


            return userAuthInfoVo;
        }).collect(Collectors.toList());

        return R.ok(result);
    }

    @Slave
    @Override
    public R queryCount(UserInfoQuery userInfoQuery) {
        Integer count ;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            count = userInfoMapper.queryCountByMemberCardExpireTime(userInfoQuery);
        } else if(Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)){
            count = userInfoMapper.queryCountByCarMemberCardExpireTime(userInfoQuery);
        } else {
            count = userInfoMapper.queryCountForBatteryService(userInfoQuery);
        }
        return R.ok(count);
    }

    @Override
    public R queryAuthenticationCount(UserInfoQuery userInfoQuery) {
        return R.ok(userInfoMapper.queryAuthenticationCount(userInfoQuery));
    }

    @Slave
    @Override
    public Integer querySumCount(UserInfoQuery userInfoQuery) {
        return userInfoMapper.queryCount(userInfoQuery);
    }

    //后台绑定电池
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo oldUserInfo = queryByUidFromCache(userInfoBatteryAddAndUpdate.getUid());
        if (Objects.isNull(oldUserInfo) || !Objects.equals(tenantId, oldUserInfo.getTenantId())) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //未实名认证
        if (!Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("WEBBIND ERROR! user not auth! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(oldUserInfo.getUid());
        if (!(Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(oldUserInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode()))) {
            log.error("WEBBIND ERROR ERROR! not pay deposit! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //判断电池滞纳金

            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(oldUserInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("WEBBIND ERROR ERROR! user haven't memberCard uid={}", oldUserInfo.getUid());
                return R.fail( "100210", "用户未开通套餐");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("WEBBIND ERROR ERROR! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail( "100211", "换电套餐停卡审核中");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("WEBBIND ERROR ERROR! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐已暂停");
            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if(Objects.isNull(batteryMemberCard)){
                log.error("WEBBIND ERROR ERROR! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(),userBatteryMemberCard.getMemberCardId());
                return R.fail( "ELECTRICITY.00121","套餐不存在");
            }

            //判断用户电池服务费
            Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(oldUserInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("WEBBIND ERROR ERROR! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费");
            }

            if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                log.error("WEBBIND ERROR ERROR! battery memberCard is Expire,uid={}", oldUserInfo.getUid());
                return R.fail( "ELECTRICITY.0023", "套餐已过期");
            }
        }else{
            //判断车电一体滞纳金
            if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(oldUserInfo.getTenantId(),oldUserInfo.getUid()))) {
                log.warn("ORDER WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("300001", "存在滞纳金，请先缴纳");
            }
        }

        //判断电池是否存在，或者已经被绑定
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("WEBBIND ERROR ERROR! not found Battery! batteryName={}", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(), userInfoBatteryAddAndUpdate.getUid())) {
            log.error("WEBBIND ERROR ERROR! battery is bind user! sn={} ", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            return R.fail("100019", "该电池已经绑定用户");
        }

        if (!Objects.equals(oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId())) {
            log.error("WEBBIND ERROR ERROR! franchiseeId not equals,userFranchiseeId={},batteryFranchiseeId={}",
                    oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId());
            return R.fail("100371", "电池加盟商与用户加盟商不一致");
        }

        //多型号  绑定电池需要判断电池是否和用户型号一致
        Triple<Boolean, String, Object> verifyUserBatteryTypeResult = verifyUserBatteryType(oldElectricityBattery, oldUserInfo);
        if (Boolean.FALSE.equals(verifyUserBatteryTypeResult.getLeft())) {
            return R.fail(verifyUserBatteryTypeResult.getMiddle(), (String) verifyUserBatteryTypeResult.getRight());
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(oldUserInfo.getUid());
        updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = updateByUid(updateUserInfo);

        //之前有电池，将原来的电池解绑
        ElectricityBattery isBindElectricityBattery = electricityBatteryService.queryByUid(userInfoBatteryAddAndUpdate.getUid());
        if (Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.EDIT_TYPE) && Objects.nonNull(isBindElectricityBattery)) {
            ElectricityBattery notBindOldElectricityBattery = new ElectricityBattery();
            notBindOldElectricityBattery.setId(isBindElectricityBattery.getId());
            notBindOldElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
            notBindOldElectricityBattery.setElectricityCabinetId(null);
            notBindOldElectricityBattery.setElectricityCabinetName(null);
            notBindOldElectricityBattery.setUid(null);
            notBindOldElectricityBattery.setBorrowExpireTime(null);
            notBindOldElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(notBindOldElectricityBattery);
        }

        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
            rentBatteryOrder.setUid(oldUserInfo.getUid());
            rentBatteryOrder.setName(oldUserInfo.getName());
            rentBatteryOrder.setPhone(oldUserInfo.getPhone());
            rentBatteryOrder.setElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            rentBatteryOrder.setBatteryDeposit(Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
            rentBatteryOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_BATTERY, user.getUid()));
            rentBatteryOrder.setStatus(RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS);
            rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
            rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
            rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
            rentBatteryOrder.setCreateTime(System.currentTimeMillis());
            rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
            rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_BIND);
            rentBatteryOrderService.insert(rentBatteryOrder);

            //生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.BATTERY_MODEL)
                    .operateContent(Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.EDIT_TYPE) ? EleUserOperateRecord.EDIT_BATTERY_CONTENT : EleUserOperateRecord.BIND_BATTERY_CONTENT)
                    .operateUid(user.getUid())
                    .uid(oldUserInfo.getUid())
                    .tenantId(TenantContextHolder.getTenantId())
                    .name(user.getUsername())
                    .initElectricityBatterySn(Objects.nonNull(isBindElectricityBattery) ? isBindElectricityBattery.getSn() : "")
                    .nowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserOperateRecord);

            //修改电池状态
            ElectricityBattery electricityBattery = new ElectricityBattery();
            electricityBattery.setId(oldElectricityBattery.getId());
            electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            electricityBattery.setElectricityCabinetId(null);
            electricityBattery.setElectricityCabinetName(null);
            electricityBattery.setUid(userInfoBatteryAddAndUpdate.getUid());
            electricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(electricityBattery);
    
            enterpriseRentRecordService.saveEnterpriseRentRecord(rentBatteryOrder.getUid());
            return null;
        });
        return R.ok();
    }

    private Triple<Boolean, String, Object> verifyUserBatteryType(ElectricityBattery electricityBattery, UserInfo userInfo) {

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }

        if (!Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            return Triple.of(true, null, null);
        }

        //获取用户绑定的电池型号
        List<String> userBindBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isEmpty(userBindBatteryTypes)) {
            return Triple.of(true, null, null);
        }

        String batterType = batteryModelService.analysisBatteryTypeByBatteryName(electricityBattery.getSn());

        if (!userBindBatteryTypes.contains(batterType)) {
            return Triple.of(false, "100297", "电池型号与用户绑定的型号不一致");
        }

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R webUnBindBattery(Long uid) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("WEBUNBIND ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //查找用户
        UserInfo oldUserInfo = queryByUidFromCache(uid);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
            return R.ok();
        }

        //未实名认证
        if (!Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("WEBUNBIND ERROR! user not auth,uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (!Objects.equals(oldUserInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("WEBUNBIND ERROR! not  rent battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(oldUserInfo.getUid());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("WEBUNBIND ERROR! not found user bind battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }

        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //判断电池滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(oldUserInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("WEBBIND ERROR ERROR! user haven't memberCard uid={}", oldUserInfo.getUid());
                return R.fail( "100210", "用户未开通套餐");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("WEBBIND ERROR ERROR! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail( "100211", "换电套餐停卡审核中");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("WEBBIND ERROR ERROR! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐已暂停");
            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if(Objects.isNull(batteryMemberCard)){
                log.error("WEBBIND ERROR ERROR! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(),userBatteryMemberCard.getMemberCardId());
                return R.fail( "ELECTRICITY.00121","套餐不存在");
            }

            //判断用户电池服务费
            Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(oldUserInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("WEBBIND ERROR ERROR! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费");
            }
        }else{
            //判断车电一体滞纳金
            if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(oldUserInfo.getTenantId(),oldUserInfo.getUid()))) {
                log.warn("ORDER WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("300001","存在滞纳金，请先缴纳");
            }
        }

        //解绑电池
        ElectricityBattery electricityBattery = new ElectricityBattery();
        electricityBattery.setId(oldElectricityBattery.getId());
        electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_EXCEPTION);
        electricityBattery.setElectricityCabinetId(null);
        electricityBattery.setElectricityCabinetName(null);
        electricityBattery.setUid(null);
        electricityBattery.setBorrowExpireTime(null);
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.updateBatteryUser(electricityBattery);
    
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(oldUserInfo.getUid());
        //生成退电池记录
        RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
        rentBatteryOrder.setUid(oldUserInfo.getUid());
        rentBatteryOrder.setName(oldUserInfo.getName());
        rentBatteryOrder.setPhone(oldUserInfo.getPhone());
        rentBatteryOrder.setElectricityBatterySn(oldElectricityBattery.getSn());
        rentBatteryOrder.setBatteryDeposit(Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
        rentBatteryOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_BATTERY, user.getUid()));
        rentBatteryOrder.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
        rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
        rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
        rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
        rentBatteryOrder.setCreateTime(System.currentTimeMillis());
        rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_UNBIND);
        rentBatteryOrderService.insert(rentBatteryOrder);
        
        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.BATTERY_MODEL)
                .operateContent(EleUserOperateRecord.UN_BIND_BATTERY_CONTENT)
                .operateUid(user.getUid())
                .uid(oldUserInfo.getUid())
                .name(user.getUsername())
                .initElectricityBatterySn(oldElectricityBattery.getSn())
                .nowElectricityBatterySn(null)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        enterpriseRentRecordService.saveEnterpriseReturnRecord(rentBatteryOrder.getUid());
        return R.ok();
    }


    @Override
    public Integer deleteByUid(Long uid) {
//        return userInfoMapper.delete(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid));

        //改为逻辑删除
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setDelFlag(UserInfo.DEL_DEL);

        return this.updateByUid(userInfo);
    }

    @Override
    public R queryUserBelongFranchisee(Long franchiseeId, Integer tenantId) {
        return R.ok(franchiseeService.queryByIdAndTenantId(franchiseeId, tenantId));
    }

    @Override
    public R queryUserAllConsumption(Long id) {
        UserTurnoverVo userTurnoverVo = queryUserConsumptionPay(id);

        BigDecimal sum = userTurnoverVo.getBatteryServiceFee();
        sum = sum.add(userTurnoverVo.getCarMemberCardTurnover());
        sum = sum.add(userTurnoverVo.getMemberCardTurnover());

        return R.ok(new UserInfoSumTurnoverVo(sum));
    }

    private UserTurnoverVo queryUserConsumptionPay(Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();

        UserTurnoverVo userTurnoverVo = new UserTurnoverVo();
        //用户电池总套餐消费额
        CompletableFuture<Void> queryMemberCardPayAmount = CompletableFuture.runAsync(() -> {
            //用户套餐总支付金额
            BigDecimal pay = electricityMemberCardOrderService.queryTurnOver(tenantId, id);
            BigDecimal refund = batteryMembercardRefundOrderService.selectUserTotalRefund(tenantId, id);
            userTurnoverVo.setMemberCardTurnover(pay.subtract(refund));

            userBatteryMemberCardPackageService.batteryMembercardTransform(id);
        }, threadPool).exceptionally(e -> {
            log.error("MEMBER CARD ORDER ERROR! query turn over error", e);
            return null;
        });

        //用户租车总套餐消费额
        CompletableFuture<Void> queryCarMemberCardPayAmount = CompletableFuture.runAsync(() -> {
            BigDecimal pay = carMemberCardOrderService.queryTurnOver(tenantId, id);
            userTurnoverVo.setCarMemberCardTurnover(pay);
        }, threadPool).exceptionally(e -> {
            log.error("CAR MEMBER CARD ORDER ERROR! query turn over error", e);
            return null;
        });

        //用户电池服务费消费额
        CompletableFuture<Void> queryBatteryServiceFeePayAmount = CompletableFuture.runAsync(() -> {
            BigDecimal pay = eleBatteryServiceFeeOrderService.queryUserTurnOver(tenantId, id);
            userTurnoverVo.setBatteryServiceFee(pay);
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        //等待所有线程停止 thenAcceptBoth方法会等待a,b线程结束后获取结果
        CompletableFuture<Void> resultFuture = CompletableFuture
                .allOf(queryMemberCardPayAmount, queryCarMemberCardPayAmount, queryBatteryServiceFeePayAmount);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return userTurnoverVo;
    }

    @Override
    public UserInfo queryUserInfoByPhone(String phone, Integer tenantId) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }

    @Override
    public UserInfo queryUserByPhoneAndFranchisee(String phone, Integer franchiseeId, Integer tenantId) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId).eq(UserInfo::getFranchiseeId, franchiseeId).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }

    @Slave
    @Override
    public Integer queryAuthenticationUserCount(Integer tenantId) {
        return userInfoMapper.queryAuthenticationUserCount(tenantId);
    }

    @Slave
    @Override
    public List<HomePageUserByWeekDayVo> queryUserAnalysisForAuthUser(Integer tenantId, Long beginTime, Long endTime) {
        return userInfoMapper.queryUserAnalysisForAuthUser(tenantId, beginTime, endTime);
    }

    @Slave
    @Override
    public List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(Integer tenantId, Integer userStatus, Long beginTime, Long endTime) {
        return userInfoMapper.queryUserAnalysisByUserStatus(tenantId, userStatus, beginTime, endTime);
    }

    @Override
    public UserInfoDetailVO selectUserInfoDetail() {
        UserInfoDetailVO userInfoDetailVO = new UserInfoDetailVO();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user ");
            return userInfoDetailVO;
        }

        //审核状态
        UserInfo userInfo = this.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo! uid={}", user.getUid());
            return userInfoDetailVO;
        }
        BeanUtils.copyProperties(userInfo, userInfoDetailVO);


        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());


        //套餐状态
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE))) {
            userInfoDetailVO.setIsExistMemberCard(UserInfoDetailVO.NOT_EXIST_MEMBER_CARD);
        } else {
            userInfoDetailVO.setIsExistMemberCard(UserInfoDetailVO.EXIST_MEMBER_CARD);
        }

        //服务状态
//        userInfoDetailVO.setServiceStatus(getServiceStatus(userInfo, franchiseeUserInfo));
        //兼容旧版小程序
        userInfoDetailVO.setServiceStatus(getServiceStatus(userInfo, null));


        //电池服务费
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = serviceFeeUserInfoService.queryUserBatteryServiceFee(user.getUid());
        userInfoDetailVO.setBatteryServiceFee(eleBatteryServiceFeeVO);


        //用户状态(离线换电)
        UserFrontDetectionVO userFrontDetection = offLineElectricityCabinetService.getUserFrontDetection(userInfo, userBatteryMemberCard);
        userInfoDetailVO.setUserFrontDetection(userFrontDetection);

//        InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.queryByUidAndTenantId(user.getUid(), user.getTenantId());
//        userInfoDetailVO.setInsuranceUserInfoVo(insuranceUserInfoVo);
        return userInfoDetailVO;
    }

    @Override
    public Triple<Boolean, String, Object> selectUserInfoStatus() {
        UserInfoResultVO userInfoResult = new UserInfoResultVO();

        if (Objects.isNull(SecurityUtils.getUid())) {
            log.error("ELE ERROR! not found user ");
            return Triple.of(false, "100001", userInfoResult);
        }

        UserInfo userInfo = this.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo! uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100001", userInfoResult);
        }

        //电池订单类型为免押
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        acquireBatteryFreeDepositResult(userBatteryDeposit, userInfo, userInfoResult);

        //车辆订单类型为免押
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(SecurityUtils.getUid());
        acquireCarFreeDepositResult(userCarDeposit, userInfo, userInfoResult);


        //审核状态
        userInfoResult.setAuthStatus(userInfo.getAuthStatus());
        userInfoResult.setFranchiseeId(userInfo.getFranchiseeId());


        UserCarDetail userCarDetail = new UserCarDetail();
        UserBatteryDetail userBatteryDetail = new UserBatteryDetail();
        userInfoResult.setUserCarDetail(userCarDetail);
        userInfoResult.setUserBatteryDetail(userBatteryDetail);


        //是否缴纳租电池押金
        //是否缴纳租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);

            //是否缴纳车电一体押金
            if(Objects.equals(userInfo.getCarBatteryDepositStatus(),YesNoEnum.YES.getCode())){
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
            }else{
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            }
        } else {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
        }



        //是否购买租电池套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)
                || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) //如果送的次数卡  首页提示没有购买套餐
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
        } else {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
            userBatteryDetail.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        }

        //套餐是否过期(前端要兼容旧代码  不能删除)
        if (!Objects.isNull(userBatteryMemberCard) && !Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && !Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
        }

        //是否购买车电一体套餐
        if(Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())){
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(userInfo.getTenantId(), userInfo.getUid());
            if(Objects.nonNull(memberTermEntity) && Objects.equals( memberTermEntity.getRentalPackageType(), RentalPackageTypeEnum.CAR_BATTERY.getCode()) && Objects.nonNull(memberTermEntity.getRentalPackageId())){
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
                userBatteryDetail.setMemberCardExpireTime(memberTermEntity.getDueTimeTotal());
            }else{
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
            }

            //车电一体套餐是否过期
            if(Objects.nonNull(memberTermEntity) && Objects.nonNull(memberTermEntity.getDueTimeTotal()) && memberTermEntity.getDueTimeTotal()< System.currentTimeMillis()){
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
            }else{
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
            }
        }

        //套餐是否暂停
        if (!Objects.isNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.NO);
        }

        //是否产生电池服务费
        if(Objects.nonNull(userBatteryMemberCard)){
            Triple<Boolean, Integer, BigDecimal> batteryServiceFeeTriple = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()), serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(batteryServiceFeeTriple.getLeft())) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
                userBatteryDetail.setBatteryServiceFee(batteryServiceFeeTriple.getRight());
            } else {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }


        //用户状态(离线换电)
        UserFrontDetectionVO userFrontDetection = offLineElectricityCabinetService.getUserFrontDetection(userInfo, userBatteryMemberCard);
        userInfoResult.setUserFrontDetection(userFrontDetection);


        //是否有车电一体滞纳金
        if(Objects.isNull(userBatteryMemberCard) || StringUtils.isBlank(userBatteryMemberCard.getOrderId())){
            if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
            }else{
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }

        //是否绑定的有电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.YES);
            userBatteryDetail.setBatteryInfo(electricityBatteryService.queryByUid(userInfo.getUid()));
        } else {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.NO);
        }

        //是否缴纳租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO) && Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.NO.getCode()) ) {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.NO);
        } else {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.YES);
        }

        //是否购买租车套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getOrderId())) {
            userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
        } else {
            userCarDetail.setIsCarMemberCard(UserInfoResultVO.YES);
            userCarDetail.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
        }

        //租车套餐是否过期
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getMemberCardExpireTime()) && userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.YES);
        } else {
            userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
        }

        //是否绑定车辆
        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar) || StringUtils.isBlank(userCar.getSn())) {
            userCarDetail.setIsRentCar(UserInfoResultVO.NO);
        } else {
            userCarDetail.setIsRentCar(UserInfoResultVO.YES);
            userCarDetail.setCarSN(userCar.getSn());
        }

        // V3 版本
        if (CommonConstant.SWITCH_VERSION.equals(switchVersion)) {
            // 是否存在滞纳金
            boolean exitUnpaid = carRentalPackageOrderSlippageService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid());
            if (exitUnpaid) {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.YES.getCode());
            } else {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.NO.getCode());
            }
            // 是否购买租车套餐、是否过期
            CarRentalPackageMemberTermPo carRentalPackageMemberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isEmpty(carRentalPackageMemberTermPo)) {
                userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
                userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
            } else {
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(carRentalPackageMemberTermPo.getStatus()) || StringUtils.isBlank(carRentalPackageMemberTermPo.getRentalPackageOrderNo())) {
                    userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
                    userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
                } else {
                    userCarDetail.setIsCarMemberCard(UserInfoResultVO.YES);
                    userCarDetail.setMemberCardExpireTime(carRentalPackageMemberTermPo.getDueTimeTotal());
                    if (carRentalPackageMemberTermPo.getDueTimeTotal() <= System.currentTimeMillis()) {
                        userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.YES);
                    }
                }
            }

            //是否绑定车辆
            ElectricityCar car = electricityCarService.selectByUid(userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isNotEmpty(car)) {
                userCarDetail.setIsRentCar(UserInfoResultVO.YES);
                userCarDetail.setCarSN(car.getSn());
            } else {
                userCarDetail.setIsRentCar(UserInfoResultVO.NO);
            }

        }

        threadPool.execute(() -> userBatteryMemberCardPackageService.batteryMembercardTransform(userInfo.getUid()));

        log.info("HOME INFO!acquire user status info! uid={},result={}", userInfo.getUid(), JsonUtil.toJson(userInfoResult));
        return Triple.of(true, "", userInfoResult);
    }

    private void acquireCarFreeDepositResult(UserCarDeposit userCarDeposit, UserInfo userInfo, UserInfoResultVO userInfoResult) {
        if (Objects.isNull(userCarDeposit) || !Objects.equals(userCarDeposit.getDepositType(), UserCarDeposit.DEPOSIT_TYPE_FREE)) {
            return;
        }

        userInfoResult.setCarFreeApplyTime(userCarDeposit.getApplyDepositTime());

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            return;
        }

        //若免押状态为待冻结
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_INIT) || Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE)) {
            //获取车辆免押结果
            FreeDepositUserInfoVo freeDepositUserInfoVo = null;
            Triple<Boolean, String, Object> freeCarDepositOrderResult = freeDepositOrderService.acquireFreeCarDepositStatus();
            if (Boolean.TRUE.equals(freeCarDepositOrderResult.getLeft())) {
                freeDepositUserInfoVo = (FreeDepositUserInfoVo) freeCarDepositOrderResult.getRight();
            }

            userInfoResult.setCarFreeStatus(Objects.nonNull(freeDepositUserInfoVo) ? freeDepositUserInfoVo.getCarDepositAuthStatus() : null);
        } else {
            userInfoResult.setCarFreeStatus(freeDepositOrder.getAuthStatus());
        }
    }

    private void acquireBatteryFreeDepositResult(UserBatteryDeposit userBatteryDeposit, UserInfo userInfo, UserInfoResultVO userInfoResult) {
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE)) {
            return;
        }

        userInfoResult.setBatteryFreeApplyTime(userBatteryDeposit.getApplyDepositTime());

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            return;
        }

        //若免押状态为待冻结
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_INIT) || Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE)) {
            //获取电池免押结果
            FreeDepositUserInfoVo freeDepositUserInfoVo = null;
            Triple<Boolean, String, Object> freeBatteryDepositOrderResult = freeDepositOrderService.acquireUserFreeBatteryDepositStatus();
            if (Boolean.TRUE.equals(freeBatteryDepositOrderResult.getLeft())) {
                freeDepositUserInfoVo = (FreeDepositUserInfoVo) freeBatteryDepositOrderResult.getRight();
            }

            userInfoResult.setBatteryFreeStatus(Objects.nonNull(freeDepositUserInfoVo) ? freeDepositUserInfoVo.getBatteryDepositAuthStatus() : null);
        } else {
            userInfoResult.setBatteryFreeStatus(freeDepositOrder.getAuthStatus());
        }
    }

    @Deprecated
    private Integer getServiceStatus(UserInfo userInfo, FranchiseeUserInfo franchiseeUserInfo) {

////        Integer serviceStatus = userInfo.getServiceStatus();
//        Integer serviceStatus = userInfo.getAuthStatus();
//
//        if (!Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)) {
//            serviceStatus = franchiseeUserInfo.getServiceStatus();
//        }
//
////        //用户是否开通月卡
////        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
////            log.error("ELE ERROR! not found memberCard,uid={} ", userInfo.getUid());
////            serviceStatus = -1;
////        } else if (franchiseeUserInfo.getMemberCardExpireTime() < System.currentTimeMillis() || franchiseeUserInfo.getRemainingNumber() == 0) {
////            log.error("ELE ERROR! memberCard  is Expire,uid={} ", userInfo.getUid());
////            serviceStatus = -1;
////        }
//
//        return serviceStatus;


        Integer serviceStatus = null;

        //兼容UserInfo serviceStatus
        if (Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            serviceStatus = UserInfo.STATUS_IS_AUTH;
        } else {
            serviceStatus = UserInfo.STATUS_INIT;
        }

        //FranchiseeUserInfo serviceStatus
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            serviceStatus = FranchiseeUserInfo.STATUS_IS_DEPOSIT;
        }

        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            serviceStatus = FranchiseeUserInfo.STATUS_IS_BATTERY;
        }

        return serviceStatus;
    }


    @Override
    public R deleteUserInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found userInfo,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        Triple<Boolean, String, Object> result = userService.deleteNormalUser(uid);
        if (result.getLeft()) {
            return R.ok();
        }

        return R.fail(result.getMiddle(), String.valueOf(result.getRight()));
    }

    @Override
    public Integer updateByUid(UserInfo userInfo) {
        Integer result = this.userInfoMapper.updateByUid(userInfo);
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        return result;
    }

    @Override
    public Triple<Boolean, String, Object> updateRentBatteryStatus(Long uid, Integer rentStatus) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(rentStatus, UserInfo.BATTERY_RENT_STATUS_NO)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(userInfo.getUid());
            if (!Objects.isNull(battery)) {
                return Triple.of(false, "ELECTRICITY.0045", String.format("用户已绑定电池【%s】, 请先解绑！", battery.getSn()));
            }

            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, Objects.isNull(userBatteryMemberCard) ? null : batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()), serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(checkUserBatteryServiceFeeResult.getLeft())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user exit battery service fee,uid={}", userInfo.getUid());
                return Triple.of(false, "100220", "用户存在电池服务费");
            }
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryRentStatus(rentStatus);
        updateUserInfo.setTenantId(TenantContextHolder.getTenantId());
        updateUserInfo.setUpdateTime(System.currentTimeMillis());

        this.updateByUid(updateUserInfo);
        return Triple.of(true, "", null);
    }

    @Override
    public Triple<Boolean, String, Object> updateRentCarStatus(Long uid, Integer carRentStatus) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(carRentStatus, UserInfo.CAR_RENT_STATUS_YES)) {
            UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
            if (!Objects.isNull(userCar)) {
                return Triple.of(false, "ELECTRICITY.0045", String.format("用户已绑定车辆【%s】, 请先解绑！", userCar.getSn()));
            }
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarRentStatus(carRentStatus);
        updateUserInfo.setTenantId(TenantContextHolder.getTenantId());
        updateUserInfo.setUpdateTime(System.currentTimeMillis());

        this.updateByUid(updateUserInfo);
        return Triple.of(true, "", null);
    }

    @Override
    public int selectCountByFranchiseeId(Long id) {
        return userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getFranchiseeId, id));
    }

    /**
     * 检查是否有用户绑定该加盟商
     *
     * @param id
     * @param tenantId
     * @return
     */
    @Override
    public Integer isFranchiseeBindUser(Long id, Integer tenantId) {
        return userInfoMapper.isFranchiseeBindUser(id, tenantId);
    }

    @Override
    public List<UserInfo> queryByIdNumber(String idNumber) {
        return userInfoMapper.queryByIdNumber(idNumber, TenantContextHolder.getTenantId());
    }

    @Override
    public Integer verifyIdNumberExist(String idNumber, Integer tenantId) {
        return userInfoMapper.verifyIdNumberExist(idNumber, tenantId);
    }

    @Override
    public R queryDetailsBasicInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        DetailsUserInfoVo vo = new DetailsUserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        vo.setUserCertificationTime(userInfo.getCreateTime());

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        vo.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
        vo.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());

        Store store = storeService.queryByIdFromCache(userInfo.getStoreId());
        vo.setStoreName(Objects.isNull(store) ? "" : store.getName());

        UserTurnoverVo userTurnoverVo = queryUserConsumptionPay(uid);
        BeanUtils.copyProperties(userTurnoverVo, vo);

        vo.setCarRentalPackageOrderAmountTotal(carRentalPackageOrderBizService.queryAmountTotalByUid(userInfo.getTenantId(), userInfo.getUid()));
        vo.setCarRentalPackageOrderSlippageAmountTotal(carRentalPackageOrderSlippageService.selectPaySuccessAmountTotal(userInfo.getTenantId(), userInfo.getUid()));
    
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(uid);
        if(Objects.nonNull(enterpriseChannelUserVO)){
            vo.setEnterpriseChannelUserInfo(enterpriseChannelUserVO);
        }

        return R.ok(vo);
    }

    @Override
    public R queryDetailsBatteryInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        DetailsBatteryInfoVo vo = new DetailsBatteryInfoVo();
        vo.setUid(userInfo.getUid());

        //用户电池押金
        CompletableFuture<Void> queryUserBatteryDeposit = CompletableFuture.runAsync(() -> {
            queryUserBatteryDeposit(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery deposit error!", e);
            return null;
        });

        //用户会员信息
        CompletableFuture<Void> queryUserBatteryMemberCard = CompletableFuture.runAsync(() -> {
            queryUserBatteryMemberCard(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery member card error!", e);
            return null;
        });

        //用户电池信息
        CompletableFuture<Void> queryUserBattery = CompletableFuture.runAsync(() -> {
            queryUserBattery(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture
                .allOf(queryUserBatteryDeposit, queryUserBatteryMemberCard, queryUserBattery);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(vo);
    }

    @Override
    public R queryDetailsCarInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        DetailsCarInfoVo vo = new DetailsCarInfoVo();
        vo.setUid(userInfo.getUid());

        //用户车辆信息
        CompletableFuture<Void> queryUserCar = CompletableFuture.runAsync(() -> {
            queryUserCar(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS CAR INFO ERROR! query user car error!", e);
            return null;
        });

        //用户车辆押金信息
        CompletableFuture<Void> queryUserCarDeposit = CompletableFuture.runAsync(() -> {
            queryUserCarDeposit(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS CAR INFO ERROR! query user car deposit error!", e);
            return null;
        });

        //用户车辆套餐信息
        CompletableFuture<Void> queryUserCarMemberCard = CompletableFuture.runAsync(() -> {
            queryUserCarMemberCard(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS CAR INFO ERROR! query user car member card error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture
                .allOf(queryUserCar, queryUserCarDeposit, queryUserCarMemberCard);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(vo);
    }

    @Override
    public R webBindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("WEB BIND CAR ERROR ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //查找用户
        UserInfo userInfo = queryByUidFromCache(userInfoCarAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("WEB BIND CAR ERROR ERROR! not found user error uid={}", userInfoCarAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return R.ok();
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("WEB BIND CAR ERROR ERROR! user not auth! uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("WEB BIND CAR ERROR ERROR! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            log.error("WEB BIND CAR ERROR ERROR! user haven't userCar uid={}", userInfo.getUid());
            return R.failMsg("未找到用户信息!");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("WEB BIND CAR ERROR ERROR! user haven't userCarDeposit uid={}", userInfo.getUid());
            return R.failMsg("未找到用户信息!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.warn("WEB BIND CAR WARN! user haven't carMemberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        ElectricityCarModel userCarModel = electricityCarModelService
                .queryByIdFromCache(Objects.isNull(userCar.getCarModel()) ? null : userCar.getCarModel().intValue());
        if (Objects.isNull(userCarModel) || !Objects
                .equals(userCarModel.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("WEB BIND CAR ERROR ERROR! not found userCarModel! uid={}", userInfo.getUid());
            return R.fail("100258", "未找到车辆型号");
        }

        ElectricityCar electricityCar = electricityCarService
                .selectBySn(userInfoCarAddAndUpdate.getCarSn(), TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityCar)) {
            log.error("WEB BIND CAR ERROR ERROR! not found electricityCar! carSn={}",
                    userInfoCarAddAndUpdate.getCarSn());
            return R.fail("100259", "未找到车辆");
        }

        if (Objects.nonNull(electricityCar.getUid()) && !Objects.equals(electricityCar.getUid(), userInfo.getUid())) {
            log.error("WEB BIND CAR ERROR ERROR! car is bind user! carSn={} ", userInfoCarAddAndUpdate.getCarSn());
            return R.fail("100231", "该车辆已经绑定用户");
        }

        if (!Objects.equals(electricityCar.getModelId().longValue(), userCar.getCarModel())) {
            log.error("WEB BIND CAR ERROR ERROR! car is bind user! carSn={} ", userInfoCarAddAndUpdate.getCarSn());
            return R.fail("100260", "绑定车辆型号与用户车辆型号不一致");
        }

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = updateByUid(updateUserInfo);

        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(userInfo.getUid());
        updateUserCar.setCid(electricityCar.getId().longValue());
        updateUserCar.setSn(electricityCar.getSn());
        updateUserCar.setUpdateTime(System.currentTimeMillis());
        userCarService.updateByUid(updateUserCar);

        ElectricityCar userBindElectricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.nonNull(userBindElectricityCar)) {
            ElectricityCar updateElectricityCar = new ElectricityCar();
            updateElectricityCar.setId(userBindElectricityCar.getId());
            updateElectricityCar.setStatus(ElectricityCar.STATUS_NOT_RENT);
            updateElectricityCar.setUid(null);
            updateElectricityCar.setPhone(null);
            updateElectricityCar.setUserInfoId(null);
            updateElectricityCar.setUserName(null);
            updateElectricityCar.setUpdateTime(System.currentTimeMillis());
            electricityCarService.carUnBindUser(updateElectricityCar);
        }

        DbUtils.dbOperateSuccessThen(update, () -> {
            //生成租车记录
            String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, user.getUid());
            RentCarOrder rentCarOrder = new RentCarOrder();
            rentCarOrder.setOrderId(orderId);
            rentCarOrder.setStatus(RentCarOrder.STATUS_SUCCESS);
            rentCarOrder.setCarModelId(electricityCar.getModelId().longValue());
            rentCarOrder.setCarSn(electricityCar.getSn());
            rentCarOrder.setCarDeposit(userCarDeposit.getCarDeposit().doubleValue());
            rentCarOrder.setType(RentCarOrder.TYPE_RENT);
            rentCarOrder.setName(userInfo.getName());
            rentCarOrder.setPhone(userInfo.getPhone());
            rentCarOrder.setUid(user.getUid());
            rentCarOrder.setFranchiseeId(userInfo.getFranchiseeId());
            rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
            rentCarOrder.setStoreId(electricityCar.getStoreId());
            rentCarOrder.setTransactionType(RentCarOrder.TYPE_TRANSACTION_OFFLINE);
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setUpdateTime(System.currentTimeMillis());
            rentCarOrderService.insert(rentCarOrder);

            //修改车辆
            ElectricityCar updateElectricityCar = new ElectricityCar();
            updateElectricityCar.setId(electricityCar.getId());
            updateElectricityCar.setStatus(ElectricityCar.STATUS_IS_RENT);
            updateElectricityCar.setUid(userInfo.getUid());
            updateElectricityCar.setPhone(userInfo.getPhone());
            updateElectricityCar.setUserInfoId(userInfo.getId());
            updateElectricityCar.setUserName(userInfo.getName());
            updateElectricityCar.setUpdateTime(System.currentTimeMillis());
            electricityCarService.update(updateElectricityCar);

            //生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.CAR_MODEL).operateContent(
                            Objects.nonNull(userBindElectricityCar) ? EleUserOperateRecord.EDIT_CAR_CONTENT
                                    : EleUserOperateRecord.BIND_CAR_CONTENT).operateUid(user.getUid())
                    .uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).name(user.getUsername())
                    .initElectricityCarSn(Objects.nonNull(userBindElectricityCar) ? userBindElectricityCar.getSn() : "")
                    .newElectricityCarSn(electricityCar.getSn()).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserOperateRecord);

            //车辆控制 -- 用户绑定解锁
            ElectricityConfig electricityConfig = electricityConfigService
                    .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects
                    .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
                boolean result = electricityCarService
                        .retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);

                CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
                carLockCtrlHistory.setUid(userInfo.getUid());
                carLockCtrlHistory.setName(userInfo.getName());
                carLockCtrlHistory.setPhone(userInfo.getPhone());
                carLockCtrlHistory.setStatus(
                        result ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_BIND_USER_UN_LOCK);
                carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
                carLockCtrlHistory.setCarModel(electricityCar.getModel());
                carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
                carLockCtrlHistory.setCarSn(electricityCar.getSn());
                carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
                carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
                carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
                carLockCtrlHistoryService.insert(carLockCtrlHistory);
            }

            return null;
        });

        return R.ok();
    }

    @Override
    public R webUnBindCar(Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("WEB BIND CAR ERROR ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //查找用户
        UserInfo userInfo = queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("WEB BIND CAR ERROR ERROR! not found user error uid={}", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(TenantContextHolder.getTenantId(), userInfo.getTenantId())) {
            return R.ok();
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("WEB BIND CAR ERROR ERROR! user not auth! uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("WEBUNBIND ERROR! not  rent car,uid={}", uid);
            return R.fail("100261", "用户未绑定车辆");
        }

        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.isNull(electricityCar)) {
            log.error("WEBUNBIND ERROR! not  rent car,uid={}", uid);
            return R.fail("100261", "用户未绑定车辆");
        }

//        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
//        if (Objects.isNull(userCarDeposit)) {
//            log.error("WEB BIND CAR ERROR ERROR! not found userCarDeposit error uid={}", uid);
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//        }
//
//        Integer count = carRefundOrderService
//                .queryStatusByLastCreateTime(user.getUid(), TenantContextHolder.getTenantId(), electricityCar.getSn(),
//                        userCarDeposit.getOrderId());
//        if (Objects.nonNull(count) && count > 0) {
//            log.error("WEB BIND CAR ERROR ERROR! uid has runing carRefundOrder uid={}", uid);
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//        }

        ElectricityCar updateElectricityCar = new ElectricityCar();
        updateElectricityCar.setId(electricityCar.getId());
        updateElectricityCar.setStatus(ElectricityCar.STATUS_NOT_RENT);
        updateElectricityCar.setUid(null);
        updateElectricityCar.setUserName(null);
        updateElectricityCar.setUserInfoId(null);
        updateElectricityCar.setPhone(null);
        updateElectricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCarService.carUnBindUser(updateElectricityCar);

        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(userInfo.getUid());
        updateUserCar.setCid(null);
        updateUserCar.setSn("");
        updateUserCar.setUpdateTime(System.currentTimeMillis());
        userCarService.unBindingCarByUid(updateUserCar);

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        updateByUid(updateUserInfo);

        //        生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.CAR_MODEL).operateContent(EleUserOperateRecord.UN_BIND_CAR_CONTENT)
                .operateUid(user.getUid()).uid(userInfo.getUid()).name(user.getUsername())
                .initElectricityCarSn(electricityCar.getSn()).newElectricityCarSn(null)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);

        //车辆控制 -- 用户解绑加锁
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
            boolean result = electricityCarService
                    .retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);

            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory
                    .setStatus(result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
            carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_UN_BIND_USER_LOCK);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
        return R.ok();
    }

    @Override
    public R userInfoSearch(Long size, Long offset, String name) {
        List<UserInfoSearchVo> qeury = userInfoMapper
                .userInfoSearch(size, offset, name, TenantContextHolder.getTenantId());
        return R.ok(qeury);
    }

    private void queryUserCarMemberCard(DetailsCarInfoVo vo, UserInfo userInfo) {
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO)) {
            return;
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            return;
        }

        CarMemberCardOrder carMemberCardOrder = this.carMemberCardOrderService
                .queryLastPayMemberCardTimeByUid(userInfo.getUid(), userInfo.getFranchiseeId(), userInfo.getTenantId());
        if (Objects.isNull(carMemberCardOrder)) {
            return;
        }

        vo.setMemberCardType(carMemberCardOrder.getMemberCardType());
        vo.setCardName(carMemberCardOrder.getCardName());
        vo.setValidDays(carMemberCardOrder.getValidDays());
        vo.setMemberCardCreateTime(carMemberCardOrder.getCreateTime());


        vo.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());

        //计算剩余天数
        long carDays = 0;
        long memberCardExpireTime = Objects.isNull(vo.getMemberCardExpireTime()) ? 0L : vo.getMemberCardExpireTime();
        if (memberCardExpireTime > System.currentTimeMillis()) {
            long limitTime = memberCardExpireTime - System.currentTimeMillis();
            double carDayTemp = Math.ceil(limitTime / 3600000 / 24.0);
            carDays = (long) carDayTemp;
        }
        vo.setCarDays(carDays);
    }

    private void queryUserCarDeposit(DetailsCarInfoVo vo, UserInfo userInfo) {
        vo.setCarDepositStatus(userInfo.getCarDepositStatus());
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO)) {
            return;
        }

        CarDepositOrder carDepositOrder = carDepositOrderService
                .queryLastPayDepositTimeByUid(userInfo.getUid(), userInfo.getFranchiseeId(), userInfo.getTenantId());
        if (Objects.nonNull(carDepositOrder)) {
            vo.setPayDepositTime(carDepositOrder.getCreateTime());

            Store store = storeService.queryByIdFromCache(carDepositOrder.getStoreId());
            if (Objects.nonNull(store)) {
                vo.setStoreId(store.getId());
                vo.setStoreName(store.getName());
            }
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userCarDeposit)) {
            vo.setCarDeposit(userCarDeposit.getCarDeposit());
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.nonNull(franchisee)) {
            vo.setFranschiseeId(franchisee.getId());
            vo.setFranschiseeName(franchisee.getName());
        }

    }

    private void queryUserCar(DetailsCarInfoVo vo, UserInfo userInfo) {
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO)) {
            return;
        }

        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            return;
        }

        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.nonNull(electricityCar)) {
            vo.setCarSn(electricityCar.getSn());
        }

        vo.setCarModelId(Objects.isNull(userCar.getCarModel()) ? null : userCar.getCarModel().intValue());

        ElectricityCarModel electricityCarModel = electricityCarModelService
                .queryByIdFromCache(Objects.isNull(userCar.getCarModel()) ? null : userCar.getCarModel().intValue());
        if (Objects.isNull(electricityCarModel)) {
            return;
        }

        vo.setCarModelName(electricityCarModel.getName());
    }

    private void queryUserBattery(DetailsBatteryInfoVo vo, UserInfo userInfo) {

        List<String> userBatteryModels = userBatteryTypeService.selectByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(userBatteryModels)) {
            vo.setBatteryModels(batteryModelService.selectShortBatteryType(userBatteryModels, userInfo.getTenantId()));
        }

        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.isNull(electricityBattery)) {
            return;
        }

        vo.setBatterySn(electricityBattery.getSn());
        vo.setBatteryModel(electricityBattery.getModel());
        vo.setPower(electricityBattery.getPower());
    }

    private void queryUserBatteryMemberCard(DetailsBatteryInfoVo vo, UserInfo userInfo) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return;
        }

        vo.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        vo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        vo.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
        vo.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        vo.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
        vo.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        vo.setUserBatteryServiceFee(serviceFeeUserInfoService.queryUserBatteryServiceFee(userInfo));

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        vo.setCardName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
        vo.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());

        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        vo.setMemberCardCreateTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
/*
        //开始时间
        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService
                    .queryLastPayMemberCardTimeByUid(userInfo.getUid(), userInfo.getFranchiseeId(),
                            userInfo.getTenantId());
            if (Objects.nonNull(electricityMemberCardOrder)) {
                vo.setMemberCardCreateTime(electricityMemberCardOrder.getCreateTime());
            }
        }

        long carDays = 0;
        if (userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()) {
            Double carDayTemp = Math
                    .ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60
                            / 60 / 24.0);
            carDays = carDayTemp.longValue();
        }
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            carDays =
                    (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime())
                            / (24 * 60 * 60 * 1000L);
        }
        vo.setCardDays(carDays);
*/
    }

    private void queryUserBatteryDeposit(DetailsBatteryInfoVo vo, UserInfo userInfo) {
        vo.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryDeposit)) {
            vo.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        }

        if (Objects.nonNull(userBatteryDeposit) && StringUtils.isNotBlank(userBatteryDeposit.getOrderId())) {
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.nonNull(eleDepositOrder)) {
            vo.setPayDepositTime(eleDepositOrder.getCreateTime());

            Store store = storeService.queryByIdFromCache(eleDepositOrder.getStoreId());
            if (Objects.nonNull(store)) {
                vo.setStoreId(store.getId());
                vo.setStoreName(store.getName());
            }
        }
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.nonNull(franchisee)) {
            vo.setFranschiseeId(franchisee.getId());
            vo.setFranschiseeName(franchisee.getName());
        }


    }

    @Override
    public void unBindUserFranchiseeId(Long uid) {

        UserInfo userInfo = this.queryByUidFromCache(uid);
        if(Objects.isNull(userInfo)){
            return;
        }

        //租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return;
        }

        //租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return;
        }
        
        //檢查当前用户是否为企业用户，若为企业用户，则不解绑加盟商
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryEnterpriseChannelUser(uid);
        if(Objects.nonNull(enterpriseChannelUserVO)){
            return;
        }

        //若租车和租电押金都退了，则解绑用户所属加盟商
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(uid);
        updateUserInfo.setStoreId(NumberConstant.ZERO_L);
        updateUserInfo.setFranchiseeId(NumberConstant.ZERO_L);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());

        this.updateByUid(updateUserInfo);
    }

    @Slave
    @Override
    public void exportExcel(UserInfoQuery userInfoQuery, HttpServletResponse response) {
        userInfoQuery.setOffset(0L);
        userInfoQuery.setSize(2000L);

        List<UserBatteryInfoVO> userBatteryInfoVOS ;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByMemberCardExpireTime(userInfoQuery);
        } else if(Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)){
            userBatteryInfoVOS = userInfoMapper.queryListByCarMemberCardExpireTime(userInfoQuery);
        } else {
            userBatteryInfoVOS = userInfoMapper.queryListForBatteryService(userInfoQuery);
        }

        if(CollectionUtils.isEmpty(userBatteryInfoVOS)){
            throw new CustomBusinessException("用户列表为空！");
        }

        List<UserInfoExcelVO> userInfoExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (UserBatteryInfoVO userBatteryInfoVO : userBatteryInfoVOS) {
            index++;

            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userBatteryInfoVO.getUid());

            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(Objects.isNull(userBatteryInfoVO.getMemberCardId())?0:userBatteryInfoVO.getMemberCardId().intValue());

            UserInfoExcelVO excelVo = new UserInfoExcelVO();
            excelVo.setId(index);
            excelVo.setPhone(userBatteryInfoVO.getPhone());
            excelVo.setName(userBatteryInfoVO.getName());
            excelVo.setBatteryDeposit(Objects.nonNull(userBatteryDeposit) ? userBatteryDeposit.getBatteryDeposit() : BigDecimal.valueOf(0));
            excelVo.setCardName(Objects.nonNull(electricityMemberCard) ? electricityMemberCard.getName() : "");
            excelVo.setNowElectricityBatterySn(userBatteryInfoVO.getNowElectricityBatterySn());
            excelVo.setInviterUserName(
                    queryFinalInviterUserName(userBatteryInfoVO.getUid(), userBatteryInfoVO.getTenantId()));

            if (Objects.nonNull(userBatteryInfoVO.getMemberCardExpireTime()) && !Objects.equals(userBatteryInfoVO.getMemberCardExpireTime(),NumberConstant.ZERO_L)) {
                excelVo.setMemberCardExpireTime(simpleDateFormat.format(new Date(userBatteryInfoVO.getMemberCardExpireTime())));
            }

            userInfoExcelVOS.add(excelVo);
        }

        String fileName = "会员列表报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, UserInfoExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(userInfoExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }

    }

    @Deprecated
    @Override
    public void exportCarRentalExcel(UserInfoQuery userInfoQuery, HttpServletResponse response) {
        userInfoQuery.setOffset(0L);
        userInfoQuery.setSize(2000L);

        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(userInfoQuery);
        if (ObjectUtil.isEmpty(userCarRentalPackageDOList)) {
            throw new CustomBusinessException("用户租车列表信息为空！");
        }

        List<UserCarRentalInfoExcelVO> userCarRentalInfoExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for(UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList){
            UserCarRentalInfoExcelVO userCarRentalInfoExcelVO = new UserCarRentalInfoExcelVO();

            userCarRentalInfoExcelVO.setUserName(userCarRentalPackageDO.getName());
            userCarRentalInfoExcelVO.setPhone(userCarRentalPackageDO.getPhone());
            if(RentalPackageTypeEnum.CAR.getCode().equals(userCarRentalPackageDO.getPackageType())){
                userCarRentalInfoExcelVO.setPackageType(CarRentalPackageExlConstant.PACKAGE_TYPE_CAR);
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus() == 1 ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            }else if(RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())){
                userCarRentalInfoExcelVO.setPackageType(CarRentalPackageExlConstant.PACKAGE_TYPE_CAR_WITH_BATTERY);
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus() == 0 ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            }

            //设置套餐冻结状态
            if(MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())){
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_FROZE_STATUS);
            }else{
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_UN_FREEZE_STATUS);
            }

            userCarRentalInfoExcelVO.setCurrentCar(userCarRentalPackageDO.getCarModel());

            userCarRentalInfoExcelVO.setPackageExpiredTime(simpleDateFormat.format(new Date(userCarRentalPackageDO.getPackageExpiredTime())));

            //获取电池信息
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userCarRentalPackageDO.getUid());
            userCarRentalInfoExcelVO.setCurrentBattery(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());

            //获取套餐名称
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(userCarRentalPackageDO.getPackageId());
            if(Objects.nonNull(carRentalPackagePo)){
                userCarRentalInfoExcelVO.setPackageName(carRentalPackagePo.getName());
            }

            //获取保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userCarRentalPackageDO.getUid(), userCarRentalPackageDO.getPackageType());
            userCarRentalInfoExcelVO.setInsuranceStatus(Objects.isNull(insuranceUserInfoVo) ? "" : getInsuranceStatusDesc(insuranceUserInfoVo.getIsUse()));
            userCarRentalInfoExcelVO.setInsuranceExpiredTime(Objects.isNull(insuranceUserInfoVo) ? "" : simpleDateFormat.format(new Date(insuranceUserInfoVo.getInsuranceExpireTime())));

            //获取用户所属加盟商
            Franchisee franchisee = franchiseeService.queryByIdFromCache(userCarRentalPackageDO.getFranchiseeId());
            userCarRentalInfoExcelVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

            userCarRentalInfoExcelVO.setUserAuthTime(simpleDateFormat.format(new Date(userCarRentalPackageDO.getUserAuthTime())));

            userCarRentalInfoExcelVOS.add(userCarRentalInfoExcelVO);

        }

        String fileName = "会员租车列表报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, UserCarRentalInfoExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(userCarRentalInfoExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出租车报表失败！", e);
        }

    }

    private String getInsuranceStatusDesc(Integer status) {
        String result = "";
        switch (status) {
            case 0:
                result = CarRentalPackageExlConstant.PACKAGE_INSURANCE_STATUS_UNUSED;
                break;
            case 1:
                result = CarRentalPackageExlConstant.PACKAGE_INSURANCE_STATUS_USED;
                break;
            case 2:
                result = CarRentalPackageExlConstant.PACKAGE_INSURANCE_STATUS_EXPIRED;
                break;
            case 3:
                result = CarRentalPackageExlConstant.PACKAGE_INSURANCE_STATUS_INVALID;
                break;
            default:
                result = StringUtils.EMPTY;
        }
        return result;
    }

    //TODO 优化
    private String queryFinalInviterUserName(Long uid, Integer tenantId) {
        FinalJoinShareActivityHistoryVo finalJoinShareActivityHistoryVo = joinShareActivityHistoryService
                .queryFinalHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(finalJoinShareActivityHistoryVo)) {
            return finalJoinShareActivityHistoryVo.getUserName();
        }

        FinalJoinShareMoneyActivityHistoryVo finalJoinShareMoneyActivityHistoryVo = joinShareMoneyActivityHistoryService
                .queryFinalHistoryByJoinUid(uid, tenantId);
        if (Objects.nonNull(finalJoinShareMoneyActivityHistoryVo)) {
            return finalJoinShareMoneyActivityHistoryVo.getUserName();
        }

        //渠道活动
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(uid);
        if (Objects.nonNull(channelActivityHistory)) {
            UserInfo userInfo = this.queryByUidFromCache(channelActivityHistory.getInviteUid());
            return Objects.isNull(userInfo) ? "" : userInfo.getName();
        }

        //套餐返现
        InvitationActivityJoinHistory invitationActivityJoinHistory = invitationActivityJoinHistoryService.selectByJoinUid(uid);
        if(Objects.nonNull(invitationActivityJoinHistory)){
            UserInfo userInfo = this.queryByUidFromCache(invitationActivityJoinHistory.getUid());
            return Objects.isNull(userInfo) ? "" : userInfo.getName();
        }

        return null;
    }

    @Override
    @Slave
    public R queryEleList(UserInfoQuery userInfoQuery) {
        List<UserEleInfoVO> userEleInfoVOS = userInfoMapper.queryEleList(userInfoQuery);
        if (ObjectUtil.isEmpty(userEleInfoVOS)) {
            return R.ok(Collections.emptyList());
        }
        //获取用户电池套餐相关信息
        CompletableFuture<Void> queryUserBatteryMemberCardInfo = CompletableFuture.runAsync(() -> {
            userEleInfoVOS.forEach(item -> {
                //获取用户所属加盟商
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

                if (Objects.nonNull(item.getMemberCardStatus())) {
                    if(Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)){
                        //冻结
                        item.setMemberCardFreezeStatus(1);
                    }else{
                        //正常
                        item.setMemberCardFreezeStatus(0);
                    }
                }else{
                    //正常
                    item.setMemberCardFreezeStatus(0);
                }

                //获取用户当前绑定的套餐
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard)) {
                    item.setMemberCardName(batteryMemberCard.getName());
                    item.setLimitCount(batteryMemberCard.getLimitCount());
                    item.setUseCount(batteryMemberCard.getUseCount());
                }
                // 邀请人
                item.setInviterUserName(queryFinalInviterUserName(item.getUid(), item.getTenantId()));
                
                // 设置企业信息
                EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(item.getUid());
                if(Objects.nonNull(enterpriseChannelUserVO)){
                    item.setEnterpriseName(enterpriseChannelUserVO.getEnterpriseName());
                }
                
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user battery other info error!", e);
            return null;
        });

        CompletableFuture<Void> queryUserOtherInfo = CompletableFuture.runAsync(() -> {
            userEleInfoVOS.forEach(item -> {
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                item.setSn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());

                InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(item.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfoVo)) {
                    item.setIsUse(insuranceUserInfoVo.getIsUse());
                    item.setInsuranceExpireTime(insuranceUserInfoVo.getInsuranceExpireTime());
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user other info error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryMemberCardInfo, queryUserOtherInfo);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(userEleInfoVOS);
    }

    @Override
    @Slave
    public R queryEleListCount(UserInfoQuery userInfoQuery) {
        Integer count = userInfoMapper.queryEleListCount(userInfoQuery);

        return R.ok(count);
    }
}
