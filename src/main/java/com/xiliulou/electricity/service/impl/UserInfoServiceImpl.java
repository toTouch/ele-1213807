package com.xiliulou.electricity.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CarRentalPackageExlConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.EleUserOperateHistoryConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.entity.EleUserOperateHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserAuthMessage;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SignStatusEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.enterprise.RentBatteryOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.enums.merchant.MerchantInviterCanModifyEnum;
import com.xiliulou.electricity.enums.merchant.MerchantInviterSourceEnum;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.user.UnbindOpenIdRequest;
import com.xiliulou.electricity.request.user.UpdateUserPhoneRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.EleUserOperateHistoryService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.InvitationActivityJoinHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.OffLineElectricityCabinetService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserAuthMessageService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserMoveHistoryService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.DetailsBatteryInfoVo;
import com.xiliulou.electricity.vo.DetailsUserInfoVo;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.HomePageUserByWeekDayVo;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.electricity.vo.OwnMemberCardInfoVo;
import com.xiliulou.electricity.vo.UserAuthInfoVo;
import com.xiliulou.electricity.vo.UserBatteryDetail;
import com.xiliulou.electricity.vo.UserBatteryInfoVO;
import com.xiliulou.electricity.vo.UserCarDetail;
import com.xiliulou.electricity.vo.UserFrontDetectionVO;
import com.xiliulou.electricity.vo.UserInfoExcelVO;
import com.xiliulou.electricity.vo.UserInfoResultVO;
import com.xiliulou.electricity.vo.UserInfoSearchVo;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalInfoExcelVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO;
import com.xiliulou.electricity.vo.userinfo.UserEleInfoVO;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupIdAndNameVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO.FREE_OF_CHARGE;
import static com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO.UNPAID;

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
    
    @Autowired
    private CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
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
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
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
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Resource
    EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    EleUserOperateHistoryService eleUserOperateHistoryService;
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private MerchantService merchantService;
    
    @Autowired
    CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    @Autowired
    EleUserEsignRecordService eleUserEsignRecordService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Autowired
    private OverdueUserRemarkPublish overdueUserRemarkPublish;
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
        List<String> ordersOn = userCarRentalPackageDOList.stream().filter(f -> f.getCarDepositStatus() == 1 || f.getCarBatteryDepositStatus() == 0)
                .map(UserCarRentalPackageDO::getDepositOrderNo).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        //t_car_rental_package_deposit_pay
        Map<String, Integer> orderMapPayType = carRentalPackageDepositPayService.selectPayTypeByOrders(ordersOn);
        //处理租车/车店一体押金状态 和 当前套餐冻结状态
        List<UserCarRentalPackageVO> userCarRentalPackageVOList = Lists.newArrayList();
        for (UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList) {
            UserCarRentalPackageVO userCarRentalPackageVO = new UserCarRentalPackageVO();
            BeanUtils.copyProperties(userCarRentalPackageDO, userCarRentalPackageVO);
            //如果用户尚未购买任何套餐则显示未缴纳
            userCarRentalPackageVO.setDepositStatus(UNPAID);
            if (RentalPackageTypeEnum.CAR.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalPackageVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus());
            } else if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalPackageVO.setDepositStatus(convertCarBatteryDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus()));
            }
            
            if (orderMapPayType.containsKey(userCarRentalPackageDO.getDepositOrderNo()) && orderMapPayType.get(userCarRentalPackageDO.getDepositOrderNo()) == 3) {
                userCarRentalPackageVO.setDepositStatus(FREE_OF_CHARGE);
            }
            
            if (MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())) {
                userCarRentalPackageVO.setPackageFreezeStatus(0);
            } else {
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
                item.setInviterUserName(queryFinalInviterUserName(item.getUid()));
                
            });
        }, threadPool).exceptionally(e -> {
            log.error("Query user battery info error for car rental.", e);
            return null;
        });
        
        //获取用户租车保险相关信息
        CompletableFuture<Void> queryUserInsuranceInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {
                
                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(item.getPackageId());
                if (Objects.nonNull(carRentalPackagePo)) {
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
    
        CompletableFuture<Void> queryUserGroupInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {
                List<UserInfoGroupNamesBO> namesBOList = userInfoGroupDetailService.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(item.getUid()).build());
                List<UserInfoGroupIdAndNameVO> groupVoList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(namesBOList)) {
                    groupVoList = namesBOList.stream().map(bo -> UserInfoGroupIdAndNameVO.builder().id(bo.getGroupId()).name(bo.getGroupName()).groupNo(bo.getGroupNo()).build()).collect(Collectors.toList());
                }
            
                item.setGroupList(CollectionUtils.isEmpty(groupVoList) ? Collections.emptyList() : groupVoList);
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user group info error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryInfo, queryUserInsuranceInfo, queryUserGroupInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for car rental.", e);
        }
        
        //处理payCount为空（押金已退），根据uid查詢套餐列表
        List<Long> uidList = userCarRentalPackageVOList.stream().map(UserCarRentalPackageVO::getUid).collect(Collectors.toList());
        List<CarRentalPackageMemberTermPo> packageDOList = carRentalPackageMemberTermService.listUserPayCountByUidList(uidList);
        
        Map<Long, Integer> payCountMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(packageDOList)) {
            payCountMap = packageDOList.stream().collect(Collectors.toMap(CarRentalPackageMemberTermPo::getUid, CarRentalPackageMemberTermPo::getPayCount, (k1, k2) -> k1));
        }
        
        //payCount为空时，进行处理
        Map<Long, Integer> finalPayCountMap = payCountMap;
        userCarRentalPackageVOList.forEach(userCarRentalPackageVO -> {
            if (Objects.isNull(userCarRentalPackageVO.getPayCount()) && finalPayCountMap.containsKey(userCarRentalPackageVO.getUid())) {
                userCarRentalPackageVO.setPayCount(finalPayCountMap.get(userCarRentalPackageVO.getUid()));
            }
        });
        
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
     *
     * @param depositStatus
     * @return
     */
    public static Integer convertCarBatteryDepositStatus(Integer depositStatus) {
        Integer status = 0;
        
        if (Objects.isNull(depositStatus)) {
            return status;
        }
        
        if (YesNoEnum.YES.getCode().equals(depositStatus)) {
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
            if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(),
                    EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)) {
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
    
    @Slave
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
        Integer count;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            count = userInfoMapper.queryCountByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
            count = userInfoMapper.queryCountByCarMemberCardExpireTime(userInfoQuery);
        } else {
            count = userInfoMapper.queryCountForBatteryService(userInfoQuery);
        }
        return R.ok(count);
    }
    
    @Slave
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
            log.warn("WEBBIND WARN! user not auth! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        //判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(oldUserInfo.getUid());
        if (!(Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(oldUserInfo.getCarBatteryDepositStatus(),
                YesNoEnum.YES.getCode()))) {
            log.warn("WEBBIND ERROR WARN! not pay deposit! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        Integer orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();
        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //判断电池滞纳金
            
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(oldUserInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("WEBBIND ERROR WARN! user haven't memberCard uid={}", oldUserInfo.getUid());
                return R.fail("100210", "用户未开通套餐");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("WEBBIND ERROR WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐停卡审核中");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("WEBBIND ERROR WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐已暂停");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("WEBBIND ERROR WARN! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return R.fail("ELECTRICITY.00121", "套餐不存在");
            }
            
            if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())) {
                orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
            }
            
            //判断用户电池服务费
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(oldUserInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("WEBBIND ERROR ERROR! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费");
            }
            
            if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                    && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                log.warn("WEBBIND ERROR WARN! battery memberCard is Expire,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.0023", "套餐已过期");
            }
    
            //判断车电关联是否可租电
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(oldUserInfo.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
                if (Objects.equals(oldUserInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                    try {
                        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(oldUserInfo.getTenantId(), oldUserInfo.getUid())) {
                            log.error("WEBBIND ERROR WARN! user car memberCard expire,uid={}", oldUserInfo.getUid());
                            return R.fail("100233", "租车套餐已过期");
                        }
                    } catch (Exception e) {
                        log.error("WEBBIND ERROR WARN! acquire car membercard expire result fail,uid={}", oldUserInfo.getUid(), e);
                    }
                }
            }
        } else {
            carRentalPackageMemberTermBizService.verifyMemberSwapBattery(oldUserInfo.getTenantId(), oldUserInfo.getUid());
        }
        
        //判断电池是否存在，或者已经被绑定
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            log.warn("WEBBIND ERROR WARN! not found Battery! batteryName={}", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(), userInfoBatteryAddAndUpdate.getUid())) {
            log.warn("WEBBIND ERROR WARN! battery is bind user! sn={} ", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            return R.fail("100019", "该电池已经绑定用户");
        }
        
        if (!Objects.equals(oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId())) {
            log.warn("WEBBIND ERROR WARN! franchiseeId not equals,userFranchiseeId={},batteryFranchiseeId={}", oldUserInfo.getFranchiseeId(),
                    oldElectricityBattery.getFranchiseeId());
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
            if (!Objects.equals(isBindElectricityBattery.getSn(), userInfoBatteryAddAndUpdate.getInitElectricityBatterySn())) {
                ElectricityBattery notBindOldElectricityBattery = new ElectricityBattery();
                notBindOldElectricityBattery.setId(isBindElectricityBattery.getId());
                notBindOldElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_ADMIN_UNBIND);
                notBindOldElectricityBattery.setElectricityCabinetId(null);
                notBindOldElectricityBattery.setElectricityCabinetName(null);
                notBindOldElectricityBattery.setUid(null);
                notBindOldElectricityBattery.setBorrowExpireTime(null);
                notBindOldElectricityBattery.setUpdateTime(System.currentTimeMillis());
                notBindOldElectricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(notBindOldElectricityBattery);
                
                // 添加退电记录
                RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
                rentBatteryOrder.setUid(oldUserInfo.getUid());
                rentBatteryOrder.setName(oldUserInfo.getName());
                rentBatteryOrder.setPhone(oldUserInfo.getPhone());
                rentBatteryOrder.setElectricityBatterySn(isBindElectricityBattery.getSn());
                rentBatteryOrder.setBatteryDeposit(Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
                rentBatteryOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_BATTERY, user.getUid()));
                rentBatteryOrder.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
                rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
                rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
                rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
                rentBatteryOrder.setCreateTime(System.currentTimeMillis());
                rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_UNBIND);
                rentBatteryOrder.setOrderType(orderType);
                rentBatteryOrderService.insert(rentBatteryOrder);
            }
        }
        
        Integer finalOrderType = orderType;
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
            rentBatteryOrder.setOrderType(finalOrderType);
            rentBatteryOrderService.insert(rentBatteryOrder);
            
            //生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.BATTERY_MODEL).operateContent(
                            Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.EDIT_TYPE) ? EleUserOperateRecord.EDIT_BATTERY_CONTENT
                                    : EleUserOperateRecord.BIND_BATTERY_CONTENT).operateUid(user.getUid()).uid(oldUserInfo.getUid()).tenantId(TenantContextHolder.getTenantId())
                    .name(user.getUsername()).initElectricityBatterySn(Objects.nonNull(isBindElectricityBattery) ? isBindElectricityBattery.getSn() : "")
                    .nowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn()).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            
            //判断是单电的电池操作还是车电一体的电池操作
            if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            } else {
                eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            }
            
            eleUserOperateRecordService.insert(eleUserOperateRecord);
            
            //修改电池状态
            ElectricityBattery electricityBattery = new ElectricityBattery();
            electricityBattery.setId(oldElectricityBattery.getId());
            electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
            electricityBattery.setElectricityCabinetId(null);
            electricityBattery.setElectricityCabinetName(null);
            electricityBattery.setUid(userInfoBatteryAddAndUpdate.getUid());
            electricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBattery.setBindTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryUser(electricityBattery);
            
            enterpriseRentRecordService.saveEnterpriseRentRecord(rentBatteryOrder.getUid());
            
            //记录企业用户租电池记录
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode(), rentBatteryOrder);
            
            try {
                //发送操作记录
                //判断没有发送实际的电池变更则不记录
                if (!Objects.isNull(isBindElectricityBattery) && Objects.equals(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn(), isBindElectricityBattery.getSn())) {
                    return null;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("username", oldUserInfo.getName());
                map.put("phone", oldUserInfo.getPhone());
                map.put("editType", userInfoBatteryAddAndUpdate.getEdiType());
                map.put("batterySN", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
                operateRecordUtil.record(Objects.isNull(isBindElectricityBattery) ? null : MapUtil.of("batterySN", isBindElectricityBattery.getSn()), map);
            } catch (Throwable e) {
                log.error("Recording user operation records failed because:", e);
            }
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
            log.warn("WEBUNBIND WARN! user not auth,uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(oldUserInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("WEBUNBIND WARN! not  rent battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        
        if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(oldUserInfo.getUid());
        if (Objects.isNull(oldElectricityBattery)) {
            log.warn("WEBUNBIND WARN! not found user bind battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        
        Integer orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();
        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //判断电池滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(oldUserInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("WEBBIND WARN! user haven't memberCard uid={}", oldUserInfo.getUid());
                return R.fail("100210", "用户未开通套餐");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("WEBBIND WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐停卡审核中");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("WEBBIND WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐已暂停");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("WEBBIND WARN! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                return R.fail("ELECTRICITY.00121", "套餐不存在");
            }
            
            //根据套餐类型，设置租退订单类型
            if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())) {
                orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
            }
            
            //判断用户电池服务费
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(oldUserInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("WEBBIND WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费");
            }
        } else {
            //判断车电一体滞纳金
            if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(oldUserInfo.getTenantId(), oldUserInfo.getUid()))) {
                log.warn("ORDER WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("300001", "存在滞纳金，请先缴纳");
            }
        }
        
        //解绑电池
        ElectricityBattery electricityBattery = new ElectricityBattery();
        electricityBattery.setId(oldElectricityBattery.getId());
        electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_ADMIN_UNBIND);
        electricityBattery.setElectricityCabinetId(null);
        electricityBattery.setElectricityCabinetName(null);
        electricityBattery.setUid(null);
        electricityBattery.setBorrowExpireTime(null);
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBattery.setBindTime(System.currentTimeMillis());
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
        rentBatteryOrder.setOrderType(orderType);
        rentBatteryOrderService.insert(rentBatteryOrder);
        
        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.BATTERY_MODEL)
                .operateContent(EleUserOperateRecord.UN_BIND_BATTERY_CONTENT).operateUid(user.getUid()).uid(oldUserInfo.getUid()).name(user.getUsername())
                .initElectricityBatterySn(oldElectricityBattery.getSn()).nowElectricityBatterySn(null).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        OverdueType type = null;
        //判断是单电的电池操作还是车电一体的电池操作
        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            type = OverdueType.BATTERY;
        } else {
            eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            type = OverdueType.CAR;
        }
        
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        enterpriseRentRecordService.saveEnterpriseReturnRecord(rentBatteryOrder.getUid());
        
        //记录企业用户还电池记录
        enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode(), rentBatteryOrder);
        //清除逾期用户备注
        overdueUserRemarkPublish.publish(uid,type.getCode(), tenantId);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("username", oldUserInfo.getName());
            map.put("phone", oldUserInfo.getPhone());
            map.put("batterySN", oldElectricityBattery.getSn());
            operateRecordUtil.record(null, map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }
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
    
    @Slave
    @Override
    public R queryUserBelongFranchisee(Long franchiseeId, Integer tenantId) {
        return R.ok(franchiseeService.queryByIdAndTenantId(franchiseeId, tenantId));
    }

    @Slave
    @Override
    public UserInfo queryUserInfoByPhone(String phone, Integer tenantId) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }
    
    @Override
    public UserInfo queryUserByPhoneAndFranchisee(String phone, Integer franchiseeId, Integer tenantId) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId).eq(UserInfo::getFranchiseeId, franchiseeId)
                        .eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
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
    public Triple<Boolean, String, Object> selectUserInfoStatus() {
        UserInfoResultVO userInfoResult = new UserInfoResultVO();
        
        if (Objects.isNull(SecurityUtils.getUid())) {
            log.warn("ELE WARN! not found user ");
            return Triple.of(false, "100001", userInfoResult);
        }
        
        UserInfo userInfo = this.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN! not found userInfo! uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100001", userInfoResult);
        }
        
        //电池订单类型为免押
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        acquireBatteryFreeDepositResult(userBatteryDeposit, userInfo, userInfoResult);
        
        //车辆订单类型为免押
