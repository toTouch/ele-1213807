package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
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
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.UserBatteryDepositBO;
import com.xiliulou.electricity.bo.batteryPackage.UserBatteryMemberCardPackageBO;
import com.xiliulou.electricity.bo.user.UserInfoBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CarRentalPackageExlConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.EleUserOperateHistoryConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.OrderForBatteryConstants;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.constant.UserInfoExtraConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.domain.car.UserCarRentalPackageDO;
import com.xiliulou.electricity.dto.CarUserMemberInfoProDTO;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.EleRefundOrder;
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
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserAuthMessage;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.dto.UserDelStatusDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SignStatusEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SignStatusEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageStatusVOEnum;
import com.xiliulou.electricity.enums.enterprise.CloudBeanStatusEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.enums.enterprise.RentBatteryOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.enums.merchant.MerchantInviterCanModifyEnum;
import com.xiliulou.electricity.enums.merchant.MerchantInviterSourceEnum;
import com.xiliulou.electricity.enums.thirdParty.ThirdPartyOperatorTypeEnum;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.UserOauthBindListQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.user.BindBatteryRequest;
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
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.EleUserEsignRecordService;
import com.xiliulou.electricity.service.EleUserOperateHistoryService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigExtraService;
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
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
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
import com.xiliulou.electricity.service.battery.ElectricityBatteryLabelBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.service.thirdParty.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.service.userinfo.emergencyContact.EmergencyContactService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.DesensitizationUtil;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.OrderForBatteryUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.DetailsBatteryInfoProVO;
import com.xiliulou.electricity.vo.DetailsBatteryInfoVo;
import com.xiliulou.electricity.vo.DetailsUserInfoVo;
import com.xiliulou.electricity.vo.EleDepositRefundVO;
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
import com.xiliulou.electricity.vo.UserTurnoverVo;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.electricity.vo.userinfo.UserAccountInfoVO;
import com.xiliulou.electricity.vo.userinfo.UserBasicInfoCarProVO;
import com.xiliulou.electricity.vo.userinfo.UserBasicInfoEleProVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalInfoExcelVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageProV2VO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageProVO;
import com.xiliulou.electricity.vo.userinfo.UserCarRentalPackageVO;
import com.xiliulou.electricity.vo.userinfo.UserDepositStatusVO;
import com.xiliulou.electricity.vo.userinfo.UserEleInfoProV2VO;
import com.xiliulou.electricity.vo.userinfo.UserEleInfoProVO;
import com.xiliulou.electricity.vo.userinfo.UserEleInfoVO;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupIdAndNameVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.EleEsignConstant.ESIGN_STATUS_FAILED;
import static com.xiliulou.electricity.constant.EleEsignConstant.ESIGN_STATUS_SUCCESS;
import static com.xiliulou.electricity.request.user.UpdateUserPhoneRequest.CONSTRAINT_NOT_FORCE;
import static com.xiliulou.electricity.request.user.UpdateUserPhoneRequest.CONSTRAINT_FORCE;
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
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("DATA-SCREEN-THREAD-POOL", 4,
            "dataScreenThread:");

    TtlXllThreadPoolExecutorServiceWrapper threadPoolPro = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("PRO-USER-INFO-LIST-THREAD-POOL", 4, "pro-user-info-list-thread:"));

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
    private UserInfoExtraService userInfoExtraService;

    @Autowired
    CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    @Autowired
    EleUserEsignRecordService eleUserEsignRecordService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Autowired
    private OverdueUserRemarkPublish overdueUserRemarkPublish;
    
    @Resource
    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;

    @Resource
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Resource
    EleRefundOrderService eleRefundOrderService;
    
    @Resource
    MeiTuanRiderMallOrderService meiTuanRiderMallOrderService;
    
    @Resource
    private TenantFranchiseeMutualExchangeService mutualExchangeService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private EmergencyContactService emergencyContactService;


    @Resource
    private InstallmentRecordService installmentRecordService;

    @Resource
    private EnterpriseInfoService enterpriseInfoService;

    @Resource
    private UserDelRecordService userDelRecordService;
    
    @Resource
    private ElectricityConfigExtraService electricityConfigExtraService;
    
    @Resource
    private PushDataToThirdService pushDataToThirdService;

    @Resource
    private ElectricityBatteryLabelBizService electricityBatteryLabelBizService;

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
     * 分页查询
     *
     * @param userInfoQuery 查询条件
     * @return 用户集
     */
    @Slave
    @Override
    public List<UserInfoBO> pageV2(UserInfoQuery userInfoQuery) {
        if (ObjectUtil.isEmpty(userInfoQuery)) {
            userInfoQuery = UserInfoQuery.builder().tenantId(TenantContextHolder.getTenantId()).offset(0L).size(10L).build();
        }
        return userInfoMapper.pageV2(userInfoQuery);
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
    public Integer update(UserInfo userInfo) {
        int result = this.userInfoMapper.update(userInfo);
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        return result;
    }
    
    @Override
    @Slave
    public R queryList(UserInfoQuery userInfoQuery) {
        
        List<UserBatteryInfoVO> userBatteryInfoVOS;
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
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

                DetailsUserInfoProVO detailsUserInfoProVO = new DetailsUserInfoProVO();
                detailsUserInfoProVO.setFranchiseeId(item.getFranchiseeId());
                detailsUserInfoProVO.setStoreId(item.getStoreId());
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
        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(
                userInfoQuery);
        if (ObjectUtil.isEmpty(userCarRentalPackageDOList)) {
            return R.ok(Collections.emptyList());
        }

        List<Long> uids = userCarRentalPackageDOList.stream().map(UserCarRentalPackageDO::getUid).collect(Collectors.toList());
        
        // 查询车辆信息
        List<ElectricityCar> carList = electricityCarService.listByUidList(uids, CommonConstant.DEL_N);
        Map<Long, ElectricityCar> carMap = null;
        if (CollectionUtils.isNotEmpty(carList)) {
            carMap = carList.stream().collect(Collectors.toMap(ElectricityCar::getUid, Function.identity()));
        }
        
        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uids,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        List<String> ordersOn = userCarRentalPackageDOList.stream()
                .filter(f -> f.getCarDepositStatus() == 1 || f.getCarBatteryDepositStatus() == 0)
                .map(UserCarRentalPackageDO::getDepositOrderNo).filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        // t_car_rental_package_deposit_pay
        Map<String, Integer> orderMapPayType = carRentalPackageDepositPayService.selectPayTypeByOrders(ordersOn);
        // 处理租车/车店一体押金状态 和 当前套餐冻结状态
        List<UserCarRentalPackageVO> userCarRentalPackageVOList = Lists.newArrayList();
        for (UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList) {
            UserCarRentalPackageVO userCarRentalPackageVO = new UserCarRentalPackageVO();
            BeanUtils.copyProperties(userCarRentalPackageDO, userCarRentalPackageVO);
            // 如果用户尚未购买任何套餐则显示未缴纳
            userCarRentalPackageVO.setDepositStatus(UNPAID);
            if (RentalPackageTypeEnum.CAR.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalPackageVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus());
            } else if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalPackageVO.setDepositStatus(
                        convertCarBatteryDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus()));
            }
            
            if (orderMapPayType.containsKey(userCarRentalPackageDO.getDepositOrderNo())
                    && orderMapPayType.get(userCarRentalPackageDO.getDepositOrderNo()) == 3) {
                userCarRentalPackageVO.setDepositStatus(FREE_OF_CHARGE);
            }
            
            if (MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())) {
                userCarRentalPackageVO.setPackageFreezeStatus(0);
            } else {
                userCarRentalPackageVO.setPackageFreezeStatus(1);
            }

            if (MapUtil.isNotEmpty(carMap) && carMap.containsKey(userCarRentalPackageDO.getUid())) {
                ElectricityCar car = carMap.get(userCarRentalPackageDO.getUid());
                if (Objects.nonNull(car)) {
                    userCarRentalPackageVO.setCarSn(car.getSn());
                    userCarRentalPackageVO.setCarModel(car.getModel());
                }
            }

            if (MapUtil.isNotEmpty(carMap) && carMap.containsKey(userCarRentalPackageDO.getUid())) {
                ElectricityCar car = carMap.get(userCarRentalPackageDO.getUid());
                if (Objects.nonNull(car)) {
                    userCarRentalPackageVO.setCarSn(car.getSn());
                    userCarRentalPackageVO.setCarModel(car.getModel());
                }
            }

            // 查询已删除/已注销
            Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
            if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(userCarRentalPackageDO.getUid()) && Objects.nonNull(
                    userStatusMap.get(userCarRentalPackageDO.getUid()))) {
                UserDelStatusDTO userDelStatusDTO = userStatusMap.get(userCarRentalPackageDO.getUid());
                userCarRentalPackageVO.setDelTime(userDelStatusDTO.getDelTime());
                userStatus = userDelStatusDTO.getUserStatus();
            }
            userCarRentalPackageVO.setUserStatus(userStatus);

            userCarRentalPackageVOList.add(userCarRentalPackageVO);
        }
        
        // 获取用户电池相关信息
        CompletableFuture<Void> queryUserBatteryInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {
                
                // 获取用户电池信息
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                item.setBatterySn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
                // item.setBusinessStatus(Objects.isNull(electricityBattery) ? null : electricityBattery.getBusinessStatus());
                item.setBatteryModel(Objects.isNull(electricityBattery) ? "" : electricityBattery.getModel());
                
                // 获取用户所属加盟商
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
                
                // 获取租车用户邀请人
                item.setInviterUserName(queryFinalInviterUserName(item.getUid()));
                
            });
        }, threadPool).exceptionally(e -> {
            log.error("Query user battery info error for car rental.", e);
            return null;
        });
        
        // 获取用户租车保险相关信息
        CompletableFuture<Void> queryUserInsuranceInfo = CompletableFuture.runAsync(() -> {
            userCarRentalPackageVOList.forEach(item -> {
                
                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(item.getPackageId());
                if (Objects.nonNull(carRentalPackagePo)) {
                    item.setPackageName(carRentalPackagePo.getName());
                }
                
                InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(
                        item.getUid(), item.getPackageType());
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
                List<UserInfoGroupNamesBO> namesBOList = userInfoGroupDetailService.listGroupByUid(
                        UserInfoGroupDetailQuery.builder().uid(item.getUid()).build());
                List<UserInfoGroupIdAndNameVO> groupVoList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(namesBOList)) {
                    groupVoList = namesBOList.stream()
                            .map(bo -> UserInfoGroupIdAndNameVO.builder().id(bo.getGroupId()).name(bo.getGroupName())
                                    .groupNo(bo.getGroupNo()).build()).collect(Collectors.toList());
                }
                
                item.setGroupList(CollectionUtils.isEmpty(groupVoList) ? Collections.emptyList() : groupVoList);

                UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(item.getUid());
                item.setRemark(Objects.isNull(userInfoExtra) ? null : userInfoExtra.getRemark());
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user group info error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryInfo, queryUserInsuranceInfo,
                queryUserGroupInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for car rental.", e);
        }
        
        // 处理payCount为空（押金已退），根据uid查詢套餐列表
        List<Long> uidList = userCarRentalPackageVOList.stream().map(UserCarRentalPackageVO::getUid)
                .collect(Collectors.toList());
        List<CarRentalPackageMemberTermPo> packageDOList = carRentalPackageMemberTermService.listUserPayCountByUidList(
                uidList);
        
        Map<Long, Integer> payCountMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(packageDOList)) {
            payCountMap = packageDOList.stream().collect(
                    Collectors.toMap(CarRentalPackageMemberTermPo::getUid, CarRentalPackageMemberTermPo::getPayCount,
                            (k1, k2) -> k1));
        }
        
        // payCount为空时，进行处理
        Map<Long, Integer> finalPayCountMap = payCountMap;
        userCarRentalPackageVOList.forEach(userCarRentalPackageVO -> {
            if (Objects.isNull(userCarRentalPackageVO.getPayCount()) && finalPayCountMap.containsKey(
                    userCarRentalPackageVO.getUid())) {
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
    
    @Slave
    @Override
    public R queryCarRentalListForPro(UserInfoQuery userInfoQuery) {
        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(userInfoQuery);
        if (ObjectUtil.isEmpty(userCarRentalPackageDOList)) {
            return R.ok(Collections.emptyList());
        }

        List<String> ordersOn = userCarRentalPackageDOList.stream().map(UserCarRentalPackageDO::getDepositOrderNo).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        // t_car_rental_package_deposit_pay
        Map<String, Integer> orderMapPayType = carRentalPackageDepositPayService.selectPayTypeByOrders(ordersOn);
        // 处理租车/车店一体押金状态 和 当前套餐冻结状态
        List<UserCarRentalPackageProVO> userCarRentalPackageVOList = Lists.newArrayList();
        List<Long> uidList = new ArrayList<>(userCarRentalPackageDOList.size());
        List<UserOauthBindListQuery> userOauthBindListQueryList = new ArrayList<>(userCarRentalPackageDOList.size());
        Integer tenantId = TenantContextHolder.getTenantId();

        for (UserCarRentalPackageDO userCarRentalPackageDO : userCarRentalPackageDOList) {
            UserCarRentalPackageProVO userCarRentalPackageVO = new UserCarRentalPackageProVO();
            BeanUtils.copyProperties(userCarRentalPackageDO, userCarRentalPackageVO);
            // 如果用户尚未购买任何套餐则显示未缴纳
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
                userCarRentalPackageVO.setPackageFreezeStatus(CarRentalPackageStatusVOEnum.CAR_PACKAGE_FROZEN.getCode());
            } else {
                userCarRentalPackageVO.setPackageFreezeStatus(CarRentalPackageStatusVOEnum.CAR_PACKAGE_UN_FROZEN.getCode());
            }

            userCarRentalPackageVOList.add(userCarRentalPackageVO);

            Long uid = userCarRentalPackageDO.getUid();
            uidList.add(uid);

            UserOauthBindListQuery query = UserOauthBindListQuery.builder().phone(userCarRentalPackageDO.getPhone()).uid(uid).build();
            userOauthBindListQueryList.add(query);
        }

        // 查询车辆信息
        List<ElectricityCar> carList = electricityCarService.listByUidList(uidList, CommonConstant.DEL_N);
        Map<Long, ElectricityCar> carMap = null;
        if (CollectionUtils.isNotEmpty(carList)) {
            carMap = carList.stream().collect(Collectors.toMap(ElectricityCar::getUid, Function.identity()));
        }

        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uidList,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        // 查询认证信息
        Map<Long, List<UserOauthBind>> userOauthMap = null;
        if (CollectionUtils.isNotEmpty(userOauthBindListQueryList)) {
            List<UserOauthBind> userOauthBinds = userOauthBindService.listByUidAndPhoneList(userOauthBindListQueryList, tenantId);
            if (CollectionUtils.isNotEmpty(userOauthBinds)) {
                userOauthMap = userOauthBinds.stream().collect(Collectors.groupingBy(UserOauthBind::getUid));
            }
        }

        // 获取用户电池相关信息
        Map<Long, ElectricityBattery> userBatteryMap = electricityBatteryService.listUserBatteryByUidList(uidList, tenantId);

        Map<Long, List<UserOauthBind>> finalUserOauthMap = userOauthMap;
        Map<Long, ElectricityCar> finalCarMap = carMap;
        CompletableFuture<Void> queryUserBatteryInfo = CompletableFuture.runAsync(() -> userCarRentalPackageVOList.forEach(item -> {
            Long uid = item.getUid();

            // 获取用户电池信息
            if (MapUtils.isNotEmpty(userBatteryMap) && userBatteryMap.containsKey(uid)) {
                ElectricityBattery electricityBattery = userBatteryMap.get(uid);
                item.setBatterySn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
                item.setBatteryModel(Objects.isNull(electricityBattery) ? "" : electricityBattery.getModel());
            }

            // 获取用户所属加盟商
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            item.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

            // 用户所属门店
            UserInfo userInfo = queryByUidFromCache(uid);
            if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getStoreId())) {
                item.setStoreId(userInfo.getStoreId());
                Store store = storeService.queryByIdFromCache(userInfo.getStoreId());
                item.setStoreName(Objects.isNull(store) ? "" : store.getName());
            }

            // 套餐信息
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(item.getPackageId());
            if (Objects.nonNull(carRentalPackagePo)) {
                item.setPackageName(carRentalPackagePo.getName());
            }

            UserBasicInfoCarProVO basicInfo = new UserBasicInfoCarProVO();
            basicInfo.setUid(uid);
            basicInfo.setPhone(item.getPhone());
            basicInfo.setName(item.getName());
            basicInfo.setFranchiseeId(item.getFranchiseeId());
            basicInfo.setStoreId(item.getStoreId());
            if (Objects.nonNull(franchisee)) {
                basicInfo.setModelType(franchisee.getModelType());
            }
            if (MapUtils.isNotEmpty(finalUserOauthMap) && finalUserOauthMap.containsKey(uid)) {
                List<UserOauthBind> userOauthBinds = finalUserOauthMap.get(uid);
                Map<Integer, UserOauthBind> sourceMap = Optional.ofNullable(userOauthBinds).orElse(Collections.emptyList()).stream()
                        .collect(Collectors.toMap(UserOauthBind::getSource, Function.identity(), (k1, k2) -> k1));
                if (MapUtils.isNotEmpty(sourceMap)) {
                    basicInfo.setBindWX(this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_WX_PRO)) ? UserOauthBind.STATUS_BIND_VX : UserOauthBind.STATUS_UN_BIND_VX);
                    basicInfo.setBindAlipay(
                            this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_ALI_PAY)) ? UserOauthBind.STATUS_BIND_ALIPAY : UserOauthBind.STATUS_UN_BIND_ALIPAY);
                }
            }

            item.setBasicInfo(basicInfo);

            if (MapUtil.isNotEmpty(finalCarMap) && finalCarMap.containsKey(uid)) {
                ElectricityCar car = finalCarMap.get(uid);
                if (Objects.nonNull(car)) {
                    item.setCarSn(car.getSn());
                    item.setCarModel(car.getModel());
                }
            }

            // 查询已删除/已注销
            Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
            if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(uid) && Objects.nonNull(userStatusMap.get(uid))) {
                UserDelStatusDTO userDelStatusDTO = userStatusMap.get(uid);
                item.setDelTime(userDelStatusDTO.getDelTime());
                userStatus = userDelStatusDTO.getUserStatus();
            }
            item.setUserStatus(userStatus);
        }), threadPoolPro).exceptionally(e -> {
            log.error("Query user battery info error for car rental pro.", e);
            return null;
        });

        CarUserMemberInfoProDTO carUserMemberInfoProDTO = carRentalPackageMemberTermBizService.queryUserMemberInfoForProPreSelect(tenantId, uidList);
        // 待使用订单是否有可退套餐
        Map<Long, Boolean> noUsingRefundMap = getCarNoUsingOrderPro(tenantId, uidList);

        // 查询用户全量信息
        CompletableFuture<Void> queryUserMemberInfo = CompletableFuture.runAsync(() -> userCarRentalPackageVOList.forEach(item -> {
            item.setUserMemberInfoVo(carRentalPackageMemberTermBizService.queryUserMemberInfoForPro(tenantId, item.getUid(), uidList, carUserMemberInfoProDTO, noUsingRefundMap));
        }), threadPoolPro).exceptionally(e -> {
            log.error("Query user member info error for car rental pro.", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryInfo, queryUserMemberInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for car rental pro.", e);
        }

        return R.ok(userCarRentalPackageVOList);
    }

    @Override
    public R queryCarRentalListForProV2(UserInfoQuery userInfoQuery) {
        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(userInfoQuery);
        if (ObjectUtil.isEmpty(userCarRentalPackageDOList)) {
            return R.ok(Collections.emptyList());
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        List<Long> uidList = new ArrayList<>(userCarRentalPackageDOList.size());
        List<Long> packageIds = new ArrayList<>(userCarRentalPackageDOList.size());
        List<Long> franchiseeIds = new ArrayList<>(userCarRentalPackageDOList.size());
        List<Long> storeIds = new ArrayList<>(userCarRentalPackageDOList.size());
        List<String> depositOrderNoList = new ArrayList<>(userCarRentalPackageDOList.size());

        userCarRentalPackageDOList.forEach(item -> {
            if (Objects.isNull(item)) {
                return;
            }

            Long uid = item.getUid();
            Long packageId = item.getPackageId();
            Long franchiseeId = item.getFranchiseeId();
            Long storeId = item.getStoreId();
            String depositOrderNo = item.getDepositOrderNo();

            uidList.add(uid);
            packageIds.add(packageId);
            if (Objects.nonNull(franchiseeId) && !Objects.equals(franchiseeId, 0L)) {
                franchiseeIds.add(franchiseeId);
            }
            storeIds.add(storeId);
            depositOrderNoList.add(depositOrderNo);
        });

        // 查询套餐信息
        Map<Long, CarRentalPackagePo> packageMap = null;
        if (CollectionUtils.isNotEmpty(packageIds)) {
            List<CarRentalPackagePo> packageList = carRentalPackageService.selectByIds(packageIds);
            if (CollectionUtils.isNotEmpty(packageList)) {
                packageMap = packageList.stream().collect(Collectors.toMap(CarRentalPackagePo::getId, Function.identity(), (k1, k2) -> k1));
            }
        }
        // 查询加盟商信息
        Map<Long, Franchisee> franchiseeMap = null;
        if (CollectionUtils.isNotEmpty(franchiseeIds)) {
            List<Franchisee> franchiseeList = franchiseeService.queryByIds(franchiseeIds, tenantId);
            if (CollectionUtils.isNotEmpty(franchiseeList)) {
                franchiseeMap = franchiseeList.stream().collect(Collectors.toMap(Franchisee::getId, Function.identity(), (k1, k2) -> k1));
            }
        }
        // 查询门店信息
        Map<Long, Store> storeMap = null;
        if (CollectionUtils.isNotEmpty(storeIds)) {
            List<Store> storeList = storeService.selectByStoreIds(storeIds);
            if (CollectionUtils.isNotEmpty(storeList)) {
                storeMap = storeList.stream().collect(Collectors.toMap(Store::getId, Function.identity(), (k1, k2) -> k1));
            }
        }
        // 查询用户电池信息
        Map<Long, ElectricityBattery> userBatteryMap = electricityBatteryService.listUserBatteryByUidList(uidList, tenantId);
        // 询用户滞纳金信息
        Map<Long, BigDecimal> userLateFeeMap = carRenalPackageSlippageBizService.listCarPackageUnpaidAmountByUidList(tenantId, uidList);
        // 查询用户押金信息
        Map<Long, CarRentalPackageDepositPayPo> userDepositMap = null;
        Map<String, Integer> orderMapPayType = null;
        if (CollectionUtils.isNotEmpty(depositOrderNoList)) {
            List<CarRentalPackageDepositPayPo> depositList = carRentalPackageDepositPayService.listByOrders(tenantId, depositOrderNoList);
            if (CollectionUtils.isNotEmpty(depositList)) {
                userDepositMap = depositList.stream().collect(Collectors.toMap(CarRentalPackageDepositPayPo::getUid, Function.identity(), (k1, k2) -> k1));

                // 押金订单支付类型
                orderMapPayType = depositList.stream()
                        .collect(Collectors.toMap(CarRentalPackageDepositPayPo::getOrderNo, CarRentalPackageDepositPayPo::getPayType, (k1, k2) -> k1));
            }
        }
        // 查询车辆信息
        List<ElectricityCar> carList = electricityCarService.listByUidList(uidList, CommonConstant.DEL_N);
        Map<Long, ElectricityCar> carMap = null;
        if (CollectionUtils.isNotEmpty(carList)) {
            carMap = carList.stream().collect(Collectors.toMap(ElectricityCar::getUid, Function.identity()));
        }
        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uidList,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        Map<Long, CarRentalPackagePo> finalPackageMap = packageMap;
        Map<Long, Franchisee> finalFranchiseeMap = franchiseeMap;
        Map<Long, Store> finalStoreMap = storeMap;
        Map<Long, CarRentalPackageDepositPayPo> finalUserDepositMap = userDepositMap;

        Map<String, Integer> finalOrderMapPayType = orderMapPayType;
        Map<Long, ElectricityCar> finalCarMap = carMap;
        List<UserCarRentalPackageProV2VO> resultList = userCarRentalPackageDOList.stream().map(item -> {
            Long uid = item.getUid();
            Long packageId = item.getPackageId();
            Long franchiseeId = item.getFranchiseeId();
            Long storeId = item.getStoreId();

            UserCarRentalPackageProV2VO vo = new UserCarRentalPackageProV2VO();
            vo.setUid(uid);
            vo.setName(item.getName());
            vo.setPhone(item.getPhone());
            vo.setUsableStatus(item.getUsableStatus());
            vo.setBatteryRentStatus(item.getBatteryRentStatus());
            vo.setPackageId(packageId);
            vo.setPackageType(item.getPackageType());
            vo.setDueTimeTotal(item.getPackageExpiredTime());
            vo.setDueTime(item.getDueTime());
            vo.setResidue(item.getResidue());
            vo.setFranchiseeId(franchiseeId);
            vo.setStoreId(storeId);
            vo.setDepositOrderNo(item.getDepositOrderNo());
            vo.setRentalPackageOrderNo(item.getRentalPackageOrderNo());

            if (MapUtils.isNotEmpty(finalPackageMap) && finalPackageMap.containsKey(packageId) && Objects.nonNull(finalPackageMap.get(packageId))) {
                vo.setPackageName(finalPackageMap.get(packageId).getName());
            }
            if (MapUtils.isNotEmpty(finalFranchiseeMap) && finalFranchiseeMap.containsKey(franchiseeId) && Objects.nonNull(finalFranchiseeMap.get(franchiseeId))) {
                vo.setFranchiseeName(finalFranchiseeMap.get(franchiseeId).getName());
            }
            if (MapUtils.isNotEmpty(finalStoreMap) && finalStoreMap.containsKey(storeId) && Objects.nonNull(finalStoreMap.get(storeId))) {
                vo.setStoreName(finalStoreMap.get(storeId).getName());
            }
            if (MapUtils.isNotEmpty(userBatteryMap) && userBatteryMap.containsKey(uid) && Objects.nonNull(userBatteryMap.get(uid))) {
                vo.setBatterySn(userBatteryMap.get(uid).getSn());
            }
            if (MapUtils.isNotEmpty(finalUserDepositMap) && finalUserDepositMap.containsKey(uid) && Objects.nonNull(finalUserDepositMap.get(uid))) {
                CarRentalPackageDepositPayPo carRentalPackageDepositPayPo = finalUserDepositMap.get(uid);
                vo.setDeposit(carRentalPackageDepositPayPo.getDeposit());
                vo.setDepositPayType(carRentalPackageDepositPayPo.getPayType());
            }
            // 如果用户尚未购买任何套餐则显示未缴纳
            vo.setDepositStatus(UNPAID);
            if (Objects.equals(item.getPackageType(), RentalPackageTypeEnum.CAR.getCode())) {
                vo.setDepositStatus(item.getCarDepositStatus());
            } else if (Objects.equals(item.getPackageType(), RentalPackageTypeEnum.CAR_BATTERY.getCode())) {
                vo.setDepositStatus(convertCarBatteryDepositStatus(item.getCarBatteryDepositStatus()));
            }
            if (MapUtils.isNotEmpty(finalOrderMapPayType) && finalOrderMapPayType.containsKey(item.getDepositOrderNo())
                    && finalOrderMapPayType.get(item.getDepositOrderNo()) == 3) {
                vo.setDepositStatus(FREE_OF_CHARGE);
            }
            if (MapUtils.isNotEmpty(userLateFeeMap) && userLateFeeMap.containsKey(uid) && Objects.nonNull(userLateFeeMap.get(uid))) {
                vo.setLateFeeAmount(userLateFeeMap.get(uid));
            }
            if (MapUtil.isNotEmpty(finalCarMap) && finalCarMap.containsKey(uid)) {
                ElectricityCar car = finalCarMap.get(uid);
                if (Objects.nonNull(car)) {
                    vo.setCarSn(car.getSn());
                }
            }

            // 查询已删除/已注销
            Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
            if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(uid) && Objects.nonNull(userStatusMap.get(uid))) {
                UserDelStatusDTO userDelStatusDTO = userStatusMap.get(uid);
                vo.setDelTime(userDelStatusDTO.getDelTime());
                userStatus = userDelStatusDTO.getUserStatus();
            }
            vo.setUserStatus(userStatus);

            return vo;
        }).collect(Collectors.toList());

        return R.ok(resultList);
    }

    private Map<Long, Boolean> getCarNoUsingOrderPro(Integer tenantId, List<Long> uidList) {
        if (CollectionUtils.isEmpty(uidList)) {
            return null;
        }

        List<CarRentalPackageOrderPo> noUseOrderList = carRentalPackageOrderService.listUnUseAndRefundByUidList(tenantId, uidList, System.currentTimeMillis());

        Map<Long, List<CarRentalPackageOrderPo>> noUsingMap = noUseOrderList.stream().collect(Collectors.groupingBy(CarRentalPackageOrderPo::getUid));
        if (MapUtils.isEmpty(noUsingMap)) {
            return null;
        }

        Map<Long, Boolean> noUsingRefundMap = new HashMap<>(uidList.size());
        for (Long uid : uidList) {
            if (!noUsingMap.containsKey(uid)) {
                noUsingRefundMap.put(uid, false);
                continue;
            }

            List<CarRentalPackageOrderPo> carRentalPackageOrderPos = noUsingMap.get(uid);
            if (CollectionUtils.isEmpty(carRentalPackageOrderPos)) {
                noUsingRefundMap.put(uid, false);
                continue;
            }

            List<Long> packageIds = carRentalPackageOrderPos.stream().map(CarRentalPackageOrderPo::getRentalPackageId).collect(Collectors.toList());
            List<CarRentalPackagePo> carRentalPackagePos = carRentalPackageService.selectByIds(packageIds);
            if (CollectionUtils.isEmpty(carRentalPackagePos)) {
                noUsingRefundMap.put(uid, false);
                continue;
            }

            noUsingRefundMap.put(uid, true);
        }

        return noUsingRefundMap;
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
    public R updateStatus(Long uid, Integer usableStatus) {
        
        // 租户
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

        // 给第三方推送用户信息
        pushDataToThirdService.asyncPushUserInfo(TtlTraceIdSupport.get(), TenantContextHolder.getTenantId(), userInfo.getUid(), ThirdPartyOperatorTypeEnum.USER_STATUS.getType());

        return R.ok();
    }
    
    @Override
    public UserInfo queryByUidFromCache(Long uid) {
        UserInfo cache = redisService.getWithHash(CacheConstant.CACHE_USER_INFO + uid, UserInfo.class);
        
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid)
                .eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
        if (Objects.isNull(userInfo)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_INFO + uid, userInfo);
        return userInfo;
    }
    
    @Override
    public UserInfo queryByUidFromDbIncludeDelUser(Long uid) {
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
        
        // 判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ELE WARN! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                userInfo.getUid());
        
        //        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || Objects.equals(userBatteryMemberCard.getRemainingNumber().longValue(), UserBatteryMemberCard.MEMBER_CARD_ZERO_REMAINING)) {
        //            log.warn("HOME WARN! user haven't memberCard uid={}", userInfo.getUid());
        //            return R.fail("100210", "用户未开通套餐");
        //        }
        
        Long validDays = null;
        Long memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime();
        
        /**
         * 为了兼容之前那人写的2.0小程序  看不懂完全看不懂，谨慎修改
         */
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ELE ERROR!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(),
                    userBatteryMemberCard.getMemberCardId());
            return R.ok();
        }
        
        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setName("体验卡");
        
        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            
            EleDisableMemberCardRecord eleDisableMemberCardRecord = null;
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                    UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                memberCardExpireTime = System.currentTimeMillis() + (memberCardExpireTime
                        - userBatteryMemberCard.getDisableMemberCardTime());
                validDays = (userBatteryMemberCard.getMemberCardExpireTime()
                        - userBatteryMemberCard.getDisableMemberCardTime()) / (24 * 60 * 60 * 1000L);
                eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(
                        userInfo.getUid(), userInfo.getTenantId());
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
            if (Objects.isNull(userBatteryMemberCard.getRemainingNumber()) || Objects.isNull(
                    userBatteryMemberCard.getMemberCardExpireTime())
                    || System.currentTimeMillis() >= userBatteryMemberCard.getMemberCardExpireTime()) {
                return R.ok();
            }
            //            }
            ownMemberCardInfoVo.setName(batteryMemberCard.getName());
            //            ownMemberCardInfoVo.setType(batteryMemberCard.getType());
            ownMemberCardInfoVo.setMaxUseCount(batteryMemberCard.getUseCount());
            if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.equals(
                    eleDisableMemberCardRecord.getDisableCardTimeType(),
                    EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)) {
                ownMemberCardInfoVo.setEndTime(userBatteryMemberCard.getDisableMemberCardTime()
                        + eleDisableMemberCardRecord.getChooseDays() * (24 * 60 * 60 * 1000L));
            }
            
            // 兼容旧的小程序  送的次数卡 返回的套餐id必须为null
            ownMemberCardInfoVo.setCardId(userBatteryMemberCard.getMemberCardId().intValue());
        }
        
        ownMemberCardInfoVo.setMemberCardExpireTime(memberCardExpireTime);
        //        ownMemberCardInfoVo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber().longValue());
        //        ownMemberCardInfoVo.setMaxUseCount(userBatteryMemberCard.getRemainingNumber().longValue());
        ownMemberCardInfoVo.setDays(
                (long) Math.round((memberCardExpireTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
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
    public R verifyAuth(Long id, Integer authStatus, String msg) {
        // 租户
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
        // 修改资料项
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
        
        this.operatorAuthRecord(oldUserInfo.getUid(), authStatus);

        return R.ok();
    }
    
    @Override
    public R updateAuth(UserInfo userInfo) {
        
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        userInfo.setTenantId(tenantId);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = update(userInfo);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            // 实名认证数据修改
            UserInfo newUserInfo = this.queryByIdFromDB(userInfo.getId());
            // 身份证
            if (Objects.nonNull(userInfo.getIdNumber())) {
                EleUserAuth eleUserAuth1 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(),
                        EleAuthEntry.ID_ID_CARD);
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
            
            // 姓名
            if (Objects.nonNull(userInfo.getName())) {
                EleUserAuth eleUserAuth2 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(),
                        EleAuthEntry.ID_NAME_ID);
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

        // 同步操作记录
        this.operatorEditRecord(userInfo.getUid());

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

            // 紧急联系人
            userAuthInfoVo.setEmergencyContactList(emergencyContactService.listVOByUid(e.getUid()));

            List<EleUserAuth> list = (List<EleUserAuth>) eleUserAuthService.selectCurrentEleAuthEntriesList(e.getUid())
                    .getData();
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
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            count = userInfoMapper.queryCountByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
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
    
    // 后台绑定电池
    @Override
    public R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        if (!redisService.setNx(CacheConstant.CACHE_USER_BIND_BATTERY_LOCK + userInfoBatteryAddAndUpdate.getUid(), "1",
                5 * 1000L, false)) {
            return R.fail("100032", "该用户已绑定电池");
        }
        
        // 查询有没有绑定过电池，区分了绑定与编辑两种操作类型，编辑操作才会退掉已绑定电池，所以对绑定操作在此处做校验进行拦截
        ElectricityBattery isBindElectricityBattery = electricityBatteryService.queryByUid(
                userInfoBatteryAddAndUpdate.getUid());
        if (Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.BIND_TYPE)
                && Objects.nonNull(isBindElectricityBattery)) {
            return R.fail("100032", "该用户已绑定电池");
        }
        
        try {
            // 租户
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
            
            // 未实名认证
            if (!Objects.equals(oldUserInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("WEBBIND WARN! user not auth! uid={} ", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }
            
            if (Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }

            // 是否为"注销中"
            UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(oldUserInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
            if (Objects.nonNull(userDelRecord)) {
                return R.fail("120163", "账号处于注销缓冲期内，无法操作");
            }

            // 判断是否缴纳押金
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(
                    oldUserInfo.getUid());
            if (!(Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)
                    || Objects.equals(oldUserInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode()))) {
                log.warn("WEBBIND ERROR WARN! not pay deposit! uid={} ", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }
            
            Integer orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();
            if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                // 判断电池滞纳金
                
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                        oldUserInfo.getUid());
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("WEBBIND ERROR WARN! user haven't memberCard uid={}", oldUserInfo.getUid());
                    return R.fail("100210", "用户未开通套餐");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                        UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                    log.warn("WEBBIND ERROR WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                    return R.fail("100211", "换电套餐停卡审核中");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                        UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.warn("WEBBIND ERROR WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                    return R.fail("100211", "换电套餐已暂停");
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                        userBatteryMemberCard.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("WEBBIND ERROR WARN! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(),
                            userBatteryMemberCard.getMemberCardId());
                    return R.fail("ELECTRICITY.00121", "套餐不存在");
                }
                
                if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                        .equals(batteryMemberCard.getBusinessType())) {
                    orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
                }
                
                // 判断用户电池服务费
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                        oldUserInfo, userBatteryMemberCard, batteryMemberCard,
                        serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("WEBBIND ERROR ERROR! user exist battery service fee,uid={}", oldUserInfo.getUid());
                    return R.fail("ELECTRICITY.100000", "存在电池服务费");
                }
                
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (
                        Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                    log.warn("WEBBIND ERROR WARN! battery memberCard is Expire,uid={}", oldUserInfo.getUid());
                    return R.fail("ELECTRICITY.0023", "套餐已过期");
                }
                
                // 判断车电关联是否可租电
                ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(
                        oldUserInfo.getTenantId());
                if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(),
                        ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
                    if (Objects.equals(oldUserInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                        try {
                            if (carRentalPackageMemberTermBizService.isExpirePackageOrder(oldUserInfo.getTenantId(),
                                    oldUserInfo.getUid())) {
                                log.error("WEBBIND ERROR WARN! user car memberCard expire,uid={}",
                                        oldUserInfo.getUid());
                                return R.fail("100233", "租车套餐已过期");
                            }
                        } catch (Exception e) {
                            log.error("WEBBIND ERROR WARN! acquire car membercard expire result fail,uid={}",
                                    oldUserInfo.getUid(), e);
                        }
                    }
                }
            } else {
                carRentalPackageMemberTermBizService.verifyMemberSwapBattery(oldUserInfo.getTenantId(),
                        oldUserInfo.getUid());
            }
            
            // 判断电池是否存在，或者已经被绑定
            ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(
                    userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
            if (Objects.isNull(oldElectricityBattery)) {
                log.warn("WEBBIND ERROR WARN! not found Battery! batteryName={}",
                        userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
                return R.fail("ELECTRICITY.0020", "未找到电池");
            }
            if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(),
                    userInfoBatteryAddAndUpdate.getUid())) {
                log.warn("WEBBIND ERROR WARN! battery is bind user! sn={} ",
                        userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
                return R.fail("100019", "该电池已经绑定用户");
            }
            
            // 运营商绑定电池判断互通
            if (SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
                if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(oldUserInfo.getTenantId(),
                        oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId())) {
                    log.warn("WEBBIND ERROR WARN! franchiseeId not equals,userFranchiseeId={},batteryFranchiseeId={}",
                            oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId());
                    return R.fail("100371", "电池加盟商与用户加盟商不一致");
                }
            } else {
                if (!Objects.equals(oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId())) {
                    log.warn("WEBBIND ERROR WARN! franchiseeId not equals,userFranchiseeId={},batteryFranchiseeId={}",
                            oldUserInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId());
                    return R.fail("100371", "电池加盟商与用户加盟商不一致");
                }
            }
            
            // 多型号  绑定电池需要判断电池是否和用户型号一致
            Triple<Boolean, String, Object> verifyUserBatteryTypeResult = verifyUserBatteryType(oldElectricityBattery,
                    oldUserInfo);
            if (Boolean.FALSE.equals(verifyUserBatteryTypeResult.getLeft())) {
                return R.fail(verifyUserBatteryTypeResult.getMiddle(), (String) verifyUserBatteryTypeResult.getRight());
            }
            
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(oldUserInfo.getUid());
            updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            Integer update = updateByUid(updateUserInfo);
            
            // 之前有电池，将原来的电池解绑
            if (Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.EDIT_TYPE)
                    && Objects.nonNull(isBindElectricityBattery)) {
                if (!Objects.equals(isBindElectricityBattery.getSn(),
                        userInfoBatteryAddAndUpdate.getInitElectricityBatterySn())) {
                    ElectricityBattery notBindOldElectricityBattery = new ElectricityBattery();
                    notBindOldElectricityBattery.setId(isBindElectricityBattery.getId());
                    notBindOldElectricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_ADMIN_UNBIND);
                    notBindOldElectricityBattery.setElectricityCabinetId(null);
                    notBindOldElectricityBattery.setElectricityCabinetName(null);
                    notBindOldElectricityBattery.setUid(null);
                    notBindOldElectricityBattery.setBorrowExpireTime(null);
                    notBindOldElectricityBattery.setUpdateTime(System.currentTimeMillis());
                    notBindOldElectricityBattery.setBindTime(System.currentTimeMillis());
                    
                    // 删除redis中保存的租电订单或换电订单
                    OrderForBatteryUtil.delete(isBindElectricityBattery.getSn());
                    
                    electricityBatteryService.updateBatteryUser(notBindOldElectricityBattery);
                    
                    // 添加退电记录
                    RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
                    rentBatteryOrder.setUid(oldUserInfo.getUid());
                    rentBatteryOrder.setName(oldUserInfo.getName());
                    rentBatteryOrder.setPhone(oldUserInfo.getPhone());
                    rentBatteryOrder.setElectricityBatterySn(isBindElectricityBattery.getSn());
                    rentBatteryOrder.setBatteryDeposit(Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO
                            : userBatteryDeposit.getBatteryDeposit());
                    rentBatteryOrder.setOrderId(
                            OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_BATTERY, user.getUid()));
                    rentBatteryOrder.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
                    rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
                    rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
                    rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
                    rentBatteryOrder.setCreateTime(System.currentTimeMillis());
                    rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                    rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_UNBIND);
                    rentBatteryOrder.setOrderType(orderType);
                    rentBatteryOrder.setIsFreeze(RentBatteryOrder.IS_FREEZE_NO);
                    rentBatteryOrderService.insert(rentBatteryOrder);

                    // 修改原绑定电池标签为闲置
                    electricityBatteryService.asyncModifyLabel(isBindElectricityBattery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.UNUSED.getCode(), user.getUid()), false);
                    electricityBatteryLabelBizService.clearCacheBySn(isBindElectricityBattery.getSn());
                }
            }
            
            Integer finalOrderType = orderType;
            DbUtils.dbOperateSuccessThen(update, () -> {
                // 添加租电池记录
                RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
                rentBatteryOrder.setUid(oldUserInfo.getUid());
                rentBatteryOrder.setName(oldUserInfo.getName());
                rentBatteryOrder.setPhone(oldUserInfo.getPhone());
                rentBatteryOrder.setElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
                rentBatteryOrder.setBatteryDeposit(
                        Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
                rentBatteryOrder.setOrderId(
                        OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_BATTERY, user.getUid()));
                rentBatteryOrder.setStatus(RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS);
                rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
                rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
                rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
                rentBatteryOrder.setCreateTime(System.currentTimeMillis());
                rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_BIND);
                rentBatteryOrder.setOrderType(finalOrderType);
                rentBatteryOrderService.insert(rentBatteryOrder);
                
                // 生成后台操作记录
                EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                        .operateModel(EleUserOperateRecord.BATTERY_MODEL).operateContent(
                                Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(),
                                        UserInfoBatteryAddAndUpdate.EDIT_TYPE)
                                        ? EleUserOperateRecord.EDIT_BATTERY_CONTENT
                                        : EleUserOperateRecord.BIND_BATTERY_CONTENT).operateUid(user.getUid())
                        .uid(oldUserInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).name(user.getUsername())
                        .initElectricityBatterySn(
                                Objects.nonNull(isBindElectricityBattery) ? isBindElectricityBattery.getSn() : "")
                        .nowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn())
                        .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
                
                // 判断是单电的电池操作还是车电一体的电池操作
                if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                    eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
                } else {
                    eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
                }
                
                eleUserOperateRecordService.insert(eleUserOperateRecord);
                
                // 修改电池状态
                ElectricityBattery electricityBattery = new ElectricityBattery();
                electricityBattery.setId(oldElectricityBattery.getId());
                electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
                electricityBattery.setUid(userInfoBatteryAddAndUpdate.getUid());
                electricityBattery.setUpdateTime(System.currentTimeMillis());
                electricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUserExceptEid(electricityBattery);
                
                enterpriseRentRecordService.saveEnterpriseRentRecord(rentBatteryOrder.getUid());
                
                // 记录企业用户租电池记录
                enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(
                        UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode(), rentBatteryOrder);
                
                // 保存电池被取走对应的订单，供后台租借状态电池展示
                OrderForBatteryUtil.save(rentBatteryOrder.getOrderId(),
                        OrderForBatteryConstants.TYPE_RENT_BATTERY_ORDER, oldElectricityBattery.getSn());
                
                // 修改新绑定电池标签为租借正常
                electricityBatteryService.asyncModifyLabel(oldElectricityBattery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_NORMAL.getCode(), user.getUid()), false);

                try {
                    // 发送操作记录
                    // 判断没有发送实际的电池变更则不记录
                    if (!Objects.isNull(isBindElectricityBattery) && Objects.equals(
                            userInfoBatteryAddAndUpdate.getInitElectricityBatterySn(),
                            isBindElectricityBattery.getSn())) {
                        return null;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("username", oldUserInfo.getName());
                    map.put("phone", oldUserInfo.getPhone());
                    map.put("editType", userInfoBatteryAddAndUpdate.getEdiType());
                    map.put("batterySN", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
                    operateRecordUtil.record(Objects.isNull(isBindElectricityBattery) ? null
                            : MapUtil.of("batterySN", isBindElectricityBattery.getSn()), map);
                } catch (Throwable e) {
                    log.error("Recording user operation records failed because:", e);
                }
                return null;
            });
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_BIND_BATTERY_LOCK + userInfoBatteryAddAndUpdate.getUid());
        }
    }
    
    private Triple<Boolean, String, Object> verifyUserBatteryType(ElectricityBattery electricityBattery,
            UserInfo userInfo) {
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        if (!Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            return Triple.of(true, null, null);
        }
        
        // 获取用户绑定的电池型号
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
    public R webUnBindBattery(Long uid) {
        
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("WEBUNBIND ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        // 查找用户
        UserInfo oldUserInfo = queryByUidFromCache(uid);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
            return R.ok();
        }
        
        // 未实名认证
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
            // 判断电池滞纳金
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                    oldUserInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("WEBBIND WARN! user haven't memberCard uid={}", oldUserInfo.getUid());
                return R.fail("100210", "用户未开通套餐");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                    UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("WEBBIND WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐停卡审核中");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                    UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("WEBBIND WARN! user's member card is stop! uid={}", oldUserInfo.getUid());
                return R.fail("100211", "换电套餐已暂停");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                    userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("WEBBIND WARN! not found batteryMemberCard,uid={},mid={}", oldUserInfo.getUid(),
                        userBatteryMemberCard.getMemberCardId());
                return R.fail("ELECTRICITY.00121", "套餐不存在");
            }
            
            // 根据套餐类型，设置租退订单类型
            if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                    .equals(batteryMemberCard.getBusinessType())) {
                orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
            }
            
            // 判断用户电池服务费
            Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                    oldUserInfo, userBatteryMemberCard, batteryMemberCard,
                    serviceFeeUserInfoService.queryByUidFromCache(oldUserInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("WEBBIND WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费");
            }
        } else {
            // 判断车电一体滞纳金
            if (Boolean.TRUE.equals(
                    carRenalPackageSlippageBizService.isExitUnpaid(oldUserInfo.getTenantId(), oldUserInfo.getUid()))) {
                log.warn("ORDER WARN! user exist battery service fee,uid={}", oldUserInfo.getUid());
                return R.fail("300001", "存在滞纳金，请先缴纳");
            }
        }
        
        // 解绑电池
        ElectricityBattery electricityBattery = new ElectricityBattery();
        electricityBattery.setId(oldElectricityBattery.getId());
        electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_ADMIN_UNBIND);
        electricityBattery.setElectricityCabinetId(null);
        electricityBattery.setElectricityCabinetName(null);
        electricityBattery.setUid(null);
        electricityBattery.setBorrowExpireTime(null);
        electricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBattery.setBindTime(System.currentTimeMillis());
        
        // 删除redis中保存的租电订单或换电订单
        OrderForBatteryUtil.delete(oldElectricityBattery.getSn());
        
        electricityBatteryService.updateBatteryUser(electricityBattery);
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(oldUserInfo.getUid());
        // 生成退电池记录
        RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
        rentBatteryOrder.setUid(oldUserInfo.getUid());
        rentBatteryOrder.setName(oldUserInfo.getName());
        rentBatteryOrder.setPhone(oldUserInfo.getPhone());
        rentBatteryOrder.setElectricityBatterySn(oldElectricityBattery.getSn());
        rentBatteryOrder.setBatteryDeposit(
                Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
        rentBatteryOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_BATTERY, user.getUid()));
        rentBatteryOrder.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
        rentBatteryOrder.setFranchiseeId(oldUserInfo.getFranchiseeId());
        rentBatteryOrder.setStoreId(oldUserInfo.getStoreId());
        rentBatteryOrder.setTenantId(oldUserInfo.getTenantId());
        rentBatteryOrder.setCreateTime(System.currentTimeMillis());
        rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_UNBIND);
        rentBatteryOrder.setOrderType(orderType);
        rentBatteryOrder.setIsFreeze(RentBatteryOrder.IS_FREEZE_NO);
        rentBatteryOrderService.insert(rentBatteryOrder);
        
        // 生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.BATTERY_MODEL)
                .operateContent(EleUserOperateRecord.UN_BIND_BATTERY_CONTENT).operateUid(user.getUid())
                .uid(oldUserInfo.getUid()).name(user.getUsername())
                .initElectricityBatterySn(oldElectricityBattery.getSn()).nowElectricityBatterySn(null)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        OverdueType type = null;
        // 判断是单电的电池操作还是车电一体的电池操作
        if (Objects.equals(oldUserInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY);
            type = OverdueType.BATTERY;
        } else {
            eleUserOperateRecord.setOperateType(UserOperateRecordConstant.OPERATE_TYPE_CAR);
            type = OverdueType.CAR;
        }
        
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        enterpriseRentRecordService.saveEnterpriseReturnRecord(rentBatteryOrder.getUid());
        
        // 记录企业用户还电池记录
        enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(
                UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode(), rentBatteryOrder);
        // 清除逾期用户备注
        overdueUserRemarkPublish.publish(uid, type.getCode(), tenantId);
        
        // 修改电池标签为闲置，并清除一下预修改标签
        electricityBatteryService.asyncModifyLabel(oldElectricityBattery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.UNUSED.getCode(), user.getUid()), false);
        electricityBatteryLabelBizService.clearCacheBySn(oldElectricityBattery.getSn());

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
        
        // 改为逻辑删除
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
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId)
                        .eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }
    
    @Override
    public UserInfo queryUserByPhoneAndFranchisee(String phone, Integer franchiseeId, Integer tenantId) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId)
                        .eq(UserInfo::getFranchiseeId, franchiseeId).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
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
    public List<HomePageUserByWeekDayVo> queryUserAnalysisByUserStatus(Integer tenantId, Integer userStatus,
            Long beginTime, Long endTime) {
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
        
        // 电池订单类型为免押
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        acquireBatteryFreeDepositResult(userBatteryDeposit, userInfo, userInfoResult);
        
        // 车辆订单类型为免押
        //        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(SecurityUtils.getUid());
        //        acquireCarFreeDepositResult(userCarDeposit, userInfo, userInfoResult);
        
        // 审核状态
        userInfoResult.setAuthStatus(userInfo.getAuthStatus());
        userInfoResult.setFranchiseeId(userInfo.getFranchiseeId());
        
        UserCarDetail userCarDetail = new UserCarDetail();
        UserBatteryDetail userBatteryDetail = new UserBatteryDetail();
        userInfoResult.setUserCarDetail(userCarDetail);
        userInfoResult.setUserBatteryDetail(userBatteryDetail);
        
        // 是否缴纳租电池押金
        // 是否缴纳租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            
            // 是否缴纳车电一体押金
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            }
        } else {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
        }
        
        // 是否购买租电池套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)
                // 如果送的次数卡  首页提示没有购买套餐
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
        } else {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
            userBatteryDetail.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        }
        
        // 套餐是否过期(前端要兼容旧代码  不能删除)
        if (!Objects.isNull(userBatteryMemberCard) && !Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && !Objects.equals(
                userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
        }
        
        // 是否购买车电一体套餐
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(
                    userInfo.getTenantId(), userInfo.getUid());
            if (Objects.nonNull(memberTermEntity) && Objects.equals(memberTermEntity.getRentalPackageType(),
                    RentalPackageTypeEnum.CAR_BATTERY.getCode()) && Objects.nonNull(
                    memberTermEntity.getRentalPackageId())) {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
                userBatteryDetail.setMemberCardExpireTime(memberTermEntity.getDueTimeTotal());
            } else {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
            }
            
            // 车电一体套餐是否过期
            if (Objects.nonNull(memberTermEntity) && Objects.nonNull(memberTermEntity.getDueTimeTotal())
                    && memberTermEntity.getDueTimeTotal() < System.currentTimeMillis()) {
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
            }
        }
        
        // 套餐是否暂停
        if (!Objects.isNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.NO);
        }
        
        // 是否产生电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            Triple<Boolean, Integer, BigDecimal> batteryServiceFeeTriple = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                    userInfo, userBatteryMemberCard,
                    batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()),
                    serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(batteryServiceFeeTriple.getLeft())) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
                userBatteryDetail.setBatteryServiceFee(batteryServiceFeeTriple.getRight());
            } else {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }
        
        // 用户状态(离线换电)
        UserFrontDetectionVO userFrontDetection = offLineElectricityCabinetService.getUserFrontDetection(userInfo,
                userBatteryMemberCard);
        userInfoResult.setUserFrontDetection(userFrontDetection);
        
        // 是否有车电一体滞纳金
        if (Objects.isNull(userBatteryMemberCard) || StringUtils.isBlank(userBatteryMemberCard.getOrderId())) {
            if (Boolean.TRUE.equals(
                    carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }
        
        // 是否绑定的有电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.YES);
            userBatteryDetail.setBatteryInfo(electricityBatteryService.queryByUid(userInfo.getUid()));
        } else {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.NO);
        }
        
        // 是否缴纳租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO) && Objects.equals(
                userInfo.getCarBatteryDepositStatus(), YesNoEnum.NO.getCode())) {
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
            boolean exitUnpaid = carRentalPackageOrderSlippageService.isExitUnpaid(userInfo.getTenantId(),
                    userInfo.getUid());
            if (exitUnpaid) {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.YES.getCode());
            } else {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.NO.getCode());
            }
            // 是否购买租车套餐、是否过期
            CarRentalPackageMemberTermPo carRentalPackageMemberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(
                    userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isEmpty(carRentalPackageMemberTermPo)) {
                userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
                userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
            } else {
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(carRentalPackageMemberTermPo.getStatus())
                        || StringUtils.isBlank(carRentalPackageMemberTermPo.getRentalPackageOrderNo())) {
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
            
            // 是否绑定车辆
            ElectricityCar car = electricityCarService.selectByUid(userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isNotEmpty(car)) {
                userCarDetail.setIsRentCar(UserInfoResultVO.YES);
                userCarDetail.setCarSN(car.getSn());
            } else {
                userCarDetail.setIsRentCar(UserInfoResultVO.NO);
            }
            
        }

        threadPool.execute(() -> {
            userBatteryMemberCardPackageService.batteryMembercardTransform(userInfo.getUid());
            // 保险转换
            List<InsuranceUserInfo> list = insuranceUserInfoService.listByUid(userInfo.getUid());
            if (CollUtil.isEmpty(list)) {
                log.warn("Current User Not HaveInsurance, uid is {}", userInfo.getUid());
                return;
            }
            list.stream().forEach(e->{
                insuranceUserInfoService.userInsuranceExpireAutoConvert(e);
            });
        });

        
        return Triple.of(true, "", userInfoResult);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> selectUserInfoStatusV2() {
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
        
        // 电池订单类型为免押
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        this.acquireBatteryFreeDepositResultV2(userBatteryDeposit, userInfo, userInfoResult);
        
        // 审核状态
        userInfoResult.setAuthStatus(userInfo.getAuthStatus());
        userInfoResult.setFranchiseeId(userInfo.getFranchiseeId());
        
        UserCarDetail userCarDetail = new UserCarDetail();
        UserBatteryDetail userBatteryDetail = new UserBatteryDetail();
        userInfoResult.setUserCarDetail(userCarDetail);
        userInfoResult.setUserBatteryDetail(userBatteryDetail);
        
        // 是否缴纳租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            
            // 是否缴纳车电一体押金
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.NO);
            }
        } else {
            userBatteryDetail.setIsBatteryDeposit(UserInfoResultVO.YES);
        }
        
        // 是否购买租电池套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)
                // 如果送的次数卡  首页提示没有购买套餐
                || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
        } else {
            userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
            userBatteryDetail.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        }
        
        // 套餐是否过期(前端要兼容旧代码  不能删除)
        if (!Objects.isNull(userBatteryMemberCard) && !Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && !Objects.equals(
                userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
        }
        
        // 是否购买车电一体套餐
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(
                    userInfo.getTenantId(), userInfo.getUid());
            if (Objects.nonNull(memberTermEntity) && Objects.equals(memberTermEntity.getRentalPackageType(),
                    RentalPackageTypeEnum.CAR_BATTERY.getCode()) && Objects.nonNull(
                    memberTermEntity.getRentalPackageId())) {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.YES);
                userBatteryDetail.setMemberCardExpireTime(memberTermEntity.getDueTimeTotal());
            } else {
                userBatteryDetail.setIsBatteryMemberCard(UserInfoResultVO.NO);
            }
            
            // 车电一体套餐是否过期
            if (Objects.nonNull(memberTermEntity) && Objects.nonNull(memberTermEntity.getDueTimeTotal())
                    && memberTermEntity.getDueTimeTotal() < System.currentTimeMillis()) {
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryMemberCardExpire(UserInfoResultVO.NO);
            }
        }
        
        // 套餐是否暂停
        if (!Objects.isNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.YES);
        } else {
            userBatteryDetail.setIsBatteryMemberCardDisable(UserInfoResultVO.NO);
        }
        
        // 是否产生电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            Triple<Boolean, Integer, BigDecimal> batteryServiceFeeTriple = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                    userInfo, userBatteryMemberCard,
                    batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()),
                    serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(batteryServiceFeeTriple.getLeft())) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
                userBatteryDetail.setBatteryServiceFee(batteryServiceFeeTriple.getRight());
            } else {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }
        
        // 用户状态(离线换电)
        UserFrontDetectionVO userFrontDetection = offLineElectricityCabinetService.getUserFrontDetection(userInfo,
                userBatteryMemberCard);
        userInfoResult.setUserFrontDetection(userFrontDetection);
        
        // 是否有车电一体滞纳金
        if (Objects.isNull(userBatteryMemberCard) || StringUtils.isBlank(userBatteryMemberCard.getOrderId())) {
            if (Boolean.TRUE.equals(
                    carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.YES);
            } else {
                userBatteryDetail.setIsBatteryServiceFee(UserInfoResultVO.NO);
            }
        }
        
        // 是否绑定的有电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.YES);
            userBatteryDetail.setBatteryInfo(electricityBatteryService.queryByUid(userInfo.getUid()));
        } else {
            userBatteryDetail.setIsBindBattery(UserInfoResultVO.NO);
        }
        
        // 是否缴纳租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_NO) && Objects.equals(
                userInfo.getCarBatteryDepositStatus(), YesNoEnum.NO.getCode())) {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.NO);
        } else {
            userCarDetail.setIsCarDeposit(UserInfoResultVO.YES);
        }
        
        // V3 版本
        if (CommonConstant.SWITCH_VERSION.equals(switchVersion)) {
            // 是否存在滞纳金
            boolean exitUnpaid = carRentalPackageOrderSlippageService.isExitUnpaid(userInfo.getTenantId(),
                    userInfo.getUid());
            if (exitUnpaid) {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.YES.getCode());
            } else {
                userCarDetail.setCarRentalPackageSlippage(YesNoEnum.NO.getCode());
            }
            // 是否购买租车套餐、是否过期
            CarRentalPackageMemberTermPo carRentalPackageMemberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(
                    userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isEmpty(carRentalPackageMemberTermPo)) {
                userCarDetail.setIsCarMemberCard(UserInfoResultVO.NO);
                userCarDetail.setIsCarMemberCardExpire(UserInfoResultVO.NO);
            } else {
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(carRentalPackageMemberTermPo.getStatus())
                        || StringUtils.isBlank(carRentalPackageMemberTermPo.getRentalPackageOrderNo())) {
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
            
            // 是否绑定车辆
            ElectricityCar car = electricityCarService.selectByUid(userInfo.getTenantId(), userInfo.getUid());
            if (ObjectUtils.isNotEmpty(car)) {
                userCarDetail.setIsRentCar(UserInfoResultVO.YES);
                userCarDetail.setCarSN(car.getSn());
            } else {
                userCarDetail.setIsRentCar(UserInfoResultVO.NO);
            }
            
        }
        
        threadPool.execute(() -> {
            userBatteryMemberCardPackageService.batteryMembercardTransform(userInfo.getUid());
            // 保险转换
            List<InsuranceUserInfo> list = insuranceUserInfoService.listByUid(userInfo.getUid());
            if (CollUtil.isEmpty(list)) {
                log.warn("Current User Not HaveInsurance, uid is {}", userInfo.getUid());
                return;
            }
            list.stream().forEach(e->{
                insuranceUserInfoService.userInsuranceExpireAutoConvert(e);
            });
        });
        
        return Triple.of(true, "", userInfoResult);
    }
    
    private void acquireBatteryFreeDepositResult(UserBatteryDeposit userBatteryDeposit, UserInfo userInfo,
            UserInfoResultVO userInfoResult) {
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userBatteryDeposit.getDepositType(),
                UserBatteryDeposit.DEPOSIT_TYPE_FREE)) {
            return;
        }
        
        userInfoResult.setBatteryFreeApplyTime(userBatteryDeposit.getApplyDepositTime());
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            return;
        }
        
        // 若免押状态为待冻结
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_INIT) || Objects.equals(
                freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE)) {
            // 获取电池免押结果
            FreeDepositUserInfoVo freeDepositUserInfoVo = null;
            Triple<Boolean, String, Object> freeBatteryDepositOrderResult = freeDepositOrderService.acquireUserFreeBatteryDepositStatus();
            if (Boolean.TRUE.equals(freeBatteryDepositOrderResult.getLeft())) {
                freeDepositUserInfoVo = (FreeDepositUserInfoVo) freeBatteryDepositOrderResult.getRight();
            }
            
            userInfoResult.setBatteryFreeStatus(
                    Objects.nonNull(freeDepositUserInfoVo) ? freeDepositUserInfoVo.getBatteryDepositAuthStatus()
                            : null);
        } else {
            userInfoResult.setBatteryFreeStatus(freeDepositOrder.getAuthStatus());
        }
    }
    
    
    private void acquireBatteryFreeDepositResultV2(UserBatteryDeposit userBatteryDeposit, UserInfo userInfo,
            UserInfoResultVO userInfoResult) {
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userBatteryDeposit.getDepositType(),
                UserBatteryDeposit.DEPOSIT_TYPE_FREE)) {
            return;
        }
        
        userInfoResult.setBatteryFreeApplyTime(userBatteryDeposit.getApplyDepositTime());
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            return;
        }
        userInfoResult.setBatteryFreeStatus(freeDepositOrder.getAuthStatus());
        
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
                return Triple.of(false, "ELECTRICITY.0045",
                        String.format("用户已绑定电池【%s】, 请先解绑！", battery.getSn()));
            }
            
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                    userInfo.getUid());
            Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                    userInfo, userBatteryMemberCard, Objects.isNull(userBatteryMemberCard) ? null
                            : batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId()),
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
        // 清除逾期用户备注
        if (Objects.equals(rentStatus, UserInfo.BATTERY_RENT_STATUS_NO)) {
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.CAR.getCode(),
                        TenantContextHolder.getTenantId());
            } else {
                overdueUserRemarkPublish.publish(userInfo.getUid(), OverdueType.BATTERY.getCode(),
                        TenantContextHolder.getTenantId());
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
    public Integer existsByIdNumber(String idNumber, Integer tenantId) {
        return userInfoMapper.existsByIdNumber(idNumber, tenantId);
    }
    
    @Slave
    @Override
    public R queryDetailsBasicInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromDbIncludeDelUser(uid);
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
        
        UserTurnoverVo userTurnoverVo = queryUserConsumptionPay(uid);
        BeanUtils.copyProperties(userTurnoverVo, vo);
        
        vo.setCarRentalPackageOrderAmountTotal(
                carRentalPackageOrderBizService.queryAmountTotalByUid(userInfo.getTenantId(), userInfo.getUid()));
        vo.setCarRentalPackageOrderSlippageAmountTotal(
                carRentalPackageOrderSlippageService.selectPaySuccessAmountTotal(userInfo.getTenantId(),
                        userInfo.getUid()));
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(uid);
        if (Objects.nonNull(enterpriseChannelUserVO)) {
            vo.setEnterpriseChannelUserInfo(enterpriseChannelUserVO);
        }
        
        // 设置电子签名信息
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryUserEsignRecordFromDB(userInfo.getUid(),
                Long.valueOf(TenantContextHolder.getTenantId()));
        if (Objects.nonNull(eleUserEsignRecord)) {
            vo.setSignFlowId(eleUserEsignRecord.getSignFlowId());
            vo.setSignFinishStatus(Objects.equals(1, eleUserEsignRecord.getSignFinishStatus())
                    ? SignStatusEnum.SIGNED_COMPLETED.getCode() : SignStatusEnum.SIGNED_INCOMPLETE.getCode());
        } else {
            vo.setSignFinishStatus(SignStatusEnum.UNSIGNED.getCode());
        }
        
        List<UserOauthBind> userOauthBinds = userOauthBindService.selectListByUidAndPhone(vo.getPhone(), uid, tenantId);
        Map<Integer, UserOauthBind> sourceMap = Optional.ofNullable(userOauthBinds).orElse(Collections.emptyList())
                .stream().collect(Collectors.toMap(UserOauthBind::getSource, Function.identity(), (k1, k2) -> k1));
        
        vo.setBindWX(this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_WX_PRO)) ? UserOauthBind.STATUS_BIND_VX
                : UserOauthBind.STATUS_UN_BIND_VX);
        vo.setBindAlipay(
                this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_ALI_PAY)) ? UserOauthBind.STATUS_BIND_ALIPAY
                        : UserOauthBind.STATUS_UN_BIND_ALIPAY);
        
        // 邀请人是否可被修改
        Integer inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_USER_FOR_VO.getCode();
        Integer modifyInviterRecordIsView = NumberConstant.ONE;
        MerchantInviterVO merchantInviterVO = userInfoExtraService.querySuccessInviter(uid);
        if (Objects.isNull(merchantInviterVO)) {
            vo.setCanModifyInviter(MerchantInviterCanModifyEnum.MERCHANT_INVITER_CAN_NOT_MODIFY.getCode());
        } else {
            vo.setCanModifyInviter(MerchantInviterCanModifyEnum.MERCHANT_INVITER_CAN_MODIFY.getCode());
            if (Objects.equals(merchantInviterVO.getInviterSource(),
                    UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
                inviterSource = MerchantInviterSourceEnum.MERCHANT_INVITER_SOURCE_MERCHANT_FOR_VO.getCode();
            }
        }

        if (Objects.equals(vo.getCanModifyInviter(), MerchantInviterCanModifyEnum.MERCHANT_INVITER_CAN_MODIFY.getCode()) || existsModifyRecordByUid(uid)) {
            modifyInviterRecordIsView = NumberConstant.ZERO;
        }

        // 邀请记录是否显示
        vo.setModifyInviterRecordIsView(modifyInviterRecordIsView);
        // 邀请人名称
        vo.setInviterName(queryFinalInviterUserName(uid));
        // 邀请人来源
        vo.setInviterSource(inviterSource);

        // 是否限制换电套餐购买次数
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        vo.setEleLimit(
                Objects.isNull(userInfoExtra) ? UserInfoExtraConstant.ELE_LIMIT_NO : userInfoExtra.getEleLimit());
        // 紧急联系人
        vo.setEmergencyContactList(emergencyContactService.listVOByUid(uid));
        vo.setRemark(Objects.nonNull(userInfoExtra) ? userInfoExtra.getRemark() : null);
        // 查询已删除/已注销
        vo.setUserStatus(this.queryUserDelStatus(uid));

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.nonNull(userBatteryDeposit)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            vo.setPayType(Objects.nonNull(eleDepositOrder) ? eleDepositOrder.getPayType() : null);
        }

        return R.ok(vo);
    }
    
    private boolean existsModifyRecordByUid(Long uid) {
        return merchantInviterModifyRecordService.existsModifyRecordByUid(uid);
    }

    /**
     * 获取是否绑定第三方
     *
     * @param oauthBind
     * @return
     * @author caobotao.cbt
     * @date 2024/8/8 09:25
     */
    private boolean getIsBindThird(UserOauthBind oauthBind) {
        return Objects.nonNull(oauthBind) && StringUtils.isNotBlank(oauthBind.getThirdId());
    }
    
    private UserTurnoverVo queryUserConsumptionPay(Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        UserTurnoverVo userTurnoverVo = new UserTurnoverVo();
        // 用户电池总套餐消费额
        CompletableFuture<Void> queryMemberCardPayAmount = CompletableFuture.runAsync(() -> {
            // 用户套餐总支付金额
            BigDecimal pay = electricityMemberCardOrderService.queryTurnOver(tenantId, id);
            BigDecimal refund = batteryMembercardRefundOrderService.selectUserTotalRefund(tenantId, id);
            userTurnoverVo.setMemberCardTurnover(pay.subtract(refund));
            
            userBatteryMemberCardPackageService.batteryMembercardTransform(id);
        }, threadPool).exceptionally(e -> {
            log.error("MEMBER CARD ORDER ERROR! query turn over error", e);
            return null;
        });
        
        // 用户租车总套餐消费额
        //        CompletableFuture<Void> queryCarMemberCardPayAmount = CompletableFuture.runAsync(() -> {
        ////            BigDecimal pay = carMemberCardOrderService.queryTurnOver(tenantId, id);
        ////            userTurnoverVo.setCarMemberCardTurnover(pay);
        //        }, threadPool).exceptionally(e -> {
        //            log.error("CAR MEMBER CARD ORDER ERROR! query turn over error", e);
        //            return null;
        //        });
        
        // 用户电池服务费消费额
        CompletableFuture<Void> queryBatteryServiceFeePayAmount = CompletableFuture.runAsync(() -> {
            BigDecimal pay = eleBatteryServiceFeeOrderService.queryUserTurnOver(tenantId, id);
            userTurnoverVo.setBatteryServiceFee(pay);
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });
        
        // 等待所有线程停止 thenAcceptBoth方法会等待a,b线程结束后获取结果
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryMemberCardPayAmount,
                queryBatteryServiceFeePayAmount);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }
        
        return userTurnoverVo;
    }
    
    @Override
    public R unbindOpenId(UnbindOpenIdRequest unbindOpenIdRequest) {
        Long uid = unbindOpenIdRequest.getUid();
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(uid,
                TenantContextHolder.getTenantId(), unbindOpenIdRequest.getSource());
        
        if (Objects.nonNull(userOauthBind) && Objects.nonNull(userOauthBind.getThirdId())) {
            // 解绑微信成功后 强制用户重新登录
            List<UserOauthBind> userOauthBinds = userOauthBindService.queryListByUidAndSource(uid,
                    unbindOpenIdRequest.getSource());
            if (DataUtil.collectionIsUsable(userOauthBinds)) {
                clearUserOauthBindToken(userOauthBinds, CacheConstant.CLIENT_ID);
            }
            DbUtils.dbOperateSuccessThenHandleCache(
                    userOauthBindService.updateOpenIdByUid(StringUtils.EMPTY, UserOauthBind.STATUS_UN_BIND,
                            userOauthBind.getUid(), unbindOpenIdRequest.getSource(), TenantContextHolder.getTenantId()),
                    i -> {
                        // 添加解绑操作记录
                        Integer operateContent;
                        String oldOperateInfo;
                        String newOperateInfo;
                        
                        if (unbindOpenIdRequest.getSource().equals(UserOauthBind.SOURCE_WX_PRO)) {
                            operateContent = EleUserOperateHistoryConstant.OPERATE_CONTENT_UNBIND_VX;
                            oldOperateInfo = EleUserOperateHistoryConstant.UNBIND_VX_OLD_OPERATION;
                            newOperateInfo = EleUserOperateHistoryConstant.UNBIND_VX_NEW_OPERATION;
                        } else if (unbindOpenIdRequest.getSource().equals(UserOauthBind.SOURCE_ALI_PAY)) {
                            operateContent = EleUserOperateHistoryConstant.OPERATE_CONTENT_UNBIND_ALIPAY;
                            oldOperateInfo = EleUserOperateHistoryConstant.UNBIND_ALIPAY_OLD_OPERATION;
                            newOperateInfo = EleUserOperateHistoryConstant.UNBIND_ALIPAY_NEW_OPERATION;
                        } else {
                            log.warn("UserInfoServiceImpl.unbindOpenId WARN! source={} not fund",
                                    unbindOpenIdRequest.getSource());
                            return;
                        }
                        
                        EleUserOperateHistory eleUserOperateHistory = buildEleUserOperateHistory(userInfo,
                                operateContent, oldOperateInfo, newOperateInfo);
                        eleUserOperateHistoryService.asyncHandleEleUserOperateHistory(eleUserOperateHistory);
                    });
            Map<String, Object> map = new HashMap<>();
            map.put("username", userInfo.getName());
            map.put("phone", userInfo.getPhone());
            operateRecordUtil.record(null, map);
        }
        return R.ok();
    }
    
    @Slave
    @Override
    public List<UserInfo> listByUids(List<Long> uidList, Integer tenantId) {
        return userInfoMapper.selectListByUids(uidList, tenantId);
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
            List<String> tokens = redisService.getWithList(
                    TokenConstant.CACHE_LOGIN_TOKEN_LIST_KEY + finalClientId + e.getTenantId() + ":" + thirdId,
                    String.class);
            if (DataUtil.collectionIsUsable(tokens)) {
                tokens.forEach(s -> {
                    redisService.delete(TokenConstant.CACHE_LOGIN_TOKEN_KEY + finalClientId + s);
                });
            }
        });
        
    }
    
    private EleUserOperateHistory buildEleUserOperateHistory(UserInfo userInfo, Integer operateContent,
            String oldOperateInfo, String newOperateInfo) {
        return EleUserOperateHistory.builder().operateType(EleUserOperateHistoryConstant.OPERATE_TYPE_USER)
                .operateModel(EleUserOperateHistoryConstant.OPERATE_MODEL_USER_ACCOUNT).operateContent(operateContent)
                .oldOperateInfo(oldOperateInfo).newOperateInfo(newOperateInfo).uid(userInfo.getUid()).operatorName(
                        Objects.nonNull(SecurityUtils.getUserInfo()) ? SecurityUtils.getUserInfo().getUsername()
                                : StringUtils.EMPTY).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
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
        
        // 电子签名未完成时，提示先完成电子签名
        EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryUserEsignRecordFromDB(userInfo.getUid(), Long.valueOf(TenantContextHolder.getTenantId()));
        if (Objects.equals(CONSTRAINT_NOT_FORCE, updateUserPhoneRequest.getForceUpdate()) && Objects.nonNull(eleUserEsignRecord) && Objects.equals(
                eleUserEsignRecord.getSignFinishStatus(), ESIGN_STATUS_FAILED)) {
            return R.fail("100329", "请先完成电子签名");
        }

        // 取消旧手机电子签名流程
        if (Objects.equals(CONSTRAINT_FORCE, updateUserPhoneRequest.getForceUpdate()) && Objects.nonNull(eleUserEsignRecord) && !Objects.equals(
                eleUserEsignRecord.getSignFinishStatus(), ESIGN_STATUS_SUCCESS)) {
            eleUserEsignRecordService.cancelEsignFlow(eleUserEsignRecord.getId());
        }

        // 更新用戶
        DbUtils.dbOperateSuccessThenHandleCache(
                userInfoMapper.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone,
                        System.currentTimeMillis()), i -> {
                    redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
                });
        
        User user = userService.queryByUidFromCache(uid);
        DbUtils.dbOperateSuccessThenHandleCache(
                userService.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone), i -> {
                    if (Objects.nonNull(user)) {
                        redisService.delete(CacheConstant.CACHE_USER_UID + uid);
                        redisService.delete(CacheConstant.CACHE_USER_PHONE + TenantContextHolder.getTenantId() + ":"
                                + user.getPhone() + ":" + user.getUserType());
                    }
                });
        
        // 修改美团订单关联手机号
        meiTuanRiderMallOrderService.updatePhone(oldPhone, phone, userInfo.getTenantId());
        
        // 更新成功后 强制用户重新登录
        List<UserOauthBind> userOauthBinds = userOauthBindService.queryListByUid(uid);
        if (DataUtil.collectionIsUsable(userOauthBinds)) {
            clearUserOauthBindToken(userOauthBinds, CacheConstant.CLIENT_ID);
        }
        
        userOauthBindService.updatePhoneByUid(TenantContextHolder.getTenantId(), uid, phone);
        
        // 添加更換手机号操作记录
        EleUserOperateHistory eleUserOperateHistory = buildEleUserOperateHistory(userInfo,
                EleUserOperateHistoryConstant.OPERATE_CONTENT_UPDATE_PHONE, userInfo.getPhone(), phone);
        eleUserOperateHistoryService.asyncHandleEleUserOperateHistory(eleUserOperateHistory);
        eleUserOperateHistoryService.asyncHandleUpdateUserPhone(TenantContextHolder.getTenantId(), uid, phone,
                oldPhone);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("username", userInfo.getName());
            map.put("phone", phone);
            operateRecordUtil.record(MapUtil.of("phone", oldPhone), map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }

        // 给第三方推送用户信息
        pushDataToThirdService.asyncPushUserInfo(TtlTraceIdSupport.get(), TenantContextHolder.getTenantId(), userInfo.getUid(), ThirdPartyOperatorTypeEnum.USER_EDIT.getType());

        return R.ok();
    }
    
    @Slave
    @Override
    public R queryDetailsBatteryInfo(Long uid) {
        UserInfo userInfo = this.queryByUidFromDbIncludeDelUser(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        DetailsBatteryInfoVo vo = new DetailsBatteryInfoVo();
        vo.setUid(userInfo.getUid());
        
        // 用户电池押金
        CompletableFuture<Void> queryUserBatteryDeposit = CompletableFuture.runAsync(() -> {
            queryUserBatteryDeposit(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery deposit error!", e);
            return null;
        });
        
        // 用户会员信息
        CompletableFuture<Void> queryUserBatteryMemberCard = CompletableFuture.runAsync(() -> {
            queryUserBatteryMemberCard(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery member card error!", e);
            return null;
        });
        
        // 用户电池信息
        CompletableFuture<Void> queryUserBattery = CompletableFuture.runAsync(() -> {
            queryUserBattery(vo, userInfo);
        }, threadPool).exceptionally(e -> {
            log.error("DETAILS BATTERY INFO ERROR! query user battery error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryDeposit,
                queryUserBatteryMemberCard, queryUserBattery);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }
        
        return R.ok(vo);
    }
    
    @Slave
    @Override
    public R userInfoSearch(Long size, Long offset, String name, String keyWords) {
        List<UserInfoSearchVo> qeury = userInfoMapper.userInfoSearch(size, offset, name,
                TenantContextHolder.getTenantId(), keyWords);
        if (ObjectUtils.isNotEmpty(qeury)) {
            qeury.stream().forEach(userInfoSearchVo -> {
                String nameAndPhone =
                        userInfoSearchVo.getName() + StringConstant.FORWARD_SLASH + userInfoSearchVo.getPhone();
                userInfoSearchVo.setNameAndPhone(nameAndPhone);
            });
        }
        
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
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                userInfo.getUid());
        InstallmentRecord installmentRecord = installmentRecordService.queryRecordWithStatusForUser(userInfo.getUid(),
                CollUtil.newArrayList(InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN));
        if (Objects.isNull(userBatteryMemberCard)) {
            vo.setIsViewOffLineAgree(Objects.nonNull(installmentRecord) ? DetailsBatteryInfoVo.IS_VIEW_OFF_LINE_AGREE : DetailsBatteryInfoVo.NOT_IS_VIEW_OFF_LINE_AGREE);
            return;
        }

        if (userBatteryMemberCard.getMemberCardExpireTime() <= System.currentTimeMillis()){
            vo.setIsViewOffLineAgree(Objects.nonNull(installmentRecord) ? DetailsBatteryInfoVo.IS_VIEW_OFF_LINE_AGREE : DetailsBatteryInfoVo.NOT_IS_VIEW_OFF_LINE_AGREE);
        }

        vo.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        vo.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        vo.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
        vo.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        vo.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
        vo.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        //        vo.setUserBatteryServiceFee(serviceFeeUserInfoService.queryUserBatteryServiceFee(userInfo));
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                userBatteryMemberCard.getMemberCardId());
        vo.setCardName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
        vo.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
//        vo.setDownPayment(Objects.isNull(batteryMemberCard) ? BigDecimal.valueOf(0.0) : batteryMemberCard.getDownPayment());
//        vo.setValidDays(Objects.isNull(batteryMemberCard) ? 0 : batteryMemberCard.getValidDays());
//        vo.setBusinessType(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getBusinessType());
//        vo.setRentPrice(Objects.isNull(batteryMemberCard) ? BigDecimal.valueOf(0.0) : batteryMemberCard.getRentPrice());

        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(
                userBatteryMemberCard.getOrderId());
        vo.setMemberCardCreateTime(
                Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());



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
        
        // 租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return;
        }
        
        // 租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return;
        }
        
        // 檢查当前用户是否为企业用户，若为企业用户，则不解绑加盟商
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryEnterpriseChannelUser(uid);
        if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode())) {
            return;
        }
        
        // 若租车和租电押金都退了，则解绑用户所属加盟商
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
        if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_EXPIRE_TIME)) {
            userBatteryInfoVOS = userInfoMapper.queryListByMemberCardExpireTime(userInfoQuery);
        } else if (Objects.nonNull(userInfoQuery.getSortType()) && Objects.equals(userInfoQuery.getSortType(),
                UserInfoQuery.SORT_TYPE_CAR_EXPIRE_TIME)) {
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
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(
                    userBatteryInfoVO.getUid());
            
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(
                    Objects.isNull(userBatteryInfoVO.getMemberCardId()) ? 0
                            : userBatteryInfoVO.getMemberCardId().intValue());
            
            UserInfoExcelVO excelVo = new UserInfoExcelVO();
            excelVo.setId(index);
            excelVo.setPhone(userBatteryInfoVO.getPhone());
            excelVo.setName(userBatteryInfoVO.getName());
            excelVo.setBatteryDeposit(Objects.nonNull(userBatteryDeposit) ? userBatteryDeposit.getBatteryDeposit()
                    : BigDecimal.valueOf(0));
            excelVo.setCardName(Objects.nonNull(electricityMemberCard) ? electricityMemberCard.getName() : "");
            excelVo.setNowElectricityBatterySn(userBatteryInfoVO.getNowElectricityBatterySn());
            excelVo.setInviterUserName(queryFinalInviterUserName(userBatteryInfoVO.getUid()));
            
            if (Objects.nonNull(userBatteryInfoVO.getMemberCardExpireTime()) && !Objects.equals(
                    userBatteryInfoVO.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                excelVo.setMemberCardExpireTime(
                        simpleDateFormat.format(new Date(userBatteryInfoVO.getMemberCardExpireTime())));
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
            EasyExcel.write(outputStream, UserInfoExcelVO.class).sheet("sheet")
                    .registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(userInfoExcelVOS);
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
        
        List<UserCarRentalPackageDO> userCarRentalPackageDOList = carRentalPackageMemberTermService.queryUserCarRentalPackageList(
                userInfoQuery);
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
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarDepositStatus() == 1
                        ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS
                        : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            } else if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(userCarRentalPackageDO.getPackageType())) {
                userCarRentalInfoExcelVO.setPackageType(CarRentalPackageExlConstant.PACKAGE_TYPE_CAR_WITH_BATTERY);
                userCarRentalInfoExcelVO.setDepositStatus(userCarRentalPackageDO.getCarBatteryDepositStatus() == 0
                        ? CarRentalPackageExlConstant.PACKAGE_DEPOSIT_PAID_STATUS
                        : CarRentalPackageExlConstant.PACKAGE_DEPOSIT_UNPAID_STATUS);
            }
            
            // 设置套餐冻结状态
            if (MemberTermStatusEnum.FREEZE.getCode().equals(userCarRentalPackageDO.getPackageStatus())) {
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_FROZE_STATUS);
            } else {
                userCarRentalInfoExcelVO.setPackageFreezeStatus(CarRentalPackageExlConstant.PACKAGE_UN_FREEZE_STATUS);
            }
            
            userCarRentalInfoExcelVO.setCurrentCar(userCarRentalPackageDO.getCarModel());
            
            userCarRentalInfoExcelVO.setPackageExpiredTime(
                    simpleDateFormat.format(new Date(userCarRentalPackageDO.getPackageExpiredTime())));
            
            // 获取电池信息
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(
                    userCarRentalPackageDO.getUid());
            userCarRentalInfoExcelVO.setCurrentBattery(
                    Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
            
            // 获取套餐名称
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(
                    userCarRentalPackageDO.getPackageId());
            if (Objects.nonNull(carRentalPackagePo)) {
                userCarRentalInfoExcelVO.setPackageName(carRentalPackagePo.getName());
            }
            
            // 获取保险信息
            InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(
                    userCarRentalPackageDO.getUid(), userCarRentalPackageDO.getPackageType());
            userCarRentalInfoExcelVO.setInsuranceStatus(
                    Objects.isNull(insuranceUserInfoVo) ? "" : getInsuranceStatusDesc(insuranceUserInfoVo.getIsUse()));
            userCarRentalInfoExcelVO.setInsuranceExpiredTime(Objects.isNull(insuranceUserInfoVo) ? ""
                    : simpleDateFormat.format(new Date(insuranceUserInfoVo.getInsuranceExpireTime())));
            
            // 获取用户所属加盟商
            Franchisee franchisee = franchiseeService.queryByIdFromCache(userCarRentalPackageDO.getFranchiseeId());
            userCarRentalInfoExcelVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            
            userCarRentalInfoExcelVO.setUserAuthTime(
                    simpleDateFormat.format(new Date(userCarRentalPackageDO.getUserAuthTime())));
            
            userCarRentalInfoExcelVOS.add(userCarRentalInfoExcelVO);
            
        }
        
        String fileName = "会员租车列表报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, UserCarRentalInfoExcelVO.class).sheet("sheet")
                    .registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(userCarRentalInfoExcelVOS);
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
            
            if (Objects.nonNull(inviterUid) && !Objects.equals(inviterUid, NumberConstant.ZERO_L) && Objects.nonNull(
                    activitySource) && !Objects.equals(activitySource, NumberConstant.ZERO)) {
                if (Objects.equals(activitySource, UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode())) {
                    MerchantInviterVO merchantInviterVO = userInfoExtraService.judgeInviterTypeForMerchant(uid,
                            inviterUid, userInfoExtra.getTenantId());
                    if (Objects.nonNull(merchantInviterVO)) {
                        inviterName = merchantInviterVO.getInviterName();
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

        // 查询已删除/已注销
        List<Long> uidList = userEleInfoVOS.stream().map(UserEleInfoVO::getUid).collect(Collectors.toList());
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uidList,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        //获取用户电池套餐相关信息
        CompletableFuture<Void> queryUserBatteryMemberCardInfo = CompletableFuture.runAsync(() -> {
            userEleInfoVOS.forEach(item -> {
                // 获取用户所属加盟商
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
                
                if (Objects.nonNull(item.getMemberCardStatus())) {
                    if (Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                        // 冻结
                        item.setMemberCardFreezeStatus(1);
                    } else {
                        // 正常
                        item.setMemberCardFreezeStatus(0);
                    }
                }
                
                // 获取用户当前绑定的套餐
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                        item.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard)) {
                    item.setMemberCardName(batteryMemberCard.getName());
                    item.setLimitCount(batteryMemberCard.getLimitCount());
                    item.setUseCount(batteryMemberCard.getUseCount());
                }
                // 邀请人
                item.setInviterUserName(queryFinalInviterUserName(item.getUid()));
                
                // 设置企业信息
                EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(
                        item.getUid());
                if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(
                        enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
                    item.setEnterpriseName(enterpriseChannelUserVO.getEnterpriseName());
                }

                UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(item.getUid());
                item.setRemark(Objects.nonNull(userInfoExtra) ? userInfoExtra.getRemark() : null);

                // 查询已删除/已注销
                Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
                if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(item.getUid()) && Objects.nonNull(userStatusMap.get(item.getUid()))) {
                    UserDelStatusDTO userDelStatusDTO = userStatusMap.get(item.getUid());
                    item.setDelTime(userDelStatusDTO.getDelTime());
                    userStatus = userDelStatusDTO.getUserStatus();
                }
                item.setUserStatus(userStatus);

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
                
                InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(
                        item.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfoVo)) {
                    item.setIsUse(insuranceUserInfoVo.getIsUse());
                    item.setInsuranceExpireTime(insuranceUserInfoVo.getInsuranceExpireTime());
                }
                
                // 设置已缴纳押金的用户的具体状态，为实缴还是免押，实缴：1  免押：2
                if (Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, item.getBatteryDepositStatus())) {
                    UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(
                            item.getUid());
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
                List<UserInfoGroupNamesBO> namesBOList = userInfoGroupDetailService.listGroupByUid(
                        UserInfoGroupDetailQuery.builder().uid(item.getUid()).build());
                List<UserInfoGroupIdAndNameVO> groupVoList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(namesBOList)) {
                    groupVoList = namesBOList.stream()
                            .map(bo -> UserInfoGroupIdAndNameVO.builder().id(bo.getGroupId()).name(bo.getGroupName())
                                    .groupNo(bo.getGroupNo()).build()).collect(Collectors.toList());
                }
                
                item.setGroupList(CollectionUtils.isEmpty(groupVoList) ? Collections.emptyList() : groupVoList);
            });
        }, threadPool).exceptionally(e -> {
            log.error("ELE ERROR! query user group info error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryUserBatteryMemberCardInfo,
                queryUserOtherInfo, queryUserGroupInfo);
        
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
    @Slave
    public R queryEleListForPro(UserInfoQuery userInfoQuery) {
        List<UserEleInfoVO> userEleInfoVOS = userInfoMapper.queryEleList(userInfoQuery);
        if (ObjectUtil.isEmpty(userEleInfoVOS)) {
            return R.ok(Collections.emptyList());
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        List<Long> uidList = new ArrayList<>(userEleInfoVOS.size());
        List<UserBatteryMemberCard> userBatteryMemberCardList = new ArrayList<>(userEleInfoVOS.size());
        List<UserBatteryDeposit> userBatteryDepositList = new ArrayList<>(userEleInfoVOS.size());
        List<UserOauthBindListQuery> userOauthBindListQueryList = new ArrayList<>(userEleInfoVOS.size());

        // 封装用户列表
        List<UserEleInfoProVO> userInfoList = userEleInfoVOS.stream().map(item -> {
            Long uid = item.getUid();

            UserEleInfoProVO eleInfoProVO = UserEleInfoProVO.builder().uid(item.getUid()).name(item.getName()).phone(item.getPhone()).memberCardId(item.getMemberCardId())
                    .memberCardExpireTime(item.getMemberCardExpireTime()).remainingNumber(item.getRemainingNumber()).batteryRentStatus(item.getBatteryRentStatus()).batteryDepositStatus(item.getBatteryDepositStatus())
                    .usableStatus(item.getUsableStatus()).build();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
            if (Objects.nonNull(batteryMemberCard)) {
                eleInfoProVO.setMemberCardName(batteryMemberCard.getName());
                eleInfoProVO.setLimitCount(batteryMemberCard.getLimitCount());
            }

            // 押金类型
            if (Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, item.getBatteryDepositStatus())) {
                UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(item.getUid());
                if (Objects.nonNull(userBatteryDeposit)) {
                    eleInfoProVO.setFreeDeposit(userBatteryDeposit.getDepositType());
                }
            }

            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
            if (Objects.nonNull(userBatteryMemberCard)) {
                userBatteryMemberCardList.add(userBatteryMemberCard);
            }
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
            if (Objects.nonNull(userBatteryDeposit)) {
                userBatteryDepositList.add(userBatteryDeposit);
            }

            if (Objects.isNull(userBatteryMemberCard) || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()
            ) {
                InstallmentRecord installmentRecord = installmentRecordService.queryRecordWithStatusForUser(uid,
                        CollUtil.newArrayList(InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN));
                eleInfoProVO.setIsViewOffLineAgree(Objects.nonNull(installmentRecord) ? DetailsBatteryInfoVo.IS_VIEW_OFF_LINE_AGREE : DetailsBatteryInfoVo.NOT_IS_VIEW_OFF_LINE_AGREE);
            }

            // 封装basicInfo
            UserBasicInfoEleProVO basicInfo = UserBasicInfoEleProVO.builder().name(item.getName()).phone(item.getPhone()).batteryDepositStatus(item.getBatteryDepositStatus())
                    .build();
            UserInfo userInfo = this.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getFranchiseeId())) {
                basicInfo.setFranchiseeId(userInfo.getFranchiseeId());
                Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    basicInfo.setFranchiseeName(franchisee.getName());
                    basicInfo.setModelType(franchisee.getModelType());
                }
            }

            if (Objects.nonNull(userInfo) && Objects.nonNull(userInfo.getStoreId())) {
                basicInfo.setStoreId(userInfo.getStoreId());
                Store store = storeService.queryByIdFromCache(userInfo.getStoreId());
                if (Objects.nonNull(store)) {
                    basicInfo.setStoreName(store.getName());
                }
            }

            EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(uid);
            if (Objects.nonNull(enterpriseChannelUserVO)) {
                basicInfo.setEnterpriseChannelUserInfo(enterpriseChannelUserVO);
            }

            eleInfoProVO.setBasicInfo(basicInfo);

            uidList.add(uid);
            UserOauthBindListQuery query = UserOauthBindListQuery.builder().phone(item.getPhone()).uid(uid).build();
            userOauthBindListQueryList.add(query);

            return eleInfoProVO;
        }).collect(Collectors.toList());

        // 用户绑定的电池型号对应的短型号
        Map<Long, List<String>> userShortBatteryMap = userBatteryTypeService.listShortBatteryByUidList(uidList, tenantId);
        // 查询用户电池
        Map<Long, ElectricityBattery> userBatteryMap = electricityBatteryService.listUserBatteryByUidList(uidList, tenantId);
        // 查询认证信息
        Map<Long, List<UserOauthBind>> userOauthMap = null;
        if (CollectionUtils.isNotEmpty(userOauthBindListQueryList)) {
            List<UserOauthBind> userOauthBinds = userOauthBindService.listByUidAndPhoneList(userOauthBindListQueryList, tenantId);
            if (CollectionUtils.isNotEmpty(userOauthBinds)) {
                userOauthMap = userOauthBinds.stream().collect(Collectors.groupingBy(UserOauthBind::getUid));
            }
        }
        // 查询企业信息
        Map<Long, EnterpriseChannelUserVO> enterpriseChannelUserMap = null;
        List<EnterpriseChannelUserVO> enterpriseChannelUserList = enterpriseChannelUserService.listByUidList(uidList, tenantId);
        if (CollectionUtils.isNotEmpty(enterpriseChannelUserList)) {
            enterpriseChannelUserMap = enterpriseChannelUserList.stream().collect(Collectors.toMap(EnterpriseChannelUserVO::getUid, Function.identity(), (k1, k2) -> k1));
        }

        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uidList,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        // 查询用户电池、电池型号及当前套餐，滞纳金，认证信息
        Map<Long, List<UserOauthBind>> finalUserOauthMap = userOauthMap;
        Map<Long, EnterpriseChannelUserVO> finalEnterpriseChannelUserMap = enterpriseChannelUserMap;
        CompletableFuture<Void> queryBatteryInfo = CompletableFuture.runAsync(() -> {
            userInfoList.forEach(item -> {
                Long uid = item.getUid();
                UserBasicInfoEleProVO basicInfo = item.getBasicInfo();

                item.setBatteryInfo(getEleBatteryInfoPro(item, userShortBatteryMap, userBatteryMap));
                // 滞纳金
                item.setBatteryServiceFee(serviceFeeUserInfoService.queryUserBatteryServiceFee(uid));
                // 认证信息
                if (MapUtils.isNotEmpty(finalUserOauthMap) && finalUserOauthMap.containsKey(uid)) {
                    List<UserOauthBind> userOauthBinds = finalUserOauthMap.get(uid);
                    Map<Integer, UserOauthBind> sourceMap = Optional.ofNullable(userOauthBinds).orElse(Collections.emptyList()).stream()
                            .collect(Collectors.toMap(UserOauthBind::getSource, Function.identity(), (k1, k2) -> k1));
                    if (MapUtils.isNotEmpty(sourceMap)) {
                        basicInfo.setBindWX(this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_WX_PRO)) ? UserOauthBind.STATUS_BIND_VX : UserOauthBind.STATUS_UN_BIND_VX);
                        basicInfo.setBindAlipay(
                                this.getIsBindThird(sourceMap.get(UserOauthBind.SOURCE_ALI_PAY)) ? UserOauthBind.STATUS_BIND_ALIPAY : UserOauthBind.STATUS_UN_BIND_ALIPAY);
                    }
                }
                // 企业信息
                if (MapUtils.isNotEmpty(finalEnterpriseChannelUserMap) && finalEnterpriseChannelUserMap.containsKey(uid)) {
                    basicInfo.setEnterpriseChannelUserInfo(finalEnterpriseChannelUserMap.get(uid));
                }

                // 查询已删除/已注销
                Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
                if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(uid) && Objects.nonNull(userStatusMap.get(uid))) {
                    UserDelStatusDTO userDelStatusDTO = userStatusMap.get(uid);
                    item.setDelTime(userDelStatusDTO.getDelTime());
                    userStatus = userDelStatusDTO.getUserStatus();
                }
                item.setUserStatus(userStatus);
            });
        }, threadPoolPro).exceptionally(e -> {
            log.error("ELE ERROR! query user battery info for pro error!", e);
            return null;
        });

        // 押金是否可退
        Map<Long, UserBatteryDeposit> userBatteryDepositMap = null;
        Map<String, EleDepositOrder> eleDepositOrderMap = null;
        Map<String, Double> orderPayTransAmtMap = null;
        Map<String, List<EleRefundOrder>> eleRefundOrderMap = null;
        if (CollectionUtils.isNotEmpty(userBatteryDepositList)) {
            // 按uid进行分组
            userBatteryDepositMap = userBatteryDepositList.stream().filter(o -> Objects.nonNull(o.getOrderId()))
                    .collect(Collectors.toMap(UserBatteryDeposit::getUid, Function.identity(), (item1, item2) -> item2));
            List<String> orderIdList = userBatteryDepositList.stream().map(UserBatteryDeposit::getOrderId).collect(Collectors.toList());

            // 查询押金订单
            List<EleDepositOrder> eleDepositOrderList = eleDepositOrderService.listByOrderIdList(tenantId, orderIdList);
            if (CollectionUtils.isNotEmpty(eleDepositOrderList)) {
                eleDepositOrderMap = eleDepositOrderList.stream().collect(Collectors.toMap(EleDepositOrder::getOrderId, Function.identity(), (k1, k2) -> k1));
            }

            // 查询免押代扣金额
            orderPayTransAmtMap = freeDepositOrderService.selectPayTransAmtByOrderIdsToMap(orderIdList);

            // 查询退押
            List<EleRefundOrder> eleRefundOrderList = eleRefundOrderService.listByOrderIdList(tenantId, orderIdList);
            if (CollectionUtils.isNotEmpty(eleRefundOrderList)) {
                eleRefundOrderMap = eleRefundOrderList.stream().collect(Collectors.groupingBy(EleRefundOrder::getOrderId, Collectors.toList()));
            }
        }

        Map<Long, UserBatteryDeposit> finalUserBatteryDepositMap = userBatteryDepositMap;
        Map<String, Double> finalOrderPayTransAmtMap = orderPayTransAmtMap;
        Map<String, EleDepositOrder> finalEleDepositOrderMap = eleDepositOrderMap;
        Map<String, List<EleRefundOrder>> finalEleRefundOrderMap = eleRefundOrderMap;

        CompletableFuture<Void> queryDepositInfo = CompletableFuture.runAsync(() -> userInfoList.forEach(item -> item.setEleDepositRefund(
                        getEleDepositInfoPro(item, finalUserBatteryDepositMap, finalOrderPayTransAmtMap, finalEleDepositOrderMap, finalEleRefundOrderMap))), threadPoolPro)
                .exceptionally(e -> {
                    log.error("ELE ERROR! query user deposit info for pro error!", e);
                    return null;
                });

        // 租金是否可退
        Map<Long, UserBatteryMemberCard> userBatteryMemberCardMap = null;
        Map<String, ElectricityMemberCardOrder> usingOrderMap = null;
        if (CollectionUtils.isNotEmpty(userBatteryMemberCardList)) {
            userBatteryMemberCardMap = userBatteryMemberCardList.stream().collect(Collectors.toMap(UserBatteryMemberCard::getUid, Function.identity(), (item1, item2) -> item2));

            List<String> usingOrderIdList = userBatteryMemberCardList.stream().map(UserBatteryMemberCard::getOrderId).collect(Collectors.toList());
            List<ElectricityMemberCardOrder> usingOrderList = electricityMemberCardOrderService.queryListByOrderIds(usingOrderIdList);
            if (CollectionUtils.isNotEmpty(usingOrderList)) {
                usingOrderMap = usingOrderList.stream().collect(Collectors.toMap(ElectricityMemberCardOrder::getOrderId, Function.identity(), (item1, item2) -> item2));
            }
        }
        // 待使用订单是否有可退套餐
        Map<Long, Boolean> noUsingRefundMap = getEleNoUsingOrderPro(uidList, tenantId);

        Map<Long, UserBatteryMemberCard> finalUserBatteryMemberCardMap = userBatteryMemberCardMap;
        Map<String, ElectricityMemberCardOrder> finalUsingOrderMap = usingOrderMap;

        CompletableFuture<Void> rentRefundInfo = CompletableFuture.runAsync(
                () -> userInfoList.forEach(item -> item.setRentRefundFlag(getEleRentRefundFlagPro(item, finalUserBatteryMemberCardMap, finalUsingOrderMap, noUsingRefundMap))),
                threadPoolPro).exceptionally(e -> {
            log.error("ELE ERROR! query user rentRefundFlag for pro error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryBatteryInfo, queryDepositInfo, rentRefundInfo);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(userInfoList);
    }
    @Override
    @Slave
    public R queryEleListForProV2(UserInfoQuery userInfoQuery) {
        List<UserEleInfoVO> userEleInfoVOS = userInfoMapper.queryEleList(userInfoQuery);
        if (ObjectUtil.isEmpty(userEleInfoVOS)) {
            return R.ok(Collections.emptyList());
        }

        Integer tenantId = TenantContextHolder.getTenantId();
        List<Long> uidList = new ArrayList<>(userEleInfoVOS.size());
        List<Long> packageIds = new ArrayList<>(userEleInfoVOS.size());
        List<Long> franchiseeIds = new ArrayList<>(userEleInfoVOS.size());
        List<Long> storeIds = new ArrayList<>(userEleInfoVOS.size());
        List<UserInfo> userInfoList = new ArrayList<>(userEleInfoVOS.size());

        userEleInfoVOS.forEach(item -> {
            if (Objects.isNull(item)) {
                return;
            }

            Long uid = item.getUid();
            Long packageId = item.getMemberCardId();
            Long franchiseeId = item.getFranchiseeId();
            Long storeId = item.getStoreId();

            uidList.add(uid);
            packageIds.add(packageId);
            if (Objects.nonNull(franchiseeId) && !Objects.equals(franchiseeId, 0L)) {
                franchiseeIds.add(franchiseeId);
            }
            storeIds.add(storeId);
            userInfoList.add(UserInfo.builder().uid(uid).batteryRentStatus(item.getBatteryRentStatus()).build());
        });

        // 查询套餐信息
        Map<Long, BatteryMemberCard> batteryMemberCardMap = null;
        List<BatteryMemberCard> batteryMemberCards = null;
        if (CollectionUtils.isNotEmpty(packageIds)) {
            batteryMemberCards = batteryMemberCardService.listByIdListIncludeDel(packageIds);
            if (CollectionUtils.isNotEmpty(batteryMemberCards)) {
                batteryMemberCardMap = batteryMemberCards.stream().collect(Collectors.toMap(BatteryMemberCard::getId, v -> v, (k1, k2) -> k1));
            }
        }

        // 查询加盟商信息
        Map<Long, Franchisee> franchiseeMap = null;
        if (CollectionUtils.isNotEmpty(franchiseeIds)) {
            List<Franchisee> franchiseeList = franchiseeService.queryByIds(franchiseeIds, tenantId);
            if (CollectionUtils.isNotEmpty(franchiseeList)) {
                franchiseeMap = franchiseeList.stream().collect(Collectors.toMap(Franchisee::getId, Function.identity(), (k1, k2) -> k1));
            }
        }
        // 查询门店信息
        Map<Long, Store> storeMap = null;
        if (CollectionUtils.isNotEmpty(storeIds)) {
            List<Store> storeList = storeService.selectByStoreIds(storeIds);
            if (CollectionUtils.isNotEmpty(storeList)) {
                storeMap = storeList.stream().collect(Collectors.toMap(Store::getId, Function.identity(), (k1, k2) -> k1));
            }
        }
        // 查询用户押金信息
        Map<Long, UserBatteryDepositBO> userBatteryDepositMap = null;
        List<UserBatteryDepositBO> userBatteryDepositBOList = userBatteryDepositService.listPayTypeByUidList(tenantId, uidList);
        if (CollectionUtils.isNotEmpty(userBatteryDepositBOList)) {
            userBatteryDepositMap = userBatteryDepositBOList.stream().collect(Collectors.toMap(UserBatteryDepositBO::getUid, Function.identity(), (item1, item2) -> item2));
        }
        // 查询用户绑定套餐信息
        List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.listByUidList(uidList);
        // 查询用户电池服务费信息
        List<ServiceFeeUserInfo> serviceFeeUserInfoList = serviceFeeUserInfoService.listByUidList(uidList);
        // 查询用户滞纳金
        Map<Long, BigDecimal> userServiceFeeMap = serviceFeeUserInfoService.acquireUserBatteryServiceFeeByUserList(userInfoList, userBatteryMemberCardList, batteryMemberCards,
                serviceFeeUserInfoList);
        // 查询用户电池
        Map<Long, ElectricityBattery> userBatteryMap = electricityBatteryService.listUserBatteryByUidList(uidList, tenantId);
        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(uidList,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));
        // 查询企业信息
        Map<Long, EnterpriseChannelUserVO> enterpriseChannelUserMap = null;
        List<EnterpriseChannelUserVO> enterpriseChannelUserList = enterpriseChannelUserService.listByUidList(uidList, tenantId);
        if (CollectionUtils.isNotEmpty(enterpriseChannelUserList)) {
            enterpriseChannelUserMap = enterpriseChannelUserList.stream().collect(Collectors.toMap(EnterpriseChannelUserVO::getUid, Function.identity(), (k1, k2) -> k1));
        }

        Map<Long, BatteryMemberCard> finalBatteryMemberCardMap = batteryMemberCardMap;
        Map<Long, UserBatteryDepositBO> finalUserBatteryDepositMap = userBatteryDepositMap;
        Map<Long, Franchisee> finalFranchiseeMap = franchiseeMap;
        Map<Long, Store> finalStoreMap = storeMap;
        Map<Long, EnterpriseChannelUserVO> finalEnterpriseChannelUserMap = enterpriseChannelUserMap;
        List<UserEleInfoProV2VO> resultList = userEleInfoVOS.stream().map(item -> {
            UserEleInfoProV2VO vo = new UserEleInfoProV2VO();
            Long uid = item.getUid();
            Long memberCardId = item.getMemberCardId();
            Long franchiseeId = item.getFranchiseeId();
            Long storeId = item.getStoreId();

            vo.setUid(uid);
            vo.setName(item.getName());
            vo.setPhone(item.getPhone());
            vo.setUsableStatus(item.getUsableStatus());
            vo.setBatteryRentStatus(item.getBatteryRentStatus());
            vo.setMemberCardId(memberCardId);
            vo.setOrderExpireTime(item.getOrderExpireTime());
            vo.setOrderRemainingNumber(item.getOrderRemainingNumber());
            vo.setMemberCardExpireTime(item.getMemberCardExpireTime());
            vo.setRemainingNumber(item.getRemainingNumber());
            vo.setFranchiseeId(franchiseeId);
            vo.setStoreId(storeId);
            vo.setBatteryDepositStatus(item.getBatteryDepositStatus());

            if (MapUtils.isNotEmpty(finalBatteryMemberCardMap) && finalBatteryMemberCardMap.containsKey(memberCardId) && Objects.nonNull(finalBatteryMemberCardMap.get(memberCardId))) {
                vo.setMemberCardName(finalBatteryMemberCardMap.get(memberCardId).getName());
            }
            if (MapUtils.isNotEmpty(finalUserBatteryDepositMap) && finalUserBatteryDepositMap.containsKey(uid) && Objects.nonNull(finalUserBatteryDepositMap.get(uid))) {
                UserBatteryDepositBO userBatteryDepositBO = finalUserBatteryDepositMap.get(uid);
                vo.setDeposit(userBatteryDepositBO.getBatteryDeposit());
                vo.setDepositPayType(userBatteryDepositBO.getPayType());

                // 押金类型
                if (Objects.equals(UserInfo.BATTERY_DEPOSIT_STATUS_YES, item.getBatteryDepositStatus())) {
                    vo.setFreeDeposit(userBatteryDepositBO.getDepositType());
                }
            }
            if (MapUtils.isNotEmpty(finalFranchiseeMap) && finalFranchiseeMap.containsKey(franchiseeId) && Objects.nonNull(finalFranchiseeMap.get(franchiseeId))) {
                vo.setFranchiseeName(finalFranchiseeMap.get(franchiseeId).getName());
            }
            if (MapUtils.isNotEmpty(finalStoreMap) && finalStoreMap.containsKey(storeId) && Objects.nonNull(finalStoreMap.get(storeId))) {
                vo.setStoreName(finalStoreMap.get(storeId).getName());
            }
            if (MapUtils.isNotEmpty(userServiceFeeMap) && userServiceFeeMap.containsKey(uid) && Objects.nonNull(userServiceFeeMap.get(uid))) {
                vo.setUserBatteryServiceFee(userServiceFeeMap.get(uid));
            }
            if (MapUtils.isNotEmpty(userBatteryMap) && userBatteryMap.containsKey(uid) && Objects.nonNull(userBatteryMap.get(uid))) {
                vo.setBatterySn(userBatteryMap.get(uid).getSn());
            }
            // 查询已删除/已注销
            Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();
            if (MapUtils.isNotEmpty(userStatusMap) && userStatusMap.containsKey(uid) && Objects.nonNull(userStatusMap.get(uid))) {
                UserDelStatusDTO userDelStatusDTO = userStatusMap.get(uid);
                vo.setDelTime(userDelStatusDTO.getDelTime());
                userStatus = userDelStatusDTO.getUserStatus();
            }
            vo.setUserStatus(userStatus);
            if (MapUtils.isNotEmpty(finalEnterpriseChannelUserMap) && finalEnterpriseChannelUserMap.containsKey(uid) && Objects.nonNull(finalEnterpriseChannelUserMap.get(uid))) {
                vo.setRenewalStatus(finalEnterpriseChannelUserMap.get(uid).getRenewalStatus());
            }

            return vo;
        }).collect(Collectors.toList());

        return R.ok(resultList);
    }

    private DetailsBatteryInfoProVO getEleBatteryInfoPro(UserEleInfoProVO userEleInfoProVO, Map<Long, List<String>> userShortBatteryMap,
            Map<Long, ElectricityBattery> userBatteryMap) {
        Long uid = userEleInfoProVO.getUid();
        DetailsBatteryInfoProVO detailsBatteryInfoProVO = DetailsBatteryInfoProVO.builder().memberCardId(userEleInfoProVO.getMemberCardId()).build();

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.nonNull(userBatteryMemberCard)) {
            detailsBatteryInfoProVO.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
            detailsBatteryInfoProVO.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.nonNull(userBatteryDeposit)) {
            detailsBatteryInfoProVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        }

        if (MapUtils.isNotEmpty(userShortBatteryMap) && userShortBatteryMap.containsKey(uid)) {
            detailsBatteryInfoProVO.setBatteryModels(userShortBatteryMap.get(uid));
        }

        if (MapUtils.isNotEmpty(userBatteryMap) && userBatteryMap.containsKey(uid)) {
            ElectricityBattery electricityBattery = userBatteryMap.get(uid);
            detailsBatteryInfoProVO.setBatterySn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
        }

        return detailsBatteryInfoProVO;
    }

    private EleDepositRefundVO getEleDepositInfoPro(UserEleInfoProVO userEleInfoProVO, Map<Long, UserBatteryDeposit> finalUserBatteryDepositMap,
            Map<String, Double> finalOrderPayTransAmtMap, Map<String, EleDepositOrder> finalEleDepositOrderMap, Map<String, List<EleRefundOrder>> finalEleRefundOrderMap) {
        EleDepositRefundVO eleDepositRefundVO = new EleDepositRefundVO();
        Long uid = userEleInfoProVO.getUid();
        if (MapUtils.isEmpty(finalUserBatteryDepositMap) || !finalUserBatteryDepositMap.containsKey(uid)) {
            eleDepositRefundVO.setRefundFlag(false);
            userEleInfoProVO.setEleDepositRefund(eleDepositRefundVO);
            return eleDepositRefundVO;
        }

        UserBatteryDeposit userBatteryDeposit = finalUserBatteryDepositMap.get(uid);
        if (Objects.isNull(userBatteryDeposit) || Objects.isNull(userBatteryDeposit.getOrderId())) {
            eleDepositRefundVO.setRefundFlag(false);
            userEleInfoProVO.setEleDepositRefund(eleDepositRefundVO);
            return eleDepositRefundVO;
        }

        String orderId = userBatteryDeposit.getOrderId();
        if (MapUtils.isNotEmpty(finalOrderPayTransAmtMap) && finalOrderPayTransAmtMap.containsKey(orderId)) {
            eleDepositRefundVO.setPayTransAmt(finalOrderPayTransAmtMap.get(orderId));
        }

        if (MapUtils.isEmpty(finalEleDepositOrderMap) || !finalEleDepositOrderMap.containsKey(orderId)) {
            eleDepositRefundVO.setRefundFlag(false);
            userEleInfoProVO.setEleDepositRefund(eleDepositRefundVO);
            return eleDepositRefundVO;
        }

        EleDepositOrder eleDepositOrder = finalEleDepositOrderMap.get(orderId);
        if (Objects.nonNull(eleDepositOrder)) {
            eleDepositRefundVO.setOrderId(eleDepositOrder.getOrderId());
            eleDepositRefundVO.setStatus(eleDepositOrder.getStatus());
            eleDepositRefundVO.setOrderType(eleDepositOrder.getOrderType());
            eleDepositRefundVO.setPayType(eleDepositOrder.getPayType());
            eleDepositRefundVO.setPayAmount(eleDepositOrder.getPayAmount());
        }

        if (MapUtils.isNotEmpty(finalEleRefundOrderMap) && finalEleRefundOrderMap.containsKey(orderId)) {
            List<EleRefundOrder> eleRefundOrders = finalEleRefundOrderMap.get(orderId);
            if (CollectionUtils.isEmpty(eleRefundOrders)) {
                userEleInfoProVO.setEleDepositRefund(eleDepositRefundVO);
                return eleDepositRefundVO;
            }

            if (eleRefundOrders.stream()
                    .anyMatch(o -> Objects.equals(o.getStatus(), EleRefundOrder.STATUS_SUCCESS) || Objects.equals(o.getStatus(), EleRefundOrder.STATUS_REFUND))) {
                eleDepositRefundVO.setRefundFlag(false);
                userEleInfoProVO.setEleDepositRefund(eleDepositRefundVO);
                return eleDepositRefundVO;
            }
        }

        eleDepositRefundVO.setRefundFlag(true);

        return eleDepositRefundVO;
    }

    private Boolean getEleRentRefundFlagPro(UserEleInfoProVO userEleInfoProVO, Map<Long, UserBatteryMemberCard> finalUserBatteryMemberCardMap,
            Map<String, ElectricityMemberCardOrder> finalUsingOrderMap, Map<Long, Boolean> noUsingRefundMap) {
        Long uid = userEleInfoProVO.getUid();
        boolean isRentRefund = false;

        if (MapUtils.isNotEmpty(finalUserBatteryMemberCardMap) && finalUserBatteryMemberCardMap.containsKey(uid)) {
            UserBatteryMemberCard userBatteryMemberCard = finalUserBatteryMemberCardMap.get(uid);
            if (Objects.nonNull(userBatteryMemberCard)) {
                ElectricityMemberCardOrder usingOrder = null;
                if (MapUtils.isNotEmpty(finalUsingOrderMap) && finalUsingOrderMap.containsKey(userBatteryMemberCard.getOrderId())) {
                    usingOrder = finalUsingOrderMap.get(userBatteryMemberCard.getOrderId());
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.nonNull(batteryMemberCard)) {
                    // 如果套餐未过期，且是可退套餐,且未过可退期，则可退
                    if (Objects.nonNull(usingOrder) && Objects.equals(usingOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
                        if (userBatteryMemberCard.getMemberCardExpireTime() >= System.currentTimeMillis() && Objects.equals(batteryMemberCard.getIsRefund(), BatteryMemberCard.YES)
                                && usingOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000L >= System.currentTimeMillis()) {
                            isRentRefund = true;
                        }
                    }
                }
            }
        }

        if (!isRentRefund) {
            if (MapUtils.isNotEmpty(noUsingRefundMap) && noUsingRefundMap.containsKey(uid)) {
                isRentRefund = noUsingRefundMap.get(uid);
            }
        }

        return isRentRefund;
    }

    private Map<Long, Boolean> getEleNoUsingOrderPro(List<Long> uidList, Integer tenantId) {
        List<UserBatteryMemberCardPackageBO> noUsingOrderList = userBatteryMemberCardPackageService.listByUidList(uidList, tenantId);
        if (CollectionUtils.isEmpty(noUsingOrderList)) {
            return null;
        }

        Map<Long, List<UserBatteryMemberCardPackageBO>> noUsingMap = noUsingOrderList.stream()
                .collect(Collectors.groupingBy(UserBatteryMemberCardPackageBO::getUid, Collectors.toList()));
        if (MapUtils.isEmpty(noUsingMap)) {
            return null;
        }

        Map<Long, Boolean> noUsingRefundMap = new HashMap<>();
        noUsingMap.forEach((uid, noUsingList) -> {
            boolean refundFlag = false;
            if (CollectionUtils.isEmpty(noUsingList)) {
                return;
            }

            for (UserBatteryMemberCardPackageBO noUsingBo : noUsingList) {
                if (Objects.isNull(noUsingBo.getMemberCardId())) {
                    continue;
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(noUsingBo.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    continue;
                }

                if (Objects.equals(batteryMemberCard.getIsRefund(), BatteryMemberCard.YES)) {
                    refundFlag = true;
                    break;
                }
            }
            noUsingRefundMap.put(uid, refundFlag);
        });

        return noUsingRefundMap;
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
    
    @Override
    @Slave
    public List<UserInfo> queryListUserInfoByPhone(String phone) {
        return baseMapper.selectListUserInfoByPhone(phone);
    }
    
    @Slave
    @Override
    public UserAccountInfoVO selectAccountInfo() {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            log.warn("selectAccountInfo warn! tokenUser is null");
            return null;
        }
        
        UserInfo userInfo = this.queryByUidFromCache(tokenUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("selectAccountInfo warn! userInfo is null");
            return null;
        }
        
        return UserAccountInfoVO.builder().uid(userInfo.getUid()).userName(userInfo.getName())
                .phone(userInfo.getPhone()).idNumber(DesensitizationUtil.idCard(userInfo.getIdNumber(), 6, 4))
                .authStatus(userInfo.getAuthStatus()).build();
    }
    
    @Override
    public R bindBattery(BindBatteryRequest bindBatteryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("user bind battery warn! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_USER_BIND_BATTERY_LOCK + user.getUid(), "1", 5 * 1000L, false)) {
            return R.fail(false, "000000", "操作频繁，请稍后再试！");
        }
        
        try {
            // 查询有没有绑定过电池，区分了绑定与编辑两种操作类型，编辑操作才会退掉已绑定电池，所以对绑定操作在此处做校验进行拦截
            ElectricityBattery isBindElectricityBattery = electricityBatteryService.queryByUid(user.getUid());
            if (Objects.nonNull(isBindElectricityBattery)) {
                return R.fail("100032", "该用户已绑定电池");
            }
            
            UserInfo userInfo = queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            // 未实名认证
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("user bind battery warn! user not auth! uid={} ", userInfo.getUid());
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }
            
            // 判断是否缴纳押金
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (!(Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)
                    || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) || Objects.isNull(userBatteryDeposit)) {
                log.warn("user bind battery warn! not pay deposit! uid={} ", userInfo.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }
            
            // 判断电池是否存在，或者已经被绑定
            ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(
                    bindBatteryRequest.getBatterySn());
            if (Objects.isNull(oldElectricityBattery)) {
                log.warn("user bind battery warn! not found Battery! batteryName={}",
                        bindBatteryRequest.getBatterySn());
                return R.fail("ELECTRICITY.0020", "未找到电池");
            }
            
            if (Objects.nonNull(oldElectricityBattery.getUid()) && !Objects.equals(oldElectricityBattery.getUid(),
                    userInfo.getUid())) {
                log.warn("user bind battery warn! battery is bind user! sn={} ", bindBatteryRequest.getBatterySn());
                return R.fail("100019", "该电池已经绑定用户");
            }
            
            if (Objects.equals(oldElectricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)) {
                log.warn("user bind battery warn! battery is bind user! sn={} ", bindBatteryRequest.getBatterySn());
                return R.fail("100019", "该电池为租借状态，不支持绑定");
            }
            
            // 运营商绑定电池判断互通
            if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(userInfo.getTenantId(),
                    userInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId())) {
                log.warn("user bind battery warn! franchiseeId not equals,userFranchiseeId={},batteryFranchiseeId={}",
                        userInfo.getFranchiseeId(), oldElectricityBattery.getFranchiseeId());
                return R.fail("100326", "电池与用户加盟商不一致，不支持绑定");
            }
            
            // 多型号  绑定电池需要判断电池是否和用户型号一致
            Triple<Boolean, String, Object> verifyUserBatteryTypeResult = verifyUserBatteryType(oldElectricityBattery,
                    userInfo);
            if (Boolean.FALSE.equals(verifyUserBatteryTypeResult.getLeft())) {
                if (Objects.equals(verifyUserBatteryTypeResult.getMiddle(), "100297")) {
                    return R.fail("100297", "电池型号与用户套餐型号不一致，请检查");
                }
                
                return R.fail(verifyUserBatteryTypeResult.getMiddle(), (String) verifyUserBatteryTypeResult.getRight());
            }
            
            //是否有正在退款中的退款
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(),
                    EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("RENT CAR BATTERY WARN! deposit is being refunded,uid={}", userInfo.getUid());
                return R.fail("ELECTRICITY.0051", "押金正在退款中，请勿租电池");
            }
            
            Integer orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                // 判断电池滞纳金
                
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(
                        userInfo.getUid());
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("user bind battery warn! user haven't memberCard uid={}", userInfo.getUid());
                    return R.fail("100210", "用户未开通套餐");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                        UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                    log.warn("user bind battery warn! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐停卡审核中");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                        UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.warn("user bind battery warn! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐已暂停");
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(
                        userBatteryMemberCard.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("user bind battery warn! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(),
                            userBatteryMemberCard.getMemberCardId());
                    return R.fail("ELECTRICITY.00121", "套餐不存在");
                }
                
                if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode()
                        .equals(batteryMemberCard.getBusinessType())) {
                    orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
                }
                
                // 判断用户电池服务费
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(
                        userInfo, userBatteryMemberCard, batteryMemberCard,
                        serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("user bind battery warn! user exist battery service fee,uid={}", userInfo.getUid());
                    return R.fail("ELECTRICITY.100000", "请先缴纳滞纳金");
                }
                
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (
                        Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                    log.warn("user bind battery warn! battery memberCard is Expire,uid={}", userInfo.getUid());
                    return R.fail("ELECTRICITY.0023", "套餐已过期");
                }
                
                //校验是否有退租审核中的订单
                BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(
                        userBatteryMemberCard.getOrderId());
                if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(
                        batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
                    log.info("user bind battery warn! battery memberCard is refund,uid={}", userInfo.getUid());
                    return R.fail("100282", "租金退款审核中，请等待审核确认后操作");
                }
                
                // 判断车电关联是否可租电
                ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(
                        userInfo.getTenantId());
                if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(),
                        ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
                    if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                        try {
                            if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(),
                                    userInfo.getUid())) {
                                log.warn("user bind battery warn! user car memberCard expire,uid={}",
                                        userInfo.getUid());
                                return R.fail("100233", "您的车辆套餐已过期，请先续费车辆套餐");
                            }
                        } catch (Exception e) {
                            log.error("user bind battery error! acquire car memberCard expire result fail,uid={}",
                                    userInfo.getUid(), e);
                            return R.fail("100327", "绑定电池异常!");
                        }
                    }
                }

                if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBindBattery(),
                        ElectricityConfig.NOT_IS_BIND_BATTERY)) {
                    return R.fail("402035", "运营商暂未开放此功能!");
                }

                //修改按此套餐的次数
                Triple<Boolean, String, String> modifyResult = electricityCabinetOrderService.checkAndModifyMemberCardCount(
                        userBatteryMemberCard, batteryMemberCard);
                if (Boolean.FALSE.equals(modifyResult.getLeft())) {
                    return R.fail(modifyResult.getMiddle(), modifyResult.getRight());
                }
                
            } else {
                carRentalPackageMemberTermBizService.verifyMemberSwapBattery(userInfo.getTenantId(), userInfo.getUid());
                //修改按此套餐的次数
                carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
                
            }
            
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            Integer update = updateByUid(updateUserInfo);
            
            Integer finalOrderType = orderType;
            DbUtils.dbOperateSuccessThen(update, () -> {
                // 添加租电池记录
                RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
                rentBatteryOrder.setUid(userInfo.getUid());
                rentBatteryOrder.setName(userInfo.getName());
                rentBatteryOrder.setPhone(userInfo.getPhone());
                rentBatteryOrder.setElectricityBatterySn(bindBatteryRequest.getBatterySn());
                rentBatteryOrder.setBatteryDeposit(
                        Objects.isNull(userBatteryDeposit) ? BigDecimal.ZERO : userBatteryDeposit.getBatteryDeposit());
                rentBatteryOrder.setOrderId(
                        OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_BATTERY, user.getUid()));
                rentBatteryOrder.setStatus(RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS);
                rentBatteryOrder.setFranchiseeId(userInfo.getFranchiseeId());
                rentBatteryOrder.setStoreId(userInfo.getStoreId());
                rentBatteryOrder.setTenantId(userInfo.getTenantId());
                rentBatteryOrder.setCreateTime(System.currentTimeMillis());
                rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                rentBatteryOrder.setType(RentBatteryOrder.TYPE_USER_UNBIND);
                rentBatteryOrder.setOrderType(finalOrderType);
                rentBatteryOrder.setChannel(ChannelSourceContextHolder.get());
                rentBatteryOrderService.insert(rentBatteryOrder);
                
                // 修改电池状态
                ElectricityBattery electricityBattery = new ElectricityBattery();
                electricityBattery.setId(oldElectricityBattery.getId());
                electricityBattery.setBusinessStatus(ElectricityBattery.BUSINESS_STATUS_LEASE);
                electricityBattery.setElectricityCabinetId(null);
                electricityBattery.setElectricityCabinetName(null);
                electricityBattery.setUid(userInfo.getUid());
                electricityBattery.setUpdateTime(System.currentTimeMillis());
                electricityBattery.setBindTime(System.currentTimeMillis());
                electricityBatteryService.updateBatteryUser(electricityBattery);
                
                enterpriseRentRecordService.saveEnterpriseRentRecord(rentBatteryOrder.getUid());
                
                // 记录企业用户租电池记录
                enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(
                        UserCostTypeEnum.COST_TYPE_RENT_BATTERY.getCode(), rentBatteryOrder);
                
                // 保存电池被取走对应的订单，供后台租借状态电池展示
                OrderForBatteryUtil.save(rentBatteryOrder.getOrderId(),
                        OrderForBatteryConstants.TYPE_RENT_BATTERY_ORDER, oldElectricityBattery.getSn());
                
                // 修改电池标签
                electricityBatteryService.asyncModifyLabel(oldElectricityBattery, null, new BatteryLabelModifyDTO(BatteryLabelEnum.RENT_NORMAL.getCode()), false);

                return null;
            });
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_BIND_BATTERY_LOCK + user.getUid());
        }
    }
    
    @Override
    public Integer updatePayCountByUid(UserInfo userInfo) {
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        Integer result = this.userInfoMapper.updatePayCountByUid(userInfo);
        redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
        
        return result;
    }

    @Override
    @Slave
    public UserInfo queryByUidFromDB(Long uid) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }
    
    @Override
    public UserDepositStatusVO queryDepositStatus() {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            log.warn("queryDepositStatus warn! tokenUser is null");
            return null;
        }
        
        UserInfo userInfo = this.queryByUidFromCache(tokenUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("selectAccountInfo warn! userInfo is null");
            return null;
        }
        
        return UserDepositStatusVO.builder().uid(userInfo.getUid()).batteryDepositStatus(userInfo.getBatteryDepositStatus()).carDepositStatus(userInfo.getCarDepositStatus())
                .carBatteryDepositStatus(userInfo.getCarBatteryDepositStatus()).build();
    }
    
    @Override
    public Triple<Boolean, String, String> checkMemberCardGroup(UserInfo userInfo,
                                                                BatteryMemberCard batteryMemberCard, UserInfoExtra userInfoExtra) {
        // 判断套餐用户分组和用户的用户分组是否匹配
        List<UserInfoGroupNamesBO> userInfoGroupNamesBos = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(userInfo.getUid())
                        .franchiseeId(batteryMemberCard.getFranchiseeId()).build());
        
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesBos)) {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
            }
            
            List<Long> userGroupIds = userInfoGroupNamesBos.stream().map(UserInfoGroupNamesBO::getGroupId)
                    .collect(Collectors.toList());
            userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
            if (CollectionUtils.isEmpty(userGroupIds)) {
                return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
            }
        } else {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                return Triple.of(false, "100318", "您浏览的套餐已下架，请看看其他的吧");
            }
    
            Boolean everDel = userDelRecordService.existsByDelPhoneAndDelIdNumber(userInfo.getPhone(), userInfo.getIdNumber(), userInfo.getTenantId());
            if (Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
                // 流失用户不允许购买续租类型的套餐
                if (Objects.equals(batteryMemberCard.getRentType(), ApplicableTypeEnum.OLD.getCode())) {
                    log.warn("INTEGRATED PAYMENT WARN. Package type mismatch. lost user, package is old, uid = {}, buyRentalPackageId = {}", userInfo.getUid(), batteryMemberCard.getId());
                    return Triple.of(false, "100379", "该套餐已下架，无法购买，请刷新页面购买其他套餐");
                }
            } else  if ((userInfo.getPayCount() > 0 || everDel) && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
                log.warn(
                        "INTEGRATED PAYMENT WARN! The rent type of current package is a new rental package, uid={}, mid={}",
                        userInfo.getUid(), batteryMemberCard.getId());
                return Triple.of(false, "100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
            }
    
            // 新用户且未被打过删除标记，无法购买续费套餐
            if (userInfo.getPayCount() == 0 && !everDel && BatteryMemberCard.RENT_TYPE_OLD.equals(
                    batteryMemberCard.getRentType())) {
                log.warn(
                        "INTEGRATED PAYMENT WARN! The rent type of current package is a new rental package, uid={}, mid={}",
                        userInfo.getUid(), batteryMemberCard.getId());
                return Triple.of(false, "100379", "平台新用户，无法购买续租类型套餐，请刷新页面重试");
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    
    

    /**
     * 操作记录
     *
     * @param uid 用户id
     * @author caobotao.cbt
     * @date 2024/12/24 10:22
     */
    private void operatorEditRecord(Long uid) {
        UserInfo user = queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.warn("WARN operatorRecord uid:{} not exist", uid);
            return;
        }

        Map<String, Object> recordMap = com.google.common.collect.Maps.newHashMapWithExpectedSize(1);
        recordMap.put("phone", user.getPhone());
        operateRecordUtil.record(null, recordMap);

    }


    /**
     * 操作记录
     *
     * @param uid        用户id
     * @param authStatus 授权状态
     * @author caobotao.cbt
     * @date 2024/12/24 10:22
     */
    private void operatorAuthRecord(Long uid, Integer authStatus) {
        UserInfo user = queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.warn("WARN operatorRecord uid:{} not exist", uid);
            return;
        }

        Map<String, Object> recordMap = com.google.common.collect.Maps.newHashMapWithExpectedSize(2);
        recordMap.put("phone", user.getPhone());
        recordMap.put("authStatus",
                Objects.equals(UserInfo.AUTH_STATUS_REVIEW_REJECTED, authStatus) ? "拒绝用户" : "通过用户");
        operateRecordUtil.record(null, recordMap);

    }
    @Override
    public R deleteAccountPreCheck() {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            return R.fail("120162", "您还未登录，请去【我的】页面登录");
        }

        Long uid = tokenUser.getUid();
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user) || !user.getUserType().equals(User.TYPE_USER_NORMAL_WX_PRO)) {
            log.warn("DeleteAccountPreCheck WARN! not found user,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
    
        ElectricityConfigExtra electricityConfigExtra = electricityConfigExtraService.queryByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfigExtra) && Objects.equals(electricityConfigExtra.getAccountDelSwitch(), ElectricityConfigExtraEnum.SWITCH_OFF.getCode())) {
            log.warn("DeleteAccountPreCheck WARN! electricityConfigExtra is null or accountDelSwitch off,uid={} ", uid);
            return R.fail("120154", "自主注销账号功能已关闭");
        }
        
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("DeleteAccountPreCheck WARN! not found userInfo,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("DeleteAccountPreCheck WARN! not found userInfoExtra,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        Integer tenantId = userInfo.getTenantId();

        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(uid);
        if (Objects.nonNull(enterpriseInfo)) {
            log.warn("DeleteAccountPreCheck WARN! enterprise user cannot be delete! uid={}", uid);
            return R.fail("120130", "请先删除企业用户配置");
        }

        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.nonNull(enterpriseChannelUser) && Objects.equals(enterpriseChannelUser.getCloudBeanStatus(), CloudBeanStatusEnum.NOT_RECYCLE.getCode())) {
            log.warn("DeleteAccountPreCheck WARN! enterprise user cloudBean not recycled! uid={}", uid);
            return R.fail("120156", "您名下有未回收的云豆，暂无法注销，请联系站点回收后操作");
        }

        BigDecimal batteryLateFees = serviceFeeUserInfoService.selectBatteryServiceFeeByUid(uid);
        if (Objects.nonNull(batteryLateFees) && batteryLateFees.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("DeleteAccountPreCheck WARN! has batteryLateFees! uid={}", uid);
            return R.fail("120159", "您有未缴纳的滞纳金，暂无法注销，请缴纳后操作");
        }

        boolean carLateFees = carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid);
        if (carLateFees) {
            log.warn("DeleteAccountPreCheck WARN! has carLateFees! uid={}", uid);
            return R.fail("120159", "您有未缴纳的滞纳金，暂无法注销，请缴纳后操作");
        }

        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("DeleteAccountPreCheck WARN! has unreturned battery! uid={}", uid);
            return R.fail("120157", "您名下有未归还的电池，暂无法注销，请归还后操作");
        }

        if (Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.warn("DeleteAccountPreCheck WARN! has unreturned car! uid={}", uid);
            return R.fail("120158", "您名下有未归还的车辆，暂无法注销，请归还后操作");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW, userBatteryMemberCard.getMemberCardStatus())) {
                log.warn("DeleteAccountPreCheck WARN! userBatteryMemberCard is freezing! uid={}, memberCardId={}", uid, userBatteryMemberCard.getMemberCardId());
                return R.fail("120161", "您有在申请的冻结套餐，暂无法注销，请启用后操作");
            }

            if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                log.warn("DeleteAccountPreCheck warn! userBatteryMemberCard has been frozen! uid={}, memberCardId={}", uid, userBatteryMemberCard.getMemberCardId());
                return R.fail("120160", "您当前套餐已冻结，暂无法注销，请启用后操作");
            }
        }

        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (Objects.nonNull(memberTermEntity)) {
            Integer memberTermEntityStatus = memberTermEntity.getStatus();
            if (MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermEntityStatus)) {
                log.warn("DeleteAccountPreCheck WARN! memberTermEntityStatus is freezing! uid={}, rentalPackageId={}", uid, memberTermEntity.getRentalPackageId());
                return R.fail("120161", "您有在申请的冻结套餐，暂无法注销，请启用后操作");
            }

            if (MemberTermStatusEnum.FREEZE.getCode().equals(memberTermEntityStatus)) {
                log.warn("DeleteAccountPreCheck WARN! memberTermEntityStatus has been frozen! uid={}, rentalPackageId={}", uid, memberTermEntity.getRentalPackageId());
                return R.fail("120160", "您当前套餐已冻结，暂无法注销，请启用后操作");
            }
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getCarDepositStatus(),
                UserInfo.CAR_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.warn("DeleteAccountPreCheck WARN! has unreturned deposit! uid={}", uid);
            return R.fail("120155", "请退还押金后，进行注销操作");
        }

        return R.ok();
    }

    @Override
    public R deleteAccount() {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            return R.fail("120162", "您还未登录，请去【我的】页面登录");
        }

        Long uid = tokenUser.getUid();
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user) || !user.getUserType().equals(User.TYPE_USER_NORMAL_WX_PRO)) {
            log.warn("DeleteAccount WARN! not found user,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("DeleteAccount WARN! not found userInfo,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("DeleteAccount WARN! not found userInfoExtra,uid={} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(uid, List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            log.warn("DeleteAccount WARN! userAccount is cancelling, uid={}", uid);
            return R.fail("120163", "账号处于注销缓冲期内，无法操作");
        }
    
        // 注销时不打标记，batch定时任务注销成功时才打标记
        userDelRecordService.insert(uid, StringUtils.EMPTY, StringUtils.EMPTY, UserStatusEnum.USER_STATUS_CANCELLING.getCode(), userInfo.getTenantId(), userInfo.getFranchiseeId(),
                UserStatusEnum.USER_DELAY_DAY_30.getCode());

        return R.ok();
    }

    @Override
    public Integer queryUserDelStatus(Long uid) {
        Integer userStatus = UserStatusEnum.USER_STATUS_VO_COMMON.getCode();

        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(uid,
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            if (Objects.equals(userDelRecord.getStatus(), UserStatusEnum.USER_STATUS_CANCELLED.getCode())) {
                userStatus = UserStatusEnum.USER_STATUS_VO_CANCELLED.getCode();
            } else {
                userStatus = UserStatusEnum.USER_STATUS_VO_DELETED.getCode();
            }
        }

        return userStatus;
    }

    @Override
    public Boolean isOldUser(UserInfo userInfo) {
        return userInfo.getPayCount() > 0 || userDelRecordService.existsByDelPhoneAndDelIdNumber(userInfo.getPhone(), userInfo.getIdNumber(), userInfo.getTenantId());
    }

    @Override
    public void unBindEnterpriseUserFranchiseeId(Long uid) {
        UserInfo userInfo = this.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            return;
        }

        // 租车押金
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return;
        }

        // 租电池押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return;
        }

        // 若租车和租电押金都退了，则解绑用户所属加盟商
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(uid);
        updateUserInfo.setStoreId(NumberConstant.ZERO_L);
        updateUserInfo.setFranchiseeId(NumberConstant.ZERO_L);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());

        this.updateByUid(updateUserInfo);
    }
}
