package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.dto.ExchangeChainDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleCabinetUsedRecord;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetExtra;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.enterprise.RentBatteryOrderTypeEnum;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.RentBatteryOrderMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import com.xiliulou.electricity.service.pipeline.ProcessController;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import com.xiliulou.electricity.utils.AssertUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.entity.ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE;
import static com.xiliulou.electricity.entity.ElectricityCabinetExtra.MIN_RETAIN_BATTERY;
import static com.xiliulou.electricity.entity.ElectricityCabinetExtra.MIN_RETAIN_EMPTY_CELL;

/**
 * 租电池记录(TRentBatteryOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Service("rentBatteryOrderService")
@Slf4j
public class RentBatteryOrderServiceImpl implements RentBatteryOrderService {
    
    @Resource
    RentBatteryOrderMapper rentBatteryOrderMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    UserCarDepositService userCarDepositService;
    
    @Autowired
    UserActiveInfoService userActiveInfoService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Autowired
    CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    ElectricityCabinetExtraService electricityCabinetExtraService;
    

    @Autowired
    private ElectricityCabinetChooseCellConfigService chooseCellConfigService;
    
    @Autowired
    private EleUserEsignRecordService eleUserEsignRecordService;
    
    @Resource
    private ExchangeExceptionHandlerService exceptionHandlerService;

    @Resource
    private LessTimeExchangeService lessTimeExchangeService;
    
    /**
     * 吞电池优化版本
     */
    private static final String ELE_CABINET_VERSION = "2.1.7";
    @Autowired
    OverdueUserRemarkPublish overdueUserRemarkPublish;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private TenantFranchiseeMutualExchangeService mutualExchangeService;

    @Autowired
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    @Autowired
    private ProcessController processController;

    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    @Override
    public RentBatteryOrder insert(RentBatteryOrder rentBatteryOrder) {
        this.rentBatteryOrderMapper.insert(rentBatteryOrder);
        return rentBatteryOrder;
    }
    
    @Slave
    @Override
    public R queryList(RentBatteryOrderQuery rentBatteryOrderQuery) {
        
        List<RentBatteryOrderVO> rentBatteryOrderVOList = rentBatteryOrderMapper.queryList(rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(rentBatteryOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        if (ObjectUtil.isNotEmpty(rentBatteryOrderVOList)) {
            rentBatteryOrderVOList.parallelStream().forEach(e -> {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    e.setElectricityCabinetName(electricityCabinet.getName());
                }
                
                // 设置加盟商名称
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    e.setFranchiseeName(franchisee.getName());
                }
            });
        }
        
        return R.ok(rentBatteryOrderVOList);
    }
    
    @Override
    public R rentBattery(RentBatteryQuery rentBatteryQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                log.warn("RENT BATTERY WARN! exits unfinished rent battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                log.warn("RENT BATTERY WARN! exits unfinished return battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }
        
        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            log.warn("RENT BATTERY WARN! exits unfinished exchange battery order,uid={}", user.getUid());
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("RENT BATTERY WARN! not found electricityCabinet,eid={}", rentBatteryQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        if (!electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern())) {
            log.warn("RENT BATTERY WARN! electricityCabinet is offline,eid={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        if (isBusiness(electricityCabinet)) {
            log.warn("RENT BATTERY WARN! electricityCabinet is not business,eid={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        try {
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.warn("RENT BATTERY WARN! not found store,eid={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.warn("RENT BATTERY WARN! not found store,storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            
            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.warn("RENT BATTERY WARN! not found franchisee,storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            
            //判断用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("RENT BATTERY WARN! not found user,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("RENT BATTERY WARN! user is unUsable,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }
            
            //未实名认证
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("RENT BATTERY WARN! not auth,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }
            
            //已绑定电池
            if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("RENT BATTERY WARN! user rent battery,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0045", "已绑定电池");
            }
            
            //电子签署拦截
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsEnableEsign(), EleEsignConstant.ESIGN_ENABLE)) {
                EleUserEsignRecord eleUserEsignRecord = eleUserEsignRecordService.queryEsignFinishedRecordByUser(user.getUid(), Long.valueOf(user.getTenantId()));
                if (Objects.isNull(eleUserEsignRecord)) {
                    return R.fail("100329", "请先完成电子签名");
                }
            }
            
            
            Triple<Boolean, String, Object> rentBatteryResult = null;
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                //处理单电
                rentBatteryResult = handlerSingleRentBattery(userInfo, store, electricityCabinet);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return R.fail(rentBatteryResult.getMiddle(), (String) rentBatteryResult.getRight());
                }
            } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                //处理车电一体
                rentBatteryResult = handlerRentBatteryCar(userInfo, store, electricityCabinet);
                if (Boolean.FALSE.equals(rentBatteryResult.getLeft())) {
                    return R.fail(rentBatteryResult.getMiddle(), (String) rentBatteryResult.getRight());
                }
            } else {
                log.warn("RENT BATTERY WARN! not pay deposit,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }
            
            return R.ok(rentBatteryResult.getRight());
        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            log.error("RENT BATTERY ERROR! create order error,uid={}", user.getUid(), e);
        }
        
        return R.ok();
    }
    
    private Triple<Boolean, String, Object> handlerRentBatteryCar(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet) {
        //判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.warn("RENT CAR BATTERY WARN! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        //判断车电一体滞纳金
        if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
            log.warn("RENT CAR BATTERY WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "300001", "存在滞纳金，请先缴纳");
        }
        
        // 查询会员当前信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(userInfo.getTenantId(), userInfo.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }
        
        if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
            log.warn("RENT CAR BATTERY WARN! user memberCard disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户套餐不可用");
        }
        
        // 判断互通加盟商，并且获取加盟商集合
        Set<Long> mutualFranchiseeSet = null;
        try {
            Triple<Boolean, String, Object> isSameFranchiseeTriple = mutualExchangeService.orderExchangeMutualFranchiseeCheck(userInfo.getTenantId(), userInfo.getFranchiseeId(),
                    electricityCabinet.getFranchiseeId());
            if (!isSameFranchiseeTriple.getLeft()) {
                return isSameFranchiseeTriple;
            }
            mutualFranchiseeSet = (Set<Long>) isSameFranchiseeTriple.getRight();
        } catch (Exception e) {
            log.error("RENT Error! orderExchangeMutualFranchiseeCheck is error", e);
            mutualFranchiseeSet = CollUtil.newHashSet(userInfo.getFranchiseeId());
        }

        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("RENT CAR BATTERY WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //是否有正在退款中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("RENT CAR BATTERY WARN! deposit is being refunded,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0051", "押金正在退款中，请勿租电池");
        }
        
        //获取满电电池
        Triple<Boolean, String, Object> acquireFullBatteryResult = allocateFullBatteryBox(electricityCabinet, userInfo, franchisee, mutualFranchiseeSet);
        if (Boolean.FALSE.equals(acquireFullBatteryResult.getLeft())) {
            return acquireFullBatteryResult;
        }
        
        String cellNo = (String) acquireFullBatteryResult.getRight();
        
        //根据换电柜id和仓门查出电池
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(electricityCabinetBox.getSn());
        if (Objects.isNull(electricityBattery)) {
            log.warn("RENT CAR BATTERY WARN! not found battery,batteryName={},uid={}", electricityCabinetBox.getSn(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        
        //修改按此套餐的次数
        carRentalPackageMemberTermBizService.substractResidue(userInfo.getTenantId(), userInfo.getUid());
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_BATTERY, userInfo.getUid());
        
        //生成订单
        RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder().orderId(orderId).electricityBatterySn(electricityBattery.getSn()).uid(userInfo.getUid())
                .phone(userInfo.getPhone()).name(userInfo.getName()).batteryDeposit(userBatteryDeposit.getBatteryDeposit()).type(RentBatteryOrder.TYPE_USER_RENT)
                .orderSeq(RentBatteryOrder.STATUS_INIT).status(RentBatteryOrder.INIT).electricityCabinetId(electricityCabinet.getId()).cellNo(Integer.valueOf(cellNo))
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).storeId(store.getId()).franchiseeId(store.getFranchiseeId())
                .tenantId(TenantContextHolder.getTenantId())
                .channel(ChannelSourceContextHolder.get())
                .build();
        rentBatteryOrderMapper.insert(rentBatteryOrder);
        
        //发送开门命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cellNo", cellNo);
        dataMap.put("orderId", orderId);
        dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RENT_OPEN_DOOR).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        
        return Triple.of(true, null, orderId);
    }
    
    private Triple<Boolean, String, Object> handlerSingleRentBattery(UserInfo userInfo, Store store, ElectricityCabinet electricityCabinet) {
        //判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("RENT BATTERY WARN! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ORDER WARN! batteryMemberCard not found! uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("RENT BATTERY WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
        }
    
        //判断车电关联是否可租电
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
            if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                try {
                    if (carRentalPackageMemberTermBizService.isExpirePackageOrder(userInfo.getTenantId(), userInfo.getUid())) {
                        log.error("RENT BATTERY WARN! user car memberCard expire,uid={}", userInfo.getUid());
                        return Triple.of(false, "100233", "租车套餐已过期");
                    }
                } catch (Exception e) {
                    log.error("RENT BATTERY WARN! acquire car membercard expire result fail,uid={}", userInfo.getUid(), e);
                }
            }
        }
        
        //校验是否有退租审核中的订单
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
            return Triple.of(false, "100282", "租金退款审核中，请等待审核确认后操作");
        }
        

        // 判断互通加盟商，并且获取加盟商集合
        Set<Long> mutualFranchiseeSet = null;
        try {
            Triple<Boolean, String, Object> isSameFranchiseeTriple = mutualExchangeService.orderExchangeMutualFranchiseeCheck(userInfo.getTenantId(), userInfo.getFranchiseeId(),
                    electricityCabinet.getFranchiseeId());
            if (!isSameFranchiseeTriple.getLeft()) {
                return isSameFranchiseeTriple;
            }
            mutualFranchiseeSet = (Set<Long>) isSameFranchiseeTriple.getRight();
        } catch (Exception e) {
            log.error("RENT Error! orderExchangeMutualFranchiseeCheck is error", e);
            mutualFranchiseeSet = CollUtil.newHashSet(userInfo.getFranchiseeId());
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("ELE WARN! not found franchisee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
        }
        
        //是否有正在退款中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("RENT BATTERY WARN! deposit is being refunded,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0051", "押金正在退款中，请勿租电池");
        }
        
        //获取满电电池
        Triple<Boolean, String, Object> acquireFullBatteryResult = allocateFullBatteryBox(electricityCabinet, userInfo, franchisee, mutualFranchiseeSet);
        if (Boolean.FALSE.equals(acquireFullBatteryResult.getLeft())) {
            return acquireFullBatteryResult;
        }

        
        String cellNo = (String) acquireFullBatteryResult.getRight();
        
        //根据换电柜id和仓门查出电池
        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(electricityCabinetBox.getSn());
        if (Objects.isNull(electricityBattery)) {
            log.warn("RENT BATTERY WARN! not found battery,batteryName={}", electricityCabinetBox.getSn());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        
        //修改按此套餐的次数
        Triple<Boolean, String, String> modifyResult = electricityCabinetOrderService.checkAndModifyMemberCardCount(userBatteryMemberCard, batteryMemberCard);
        if (Boolean.FALSE.equals(modifyResult.getLeft())) {
            return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
        }
        
        //记录活跃时间
        userActiveInfoService.userActiveRecord(userInfo);
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_BATTERY, userInfo.getUid());
        
        //根据套餐类型, 设置当前订单类型
        Integer orderType = BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType()) ? RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode() : RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();

        //生成订单
        RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder()
                .orderId(orderId)
                .electricityBatterySn(electricityBattery.getSn())
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .batteryDeposit(userBatteryDeposit.getBatteryDeposit())
                .type(RentBatteryOrder.TYPE_USER_RENT)
                .orderSeq(RentBatteryOrder.STATUS_INIT)
                .status(RentBatteryOrder.INIT)
                .orderType(orderType)
                .electricityCabinetId(electricityCabinet.getId())
                .cellNo(Integer.valueOf(cellNo))
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .storeId(store.getId())
                .franchiseeId(store.getFranchiseeId())
                .tenantId(TenantContextHolder.getTenantId())
                .channel(ChannelSourceContextHolder.get())
                .build();
        rentBatteryOrderMapper.insert(rentBatteryOrder);
        
        //发送开门命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cellNo", cellNo);
        dataMap.put("orderId", orderId);
        dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());
        
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId()).data(dataMap)
                .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RENT_OPEN_DOOR).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        
        return Triple.of(true, null, orderId);
    }
    
    @Override
    public R returnBattery(Integer electricityCabinetId) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