//        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(SecurityUtils.getUid());
//        acquireCarFreeDepositResult(userCarDeposit, userInfo, userInfoResult);
        
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
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            }
        } else {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
        }
        
        //是否购买租电池套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                UserBatteryMemberCard.SEND_REMAINING_NUMBER) //如果送的次数卡  首页提示没有购买套餐
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
        } else {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
            userBatteryDetail.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        }
        
        //套餐是否过期(前端要兼容旧代码  不能删除)
        if (!Objects.isNull(userBatteryMemberCard) && !Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && !Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
        }
        
        //是否购买车电一体套餐
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(userInfo.getTenantId(), userInfo.getUid());
            if (Objects.nonNull(memberTermEntity) && Objects.equals(memberTermEntity.getRentalPackageType(), RentalPackageTypeEnum.CAR_BATTERY.getCode()) && Objects.nonNull(
                    memberTermEntity.getRentalPackageId())) {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
                userBatteryDetail.setMemberCardExpireTime(memberTermEntity.getDueTimeTotal());
            } else {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
            }
            
            //车电一体套餐是否过期
            if (Objects.nonNull(memberTermEntity) && Objects.nonNull(memberTermEntity.getDueTimeTotal()) && memberTermEntity.getDueTimeTotal() < System.currentTimeMillis()) {
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
            } else {
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
        if (Objects.nonNull(userBatteryMemberCard)) {
            Triple<Boolean, Integer, BigDecimal> batteryServiceFeeTriple = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()), serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
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
        if (Objects.isNull(userBatteryMemberCard) || StringUtils.isBlank(userBatteryMemberCard.getOrderId())) {
            if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
            } else {
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
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO) && Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.NO.getCode())) {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.NO);
        } else {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.YES);
        }
        
