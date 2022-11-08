package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.security.bean.TokenUser;
import com.xxl.job.core.util.DateUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    FranchiseeUserInfoService franchiseeUserInfoService;
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
        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
            return null;
        });
        return result;

    }

    @Override
    @DS("slave_1")
    public R queryList(UserInfoQuery userInfoQuery) {
        List<UserBatteryInfoVO> userBatteryInfoVOS = userInfoMapper.queryListForBatteryService(userInfoQuery);
        if (ObjectUtil.isEmpty(userBatteryInfoVOS)) {
            return R.ok(userBatteryInfoVOS);
        }

        CompletableFuture<Void> queryPayDepositTime = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getMemberCardExpireTime())) {
                    Long now = System.currentTimeMillis();
                    long carDays = 0;
                    if (item.getMemberCardExpireTime() > now) {
//                        carDays = (item.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24;
                        Double carDayTemp = Math.ceil((item.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24.0);
                        carDays = carDayTemp.longValue();
                    }
                    if (Objects.equals(item.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
                        carDays = (item.getMemberCardExpireTime() - item.getDisableMemberCardTime()) / (24 * 60 * 60 * 1000L);
                    }
                    item.setCardDays(carDays);

                    ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.queryLastPayMemberCardTimeByUid(item.getUid(), item.getFranchiseeId(), item.getTenantId());
                    if (Objects.nonNull(electricityMemberCardOrder)) {
                        item.setMemberCardCreateTime(electricityMemberCardOrder.getCreateTime());
                    }
                } else {
                    item.setCardDays(null);
                    item.setCardId(null);
                    item.setCardName(null);
                }

                if (Objects.nonNull(item.getServiceStatus()) && !Objects
                        .equals(item.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)) {
                    EleDepositOrder eleDepositOrder = eleDepositOrderService
                            .queryLastPayDepositTimeByUid(item.getUid(), item.getFranchiseeId(), item.getTenantId(), null);
                    if (Objects.nonNull(eleDepositOrder)) {
                        item.setPayDepositTime(eleDepositOrder.getCreateTime());
                    }
                }

//                if (Objects.isNull(item.getAuthStatus()) || !Objects.equals(item.getAuthStatus(),UserInfo.STATUS_AUDIT_PASS)){
//                    item.setServiceStatus(UserInfo.STATUS_INIT);
//                }

                if (Objects.nonNull(item.getModel())) {
                    item.setModel(BatteryConstant.acquireBattery(item.getModel()).toString());
                }

                if (StringUtils.isNotBlank(item.getOrderId())) {
                    EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(item.getOrderId());
                    if (Objects.nonNull(eleDepositOrder)) {
                        item.setStoreId(eleDepositOrder.getStoreId());
                    }
                }

                FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUid(item.getUid());
                if (Objects.nonNull(franchiseeUserInfo) && StringUtils.isNotBlank(franchiseeUserInfo.getCardName())) {
                    item.setMemberCardDisableStatus(franchiseeUserInfo.getMemberCardDisableStatus());
                }

                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(item.getUid());
                if (Objects.nonNull(electricityBattery)) {
                    item.setNowElectricityBatterySn(electricityBattery.getSn());
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("payDepositTime list ERROR! query memberCard error!", e);
            return null;
        });

        CompletableFuture<Void> queryMemberCard = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getCardId())) {
                    ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(item.getCardId());
                    if (Objects.nonNull(electricityMemberCard) && Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                        item.setRemainingNumber(FranchiseeUserInfo.UN_LIMIT_COUNT_REMAINING_NUMBER);
                    }
                }
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
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        CompletableFuture<Void> queryInsurance = CompletableFuture.runAsync(() -> {
            userBatteryInfoVOS.stream().forEach(item -> {
                if (Objects.nonNull(item.getUid())) {
                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUid(item.getUid(),item.getTenantId());

                    System.out.println("用户保险信息========================"+insuranceUserInfo);

                    if (Objects.nonNull(insuranceUserInfo)) {
                        item.setIsUseInsurance(insuranceUserInfo.getIsUse());
                        item.setInsuranceExpireTime(insuranceUserInfo.getInsuranceExpireTime());
                    }
                }
            });
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(queryMemberCard, queryElectricityCar, queryPayDepositTime,queryInsurance);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(userBatteryInfoVOS);
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
    public Integer homeOne(Long first, Long now, Integer tenantId) {
        return userInfoMapper.homeOne(first, now, tenantId);
    }

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
    @DS("slave_1")
    public R getMemberCardInfo(Long uid) {
        UserInfo userInfo = selectUserByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID:{}", uid);
            return R.failMsg("未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("returnBattery  ERROR! not pay deposit! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        Long memberCardExpireTime = franchiseeUserInfo.getMemberCardExpireTime();
        //算出暂停月卡时套餐剩余的天数
        Long validDays = null;
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            memberCardExpireTime = System.currentTimeMillis() + (memberCardExpireTime - franchiseeUserInfo.getDisableMemberCardTime());
            validDays = (franchiseeUserInfo.getMemberCardExpireTime() - franchiseeUserInfo.getDisableMemberCardTime()) / (24 * 60 * 60 * 1000L);
        }

        if (!Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
            if (Objects.isNull(electricityMemberCard)) {
                return R.ok();
            }
            if ((Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
                    System.currentTimeMillis() >= memberCardExpireTime) ||
                    (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
                            franchiseeUserInfo.getRemainingNumber() > 0 && System.currentTimeMillis() >= memberCardExpireTime) ||
                    (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) &&
                            franchiseeUserInfo.getRemainingNumber() == 0) ||
                    Objects.isNull(franchiseeUserInfo.getRemainingNumber()) || Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())) {
                return R.ok();
            }

            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                franchiseeUserInfo.setRemainingNumber(FranchiseeUserInfo.UN_LIMIT_COUNT_REMAINING_NUMBER);
            }
            if (Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getRemainingNumber() < 0) {
                memberCardExpireTime = System.currentTimeMillis();
            }
        } else {
            if (Objects.isNull(franchiseeUserInfo.getRemainingNumber()) || Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                    || System.currentTimeMillis() >= franchiseeUserInfo.getMemberCardExpireTime() || franchiseeUserInfo.getRemainingNumber() == 0) {
                return R.ok();
            }
        }

        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setMemberCardExpireTime(memberCardExpireTime);
        ownMemberCardInfoVo.setRemainingNumber(franchiseeUserInfo.getRemainingNumber());
        ownMemberCardInfoVo.setType(franchiseeUserInfo.getCardType());
        ownMemberCardInfoVo.setName(franchiseeUserInfo.getCardName());
        ownMemberCardInfoVo.setDays((long) Math.round((memberCardExpireTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
        ownMemberCardInfoVo.setCardId(franchiseeUserInfo.getCardId());
        ownMemberCardInfoVo.setMemberCardDisableStatus(franchiseeUserInfo.getMemberCardDisableStatus());
        ownMemberCardInfoVo.setValidDays(validDays);
        return R.ok(ownMemberCardInfoVo);
    }

    @Override
    public R getRentCarMemberCardInfo(Long uid) {

        UserInfo userInfo = selectUserByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID:{}", uid);
            return R.failMsg("未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_INIT)
                || Objects.isNull(franchiseeUserInfo.getRentCarDeposit()) || Objects.isNull(franchiseeUserInfo.getRentCarOrderId())) {
            log.error("returnBattery  ERROR! not pay deposit! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getRentCarCardId());
        if (Objects.isNull(electricityMemberCard)) {
            return R.ok();
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(franchiseeUserInfo.getBindCarModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELECTRICITY  ERROR! not found memberCard ! uid:{} ", userInfo.getUid());
            return R.fail("100005", "未找到车辆型号");
        }

        Long memberCardExpireTime = franchiseeUserInfo.getRentCarMemberCardExpireTime();

        if (Objects.isNull(memberCardExpireTime) || System.currentTimeMillis() >= memberCardExpireTime) {
            return R.ok();
        }
        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setMemberCardExpireTime(memberCardExpireTime);
        ownMemberCardInfoVo.setName(electricityMemberCard.getName());
        ownMemberCardInfoVo.setCarName(electricityCarModel.getName());
        ownMemberCardInfoVo.setType(electricityMemberCard.getType());
        ownMemberCardInfoVo.setDays((long) Math.round((memberCardExpireTime - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
        ownMemberCardInfoVo.setCardId(franchiseeUserInfo.getRentCarCardId());
        return R.ok(ownMemberCardInfoVo);
    }

    @Override
    public R queryUserInfo() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //2.判断用户是否有电池是否有月卡
        UserInfo userInfo = queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! not auth! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("returnBattery  ERROR! not pay deposit! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //判断用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            log.error("ELECTRICITY  ERROR! not found memberCard ! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }

        Long now = System.currentTimeMillis();
        long cardDays = 0;
        if (Objects.nonNull(franchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            cardDays = (now - franchiseeUserInfo.getBatteryServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
        }

        //判断用户是否产生电池服务费
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), Franchisee.DISABLE_MEMBER_CARD_PAY_TYPE)) {
            cardDays = (now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }
        }

        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY) && cardDays >= 1) {
//        if (Objects.nonNull(franchiseeUserInfo.getNowElectricityBatterySn()) && cardDays >= 1) {
            //查询用户是否存在电池服务费
            Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeUserInfo.getFranchiseeId());
            Integer modelType = franchisee.getModelType();
            if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
                Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
                List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
                for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                    if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                        //计算服务费
                        BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                        if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                            return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                        }
                    }
                }
            } else {
                BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                //计算服务费
                BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                    return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                }
            }
        }

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            log.error("returnDeposit  ERROR! member card is disable userId:{}", user.getUid());
            return R.fail("ELECTRICITY.100004", "月卡已暂停");
        }

        if (franchiseeUserInfo.getMemberCardExpireTime() < now
                || franchiseeUserInfo.getRemainingNumber() == 0) {
            log.error("ELECTRICITY  ERROR! memberCard is  Expire ! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("returnDeposit  ERROR! disable member card is reviewing userId:{}", user.getUid());
            return R.fail("ELECTRICITY.100003", "停卡正在审核中");
        }

        //未租电池
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            log.error("ELECTRICITY  ERROR! not rent battery! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        //租车未购买套餐
        if (Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_IS_RENT_CAR) && Objects.isNull(franchiseeUserInfo.getRentCarMemberCardExpireTime())) {
            log.error("order ERROR! not rent car member card! uid:{}", user.getUid());
            return R.fail("100012", "未购买租车套餐");
        }


        if (Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_IS_RENT_CAR) && Objects.nonNull(franchiseeUserInfo.getRentCarMemberCardExpireTime()) && franchiseeUserInfo.getRentCarMemberCardExpireTime() < now) {
            log.error("order ERROR! rent car memberCard  is Expire ! uid:{}", user.getUid());
            return R.fail("100013", "租车套餐已过期");
        }

        return R.ok(userInfo);
    }

    @Override
    public R verifyAuth(Long id, Integer authStatus) {
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
        if (Objects.equals(authStatus, UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            userInfo.setServiceStatus(UserInfo.STATUS_IS_AUTH);
        }
        update(userInfo);
        //修改资料项
        eleUserAuthService.updateByUid(oldUserInfo.getUid(), authStatus);
        return R.ok();
    }

    @Override
    @Transactional
    public R updateAuth(UserInfo userInfo) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if (!Objects.equals(tenantId, userInfo.getTenantId())) {
            return R.ok();
        }

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

        List<UserAuthInfoVo> result = userInfos.parallelStream().map(e -> {
            UserAuthInfoVo userAuthInfoVo = new UserAuthInfoVo();
            BeanUtils.copyProperties(e, userAuthInfoVo);

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

    @Override
    public R queryCount(UserInfoQuery userInfoQuery) {
        return R.ok(userInfoMapper.queryCountForBatteryService(userInfoQuery));
    }

    @Override
    public R queryAuthenticationCount(UserInfoQuery userInfoQuery) {
        return R.ok(userInfoMapper.queryAuthenticationCount(userInfoQuery));
    }

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

        //查找用户
        UserInfo oldUserInfo = queryByUidFromCache(userInfoBatteryAddAndUpdate.getUid());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (!Objects.equals(tenantId,oldUserInfo.getTenantId())) {
            return R.ok();
        }

        //未实名认证
        if (Objects.equals(oldUserInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("WEBBIND ERROR! user not auth! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(oldUserInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("WEBBIND ERROR ERROR! not found user! userId={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否缴纳押金
        if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
            log.error("WEBBIND ERROR ERROR! not pay deposit! uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

//        //已绑定电池
//        if (Objects.equals(userInfoBatteryAddAndUpdate.getEdiType(), UserInfoBatteryAddAndUpdate.BIND_TYPE) && Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
//            log.error("webBindBattery  ERROR! user rent battery! uid:{} ", oldUserInfo.getUid());
//            return R.fail("ELECTRICITY.0045", "已绑定电池");
//        }

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

        //更新用户绑定电池状态
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo.getInitElectricityBatterySn())) {
            franchiseeUserInfo.setInitElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        }
//        franchiseeUserInfo.setNowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_BATTERY);
        Integer update = franchiseeUserInfoService.update(franchiseeUserInfo);

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
            rentBatteryOrder.setElectricityBatterySn(franchiseeUserInfo.getInitElectricityBatterySn());
            rentBatteryOrder.setBatteryDeposit(oldFranchiseeUserInfo.getBatteryDeposit());
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

            return null;
        });
        return R.ok();
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

        if (!Objects.equals(tenantId,oldUserInfo.getTenantId())) {
            return R.ok();
        }

        //未实名认证
        if (Objects.equals(oldUserInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("WEBUNBIND ERROR! user not auth,uid={} ", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //
        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(oldUserInfo.getId());
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("WEBUNBIND ERROR! not found user,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
            log.error("WEBUNBIND ERROR! not  rent battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(oldUserInfo.getUid());
        if (Objects.isNull(oldElectricityBattery)) {
            log.error("WEBUNBIND ERROR! not found user bind battery,uid={}", oldUserInfo.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }

        Long now = System.currentTimeMillis();
        long cardDays = 0;
        if (Objects.nonNull(oldFranchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            cardDays = (now - oldFranchiseeUserInfo.getBatteryServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
        }

        Long disableMemberCardTime = oldFranchiseeUserInfo.getDisableMemberCardTime();

        //判断用户是否产生电池服务费
        if (Objects.equals(oldFranchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {

            cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }
        }

        if (Objects.nonNull(oldFranchiseeUserInfo.getServiceStatus()) && Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY) && cardDays >= 1) {
            //查询用户是否存在电池服务费
            Franchisee franchisee = franchiseeService.queryByIdFromDB(oldFranchiseeUserInfo.getFranchiseeId());
            Integer modelType = franchisee.getModelType();
            if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
                Integer model = BatteryConstant.acquireBattery(oldFranchiseeUserInfo.getBatteryType());
                List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
                for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                    if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                        //计算服务费
                        BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                        if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                            return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                        }
                    }
                }
            } else {
                BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                //计算服务费
                BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                    return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                }
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

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.BATTERY_MODEL)
                .operateContent(EleUserOperateRecord.UN_BIND_BATTERY_CONTENT)
                .operateUid(user.getUid())
                .uid(oldUserInfo.getUid())
                .name(user.getUsername())
                .initElectricityBatterySn(oldElectricityBattery.getSn())
                .nowElectricityBatterySn(null)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);

        return R.ok();
    }

    @Override
    public R userMove(UserMoveHistory userMoveHistory) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        //根据手机号查询用户
        User user = userService.queryByUserPhone(userMoveHistory.getPhone(), User.TYPE_USER_NORMAL_WX_PRO, tenantId);

        if (Objects.isNull(user)) {
            log.error("userMove  ERROR! not found user,phone:{} ", userMoveHistory.getPhone());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        UserInfo userInfo = queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("userMove  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("userMove  ERROR! not found franchiseeUserInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(franchiseeUserInfo.getFranchiseeId(), userMoveHistory.getFranchiseeId())) {
            return R.fail("ELECTRICITY.00108", "换电柜加盟商和用户加盟商不一致");
        }

        if (franchiseeUserInfo.getServiceStatus() < 2) {
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        Integer cardId = null;
        String cardName = null;
        Integer cardType = null;
        Long memberCardExpireTime = null;
        Long remainingNumber = null;
        if (Objects.nonNull(userMoveHistory.getCardId())) {
            if (Objects.isNull(userMoveHistory.getMemberCardExpireTime())
                    || Objects.isNull(userMoveHistory.getRemainingNumber())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }
            //查看套餐
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userMoveHistory.getCardId());

            if (Objects.isNull(electricityMemberCard)) {
                log.error("userMove  ERROR! not found user,electricityMemberCardId:{} ", userMoveHistory.getCardId());
                return R.fail("ELECTRICITY.0087", "未找到月卡套餐");
            }

            cardId = userMoveHistory.getCardId();
            cardName = electricityMemberCard.getName();
            cardType = electricityMemberCard.getType();
            memberCardExpireTime = userMoveHistory.getMemberCardExpireTime();
            remainingNumber = userMoveHistory.getRemainingNumber();
        }

        Integer finalCardId = cardId;
        String finalCardName = cardName;
        Integer finalCardType = cardType;
        Long finalMemberCardExpireTime = memberCardExpireTime;
        Long finalRemainingNumber = remainingNumber;


        franchiseeUserInfo.setFranchiseeId(userMoveHistory.getFranchiseeId());
        franchiseeUserInfo.setCardId(finalCardId);
        franchiseeUserInfo.setCardName(finalCardName);
        franchiseeUserInfo.setCardType(finalCardType);
        franchiseeUserInfo.setMemberCardExpireTime(finalMemberCardExpireTime);
        franchiseeUserInfo.setRemainingNumber(finalRemainingNumber);
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfo);

        //记录一下数据迁移，迁移了哪些数据
        userMoveHistory.setUid(user.getUid());
        userMoveHistory.setCreateTime(System.currentTimeMillis());
        userMoveHistory.setTenantId(user.getTenantId());
        userMoveHistoryService.insert(userMoveHistory);
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
    public R queryUserBelongFranchisee(Long franchiseeId,Integer tenantId) {
        return R.ok(franchiseeService.queryByIdAndTenantId(franchiseeId,tenantId));
    }

    @Override
    public R queryUserAllConsumption(Long id) {

        Integer tenantId = TenantContextHolder.getTenantId();

        DataBrowsingVo dataBrowsingVo = new DataBrowsingVo();
        //用户总套餐消费额
        CompletableFuture<BigDecimal> queryMemberCardPayAmount = CompletableFuture.supplyAsync(() -> {
            return electricityMemberCardOrderService.queryTurnOver(tenantId, id);
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        //用户电池服务费消费额
        CompletableFuture<BigDecimal> queryBatteryServiceFeePayAmount = CompletableFuture.supplyAsync(() -> {
            return eleBatteryServiceFeeOrderService.queryUserTurnOver(tenantId, id);
        }, threadPool).exceptionally(e -> {
            log.error("The carSn list ERROR! query carSn error!", e);
            return null;
        });

        //计算总消费额
        CompletableFuture<Void> payAmountSumFuture = queryMemberCardPayAmount
                .thenAcceptBoth(queryBatteryServiceFeePayAmount, (memberCardSumAmount, batteryServiceFeeSumAmount) -> {
                    BigDecimal result = memberCardSumAmount.add(batteryServiceFeeSumAmount);
                    dataBrowsingVo.setSumTurnover(result);
                }).exceptionally(e -> {
                    log.error("DATA SUMMARY BROWSING ERROR! statistics pay amount sum error!", e);
                    return null;
                });

        //等待所有线程停止 thenAcceptBoth方法会等待a,b线程结束后获取结果
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(payAmountSumFuture);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(dataBrowsingVo);
    }

    @Override
    public UserInfo queryUserInfoByPhone(String phone, Integer tenantId) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone).eq(UserInfo::getTenantId, tenantId).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }

    @Override
    public Integer queryAuthenticationUserCount(Integer tenantId) {
        return userInfoMapper.queryAuthenticationUserCount(tenantId);
    }

    @Override
    public List<HomePageUserByWeekDayVo> queryUserAnalysisForAuthUser(Integer tenantId, Long beginTime, Long endTime) {
        return userInfoMapper.queryUserAnalysisForAuthUser(tenantId, beginTime, endTime);
    }

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
        userInfoDetailVO.setAuthStatus(userInfo.getAuthStatus());


        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("ELE ERROR! not found franchiseeUserInfo,uid={}", userInfo.getUid());
            return userInfoDetailVO;
        }
        //服务状态
        userInfoDetailVO.setServiceStatus(getServiceStatus(userInfo,franchiseeUserInfo));


        //电池服务费
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = franchiseeUserInfoService.queryUserBatteryServiceFee(user.getUid());
        userInfoDetailVO.setBatteryServiceFee(eleBatteryServiceFeeVO);


       //用户状态(离线换电)
        UserFrontDetectionVO userFrontDetection = offLineElectricityCabinetService.getUserFrontDetection(userInfo, franchiseeUserInfo);
        userInfoDetailVO.setUserFrontDetection(userFrontDetection);

        return userInfoDetailVO;
    }

    private Integer getServiceStatus(UserInfo userInfo,FranchiseeUserInfo franchiseeUserInfo) {
        Integer serviceStatus = userInfo.getServiceStatus();

        if (!Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)) {
            serviceStatus = franchiseeUserInfo.getServiceStatus();
        }

        //用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            log.error("ELE ERROR! not found memberCard,uid={} ", userInfo.getUid());
            serviceStatus = -1;
        } else if (franchiseeUserInfo.getMemberCardExpireTime() < System.currentTimeMillis() || franchiseeUserInfo.getRemainingNumber() == 0) {
            log.error("ELE ERROR! memberCard  is Expire,uid={} ", userInfo.getUid());
            serviceStatus = -1;
        }

        return serviceStatus;
    }



    @Override
    public R deleteUserInfo(Long uid) {


        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUid(uid);
        if (Objects.nonNull(franchiseeUserInfo) && Objects.equals(FranchiseeUserInfo.STATUS_IS_BATTERY, franchiseeUserInfo.getServiceStatus())) {
            return R.fail("ELECTRICITY.0045", "已绑定电池");
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

        DbUtils.dbOperateSuccessThen(result, () -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO + userInfo.getUid());
            return null;
        });
        return result;
    }

    @Override
    public void exportExcel(UserInfoQuery userInfoQuery, HttpServletResponse response) {
        userInfoQuery.setOffset(0L);
        userInfoQuery.setSize(2000L);
        List<UserBatteryInfoVO> userBatteryInfoVOS = userInfoMapper.queryListForBatteryService(userInfoQuery);
        if (ObjectUtil.isEmpty(userBatteryInfoVOS)) {
            throw new CustomBusinessException("查不到会员用户");
        }


        List<UserInfoExcelVO> userInfoExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (UserBatteryInfoVO userBatteryInfoVO : userBatteryInfoVOS) {
            index++;
            UserInfoExcelVO excelVo = new UserInfoExcelVO();
            excelVo.setId(index);
            excelVo.setPhone(userBatteryInfoVO.getPhone());
            excelVo.setName(userBatteryInfoVO.getName());
            excelVo.setBatteryDeposit(userBatteryInfoVO.getBatteryDeposit());
            excelVo.setCardName(userBatteryInfoVO.getCardName());
            excelVo.setNowElectricityBatterySn(userBatteryInfoVO.getNowElectricityBatterySn());
            userInfoExcelVOS.add(excelVo);


            if (Objects.nonNull(userBatteryInfoVO.getMemberCardExpireTime())) {
                excelVo.setMemberCardExpireTime(simpleDateFormat.format(new Date(userBatteryInfoVO.getMemberCardExpireTime())));
            }

        }

        String fileName = "会员列表报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, UserInfoExcelVO.class).sheet("sheet").doWrite(userInfoExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }

    }
}