//        if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.NOT_ALLOW_RETURN_ELE, electricityConfig.getAllowReturnEle())) {
//            return R.fail("ELECTRICITY.100272", "当前柜机不支持退电");
//        }
        
        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                log.warn("RETURN BATTERY WARN! exits unfinished rent battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                log.warn("RETURN BATTERY WARN! exits unfinished return battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }
        
        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            log.warn("RETURN BATTERY WARN! exits unfinished exchange battery order,uid={}", user.getUid());
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }
        
        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("RETURN BATTERY WARN! not found electricityCabinet,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("RETURN BATTERY WARN! electricityCabinet is offline,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            log.warn("RETURN BATTERY WARN! electricityCabinet is not business,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }
        
        try {
            
            //检查空格挡数量
//            Triple<Boolean, String, Object> checkBoxResult = electricityCabinetBoxService.selectAvailableBoxNumber(electricityCabinetId, TenantContextHolder.getTenantId());
//            if (Boolean.FALSE.equals(checkBoxResult.getLeft())) {
//                return R.fail(checkBoxResult.getMiddle(), String.valueOf(checkBoxResult.getRight()));
//            }
            
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.warn("RETURN BATTERY WARN! not found store,electricityCabinetId={}", electricityCabinetId);
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.warn("RETURN BATTERY WARN! not found store,storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            
            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.warn("RETURN BATTERY WARN! not found franchisee,storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            
            //用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("RETURN BATTERY WARN! not found user,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("RETURN BATTERY WARN! user is unUsable,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                log.warn("RETURN BATTERY WARN! not found franchisee,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
            
            //判断该换电柜加盟商和用户加盟商是否一致
            if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(userInfo.getTenantId(), userInfo.getFranchiseeId(), electricityCabinet.getFranchiseeId())) {
                log.warn("RETURN BATTERY WARN!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", userInfo.getUid(), userInfo.getFranchiseeId(),
                        electricityCabinet.getFranchiseeId());
                return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
            }
            

            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("RETURN BATTERY WARN! not pay deposit,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }
            
            //未绑定电池
            if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("RETURN BATTERY WARN! not rent battery,uid={} ", user.getUid());
                return R.fail("ELECTRICITY.0033", "用户未绑定电池");
            }
            
            Integer orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_NORMAL.getCode();
            
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());

            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                //判断电池滞纳金
                
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("RETURN BATTERY WARN! user haven't memberCard uid={}", userInfo.getUid());
                    return R.fail("100210", "用户未开通套餐");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                    log.warn("RETURN BATTERY WARN! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐停卡审核中");
                }
                
                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.warn("RETURN BATTERY WARN! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐已暂停");
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("RETURN BATTERY WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                    return R.fail("ELECTRICITY.00121", "套餐不存在");
                }
    
                //根据套餐类型，设置租退订单类型
                if (BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode().equals(batteryMemberCard.getBusinessType())){
                    orderType = RentBatteryOrderTypeEnum.RENT_ORDER_TYPE_ENTERPRISE.getCode();
                }

                //判断用户电池服务费
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                        batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("RETURN BATTERY WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    return R.fail("ELECTRICITY.100000", "存在电池服务费", acquireUserBatteryServiceFeeResult.getRight());
                }
            } else {
                //判断车电一体滞纳金
                if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                    log.warn("RETURN BATTERY WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    return R.fail("300001", "存在滞纳金，请先缴纳");
                }
            }

            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW) && Objects.isNull(
                    electricityBattery)) {
                return R.fail("300870", "系统检测到您未绑定电池，请检查");
            }
            
            
            //分配开门格挡
            //Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNoV2(electricityCabinet.getId(), electricityCabinet.getVersion());
            Triple<Boolean, Integer, String> usableEmptyCellNo = findUsableEmptyCellNo(userInfo.getUid(), electricityCabinet.getId(), electricityCabinet.getVersion());
            
            if ((!usableEmptyCellNo.getLeft()) || Objects.isNull(usableEmptyCellNo.getMiddle())) {
                log.warn("RETURNBATTERY WARN! electricityCabinet not empty cell,electricityCabinetId={} ", electricityCabinetId);
                return R.fail("100240", usableEmptyCellNo.getRight());
            }
            
            String cellNo = usableEmptyCellNo.getMiddle().toString();
            
            String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_BATTERY, user.getUid());
            
            //记录活跃时间
            userActiveInfoService.userActiveRecord(userInfo);

            //生成订单
            RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder().orderId(orderId).uid(user.getUid())
                    .phone(userInfo.getPhone()).name(userInfo.getName())
                    .batteryDeposit(userBatteryDeposit.getBatteryDeposit())
                    .type(RentBatteryOrder.TYPE_USER_RETURN)
                    .orderSeq(RentBatteryOrder.STATUS_INIT).status(RentBatteryOrder.INIT)
                    .orderType(orderType)
                    .electricityCabinetId(electricityCabinet.getId()).cellNo(Integer.valueOf(cellNo))
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                    .storeId(store.getId())
                    .franchiseeId(store.getFranchiseeId())
                    .tenantId(TenantContextHolder.getTenantId())
                    .channel(ChannelSourceContextHolder.get())
                    .build();
            rentBatteryOrderMapper.insert(rentBatteryOrder);
            
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", cellNo);
            dataMap.put("orderId", orderId);
            dataMap.put("newUserBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
            
            //是否开启电池检测
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", Objects.isNull(electricityBattery) ? "UNKNOW" : electricityBattery.getSn());
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }
            
            List<String> oldBatteryTypes = null;
            List<String> batteryTypeList = userBatteryTypeService.selectByUid(userInfo.getUid());
            
            if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
                dataMap.put("model_type", false);
            } else {
                dataMap.put("model_type", true);
                if (Objects.nonNull(electricityBattery)) {
                    dataMap.put("multiBatteryModelName", electricityBattery.getModel());
                    dataMap.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
                } else {
                    ElectricityBattery lastElectricityBattery = selectLastExchangeOrderBattery(userInfo);
                    dataMap.put("multiBatteryModelName", Objects.isNull(lastElectricityBattery) ? "UNKNOWN" : lastElectricityBattery.getModel());
                    //获取用户绑定的电池型号
                    dataMap.put("multiBatteryModelNameList", JsonUtil.toJson(batteryTypeList));
                }
            }
            
            // 需要根据电池与用户绑定的电池型号确认是否需要取缓存内的电池型号用于还电池校验
            oldBatteryTypes = electricityCabinetOrderService.getBatteryTypesForCheck(userInfo, electricityBattery, batteryTypeList);
            if (CollectionUtils.isNotEmpty(oldBatteryTypes)) {
                dataMap.put("multiBatteryModelNameList", JsonUtil.toJson(oldBatteryTypes));
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId()).data(dataMap)
                    .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RETURN_OPEN_DOOR)
                    .build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            //为逾期用户清除备注
            overdueUserRemarkPublish.publish(user.getUid(), OverdueType.BATTERY.getCode(), user.getTenantId());
            return R.ok(orderId);
        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            log.error("RETURN BATTERY ERROR! create order error,uid={}", user.getUid(), e);
        }
        
        return R.ok();
    }
    
    private Triple<Boolean, Integer, String> findUsableEmptyCellNo(Long uid, Integer eid, String version) {
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEidFromCache(Long.valueOf(eid));
        if (Objects.isNull(cabinetExtra)) {
            return Triple.of(false, null, "换电柜异常，不存在的换电柜扩展信息");
        }
        log.info("RentBatteryOrderServiceImpl/findUsableEmptyCellNo, version={}, cabinetExtra is={}", version, JsonUtil.toJson(cabinetExtra));
        
        // 空仓数
        List<ElectricityCabinetBox> emptyCellList = electricityCabinetBoxService.listUsableEmptyCell(eid);
        if (CollUtil.isEmpty(emptyCellList)) {
            return Triple.of(false, null, "当前无空余格挡可供退电，请联系客服！");
        }
        
        if (!Objects.equals(cabinetExtra.getReturnTabType(), RentReturnNormEnum.ALL_RETURN.getCode())) {
            // 不允许退电
            if (Objects.equals(cabinetExtra.getReturnTabType(), RentReturnNormEnum.NOT_RETURN.getCode())) {
                return Triple.of(false, null, "当前柜机暂不符合退电标准，无法退电，请选择其他柜机！");
            }
            // 最少保留一个空仓
            if (Objects.equals(cabinetExtra.getReturnTabType(), RentReturnNormEnum.MIN_RETURN.getCode())) {
                if (emptyCellList.size() <= MIN_RETAIN_EMPTY_CELL) {
                    return Triple.of(false, null, "当前柜机暂不符合退电标准，无法退电，请选择其他柜机！");
                }
            }
            // 自定义退电
            if (Objects.equals(cabinetExtra.getReturnTabType(), RentReturnNormEnum.CUSTOM_RETURN.getCode())) {
                if (emptyCellList.size() <= cabinetExtra.getMaxRetainBatteryCount()) {
                    return Triple.of(false, null, "当前柜机暂不符合退电标准，无法退电，请选择其他柜机！");
                }
            }
        }
        
        //旧版本仍走旧分配逻辑
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(ELE_CABINET_VERSION, version) > 0) {
            Pair<Boolean, Integer> versionPair = electricityCabinetService.findUsableEmptyCellNo(eid);
            return versionPair.getLeft() ? Triple.of(versionPair.getLeft(), versionPair.getRight(), null)
                    : Triple.of(versionPair.getLeft(), null, "当前无空余格挡可供退电，请联系客服！");
        }
        
        Integer cellNo = null;
        //可用格挡只有一个默认直接分配
        if (emptyCellList.size() == 1) {
            cellNo = Integer.valueOf(emptyCellList.get(0).getCellNo());
            return Triple.of(true, cellNo, null);
        }
        
        // 过滤异常的仓内号
        Pair<Boolean, List<ElectricityCabinetBox>> filterEmptyExchangeCellPair = exceptionHandlerService.filterEmptyExceptionCell(eid, emptyCellList);
        if (filterEmptyExchangeCellPair.getLeft()) {
            return Triple.of(true,
                    Integer.parseInt(filterEmptyExchangeCellPair.getRight().get(ThreadLocalRandom.current().nextInt(filterEmptyExchangeCellPair.getRight().size())).getCellNo()), null);
        }
        emptyCellList = filterEmptyExchangeCellPair.getRight();
        
        
        // 舒适换电分配空仓
        Pair<Boolean, Integer> comfortExchangeGetEmptyCellPair = chooseCellConfigService.comfortExchangeGetEmptyCell(uid, emptyCellList);
        if (comfortExchangeGetEmptyCellPair.getLeft()) {
            return Triple.of(true, comfortExchangeGetEmptyCellPair.getRight(), null);
        }
        
        //有多个空格挡  优先分配开门的格挡
        List<ElectricityCabinetBox> openDoorEmptyCellList = emptyCellList.stream().filter(item -> Objects.equals(item.getIsLock(), ElectricityCabinetBox.OPEN_DOOR))
                .collect(Collectors.toList());
        if (!org.springframework.util.CollectionUtils.isEmpty(openDoorEmptyCellList)) {
            cellNo = Integer.parseInt(openDoorEmptyCellList.get(ThreadLocalRandom.current().nextInt(openDoorEmptyCellList.size())).getCellNo());
            return Triple.of(true, cellNo, null);
        }
        
        cellNo = Integer.parseInt(emptyCellList.get(ThreadLocalRandom.current().nextInt(emptyCellList.size())).getCellNo());
        return Triple.of(true, cellNo,null);
    }
    
    private boolean filterNotExchangeable(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.nonNull(electricityCabinetBox) && Objects.nonNull(electricityCabinetBox.getPower()) && StringUtils.isNotBlank(electricityCabinetBox.getSn()) && !StringUtils.startsWithIgnoreCase(
                electricityCabinetBox.getSn(), "UNKNOW");
    }
    
    private ElectricityBattery selectLastExchangeOrderBattery(UserInfo userInfo) {
        ElectricityCabinetOrder lastElectricityCabinetOrder = electricityCabinetOrderService.selectLatestByUidV2(userInfo.getUid());
        if (Objects.isNull(lastElectricityCabinetOrder) || StringUtils.isBlank(lastElectricityCabinetOrder.getNewElectricityBatterySn())) {
            return null;
        }
        
        return electricityBatteryService.queryBySnFromDb(lastElectricityCabinetOrder.getNewElectricityBatterySn());
    }
    
    
    @Override
    public void update(RentBatteryOrder rentBatteryOrder) {
        rentBatteryOrderMapper.updateById(rentBatteryOrder);
    }
    
    @Override
    public R openDoor(RentOpenDoorQuery rentOpenDoorQuery) {
        if (Objects.isNull(rentOpenDoorQuery.getOrderId()) || Objects.isNull(rentOpenDoorQuery.getOpenType())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(
                Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, rentOpenDoorQuery.getOrderId()));
        if (Objects.isNull(rentBatteryOrder)) {
            log.warn("ELECTRICITY  WARN! not found order,orderId={} ", rentOpenDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)) {
            if (!Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT) || (!Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.INIT)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_INIT_CHECK) && !Objects.equals(rentBatteryOrder.getStatus(),
                    RentBatteryOrder.RENT_BATTERY_NOT_EXISTS) && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_OPEN_FAIL))) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        
        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)) {
            if (!Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN) || (!Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.INIT)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_INIT_CHECK) && !Objects.equals(rentBatteryOrder.getStatus(),
                    RentBatteryOrder.RETURN_BATTERY_EXISTS) && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_OPEN_FAIL))) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
            
        }
        
        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(rentBatteryOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }
        
        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId={}", rentBatteryOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet={}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }
        
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.isNull(electricityBattery)) {
            log.error("RENT BATTERY ERROR! not found user bind battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        
        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId()).data(dataMap)
                    .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RENT_OPEN_DOOR)
                    .build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
        }
        
        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());
            
            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(user.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", electricityBattery.getSn());
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }
            
            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId()).data(dataMap)
                    .productKey(electricityCabinet.getProductKey()).deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.ELE_COMMAND_RETURN_OPEN_DOOR)
                    .build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            
        }
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + rentBatteryOrder.getOrderId());
        return R.ok(rentBatteryOrder.getOrderId());
    }
    
    @Override
    public RentBatteryOrder queryByOrderId(String orderId) {
        return rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
    }
    
    @Override
    public R endOrder(String orderId) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(
                new LambdaQueryWrapper<RentBatteryOrder>().eq(RentBatteryOrder::getOrderId, orderId).eq(RentBatteryOrder::getTenantId, tenantId));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("endOrder  ERROR! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0015", "未找到用户");
        }
        
        //租电池
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS) || Objects.equals(rentBatteryOrder.getStatus(),
                    RentBatteryOrder.ORDER_CANCEL) || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                log.error("endOrder  ERROR! not found order,orderId={} ", orderId);
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
            
            //回退单电套餐次数
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(rentBatteryOrder.getUid());
                if (Objects.nonNull(userBatteryMemberCard)) {
                    BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                    if (Objects.nonNull(batteryMemberCard) && Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                        userBatteryMemberCardService.plusCount(userBatteryMemberCard.getUid());
                    }
                }
            }
            
            //回退车电一体套餐次数
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                carRentalPackageMemberTermBizService.addResidue(userInfo.getTenantId(), userInfo.getUid());
            }
        }
        
        //还电池
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS) || Objects.equals(rentBatteryOrder.getStatus(),
                    RentBatteryOrder.ORDER_CANCEL) || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                log.error("endOrder  ERROR! not found order,orderId={} ", orderId);
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        
        RentBatteryOrder rentBatteryOrderUpdate = new RentBatteryOrder();
        rentBatteryOrderUpdate.setId(rentBatteryOrder.getId());
        rentBatteryOrderUpdate.setStatus(RentBatteryOrder.ORDER_EXCEPTION_CANCEL);
        rentBatteryOrderUpdate.setOrderSeq(RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        rentBatteryOrderUpdate.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrderMapper.updateById(rentBatteryOrderUpdate);
        
        //删除开门失败缓存
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        
        return R.ok();
    }
    
    @Override
    public RentBatteryOrder queryByUidAndType(Long uid) {
        return rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getUid, uid)
                .notIn(RentBatteryOrder::getStatus, RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS, RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS,
                        RentBatteryOrder.ORDER_EXCEPTION_CANCEL, RentBatteryOrder.ORDER_CANCEL).orderByDesc(RentBatteryOrder::getCreateTime).last("limit 0,1"));
    }
    
    @Slave
    @Override
    public void exportExcel(RentBatteryOrderQuery rentBatteryOrderQuery, HttpServletResponse response) {
        rentBatteryOrderQuery.setOffset(0L);
        rentBatteryOrderQuery.setSize(2000L);
        List<RentBatteryOrderVO> rentBatteryOrderVOList = rentBatteryOrderMapper.queryList(rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(rentBatteryOrderVOList)) {
            throw new CustomBusinessException("查不到订单");
        }
        
        List<RentBatteryOrderExcelVO> rentBatteryOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (RentBatteryOrderVO rentBatteryOrderVO : rentBatteryOrderVOList) {
            index++;
            RentBatteryOrderExcelVO excelVo = new RentBatteryOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(rentBatteryOrderVO.getOrderId());
            excelVo.setPhone(rentBatteryOrderVO.getPhone());
            excelVo.setName(rentBatteryOrderVO.getName());
            excelVo.setCellNo(rentBatteryOrderVO.getCellNo());
            excelVo.setElectricityBatterySn(rentBatteryOrderVO.getElectricityBatterySn());
            excelVo.setBatteryDeposit(rentBatteryOrderVO.getBatteryDeposit());
            excelVo.setEleName(
                    Optional.ofNullable(electricityCabinetService.queryByIdFromCache(rentBatteryOrderVO.getElectricityCabinetId())).orElse(new ElectricityCabinet()).getName());
            
            if (Objects.nonNull(rentBatteryOrderVO.getCreateTime())) {
                excelVo.setCreatTime(simpleDateFormat.format(new Date(rentBatteryOrderVO.getCreateTime())));
            }
            
            if (Objects.isNull(rentBatteryOrderVO.getType())) {
                excelVo.setType("");
            }
            if (Objects.equals(rentBatteryOrderVO.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                excelVo.setType("租电池");
            }
            if (Objects.equals(rentBatteryOrderVO.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                excelVo.setType("还电池");
            }
            
            //订单状态
            if (Objects.isNull(rentBatteryOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.INIT)) {
                excelVo.setStatus("初始化");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_INIT_CHECK)) {
                excelVo.setStatus("租电池前置检测");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_NOT_EXISTS)) {
                excelVo.setStatus("租电池格挡是空仓");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_OPEN_SUCCESS)) {
                excelVo.setStatus("租电池开门成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_OPEN_FAIL)) {
                excelVo.setStatus("租电池开门失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
                excelVo.setStatus("租电池成功取走");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_TIMEOUT)) {
                excelVo.setStatus("租电池超时");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_INIT_CHECK)) {
                excelVo.setStatus("还电池前置检测");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_EXISTS)) {
                excelVo.setStatus("还电池仓内有电池");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_OPEN_SUCCESS)) {
                excelVo.setStatus("还电池开门成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_OPEN_FAIL)) {
                excelVo.setStatus("还电池开门失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
                excelVo.setStatus("还电池成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_FAIL)) {
                excelVo.setStatus("还电池检测失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_TIMEOUT)) {
                excelVo.setStatus("还电池检测超时");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                excelVo.setStatus("订单异常结束");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.ORDER_CANCEL)) {
                excelVo.setStatus("订单取消");
            }
            
            rentBatteryOrderExcelVOS.add(excelVo);
        }
        
        String fileName = "租电订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, RentBatteryOrderExcelVO.class).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).sheet("sheet")
                    .doWrite(rentBatteryOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
 
    @Override
    public R queryNewStatus(String orderId) {
        Map<String, Object> map = new HashMap<>();
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
        if (Objects.isNull(rentBatteryOrder)) {
            log.warn("ELECTRICITY  WARN! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        String status = rentBatteryOrder.getStatus();
        
        //开门中
        if (rentBatteryOrder.getOrderSeq() < RentBatteryOrder.STATUS_OPEN_SUCCESS || rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_OPEN_FAIL)) {
            status = rentBatteryOrder.getCellNo() + "号仓门开门中";
        }
        
        //开门成功
        if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_OPEN_SUCCESS) || Objects.equals(rentBatteryOrder.getStatus(),
                RentBatteryOrder.RETURN_OPEN_SUCCESS)) {
            status = rentBatteryOrder.getCellNo() + "号仓门开门成功，电池检测中";
        }
        
        //订单状态
        map.put("status", status);
        
        //页面图片显示
        Integer picture = 0;
        
        //rent
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            picture = 2;
        }
        
        //return
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            picture = 1;
        }
        
        //error
        if (rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_ORDER_CANCEL) || ElectricityCabinetOrder.STATUS_INIT_DEVICE_USING.equals(rentBatteryOrder.getOrderSeq())
                || rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
            
            picture = 3;
        }
        
        //订单状态
        map.put("picture", picture);
        
        //是否出错 0--未出错 1--出错
        Integer type = 0;
        //是否重试 0--重试  1--不能重试
        Integer isTry = 1;
        
        String result = redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        if (StringUtils.isNotEmpty(result)) {
            WarnMsgVo warnMsgVo = JsonUtil.fromJson(result, WarnMsgVo.class);
            boolean isNeedEndOrder = warnMsgVo.getIsNeedEndOrder();
            if (!isNeedEndOrder) {
                isTry = 0;
            }
            
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_TIMEOUT)) {
                isTry = 1;
            }
            
            String msg = warnMsgVo.getMsg();
            
            //出错信息
            map.put("queryStatus", msg);
            type = 1;
        }
        
        map.put("type", type);
        map.put("isTry", isTry);
        log.info("map is -->{}", map);
        return R.ok(map);
    }
    
    private Triple<Boolean, String, Object> acquireFullBattery(ElectricityCabinet electricityCabinet, String batteryType) {
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, batteryType,
                electricityCabinet.getFullyCharged());
        if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
            return Triple.of(false, "", "换电柜暂无满电电池");
        }
        
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxList.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(usableBoxes)) {
            return Triple.of(false, "", "换电柜暂无满电电池");
        }
        
        //如果存在多个电量满电且相同的电池，取充电器电压最高的
        Double maxPower = usableBoxes.get(0).getPower();
        List<ElectricityCabinetBox> fullBatteryList = usableBoxes.stream().filter(item -> Objects.equals(item.getPower(), maxPower))
                .filter(item -> Objects.nonNull(item.getChargeV())).sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fullBatteryList)) {
            return Triple.of(true, null, fullBatteryList.get(0));
        }
        
        return Triple.of(true, null, usableBoxes.get(0));
    }
    
    private Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee, Set<Long> mutualFranchiseeSet) {
        // 满电标准的电池
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, null,
                electricityCabinet.getFullyCharged());
        
        // 过滤掉电池名称不符合标准的
        List<ElectricityCabinetBox> exchangeableList = electricityCabinetBoxList.stream().filter(item -> filterNotExchangeable(item)).collect(Collectors.toList());
        
        if (CollectionUtils.isEmpty(exchangeableList)) {
            log.info("RENT BATTERY INFO!not found electricityCabinetBoxList,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        
        // 租电限制
        Triple<Boolean, String, Object> rentBatteryLimitHandlerTriple = rentBatteryLimitHandler(electricityCabinet);
        if (!rentBatteryLimitHandlerTriple.getLeft()) {
            return rentBatteryLimitHandlerTriple;
        }


        List<Long> batteryIds = exchangeableList.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());
        List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
        if (CollUtil.isEmpty(electricityBatteries)) {
            return Triple.of(false, "100225", "电池不存在");
        }


        // 把本柜机加盟商的绑定电池信息拿出来
        electricityBatteries = electricityBatteries.stream().filter(e -> mutualFranchiseeSet.contains(e.getFranchiseeId())).collect(Collectors.toList());
        if (!DataUtil.collectionIsUsable(electricityBatteries)) {
            log.warn("EXCHANGE WARN!battery not bind franchisee,eid={}", electricityCabinet.getId());
            return Triple.of(false, "100219", "您的加盟商与电池加盟商不匹配，请更换柜机或联系客服处理。");
        }

        // 获取全部可用电池id
        List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());

        // 把加盟商绑定的电池过滤出来
        exchangeableList = exchangeableList.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());


        String fullBatteryCell = null;

        for (int i = 0; i < exchangeableList.size(); i++) {
            // 20240614修改：过滤掉电池不符合标准的电池
            fullBatteryCell = acquireFullBatteryBox(exchangeableList, userInfo, franchisee, electricityCabinet.getFullyCharged());
            if (StringUtils.isBlank(fullBatteryCell)) {
                log.info("RENT BATTERY INFO!not found fullBatteryCell,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            if (redisService.setNx(CacheConstant.CACHE_LAST_ALLOCATE_FULLY_BATTERY_CELL + electricityCabinet.getId() + ":" + fullBatteryCell, "1", 4 * 1000L, false)) {
                return Triple.of(true, null, fullBatteryCell);
            }
        }

        return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
    }

    private Triple<Boolean, String, Object> rentBatteryLimitHandler(ElectricityCabinet electricityCabinet) {
        ElectricityCabinetExtra cabinetExtra = electricityCabinetExtraService.queryByEidFromCache(Long.valueOf(electricityCabinet.getId()));
        if (Objects.isNull(cabinetExtra)) {
            return Triple.of(false, "ELECTRICITY.0026", "换电柜异常，不存在扩展信息");
        }
        log.info("rentBattery.allocateFullBatteryBox, cabinetExtra is {}", JSON.toJSONString(cabinetExtra));
        
        if (!Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.ALL_RENT.getCode())) {
            // 不允许租电
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.NOT_RENT.getCode())) {
                return Triple.of(false, "ELECTRICITY.0026", "当前柜机暂不符合租电标准，无法租电，请选择其他柜机");
            }
            // 在仓电池数
            List<ElectricityCabinetBox> boxList = electricityCabinetBoxService.selectHaveBatteryCellId(electricityCabinet.getId());
            // 最少保留一块电池
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.MIN_RETAIN.getCode())) {
                if (boxList.size() <= MIN_RETAIN_BATTERY) {
                    return Triple.of(false, "ELECTRICITY.0026", "当前柜机暂不符合租电标准，无法租电，请选择其他柜机");
                }
            }
            // 自定义
            if (Objects.equals(cabinetExtra.getRentTabType(), RentReturnNormEnum.CUSTOM_RENT.getCode())) {
                if (boxList.size() <= cabinetExtra.getMinRetainBatteryCount()) {
                    return Triple.of(false, "ELECTRICITY.0026", "当前柜机暂不符合租电标准，无法租电，请选择其他柜机");
                }
            }
        }
        return Triple.of(true, null, null);
    }
    
    @Override
    public String acquireFullBatteryBox(List<ElectricityCabinetBox> electricityCabinetBoxList, UserInfo userInfo, Franchisee franchisee, Double fullyCharged) {
        electricityCabinetBoxList = electricityCabinetBoxList.stream().filter(item -> !checkFullBatteryBoxIsAllocated(item.getElectricityCabinetId().longValue(), item.getCellNo()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            log.info("RENT BATTERY ALLOCATE FULL BATTERY INFO!cabinetBoxList is empty,uid={}", userInfo.getUid());
            return null;
        }
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            //获取用户绑定的电池型号
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
            if (CollectionUtils.isEmpty(userBatteryTypes)) {
                log.info("RENT BATTERY ALLOCATE FULL BATTERY INFO!not found use battery type,uid={}", userInfo.getUid());
                return null;
            }
            
            List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getSn()) && StringUtils.isNotBlank(item.getBatteryType()) && Objects.nonNull(item.getPower())
                            && userBatteryTypes.contains(item.getBatteryType())).sorted(Comparator.comparing(ElectricityCabinetBox::getPower, Comparator.reverseOrder())
                            .thenComparing(item -> item.getBatteryType().substring(item.getBatteryType().length() - 2), Comparator.reverseOrder())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(usableBoxes)) {
                log.info("RENT BATTERY ALLOCATE FULL BATTERY INFO!usableBoxes is empty,uid={}", userInfo.getUid());
                return null;
            }
            
            electricityCabinetBoxList = usableBoxes;
        }
        
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxList.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(usableBoxes)) {
            log.info("RENT BATTERY ALLOCATE FULL BATTERY INFO!not found usableBoxes,uid={}", userInfo.getUid());
            return null;
        }
        
        // 过滤异常满电仓门
        Pair<Boolean, List<ElectricityCabinetBox>> filterFullExceptionCellPair = exceptionHandlerService.filterFullExceptionCell(usableBoxes);
        if (filterFullExceptionCellPair.getLeft()) {
            return ruleAllotCell(userInfo, filterFullExceptionCellPair.getRight());
        }
        usableBoxes = filterFullExceptionCellPair.getRight();
        
        // 舒适换电
        Pair<Boolean, ElectricityCabinetBox> satisfyComfortExchange = chooseCellConfigService.comfortExchangeGetFullCell(userInfo.getUid(), usableBoxes, fullyCharged);
        if (satisfyComfortExchange.getLeft()) {
            return satisfyComfortExchange.getRight().getCellNo();
        }
        
        return ruleAllotCell(userInfo, usableBoxes);
    }
    
    @Override
    @Slave
    public List<RentBatteryOrder> listByOrderIdList(Set<String> returnOrderIdList) {
        return rentBatteryOrderMapper.selectListByOrderIdList(returnOrderIdList);
    }
    
    @Override
    public Boolean isRendReturnOrder(String orderId) {
        List<String> rentReturnList = BusinessType.getRentReturnList().stream().map(String::valueOf).collect(Collectors.toList());
        return rentReturnList.stream().anyMatch(orderId::startsWith);
    }
    
    
    private static String ruleAllotCell(UserInfo userInfo, List<ElectricityCabinetBox> usableBoxes) {
        //如果存在多个电量满电且相同的电池，取充电器电压最高的
        Double maxPower = usableBoxes.get(0).getPower();
        ElectricityCabinetBox usableCabinetBox = usableBoxes.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).filter(item -> Objects.nonNull(item.getChargeV()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV)).reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(usableCabinetBox)) {
            log.info("RENT BATTERY ALLOCATE FULL BATTERY INFO!usableCabinetBox is null,uid={}", userInfo.getUid());
            return null;
        }
        return usableCabinetBox.getCellNo();
    }
    
    private boolean checkFullBatteryBoxIsAllocated(Long eid, String cellNo) {
        return redisService.hasKey(CacheConstant.CACHE_LAST_ALLOCATE_FULLY_BATTERY_CELL + eid + ":" + cellNo);
    }
    
    @Deprecated
    private Triple<Boolean, String, Object> acquireFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee) {
        //租电满电电池分配规则：优先电量最高，若存在多个电量相同的，则取串数最大的
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, null,
                electricityCabinet.getFullyCharged());
        if (CollectionUtils.isEmpty(electricityCabinetBoxList)) {
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            //获取用户绑定的电池型号
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
            if (CollectionUtils.isEmpty(userBatteryTypes)) {
                log.error("ELE ERROR!not found use battery type,uid={}", userInfo.getUid());
                return Triple.of(false, "100352", "未找到用户电池型号");
            }
            
            List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxList.stream()
                    .filter(item -> StringUtils.isNotBlank(item.getSn()) && StringUtils.isNotBlank(item.getBatteryType()) && Objects.nonNull(item.getPower())
                            && userBatteryTypes.contains(item.getBatteryType())).sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed())
                    .collect(Collectors.toList());
            if (ObjectUtil.isEmpty(usableBoxes)) {
                return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
            }
            
            if (usableBoxes.size() == 1) {
                return Triple.of(true, null, usableBoxes.get(0));
            }
            
            //如果存在多个电量满电且相同的电池，取串数最大的
            Double maxPower = usableBoxes.get(0).getPower();
            electricityCabinetBoxList = usableBoxes.stream().filter(item -> Objects.equals(item.getPower(), maxPower))
                    .sorted(Comparator.comparing(item -> item.getBatteryType().substring(item.getBatteryType().length() - 2))).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(electricityCabinetBoxList)) {
                return Triple.of(false, "", "换电柜暂无满电电池");
            }
        }
        
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxList.stream().filter(item -> StringUtils.isNotBlank(item.getSn()) && Objects.nonNull(item.getPower()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getPower).reversed()).collect(Collectors.toList());
        if (ObjectUtil.isEmpty(usableBoxes)) {
            return Triple.of(false, "", "换电柜暂无满电电池");
        }
        
        //如果存在多个电量满电且相同的电池，取充电器电压最高的
        Double maxPower = usableBoxes.get(0).getPower();
        ElectricityCabinetBox usableCabinetBox = usableBoxes.stream().filter(item -> Objects.equals(item.getPower(), maxPower)).filter(item -> Objects.nonNull(item.getChargeV()))
                .sorted(Comparator.comparing(ElectricityCabinetBox::getChargeV)).reduce((first, second) -> second).orElse(null);
        if (Objects.isNull(usableCabinetBox)) {
            return Triple.of(false, "", "换电柜暂无满电电池");
        }
        return Triple.of(true, null, usableCabinetBox);
    }
    
    //分配满仓
    @Override
    @Deprecated  //@See selectFullBattery
    public Triple<Boolean, String, Object> findUsableBatteryCellNo(ElectricityCabinet electricityCabinet, String cellNo, String batteryType, Long franchiseeId,
            Integer orderSource) {
        
        Integer box = null;
        
        //低电量换电
        Double fullCharged = electricityCabinet.getFullyCharged();
        if (Objects.nonNull(orderSource) && Objects.equals(orderSource, OrderQuery.LOW_BATTERY_ORDER)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinet.getTenantId());
            List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
            if (Objects.nonNull(list) && list.size() > 0) {
                fullCharged = list.get(0).getBatteryPowerStandard();
            }
        }
        
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, cellNo, batteryType, fullCharged);
        if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
            return Triple.of(false, "0", "换电柜暂无满电电池");
        }
        
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = new ArrayList<>();
        
        for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
            //是否满电
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(electricityCabinetBox.getSn());
            if (Objects.nonNull(electricityBattery) && Objects.nonNull(electricityBattery.getFranchiseeId())) {
                
                ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
                BeanUtil.copyProperties(electricityCabinetBox, electricityCabinetBoxVO);
                electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                
                electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
            }
        }
        
        if (ObjectUtil.isEmpty(electricityCabinetBoxVOList)) {
            return Triple.of(false, "0", "加盟商未绑定满电电池");
        }
        
        List<ElectricityCabinetBoxVO> usableBoxes = electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(ElectricityCabinetBoxVO::getPower).reversed())
                .collect(Collectors.toList());
        
        //}
        final Double MAX_POWER = usableBoxes.get(0).getPower();
        usableBoxes = usableBoxes.stream().filter(item -> Objects.equals(item.getPower(), MAX_POWER)).collect(Collectors.toList());
        
        int maxChargeVIndex = 0;
        for (int i = 0; i < usableBoxes.size(); i++) {
            Double maxChargeV = Optional.ofNullable(usableBoxes.get(maxChargeVIndex).getChargeV()).orElse(0.0);
            Double chargeV = Optional.ofNullable(usableBoxes.get(i).getChargeV()).orElse(0.0);
            
            if (maxChargeV.compareTo(chargeV) < 0) {
                maxChargeVIndex = i;
            }
        }
        
        return Triple.of(true, usableBoxes.get(maxChargeVIndex).getCellNo(), null);
    }
    
    @Slave
    @Override
    public R queryCount(RentBatteryOrderQuery rentBatteryOrderQuery) {
        
        return R.ok(rentBatteryOrderMapper.queryCount(rentBatteryOrderQuery));
    }
    
    @Slave
    @Override
    public Integer queryCountForScreenStatistic(RentBatteryOrderQuery rentBatteryOrderQuery) {
        return rentBatteryOrderMapper.queryCount(rentBatteryOrderQuery);
    }
    

    
    @Override
    public RentBatteryOrder selectLatestByUid(Long uid, Integer tenantId, String status) {
        return rentBatteryOrderMapper.selectLatestByUid(uid, tenantId, status);
    }
    
    @Slave
    @Override
    public List<EleCabinetUsedRecordVO> findEleCabinetUsedRecords(EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery) {
        List<EleCabinetUsedRecord> eleCabinetUsedRecords = rentBatteryOrderMapper.selectEleCabinetUsedRecords(eleCabinetUsedRecordQuery);
        if (CollectionUtils.isEmpty(eleCabinetUsedRecords)) {
            return Collections.emptyList();
        }
        
        List<EleCabinetUsedRecordVO> cabinetUsedRecordVOList = new ArrayList<>();
        for (EleCabinetUsedRecord eleCabinetUsedRecord : eleCabinetUsedRecords) {
            EleCabinetUsedRecordVO eleCabinetUsedRecordVO = new EleCabinetUsedRecordVO();
            BeanUtils.copyProperties(eleCabinetUsedRecord, eleCabinetUsedRecordVO);
            
            UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(eleCabinetUsedRecord.getUid());
            if (Objects.nonNull(userInfo)) {
                eleCabinetUsedRecordVO.setUserName(userInfo.getName());
                eleCabinetUsedRecordVO.setPhone(userInfo.getPhone());
            }
            cabinetUsedRecordVOList.add(eleCabinetUsedRecordVO);
        }
        
        return cabinetUsedRecordVOList;
    }
    
    @Slave
    @Override
    public Integer findUsedRecordsTotalCount(EleCabinetUsedRecordQuery eleCabinetUsedRecordQuery) {
        return rentBatteryOrderMapper.selectUsedRecordsTotalCount(eleCabinetUsedRecordQuery);
    }
    
    @Slave
    @Override
    public List<RentBatteryOrder> selectByUidAndTime(Long uid, Long startTime, Long endTime) {
        return rentBatteryOrderMapper.selectByUidAndTime(uid,startTime,endTime);
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
        return rentBatteryOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(RentBatteryOrderQuery rentBatteryOrderQuery) {
        List<RentBatteryOrderVO> rentBatteryOrderVOList = rentBatteryOrderMapper.selectListSuperAdminPage(rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(rentBatteryOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        if (ObjectUtil.isNotEmpty(rentBatteryOrderVOList)) {
            rentBatteryOrderVOList.parallelStream().forEach(e -> {
                if (Objects.nonNull(e.getTenantId())) {
                    Tenant tenant = tenantService.queryByIdFromCache(e.getTenantId());
                    e.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
                }
                
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    e.setElectricityCabinetName(electricityCabinet.getName());
                }
            
                // 设置加盟商名称
                Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    e.setFranchiseeName(franchisee.getName());
                }
            });
        }
    
        return R.ok(rentBatteryOrderVOList);
    }
    
    public boolean isBusiness(ElectricityCabinet electricityCabinet) {
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 根据用户id获取对应的电池套餐或者车电一体套餐是否限制
     * @param uid
     * @return
     */
    @Override
    public R queryRentBatteryOrderLimitCountByUid(Long uid) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 查询用户信息并且判断租户是否与当前登录账户所在租户一致
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), tenantId)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        // 判断用户是否未缴纳押金
        if (!(Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) || Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode()))) {
            return R.fail("100209", "用户未缴纳押金");
        }
        
        // 套餐限制信息
        Integer limit = null;
        
        // 判断订单对应的电池套餐是否限制
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            // 获取用户电池套餐卡信息
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
            // 用户退租或者退押以后套餐id为零
            if (Objects.isNull(userBatteryMemberCard) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO)) {
                return R.fail("100210", "未购买套餐");
            }
            
            // 获取卡对应的套餐详情
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.nonNull(batteryMemberCard)) {
                // 返回会员套餐限制信息
                limit = batteryMemberCard.getLimitCount();
            } else {
                // 套餐不存在
                return R.fail("110202", "换电套餐不存在");
            }
        }
        
        // 判断会员车店一体套餐的限制信息
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            // 查询车电一体的会员期限信息
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // todo 判断套餐id是否为空
            if (Objects.isNull(memberTermEntity) || Objects.isNull(memberTermEntity.getRentalPackageId())) {
                // 套餐不存在
                return R.fail("110204", "车电一体套餐不存在");
            }
            limit = memberTermEntity.getRentalPackageConfine();
        }
        
        // 返回限制信息
        return R.ok(limit);
    }
    
    public Long getTime(Long time) {
        Date date1 = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date1);
        Date date2 = null;
        try {
            date2 = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long ts = date2.getTime();
        return time - ts;
    }
    
    public String generateOrderId(Long uid, String cellNo) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid + cellNo + RandomUtil.randomNumbers(4);
    }
    
    private Triple<Boolean, String, Object> checkUserCarMemberCard(UserCarMemberCard userCarMemberCard, UserInfo user) {
        
        //用户未缴纳押金可直接换电
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            return Triple.of(true, null, null);
        }
        
        //用户未缴纳押金可直接换电
        if (!Objects.equals(user.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(true, null, null);
        }
        
        //用户从未买过车辆套餐则可直接换电
        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getMemberCardExpireTime()) || Objects.equals(userCarMemberCard.getMemberCardExpireTime(), 0L)) {
            return Triple.of(false, "100232", "未购买租车套餐");
        }
        
        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userCarMemberCard.getMemberCardExpireTime() < now) {
            log.error("RENTBATTERY ERROR! user's carMemberCard is expire! uid={} cardId={}", user.getUid(), userCarMemberCard.getCardId());
            return Triple.of(false, "100233", "租车套餐已过期");
        }
        return Triple.of(true, null, null);
    }
    
    @Override
    @Slave
    public Integer existReturnRentOrderInSameCabinetAndCell(Long startTime, Long endTime, Integer eid, Integer cell) {
        return rentBatteryOrderMapper.existReturnRentOrderInSameCabinetAndCell(startTime, endTime, eid, cell);
    }
    
    
    @Override
    @Slave
    public Integer existSameCabinetCellSameTimeOpenReturnOrder(Long createTime, Integer electricityCabinetId, Integer oldCellNo) {
        return rentBatteryOrderMapper.existSameCabinetCellSameTimeOpenReturnOrder(createTime, electricityCabinetId, oldCellNo);
    }



    @Override
    @Slave
    public RentBatteryOrder queryLatelyRentReturnOrder(Long uid, Long startTime, Long currentTime, Integer orderType) {
        return rentBatteryOrderMapper.selectLatelyRentReturnOrder(uid, startTime, currentTime, orderType);
    }


    @Override
    public R returnBatteryCheck(Integer electricityCabinetId) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                log.warn("RETURN BATTERY WARN! exits unfinished rent battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                log.warn("RETURN BATTERY WARN! exits unfinished return battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            log.warn("RETURN BATTERY WARN! exits unfinished exchange battery order,uid={}", user.getUid());
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            log.warn("RETURN BATTERY WARN! not found electricityCabinet,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName(), electricityCabinet.getPattern());
        if (!eleResult) {
            log.warn("RETURN BATTERY WARN! electricityCabinet is offline,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            log.warn("RETURN BATTERY WARN! electricityCabinet is not business,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        try {
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.warn("RETURN BATTERY WARN! not found store,electricityCabinetId={}", electricityCabinetId);
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }

            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.warn("RETURN BATTERY WARN! not found store,storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.warn("RETURN BATTERY WARN! not found franchisee,storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.warn("RETURN BATTERY WARN! not found user,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }

            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("RETURN BATTERY WARN! user is unUsable,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }

            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                log.warn("RETURN BATTERY WARN! not found franchisee,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }

            //判断该换电柜加盟商和用户加盟商是否一致
            if (!mutualExchangeService.isSatisfyFranchiseeMutualExchange(userInfo.getTenantId(), userInfo.getFranchiseeId(), electricityCabinet.getFranchiseeId())) {
                log.warn("RETURN Check BATTERY WARN! FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", userInfo.getUid(), userInfo.getFranchiseeId(),
                        electricityCabinet.getFranchiseeId());
                return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
            }


            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("RETURN BATTERY WARN! not pay deposit,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }

            //未绑定电池
            if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("RETURN BATTERY WARN! not rent battery,uid={} ", user.getUid());
                return R.fail("ELECTRICITY.0033", "用户未绑定电池");
            }


            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                //判断电池滞纳金
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("RETURN BATTERY WARN! user haven't memberCard uid={}", userInfo.getUid());
                    return R.fail("100210", "用户未开通套餐");
                }

                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                    log.warn("RETURN BATTERY WARN! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐停卡审核中");
                }

                if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.warn("RETURN BATTERY WARN! user's member card is stop! uid={}", userInfo.getUid());
                    return R.fail("100211", "换电套餐已暂停");
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("RETURN BATTERY WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
                    return R.fail("ELECTRICITY.00121", "套餐不存在");
                }


                //判断用户电池服务费
                Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                        batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                    log.warn("RETURN BATTERY WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    return R.fail("ELECTRICITY.100000", "存在电池服务费", acquireUserBatteryServiceFeeResult.getRight());
                }
            } else {
                //判断车电一体滞纳金
                if (Boolean.TRUE.equals(carRenalPackageSlippageBizService.isExitUnpaid(userInfo.getTenantId(), userInfo.getUid()))) {
                    log.warn("RETURN BATTERY WARN! user exist battery service fee,uid={}", userInfo.getUid());
                    return R.fail("300001", "存在滞纳金，请先缴纳");
                }
            }

            // 多次退电判断
            Pair<Boolean, ReturnBatteryLessTimeScanVo> pair = lessTimeExchangeService.lessTimeReturnBatteryHandler(userInfo, electricityCabinet);
            if (pair.getLeft()) {
                return R.ok(pair.getRight());
            }

            // 如果超过5分钟或者返回false，这里返回不让前端不进行弹窗
            ReturnBatteryLessTimeScanVo vo = new ReturnBatteryLessTimeScanVo();
            vo.setIsEnterMoreExchange(LessScanConstant.NOT_ENTER_MORE_EXCHANGE);
            return R.ok(vo);

        } catch (BizException e) {
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            log.error("RETURN BATTERY CHECK ERROR! uid={}", user.getUid(), e);
        }
        return R.ok();
    }


    @Override
    public R lessRentReturnSelfOpenCell(LessExchangeSelfOpenCellQuery query) {
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("LessRentSelfOpenCell  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("LessRentSelfOpenCell WARN! not found user,uid is {}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        // 构造责任链入参
        ProcessContext<ExchangeAssertProcessDTO> processContext = ProcessContext.builder().code(ExchangeAssertChainTypeEnum.RENT_RETURN_BATTERY_LESS_OPEN_FULL_ASSERT.getCode()).processModel(
                ExchangeAssertProcessDTO.builder().eid(query.getEid()).cellNo(query.getCellNo()).userInfo(userInfo).orderId(query.getOrderId()).selfOpenCellKey(CacheConstant.RENT_RETURN_ALLOW_SELF_OPEN_CELL_START_TIME_KEY)
                        .chainObject(new ExchangeChainDTO()).build()).needBreak(false).build();
        // 校验
        @SuppressWarnings("unchecked")
        ProcessContext<ExchangeAssertProcessDTO> process = processController.process(processContext);
        if (process.getNeedBreak()) {
            log.warn("rentBatteryLessExchangeSelfOpenCell Warn! BreakReason is {}", JsonUtil.toJson(process.getResult()));
            return process.getResult();
        }

        RentBatteryOrder rentBatteryOrder = process.getProcessModel().getChainObject().getRentBatteryOrder();
        AssertUtil.assertObjectIsNull(rentBatteryOrder, "ELECTRICITY.0015", "未找到订单");

        ElectricityCabinet electricityCabinet = process.getProcessModel().getChainObject().getElectricityCabinet();
        AssertUtil.assertObjectIsNull(electricityCabinet, "100003", "找不到柜机");

        try {

            RentBatteryOrder batteryOrder = new RentBatteryOrder();
            batteryOrder.setId(rentBatteryOrder.getId());
            batteryOrder.setUpdateTime(System.currentTimeMillis());
            batteryOrder.setRemark(ExchangeRemarkConstant.USER_SELF_OPEN_CELL);
            update(batteryOrder);

            // 发送自助开仓命令
            // 发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", query.getOrderId());
            dataMap.put("cellNo", query.getCellNo());
//            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS) && Objects.equals(
//                    electricityCabinetOrder.getNewCellNo(), query.getCellNo())) {
//                dataMap.put("isTakeCell", true);
//            }
            dataMap.put("userSelfOpenCell", true);


            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId();

            HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName()).command(ElectricityIotConstant.SELF_OPEN_CELL).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, electricityCabinet);
            return R.ok(sessionId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "自助开仓失败");
        }
    }
}