//        //是否购买租车套餐
//        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
//        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getOrderId())) {
//            userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
//        } else {
//            userCarDetail.setIsCarMemberCard(UserInfoResultVO.YES);
//            userCarDetail.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
//        }
//
//        //租车套餐是否过期
//        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getMemberCardExpireTime())
//                && userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
//            userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.YES);
//        } else {
//            userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
//        }
//
//        //是否绑定车辆
//        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
//        if (Objects.isNull(userCar) || StringUtils.isBlank(userCar.getSn())) {
//            userCarDetail.setIsRentCar(UserInfoResultVO.NO);
//        } else {
//            userCarDetail.setIsRentCar(UserInfoResultVO.YES);
//            userCarDetail.setCarSN(userCar.getSn());
//        }
        
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
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(carRentalPackageMemberTermPo.getStatus()) || StringUtils.isBlank(
                        carRentalPackageMemberTermPo.getRentalPackageOrderNo())) {
                    userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
                    userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
                } else {
                    userCarDetail.setIsCarMemberCard(UserInfoResultVO.YES);
                    userCarDetail.setMemberCardExpireTime(carRentalPackageMemberTermPo.getDueTimeTotal());
                    userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
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
        
        return Triple.of(true, "", userInfoResult);
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
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_INIT) || Objects.equals(freeDepositOrder.getAuthStatus(),
                FreeDepositOrder.AUTH_PENDING_FREEZE)) {
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
    
    @Override
    public R deleteUserInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN! not found userInfo,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        Triple<Boolean, String, Object> result = userService.deleteNormalUser(uid);
        if (result.getLeft()) {
            operateRecordUtil.record(null, userInfo);
            return R.ok();
        }
        
        return R.fail(result.getMiddle(), String.valueOf(result.getRight()));
    }
    
    @Override
    public Integer updateByUid(UserInfo userInfo) {
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
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
            Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    Objects.isNull(userBatteryMemberCard) ? null : batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()),
                    serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
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
        //清除逾期用户备注
        if (Objects.equals(rentStatus, UserInfo.BATTERY_RENT_STATUS_NO)){
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(),YesNoEnum.YES.getCode())) {
                overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.CAR.getCode(),TenantContextHolder.getTenantId());
            }else {
                overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.BATTERY.getCode(),TenantContextHolder.getTenantId());
            }
        }
        return Triple.of(true, "", null);
    }
    
    @Slave
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
    @Slave
    @Override
    public Integer isFranchiseeBindUser(Long id, Integer tenantId) {
        return userInfoMapper.isFranchiseeBindUser(id, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfo> queryByIdNumber(String idNumber) {
        return userInfoMapper.queryByIdNumber(idNumber, TenantContextHolder.getTenantId());
    }
    
    @Slave
    @Override
    public Integer verifyIdNumberExist(String idNumber, Integer tenantId) {
        return userInfoMapper.verifyIdNumberExist(idNumber, tenantId);
    }
    
    @Slave
    @Override
    public R queryDetailsBasicInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromDb(uid);
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), tenantId)) {
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
        
        vo.setCarRentalPackageOrderAmountTotal(carRentalPackageOrderBizService.queryAmountTotalByUid(userInfo.getTenantId(), userInfo.getUid()));
        vo.setCarRentalPackageOrderSlippageAmountTotal(carRentalPackageOrderSlippageService.selectPaySuccessAmountTotal(userInfo.getTenantId(), userInfo.getUid()));
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(uid);
        if (Objects.nonNull(enterpriseChannelUserVO)) {
            vo.setEnterpriseChannelUserInfo(enterpriseChannelUserVO);
        }
        
        // 设置电子签名信息
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryUserEsignRecordFromDB(userInfo.getUid(), Long.valueOf(TenantContextHolder.getTenantId()));
        if (Objects.nonNull(eleUserEsignRecord)) {
            vo.setSignFlowId(eleUserEsignRecord.getSignFlowId());
            vo.setSignFinishStatus(Objects.equals(1, eleUserEsignRecord.getSignFinishStatus()) ? SignStatusEnum.SIGNED_COMPLETED.getCode() : SignStatusEnum.SIGNED_INCOMPLETE.getCode());
        }else {
            vo.setSignFinishStatus(SignStatusEnum.UNSIGNED.getCode());
        }
        
        // 根据openId判断是否可解绑微信
        UserOauthBind userOauthBind = userOauthBindService.selectByUidAndPhone(vo.getPhone(), uid, tenantId);
        if (Objects.nonNull(userOauthBind) && StringUtils.isNotBlank(userOauthBind.getThirdId())) {
            vo.setBindWX(UserOauthBind.STATUS_BIND_VX);
        } else {
            vo.setBindWX(UserOauthBind.STATUS_UN_BIND_VX);
        }
        
        // 邀请人是否可被修改
        Integer inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_USER_FOR_VO.getCode();
        MerchantInviterVO merchantInviterVO = userInfoExtraService.querySuccessInviter(uid);
        if (Objects.isNull(merchantInviterVO)) {
            vo.setCanModifyInviter(MerchantInviterCanModifyEnum.MERCHANT_INVITER_CAN_NOT_MODIFY.getCode());
        } else {
            vo.setCanModifyInviter(MerchantInviterCanModifyEnum.MERCHANT_INVITER_CAN_MODIFY.getCode());
            if (Objects.equals(merchantInviterVO.getInviterSource(), UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
                inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_MERCHANT_FOR_VO.getCode();
            }
        }
        
        // 邀请人名称
        vo.setInviterName(queryFinalInviterUserName(uid));
        //邀请人来源
        vo.setInviterSource(inviterSource);
    
        return R.ok(vo);
    }
    
    @Override
    public R unbindOpenId(UnbindOpenIdRequest unbindOpenIdRequest) {
        Long uid = unbindOpenIdRequest.getUid();
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, TenantContextHolder.getTenantId());
        if (Objects.nonNull(userOauthBind) && Objects.nonNull(userOauthBind.getThirdId())) {
            // 解绑微信成功后 强制用户重新登录
            List<UserOauthBind> userOauthBinds = userOauthBindService.queryListByUid(uid);
            if (DataUtil.collectionIsUsable(userOauthBinds)) {
                clearUserOauthBindToken(userOauthBinds, CacheConstant.CLIENT_ID);
            }
            DbUtils.dbOperateSuccessThenHandleCache(
                    userOauthBindService.updateOpenIdByUid(StringUtils.EMPTY, UserOauthBind.STATUS_UN_BIND, userOauthBind.getUid(), TenantContextHolder.getTenantId()), i -> {
                        // 添加解绑操作记录
                        EleUserOperateHistory eleUserOperateHistory = buildEleUserOperateHistory(userInfo, EleUserOperateHistoryConstant.OPERATE_CONTENT_UNBIND_VX,
                                EleUserOperateHistoryConstant.UNBIND_VX_OLD_OPERATION, EleUserOperateHistoryConstant.UNBIND_VX_NEW_OPERATION);
                        eleUserOperateHistoryService.asyncHandleEleUserOperateHistory(eleUserOperateHistory);
                    });
            
        }
        Map<String, Object> map = new HashMap<>();
        map.put("username", userInfo.getName());
        map.put("phone", userInfo.getPhone());
        operateRecordUtil.record(null, map);
        return R.ok();
    }
    
    @Override
    public void clearUserOauthBindToken(List<UserOauthBind> userOauthBinds, String clientId) {
        if (StringUtils.isBlank(clientId)) {
            clientId = CacheConstant.CLIENT_ID;
        }
        
        String finalClientId = clientId;
        userOauthBinds.forEach(e -> {
            String thirdId = e.getThirdId();
            if (CacheConstant.MERCHANT_CLIENT_ID.equals(finalClientId)) {
                thirdId = e.getThirdId() + e.getUid();
            }
            List<String> tokens = redisService.getWithList(TokenConstant.CACHE_LOGIN_TOKEN_LIST_KEY + finalClientId + e.getTenantId() + ":" + thirdId, String.class);
            if (DataUtil.collectionIsUsable(tokens)) {
                tokens.forEach(s -> {
                    redisService.delete(TokenConstant.CACHE_LOGIN_TOKEN_KEY + finalClientId + s);
                });
            }
        });
        
    }
    
    private EleUserOperateHistory buildEleUserOperateHistory(UserInfo userInfo, Integer operateContent, String oldOperateInfo, String newOperateInfo) {
        return EleUserOperateHistory.builder().operateType(EleUserOperateHistoryConstant.OPERATE_TYPE_USER).operateModel(EleUserOperateHistoryConstant.OPERATE_MODEL_USER_ACCOUNT)
                .operateContent(operateContent).oldOperateInfo(oldOperateInfo).newOperateInfo(newOperateInfo).uid(userInfo.getUid())
                .operatorName(Objects.nonNull(SecurityUtils.getUserInfo()) ? SecurityUtils.getUserInfo().getUsername() : StringUtils.EMPTY)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
    }
    
    @Override
    public R updateUserPhone(UpdateUserPhoneRequest updateUserPhoneRequest) {
        Long uid = updateUserPhoneRequest.getUid();
        String phone = updateUserPhoneRequest.getPhone();
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        String oldPhone = userInfo.getPhone();
        
        // 更换手机号的前提：新手机号在系统中不存在
        if (StringUtils.equals(userInfo.getPhone(), phone)) {
            return R.fail("100566", "新手机号与原手机号一致，请重新输入");
        }
        
        UserInfo updatePhone = this.queryUserInfoByPhone(phone, TenantContextHolder.getTenantId());
        if (Objects.nonNull(updatePhone)) {
            return R.fail("100565", "手机号已被使用，请重新输入");
        }
        
        // 更新用戶
        DbUtils.dbOperateSuccessThenHandleCache(userInfoMapper.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone, System.currentTimeMillis()), i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        });
        
        User user = userService.queryByUidFromCache(uid);
        DbUtils.dbOperateSuccessThenHandleCache(userService.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone), i -> {
            if (Objects.nonNull(user)) {
                redisService.delete(CacheConstant.CACHE_USER_UID + uid);
                redisService.delete(CacheConstant.CACHE_USER_PHONE + TenantContextHolder.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
            }
        });
        
        // 更新成功后 强制用户重新登录
        List<UserOauthBind> userOauthBinds = userOauthBindService.queryListByUid(uid);
        if (DataUtil.collectionIsUsable(userOauthBinds)) {
            clearUserOauthBindToken(userOauthBinds, CacheConstant.CLIENT_ID);
        }
        
        userOauthBindService.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone);
        
        // 添加更換手机号操作记录
        EleUserOperateHistory eleUserOperateHistory = buildEleUserOperateHistory(userInfo, EleUserOperateHistoryConstant.OPERATE_CONTENT_UPDATE_PHONE, userInfo.getPhone(), phone);
        eleUserOperateHistoryService.asyncHandleEleUserOperateHistory(eleUserOperateHistory);
        eleUserOperateHistoryService.asyncHandleUpdateUserPhone(TenantContextHolder.getTenantId(), uid, phone, oldPhone);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("username", userInfo.getName());
            map.put("phone", phone);
            operateRecordUtil.record(MapUtil.of("phone", oldPhone), map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }
        return R.ok();
    }
    
    @Slave
    @Override
    public R queryDetailsBatteryInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromDb(uid);
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
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryDeposit, queryUserBatteryMemberCard, queryUserBattery);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }
        
        return R.ok(vo);
    }
    
    @Slave
    @Override
    public R userInfoSearch(Long size, Long offset, String name) {
        List<UserInfoSearchVo> qeury = userInfoMapper.userInfoSearch(size, offset, name, TenantContextHolder.getTenantId());
        return R.ok(qeury);
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
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return;
        }
        
        vo.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        vo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        vo.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
        vo.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        vo.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
        vo.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
//        vo.setUserBatteryServiceFee(serviceFeeUserInfoService.queryUserBatteryServiceFee(userInfo));
        
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
        if (Objects.isNull(userInfo)) {
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
        if (Objects.nonNull(enterpriseChannelUserVO)) {
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
        
        List<UserBatteryInfoVO> userBatteryInfoVOS;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(), UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByCarMemberCardExpireTime(userInfoQuery);
        } else {
            userBatteryInfoVOS = userInfoMapper.queryListForBatteryService(userInfoQuery);
        }
        
        if (CollectionUtils.isEmpty(userBatteryInfoVOS)) {
            throw new CustomBusinessException("用户列表为空！");
        }
        
        List<UserInfoExcelVO> userInfoExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (UserBatteryInfoVO userBatteryInfoVO : userBatteryInfoVOS) {
            index++;
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userBatteryInfoVO.getUid());
            
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(
                    Objects.isNull(userBatteryInfoVO.getMemberCardId()) ? 0 : userBatteryInfoVO.getMemberCardId().intValue());
            
            UserInfoExcelVO excelVo = new UserInfoExcelVO();
            excelVo.setId(index);
            excelVo.setPhone(userBatteryInfoVO.getPhone());
            excelVo.setName(userBatteryInfoVO.getName());
            excelVo.setBatteryDeposit(Objects.nonNull(userBatteryDeposit) ? userBatteryDeposit.getBatteryDeposit() : BigDecimal.valueOf(0));
            excelVo.setCardName(Objects.nonNull(electricityMemberCard) ? electricityMemberCard.getName() : "");
            excelVo.setNowElectricityBatterySn(userBatteryInfoVO.getNowElectricityBatterySn());
            excelVo.setInviterUserName(queryFinalInviterUserName(userBatteryInfoVO.getUid()));
            
            if (Objects.nonNull(userBatteryInfoVO.getMemberCardExpireTime()) && !Objects.equals(userBatteryInfoVO.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
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
        
        for (UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList) {
            UserCarRentalInfoExcelVO userCarRentalInfoExcelVO = new UserCarRentalInfoExcelVO();
            
            userCarRentalInfoExcelVO.setUserName(userCarRentalPackageDO.getName());
            userCarRentalInfoExcelVO.setPhone(userCarRentalPackageDO.getPhone());
            if (RentalPackageTypeEnum.CAR.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalInfoExcelVO.setPackageType(CarRentalPackageExlConstant.PACKAGE_TYPE_CAR);
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus() == 1 ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS
                        : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            } else if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalInfoExcelVO.setPackageType(CarRentalPackageExlConstant.PACKAGE_TYPE_CAR_WITH_BATTERY);
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus() == 0 ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS
                        : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            }
            
            //设置套餐冻结状态
            if (MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())) {
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_FROZE_STATUS);
            } else {
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_UN_FREEZE_STATUS);
            }
            
            userCarRentalInfoExcelVO.setCurrentCar(userCarRentalPackageDO.getCarModel());
            
            userCarRentalInfoExcelVO.setPackageExpiredTime(simpleDateFormat.format(new Date(userCarRentalPackageDO.getPackageExpiredTime())));
            
            //获取电池信息
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userCarRentalPackageDO.getUid());
            userCarRentalInfoExcelVO.setCurrentBattery(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
            
            //获取套餐名称
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(userCarRentalPackageDO.getPackageId());
            if (Objects.nonNull(carRentalPackagePo)) {
                userCarRentalInfoExcelVO.setPackageName(carRentalPackagePo.getName());
            }
            
            //获取保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userCarRentalPackageDO.getUid(),
                    userCarRentalPackageDO.getPackageType());
            userCarRentalInfoExcelVO.setInsuranceStatus(Objects.isNull(insuranceUserInfoVo) ? "" : getInsuranceStatusDesc(insuranceUserInfoVo.getIsUse()));
            userCarRentalInfoExcelVO.setInsuranceExpiredTime(
                    Objects.isNull(insuranceUserInfoVo) ? "" : simpleDateFormat.format(new Date(insuranceUserInfoVo.getInsuranceExpireTime())));
            
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
            EasyExcel.write(outputStream, UserCarRentalInfoExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    .doWrite(userCarRentalInfoExcelVOS);
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
    
    @Override
    @Slave
    public String queryFinalInviterUserName(Long uid) {
        String inviterName = StringUtils.EMPTY;
        
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.nonNull(userInfoExtra)) {
            Long inviterUid = userInfoExtra.getInviterUid();
            Integer activitySource = userInfoExtra.getActivitySource();
            
            if (Objects.nonNull(inviterUid) && !Objects.equals(inviterUid, NumberConstant.ZERO_L) && Objects.nonNull(activitySource) && !Objects.equals(activitySource,
                    NumberConstant.ZERO)) {
                //商户活动的邀请人来源于user
                if (Objects.equals(activitySource, UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
                    Merchant merchant = merchantService.queryByUid(inviterUid);
                    if (Objects.nonNull(merchant)) {
                        inviterName = merchant.getName();
                    }
                } else {
                    UserInfo userInfo = this.queryByUidFromCache(inviterUid);
                    if (Objects.nonNull(userInfo)) {
                        inviterName = userInfo.getName();
                    }
                }
            }
        }
        
        return inviterName;
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
                    if (Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                        //冻结
                        item.setMemberCardFreezeStatus(1);
                    } else {
                        //正常
                        item.setMemberCardFreezeStatus(0);
                    }
                } else {
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
                item.setInviterUserName(queryFinalInviterUserName(item.getUid()));
                
                // 设置企业信息
                EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(item.getUid());
                if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
                    item.setEnterpriseName(enterpriseChannelUserVO.getEnterpriseName());
                }
                
                threadPool.execute(() -> userBatteryMemberCardPackageService.batteryMembercardTransform(item.getUid()));
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
                
                // 设置已缴纳押金的用户的具体状态，为实缴还是免押，实缴：1  免押：2
                if (Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, item.getBatteryDepositStatus())) {
                    UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(item.getUid());
                    if (Objects.nonNull(userBatteryDeposit)) {
                        
                        item.setBatteryDepositStatus(Objects.equals(0, userBatteryDeposit.getDepositType()) ? 1 : 2);
                    }
                }
                
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user other info error!", e);
            return null;
        });
    
        CompletableFuture<Void> queryUserGroupInfo = CompletableFuture.runAsync(() -> {
            userEleInfoVOS.forEach(item -> {
                List<UserInfoGroupNamesBO> namesBOList = userInfoGroupDetailService.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(item.getUid()).build());
                List<UserInfoGroupIdAndNameVO> groupVoList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(namesBOList)) {
                    groupVoList = namesBOList.stream().map(bo -> UserInfoGroupIdAndNameVO.builder().id(bo.getGroupId()).name(bo.getGroupName()).groupNo(bo.getGroupNo()).build()).collect(Collectors.toList());
                }
    
                item.setGroupList(CollectionUtils.isEmpty(groupVoList) ? Collections.emptyList() : groupVoList);
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user group info error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryMemberCardInfo, queryUserOtherInfo, queryUserGroupInfo);
        
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
    
    @Override
    public void deleteCache(Long uid) {
        redisService.delete(CacheConstant.CACHE_USER_INFO + uid);
    }
    
    @Slave
    @Override
    public List<UserInfo> listByUidList(List<Long> uidList) {
        return userInfoMapper.selectListByUidList(uidList);
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
        return userInfoMapper.updatePhoneByUid(tenantId, uid, newPhone, System.currentTimeMillis());
    }
    
}
