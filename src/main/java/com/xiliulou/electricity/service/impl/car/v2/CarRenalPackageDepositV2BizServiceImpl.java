package com.xiliulou.electricity.service.impl.car.v2;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.hash.MD5Utils;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.converter.PayConfigConverter;
import com.xiliulou.electricity.converter.model.OrderRefundParamConverterModel;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.DepositTypeEnum;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SystemDefinitionEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.v2.CarRenalPackageDepositV2BizService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;

import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.FreeDepositVO;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.pay.base.PayServiceDispatcher;
import com.xiliulou.pay.base.dto.BasePayOrderRefundDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3JsapiInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.xiliulou.electricity.constant.CacheConstant.CAR_FREE_DEPOSIT_USER_INFO_LOCK_KEY;

/**
 * 租车套餐押金业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageDepositV2BizServiceImpl implements CarRenalPackageDepositV2BizService {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private PayConfigBizService payConfigBizService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private CarRentalPackageService carRentalPackageService;
    
    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FreeDepositService freeDepositService;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private SiteMessagePublish siteMessagePublish;
    
    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;
    
    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Resource
    private InsuranceOrderService insuranceOrderService;
    
    @Resource
    private UserBizService userBizService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;
    
    @Resource
    private ElectricityCarService carService;
    
    @Resource
    private ElectricityBatteryService electricityBatteryService;
    
    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;
    
    @Resource
    private WechatConfig wechatConfig;
    
    @Resource
    private WechatV3JsapiInvokeService wechatV3JsapiInvokeService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Resource
    private PayConfigConverter payConfigConverter;
    
    @Resource
    private PayServiceDispatcher payServiceDispatcher;
    
    
    
    @Override
    public FreeDepositUserInfoVo queryFreeDepositStatus(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        // 检测用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userInfo)) {
            log.warn("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_user_info. uid is {}", uid);
            return null;
        }
        // 定义返回信息
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        //这个key代表业务一定是回调成功了，即免押必定是成功的
        String freeSuccessKey = String.format(CAR_FREE_DEPOSIT_USER_INFO_LOCK_KEY,tenantId, uid);
        if (redisService.hasKey(freeSuccessKey)){
            String order = redisService.get(freeSuccessKey);
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(order);
            if (ObjectUtils.isEmpty(freeDepositOrder)) {
                log.warn("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_free_deposit_order. depositPayOrderNo is {}", order);
                return null;
            }
            // 成功返回判定，前端按照时间比对轮询
            if (FreeDepositOrder.DEPOSIT_TYPE_CAR.equals(freeDepositOrder.getDepositType()) || FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY.equals(freeDepositOrder.getDepositType())) {
                freeDepositUserInfoVo.setApplyCarDepositTime(freeDepositOrder.getCreateTime());
                freeDepositUserInfoVo.setCarDepositAuthStatus(freeDepositOrder.getAuthStatus());
            }
            
            //免押成功了，删除对于的key，并返回
            //1.有一种情况未必进入，免押后前端没有调用该接口，就会导致这个key长期持有，此时状态发生改变，也不会进入
            if (Objects.equals(freeDepositOrder.getAuthStatus(),FreeDepositOrder.AUTH_FROZEN)){
                redisService.delete(freeSuccessKey);
                return freeDepositUserInfoVo;
            }
        }
        String userKey = String.format(CacheConstant.FREE_DEPOSIT_USER_INFO_KEY, uid);
        if (redisService.hasKey(userKey)){
            CarRentalPackageDepositPayPo depositPayPo = carRentalPackageDepositPayService.selectLastByUid(tenantId, uid);
            freeDepositUserInfoVo.setApplyCarDepositTime(ObjectUtils.defaultIfNull(depositPayPo.getCreateTime(),0L));
            freeDepositUserInfoVo.setCarDepositAuthStatus(FreeDepositOrder.AUTH_FREEZING);
            return freeDepositUserInfoVo;
        }
        //当业务到这里，说明既没有免押成功的，也没有免押中的，此时该用户对应的订单即失效
        if (redisService.hasKey(freeSuccessKey)){
            redisService.delete(freeSuccessKey);
        }
        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.warn("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_car_rental_package_member_term. uid is {}", uid);
            return null;
        }
        // 查询免押记录信息
        String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(depositPayOrderNo);
        if (ObjectUtils.isEmpty(freeDepositOrder)) {
            log.warn("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_free_deposit_order. depositPayOrderNo is {}", depositPayOrderNo);
            return null;
        }
        // 成功返回判定，前端按照时间比对轮询
        if (FreeDepositOrder.DEPOSIT_TYPE_CAR.equals(freeDepositOrder.getDepositType()) || FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY.equals(freeDepositOrder.getDepositType())) {
            freeDepositUserInfoVo.setApplyCarDepositTime(freeDepositOrder.getCreateTime());
            freeDepositUserInfoVo.setCarDepositAuthStatus(freeDepositOrder.getAuthStatus());
        }
        return freeDepositUserInfoVo;
    }
    
    /**
     * 创建免押订单，生成二维码<br /> 创建押金缴纳订单、生成免押记录
     *  @param tenantId          租户ID
     * @param uid               C端用户ID
     * @param freeDepositOptReq 免押数据申请
     * @return
     */
    @Override
    public FreeDepositVO createFreeDeposit(Integer tenantId, Long uid, FreeDepositOptReq freeDepositOptReq) {
        if (!ObjectUtils.allNotNull(tenantId, uid, freeDepositOptReq, freeDepositOptReq.getRentalPackageId(), freeDepositOptReq.getPhoneNumber(), freeDepositOptReq.getRealName(),
                freeDepositOptReq.getIdCard())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 获取加锁 KEY
        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_CREATE_FREE_ORDER_UID_KEY, uid);
        
        // 加锁
        if (!redisService.setNx(buyLockKey, uid.toString(), 3 * 1000L, false)) {
            throw new BizException("ELECTRICITY.0034", "操作频繁");
        }
        
        // 检测用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            throw new BizException("ELECTRICITY.0041", "未实名认证");
        }
        
        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(memberTermEntity) && !MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300028", "已缴纳押金");
        }
        
        // 查询套餐信息
        Long rentalPackageId = freeDepositOptReq.getRentalPackageId();
        CarRentalPackagePo carRentalPackage = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(carRentalPackage) || UpDownEnum.DOWN.getCode().equals(carRentalPackage.getStatus())) {
            log.warn("CarRenalPackageDepositBizService.createFreeDeposit failed. not found t_car_rental_package or status is wrong. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }
        if (!carRentalPackage.getTenantId().equals(tenantId)) {
            log.warn("CarRenalPackageDepositBizService.createFreeDeposit failed. Tenant mismatch. rentalPackage tenantId is {}, param tenantId is {}",
                    carRentalPackage.getTenantId(), tenantId);
            throw new BizException("300000", "数据有误");
        }
        
        if ((!UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(userInfo.getBatteryDepositStatus()) && RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackage.getType())) || (
                !UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus()) && RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackage.getType()))) {
            log.warn("CarRenalPackageDepositBizService.createFreeDeposit failed. rentalPackage type mismatch. rentalPackage type is {}", carRentalPackage.getType());
            throw new BizException("300005", "套餐不匹配");
        }
        
        FreeDepositUserDTO freeDepositUserDTO = FreeDepositUserDTO.builder().uid(userInfo.getUid()).realName(freeDepositOptReq.getRealName())
                .phoneNumber(freeDepositOptReq.getPhoneNumber()).idCard(freeDepositOptReq.getIdCard()).tenantId(tenantId).packageId(freeDepositOptReq.getRentalPackageId())
                .packageType(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()) //针对租车和车电一体套餐统一判断。为了区别换电套餐。换电套餐类型为1，租车/车电一体统一使用2。
                .build();
        
        //检查用户是否已经进行过免押操作，且已免押成功
        Triple<Boolean, String, Object> useFreeDepositStatusResult = freeDepositService.checkExistSuccessFreeDepositOrder(freeDepositUserDTO);
        
        if (Boolean.TRUE.equals(useFreeDepositStatusResult.getLeft())) {
            throw new BizException(useFreeDepositStatusResult.getMiddle(), useFreeDepositStatusResult.getRight().toString());
        }
        String md5 = MD5Utils.digest(Optional.ofNullable(freeDepositUserDTO.getRealName()).orElse("").trim()+
                Optional.ofNullable(freeDepositUserDTO.getIdCard()).orElse("").trim()+
                Optional.ofNullable(freeDepositUserDTO.getPackageId()).orElse(-1L));
        //查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(String.format(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2,uid,md5));
        if (Objects.isNull(useFreeDepositStatusResult.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(String.format(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2,uid,md5)), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result from cache for car rental. uid = {}, result = {}", uid, result);
            
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.queryUserOrderByHash(freeDepositUserDTO.getTenantId(), freeDepositUserDTO.getUid(),md5);
            // 查询最后一次的免押订单信息
            CarRentalPackageDepositPayPo carRentalPackageDepositPayOri = carRentalPackageDepositPayService.selectByOrderNo(freeDepositOrder.getOrderId());
            if (ObjectUtils.isEmpty(carRentalPackageDepositPayOri)) {
                log.error("t_car_rental_package_deposit_pay not found. uid = {}", uid);
                throw new BizException("300015", "押金订单状态异常");
            }
            CarRentalPackageMemberTermPo memberTermInsertOrUpdateEntity = buildCarRentalPackageMemberTerm(tenantId, uid, carRentalPackage, carRentalPackageDepositPayOri.getOrderNo(),
                    memberTermEntity);
            // 此时代表：在5分钟内用户调用了取消订单的接口且二次申请免押，则需要创建租车会员信息
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                carRentalPackageMemberTermService.insert(memberTermInsertOrUpdateEntity);
            }else {
                memberTermInsertOrUpdateEntity.setId(memberTermEntity.getId());
                carRentalPackageMemberTermService.updateById(memberTermInsertOrUpdateEntity);
            }
            return result;
        }
        
        // 创建押金缴纳订单
        CarRentalPackageDepositPayPo carRentalPackageDepositPayInsert = buildCarRentalPackageDepositPayEntity(tenantId, uid, carRentalPackage, YesNoEnum.YES.getCode(),
                PayTypeEnum.EXEMPT.getCode());
        // 创建免押记录
        FreeDepositOrder freeDepositOrder = buildFreeDepositOrderEntity(tenantId, uid, carRentalPackageDepositPayInsert, freeDepositOptReq, carRentalPackage);
        // 创建租车会员信息
        CarRentalPackageMemberTermPo memberTermInsertOrUpdateEntity = buildCarRentalPackageMemberTerm(tenantId, uid, carRentalPackage,
                carRentalPackageDepositPayInsert.getOrderNo(), memberTermEntity);
        
        // 调用第三方
        Triple<Boolean, String, Object> triple = freeDepositService.freeDepositOrder(
                FreeDepositOrderRequest.builder().idCard(freeDepositOrder.getIdCard()).tenantId(tenantId).realName(freeDepositOrder.getRealName()).subject("租车套餐免押")
                        .payAmount(BigDecimal.valueOf(freeDepositOrder.getTransAmt())).freeDepositOrderId(freeDepositOrder.getOrderId()).phoneNumber(freeDepositOrder.getPhone())
                        .uid(uid).build());
        if (Boolean.FALSE.equals(triple.getLeft())) {
            throw new BizException(triple.getMiddle(), triple.getRight().toString());
        }
        FreeDepositOrderDTO freeDepositOrderDTO = (FreeDepositOrderDTO) triple.getRight();
        
        freeDepositOrder.setChannel(freeDepositOrderDTO.getChannel());
        freeDepositOrder.setPayTransAmt(freeDepositOrder.getTransAmt());
        freeDepositOrder.setPackageId(freeDepositOptReq.getRentalPackageId());
        log.info("Transaction inventory: md5: {}, free: {}",md5 , JsonUtil.toJson(freeDepositOrder));
        // TX 事务落库
        saveFreeDepositTx(carRentalPackageDepositPayInsert, freeDepositOrder, memberTermInsertOrUpdateEntity);
        //保存返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(String.format(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2,uid,md5), UriUtils.encode(freeDepositOrderDTO.getData(), StandardCharsets.UTF_8),
                300 * 1000L, false);
        String userKey = String.format(CacheConstant.FREE_DEPOSIT_USER_INFO_KEY, uid);
        String val = redisService.get(userKey);
        if (StringUtils.isNotEmpty(val) && !val.contains(md5)){
            val = String.format("%s,%s",val,md5);
        }
        redisService.set(userKey,StringUtils.isEmpty(val)?md5:val ,5L, TimeUnit.MINUTES);
    
        FreeDepositVO freeDepositVO = new FreeDepositVO();
        freeDepositVO.setQrCode(freeDepositOrderDTO.getData());
        freeDepositVO.setPath(freeDepositOrderDTO.getPath());
        freeDepositVO.setExtraData(freeDepositOrderDTO.getExtraData());
        
        return freeDepositVO;
    }
    
    /**
     * 免押申请数据落库事务处理
     *
     * @param carRentalPackageDepositPay 车辆押金缴纳订单
     * @param freeDepositOrder           免押记录
     * @param memberTermEntity           新增的会员期限信息
     */
    public void saveFreeDepositTx(CarRentalPackageDepositPayPo carRentalPackageDepositPay, FreeDepositOrder freeDepositOrder, CarRentalPackageMemberTermPo memberTermEntity) {
        carRentalPackageDepositPayService.insert(carRentalPackageDepositPay);
        freeDepositOrderService.insert(freeDepositOrder);
        // 此时会员期限表，数据要么为空，要么就是待生效状态
        if (ObjectUtils.isNotEmpty(memberTermEntity)) {
            if (ObjectUtils.isNotEmpty(memberTermEntity.getId())) {
                carRentalPackageMemberTermService.updateById(memberTermEntity);
            } else {
                carRentalPackageMemberTermService.insert(memberTermEntity);
            }
        }
    }
    
    
    /**
     * 构建租车套餐会员期限信息
     *
     * @param tenantId          租户ID
     * @param uid               用户ID
     * @param packageEntity     租车套餐信息
     * @param depositPayOrderNo 押金缴纳订单编码
     * @param memberTermEntity  DB层的会员期限数据
     * @return 将要新增或修改的租车会员期限信息
     */
    public CarRentalPackageMemberTermPo buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePo packageEntity, String depositPayOrderNo,
            CarRentalPackageMemberTermPo memberTermEntity) {
        CarRentalPackageMemberTermPo carRentalPackageMemberTermEntity = new CarRentalPackageMemberTermPo();
        carRentalPackageMemberTermEntity.setUid(uid);
        carRentalPackageMemberTermEntity.setRentalPackageType(packageEntity.getType());
        carRentalPackageMemberTermEntity.setRentalPackageConfine(packageEntity.getConfine());
        carRentalPackageMemberTermEntity.setStatus(MemberTermStatusEnum.PENDING_EFFECTIVE.getCode());
        carRentalPackageMemberTermEntity.setDeposit(packageEntity.getDeposit());
        carRentalPackageMemberTermEntity.setRentalPackageDeposit(packageEntity.getDeposit());
        carRentalPackageMemberTermEntity.setDepositPayOrderNo(depositPayOrderNo);
        carRentalPackageMemberTermEntity.setTenantId(tenantId);
        carRentalPackageMemberTermEntity.setFranchiseeId(packageEntity.getFranchiseeId());
        carRentalPackageMemberTermEntity.setStoreId(packageEntity.getStoreId());
        carRentalPackageMemberTermEntity.setCreateUid(uid);
        carRentalPackageMemberTermEntity.setUpdateUid(uid);
        carRentalPackageMemberTermEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setDelFlag(DelFlagEnum.OK.getCode());
        if (ObjectUtils.isNotEmpty(memberTermEntity)) {
            carRentalPackageMemberTermEntity.setId(memberTermEntity.getId());
        }
        return carRentalPackageMemberTermEntity;
    }
    
    /**
     * 生成免押记录订单
     *
     * @param tenantId                         租户ID
     * @param uid                              用户ID
     * @param carRentalPackageDepositPayInsert 租车套餐押金订单
     * @param freeDepositOptReq                免押申请数据
     * @return 免押记录订单
     */
    private FreeDepositOrder buildFreeDepositOrderEntity(Integer tenantId, Long uid, CarRentalPackageDepositPayPo carRentalPackageDepositPayInsert,
            FreeDepositOptReq freeDepositOptReq, CarRentalPackagePo carRentalPackagePo) {
        Integer depositType = FreeDepositOrder.DEPOSIT_TYPE_CAR;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackageDepositPayInsert.getRentalPackageType())) {
            depositType = FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY;
        }
        return FreeDepositOrder.builder().uid(uid).authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE).idCard(freeDepositOptReq.getIdCard())
                .orderId(carRentalPackageDepositPayInsert.getOrderNo()).phone(freeDepositOptReq.getPhoneNumber()).realName(freeDepositOptReq.getRealName())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).payStatus(FreeDepositOrder.PAY_STATUS_INIT).tenantId(tenantId)
                .transAmt(carRentalPackageDepositPayInsert.getDeposit().doubleValue()).type(FreeDepositOrder.TYPE_ZHIFUBAO).depositType(depositType)
                .franchiseeId(Long.valueOf(carRentalPackagePo.getFranchiseeId())).build();
    }
    
    /**
     * 生成押金缴纳订单
     *
     * @param tenantId         租户ID
     * @param uid              用户ID
     * @param carRentalPackage 租车套餐信息
     * @return 租车套餐押金订单
     */
    private CarRentalPackageDepositPayPo buildCarRentalPackageDepositPayEntity(Integer tenantId, Long uid, CarRentalPackagePo carRentalPackage, Integer freeDeposit,
            Integer payType) {
        CarRentalPackageDepositPayPo carRentalPackageDepositPay = new CarRentalPackageDepositPayPo();
        carRentalPackageDepositPay.setUid(uid);
        carRentalPackageDepositPay.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, uid));
        carRentalPackageDepositPay.setRentalPackageId(carRentalPackage.getId());
        carRentalPackageDepositPay.setRentalPackageType(carRentalPackage.getType());
        carRentalPackageDepositPay.setType(DepositTypeEnum.NORMAL.getCode());
        carRentalPackageDepositPay.setDeposit(carRentalPackage.getDeposit());
        carRentalPackageDepositPay.setFreeDeposit(freeDeposit);
        carRentalPackageDepositPay.setPayType(payType);
        carRentalPackageDepositPay.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageDepositPay.setTenantId(tenantId);
        carRentalPackageDepositPay.setFranchiseeId(carRentalPackage.getFranchiseeId());
        carRentalPackageDepositPay.setStoreId(carRentalPackage.getStoreId());
        carRentalPackageDepositPay.setCreateUid(uid);
        carRentalPackageDepositPay.setRentalPackageDeposit(carRentalPackage.getDeposit());
        return carRentalPackageDepositPay;
    }
    
    
    
    /**
     * C端退押申请
     *
     * @param tenantId          租户ID
     * @param uid               用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @return
     */
    @Override
    public boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, depositPayOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        if (Objects.isNull(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 获取加锁 KEY
        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_REFUND_DEPOSIT_ORDER_UID_KEY, uid);
        
        // 加锁
        if (!redisService.setNx(buyLockKey, uid.toString(), 5 * 1000L, false)) {
            throw new BizException("ELECTRICITY.0034", "操作频繁");
        }
        
        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }
        
        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.warn("CarRenalPackageDepositBizService.refundDeposit failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}", uid,
                    depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }
        
        Integer payType = depositPayEntity.getPayType();
        if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
            log.warn("CarRenalPackageDepositBizService.refundDeposit failed. t_car_rental_package_deposit_pay pay type is {}, depositPayOrderNo is {}", payType, depositPayOrderNo);
            throw new BizException("300045", "请前往门店退押金");
        }
        
        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);
        
        // 判定是否退押审核
        boolean depositAuditFlag = true;
        if (BigDecimal.ZERO.compareTo(memberTermEntity.getDeposit()) == 0) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            Integer zeroDepositAuditEnabled = electricityConfig.getIsZeroDepositAuditEnabled();
            depositAuditFlag = ElectricityConfig.ENABLE_ZERO_DEPOSIT_AUDIT.equals(zeroDepositAuditEnabled);
        }
        
        // 生成退押申请单
        CarRentalPackageDepositRefundPo refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo, SystemDefinitionEnum.WX_APPLET,
                depositAuditFlag, payType, null, uid, null,depositPayEntity.getPaymentChannel());
        
        // 待审核
        if (RefundStateEnum.PENDING_APPROVAL.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);
            // 发送站内信
            siteMessagePublish.publish(SiteMessageEvent.builder(this).tenantId(TenantContextHolder.getTenantId().longValue()).code(SiteMessageType.CAR_RENTAL_REFUND)
                    .notifyTime(System.currentTimeMillis()).addContext("name", userInfo.getName()).addContext("phone", userInfo.getPhone())
                    .addContext("orderNo", refundDepositInsertEntity.getOrderNo()).addContext("amount", refundDepositInsertEntity.getApplyAmount().toString()).build());
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
        }
        
        return true;
    }
    
    /**
     * 退押申请事务处理
     *
     * @param refundDepositInsertEntity 退押申请单数据
     * @param memberTermEntity          会员期限实体数据
     * @param optId                     操作人ID
     * @param delFlag                   删除标识
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRefundDepositInfoTx(CarRentalPackageDepositRefundPo refundDepositInsertEntity, CarRentalPackageMemberTermPo memberTermEntity, Long optId, boolean delFlag) {
        carRentalPackageDepositRefundService.insert(refundDepositInsertEntity);
        if (!delFlag) {
            // 处理状态
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode(), optId);
        } else {
            // 作废所有的套餐购买订单（未使用、使用中）
            carRentalPackageOrderService.refundDepositByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid(), optId);
            // 查询用户保险
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 按照人+类型，作废用户保险
            insuranceUserInfoService.deleteByUidAndType(memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 作废保险订单
            if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
            }
            // 删除会员期限表信息
            carRentalPackageMemberTermService.delByUidAndTenantId(memberTermEntity.getTenantId(), memberTermEntity.getUid(), optId);
            // 清理user信息/解绑车辆/解绑电池
            userBizService.depositRefundUnbind(memberTermEntity.getTenantId(), memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 车电一体押金，同步删除电池那边的数据
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
                log.info("saveRefundDepositInfoTx, delete from battery member info. depositPayOrderNo is {}", memberTermEntity.getDepositPayOrderNo());
                userBatteryTypeService.deleteByUid(memberTermEntity.getUid());
                userBatteryDepositService.deleteByUid(memberTermEntity.getUid());
            }
            
            // 删除用户分组
            userInfoGroupDetailService.handleAfterRefundDeposit(memberTermEntity.getUid());
        }
    }
    
    /**
     * 构建退押申请单数据
     *
     * @param memberTermEntity  会员期限信息
     * @param depositPayOrderNo 押金缴纳订单编码
     * @param systemDefinition  操作系统
     * @param depositAuditFlag  退押审批标识
     * @param payType           支付方式
     * @param refundAmount      实际退款金额(可为空，后端操作不能为空)
     * @param optUid            操作用户UID
     * @param compelOffLine     是否强制线下退款
     * @return
     */
    private CarRentalPackageDepositRefundPo budidCarRentalPackageOrderRentRefund(CarRentalPackageMemberTermPo memberTermEntity, String depositPayOrderNo,
            SystemDefinitionEnum systemDefinition, boolean depositAuditFlag, Integer payType, BigDecimal refundAmount, Long optUid, Integer compelOffLine,String paymentChannel) {
        CarRentalPackageDepositRefundPo refundDepositInsertEntity = new CarRentalPackageDepositRefundPo();
        refundDepositInsertEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT_REFUND, memberTermEntity.getUid()));
        refundDepositInsertEntity.setUid(memberTermEntity.getUid());
        refundDepositInsertEntity.setDepositPayOrderNo(depositPayOrderNo);
        refundDepositInsertEntity.setApplyAmount(memberTermEntity.getDeposit());
        refundDepositInsertEntity.setTenantId(memberTermEntity.getTenantId());
        refundDepositInsertEntity.setFranchiseeId(memberTermEntity.getFranchiseeId());
        refundDepositInsertEntity.setStoreId(memberTermEntity.getStoreId());
        refundDepositInsertEntity.setCreateUid(memberTermEntity.getUid());
        refundDepositInsertEntity.setPayType(payType);
        refundDepositInsertEntity.setRentalPackageType(memberTermEntity.getRentalPackageType());
        // 默认状态，待审核
        refundDepositInsertEntity.setRefundState(RefundStateEnum.PENDING_APPROVAL.getCode());
        refundDepositInsertEntity.setPaymentChannel(paymentChannel);
        // 设置退款状态
        if (SystemDefinitionEnum.BACKGROUND.getCode().equals(systemDefinition.getCode())) {
            refundDepositInsertEntity.setRealAmount(refundAmount);
            refundDepositInsertEntity.setCreateUid(optUid);
            
            // 若为强制线下退款，则默认退款成功，并未将线上的退款方式，更改为线下，免押不影响，非强制线下退款不影响
            if (ObjectUtils.isNotEmpty(compelOffLine) && YesNoEnum.YES.getCode().equals(compelOffLine)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                    refundDepositInsertEntity.setPayType(PayTypeEnum.OFF_LINE.getCode());
                    refundDepositInsertEntity.setCompelOffLine(YesNoEnum.YES.getCode());
                }
            } else {
                // 0元退
                if (BigDecimal.ZERO.compareTo(refundAmount) == 0 && !PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    // 线上、线下，退款成功
                    if (PayTypeEnum.ON_LINE.getCode().equals(payType) || PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                        refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                    }
                } else {
                    // 线下，退款成功
                    if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                        refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                    }
                    // 线上、免押，退款中
                    if (PayTypeEnum.ON_LINE.getCode().equals(payType) || PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                        refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
                    }
                }
            }
        }
        
        if (SystemDefinitionEnum.WX_APPLET.getCode().equals(systemDefinition.getCode())) {
            // 不需要审核，必定是0元退押，所以直接退款成功即可
            if (!depositAuditFlag) {
                // 线上、线下 退款成功
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                refundDepositInsertEntity.setRealAmount(BigDecimal.ZERO);
                // 免押，退款中
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
                }
            }
        }
        
        return refundDepositInsertEntity;
    }
    
    
    /**
     * 调用退款
     *
     * @param refundOrder
     * @return
     * @throws PayException
     */
    private BasePayOrderRefundDTO refund(RefundOrder refundOrder) throws PayException {
        
        //第三方订单号
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
            log.warn("CarRenalPackageDepositBizService.wxRefund failed, not found t_electricity_trade_order. orderId is {}", refundOrder.getOrderId());
            throw new BizException("300000", "数据有误");
        }
        BasePayConfig config = null;
        try {
            config = payConfigBizService
                    .queryPrecisePayParams(electricityTradeOrder.getPaymentChannel(), electricityTradeOrder.getTenantId(), electricityTradeOrder.getPayFranchiseeId(),null);
            if (Objects.isNull(config)) {
                throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
            }
        } catch (Exception e) {
            // 缓存问题，事务在管理其中没有提交，但是缓存已经存在，所以需要删除一次缓存
            carRentalPackageMemberTermService.deleteCache(electricityTradeOrder.getTenantId(), electricityTradeOrder.getUid());
            throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        
        OrderRefundParamConverterModel model = new OrderRefundParamConverterModel();
        model.setRefundId(refundOrder.getRefundOrderNo());
        model.setOrderId(electricityTradeOrder.getTradeOrderNo());
        model.setReason("押金退款");
        model.setRefund(refundOrder.getRefundAmount());
        model.setTotal(electricityTradeOrder.getTotalFee().intValue());
        model.setCurrency("CNY");
        model.setPayConfig(config);
        model.setTenantId(electricityTradeOrder.getTenantId());
        model.setFranchiseeId(electricityTradeOrder.getPayFranchiseeId());
        model.setRefundType(RefundPayOptTypeEnum.CAR_DEPOSIT_REFUND_CALL_BACK.getCode());
        BasePayRequest basePayRequest = payConfigConverter.converterOrderRefund(model);
        
        return payServiceDispatcher.refund(basePayRequest);
    }
    
    /**
     * 调用微信支付
     *
     * @param refundOrder
     * @return
     * @throws WechatPayException
     */
    private WechatJsapiRefundResultDTO wxRefund(RefundOrder refundOrder) throws WechatPayException {
        //第三方订单号
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
            log.warn("CarRenalPackageDepositBizService.wxRefund failed, not found t_electricity_trade_order. orderId is {}", refundOrder.getOrderId());
            throw new BizException("300000", "数据有误");
        }
        
        //调用退款
        WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
        wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setNotifyUrl(wechatConfig.getCarDepositRefundCallBackUrl() + electricityTradeOrder.getTenantId());
        
        WechatV3RefundRequest wechatV3RefundRequest = new WechatV3RefundRequest();
        wechatV3RefundRequest.setRefundId(refundOrder.getRefundOrderNo());
        wechatV3RefundRequest.setOrderId(electricityTradeOrder.getTradeOrderNo());
        wechatV3RefundRequest.setReason("押金退款");
        wechatV3RefundRequest.setNotifyUrl(wechatConfig.getCarDepositRefundCallBackUrl() + electricityTradeOrder.getTenantId() + "/" + electricityTradeOrder.getPayFranchiseeId());
        wechatV3RefundRequest.setRefund(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundRequest.setTotal(electricityTradeOrder.getTotalFee().intValue());
        wechatV3RefundRequest.setCurrency("CNY");
        
        // 调用支付配置参数
        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(electricityTradeOrder.getTenantId(),
                    electricityTradeOrder.getPayFranchiseeId());
        } catch (Exception e) {
            // 缓存问题，事务在管理其中没有提交，但是缓存已经存在，所以需要删除一次缓存
            carRentalPackageMemberTermService.deleteCache(electricityTradeOrder.getTenantId(), electricityTradeOrder.getUid());
            throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        
        wechatV3RefundRequest.setCommonRequest(ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails));
        
        return wechatV3JsapiInvokeService.refund(wechatV3RefundRequest);
    }
    
    
    /**
     * 退押检测
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     */
    private void checkRefundDeposit(Integer tenantId, Long uid, Integer rentalPackageType, String depositPayOrderNo) {
        
        // 是否存在正常的退押申请单
        CarRentalPackageDepositRefundPo depositRefundPo = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(depositPayOrderNo);
        if (ObjectUtils.isNotEmpty(depositRefundPo) && !RefundStateEnum.getRefundStateList().contains(depositRefundPo.getRefundState())) {
            throw new BizException("100031", "不能重复退押金");
        }
        
        // 检测是否存在滞纳金
        if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }
        
        // 查询设备(车辆)
        ElectricityCar userCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(userCar) && StringUtils.isNotBlank(userCar.getSn())) {
            throw new BizException("300041", "需先退还资产再退押金");
        }
        
        // 车电一体，查询设备(电池)
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                throw new BizException("300041", "需先退还资产再退押金");
            }
        }
    }
    
    
    /**
     * 运营商端创建退押，特殊退押(2.0过度数据)
     *
     * @param optModel 操作数据模型
     * @param optUid   操作用户UID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundDepositCreateSpecial(CarRentalPackageDepositRefundOptModel optModel, Long optUid) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getTenantId(), optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer tenantId = optModel.getTenantId();
        Long uid = optModel.getUid();
        String depositPayOrderNo = optModel.getDepositPayOrderNo();
        BigDecimal realAmount = optModel.getRealAmount();
        
        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        if (Objects.isNull(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)/* || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())*/) {
            log.warn("CarRenalPackageDepositBizService.refundDepositCreateSpecial failed. t_car_rental_package_member_term not found. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }
        
        // 检测是否存在滞纳金
        if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }
        
        // 查询设备(车辆)
        ElectricityCar userCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(userCar) && StringUtils.isNotBlank(userCar.getSn())) {
            throw new BizException("300041", "需先退还资产再退押金");
        }
        
        // 车电一体，查询设备(电池)
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                throw new BizException("300041", "需先退还资产再退押金");
            }
        }
        
        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState()) || 0L != depositPayEntity.getCreateUid()) {
            log.warn(
                    "CarRenalPackageDepositBizService.refundDepositCreateSpecial failed. t_car_rental_package_deposit_pay not found or status or createUserId is error. uid is {}, depositPayOrderNo is {}",
                    uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }
        
        // 退押检测
        // checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);
        
        // 否则提前申请
        CarRentalPackageDepositRefundPo carRentalPackageDepositRefundPo = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(depositPayOrderNo);
        if (ObjectUtils.isNotEmpty(carRentalPackageDepositRefundPo) && RefundStateEnum.PENDING_APPROVAL.getCode().equals(carRentalPackageDepositRefundPo.getRefundState())) {
            carRentalPackageDepositRefundService.delById(carRentalPackageDepositRefundPo.getId());
        }
        
        if (ObjectUtils.isNotEmpty(carRentalPackageDepositRefundPo) && (RefundStateEnum.REFUNDING.getCode().equals(carRentalPackageDepositRefundPo.getRefundState())
                || RefundStateEnum.SUCCESS.getCode().equals(carRentalPackageDepositRefundPo.getRefundState()))) {
            log.warn("CarRenalPackageDepositBizService.refundDepositCreateSpecial failed. t_car_rental_package_deposit_refund status is error. uid is {}, depositPayOrderNo is {}",
                    uid, depositPayOrderNo);
            throw new BizException("300000", "请勿重复操作此订单");
        }
        
        // 默认线下
        Integer payType = PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType()) ? PayTypeEnum.EXEMPT.getCode() : PayTypeEnum.OFF_LINE.getCode();
        
        // 生成退押申请单
        CarRentalPackageDepositRefundPo refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo, SystemDefinitionEnum.BACKGROUND,
                false, payType, realAmount, optUid, optModel.getCompelOffLine(),depositPayEntity.getPaymentChannel());
        
        // 退款中（只能是免押）
        if (RefundStateEnum.REFUNDING.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 退款中，先落库
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);
            
            // 免押
            if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                String freeDepositOrderNo = depositPayEntity.getOrderNo();
                FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                Integer payStatus = freeDepositAlipayHistoryService.queryPayingByOrderId(freeDepositOrderNo);
                if (ObjectUtils.isEmpty(freeDepositOrder)) {
                    log.warn("refundDepositCreateSpecial failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                    throw new BizException("300000", "数据有误");
                }
                
                if (Objects.nonNull(freeDepositOrder.getPayStatus()) && FreeDepositOrder.PAY_STATUS_DEALING.equals(payStatus)){
                    throw new BizException("300000", "当前有正在执行中的免押代扣，无法退押");
                }
                if (BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).compareTo(realAmount) < 0){
                    throw new BizException("300000", "退款金额不能大于剩余可代扣金额");
                }
                Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(
                        UnFreeDepositOrderQuery.builder().uid(freeDepositOrder.getUid()).tenantId(depositPayEntity.getTenantId()).subject("租车套餐免押解冻")
                                .orderId(freeDepositOrderNo).channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).amount(realAmount.toString()).build());
                if (!triple.getLeft()){
                    throw new BizException(triple.getMiddle(), String.valueOf(triple.getRight()));
                }
                
                FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
                freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                freeDepositOrderService.update(freeDepositOrderUpdate);
            }
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
        }
        return true;
    }
    
    /**
     * 运营商端创建退押
     *
     * @param optModel 租户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundDepositCreate(CarRentalPackageDepositRefundOptModel optModel, Long optUid) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getTenantId(), optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        Integer tenantId = optModel.getTenantId();
        Long uid = optModel.getUid();
        String depositPayOrderNo = optModel.getDepositPayOrderNo();
        BigDecimal realAmount = optModel.getRealAmount();
        
        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        
        if (Objects.isNull(userInfo)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }
        
        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.warn("CarRenalPackageDepositBizService.refundDepositCreate failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}",
                    uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }
        
        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);
        
        Integer payType = depositPayEntity.getPayType();
        
        // 生成退押申请单
        CarRentalPackageDepositRefundPo refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo, SystemDefinitionEnum.BACKGROUND,
                false, payType, realAmount, optUid, optModel.getCompelOffLine(),depositPayEntity.getPaymentChannel());
        
        // 待审核
        if (RefundStateEnum.REFUNDING.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 实际退款0元，则直接成功，不调用退款接口
            if (BigDecimal.ZERO.compareTo(realAmount) == 0 && !PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                // 退款中，先落库
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            } else {
                // 退款中，先落库，在调用退款接口
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);
    
                // 线上，调用微信
                if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                    try {
                        // 根据购买订单编码获取当初的支付流水
                        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(depositPayEntity.getRentalPackageOrderNo());
                        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
                            log.warn("refundDepositCreate faild. not find t_electricity_trade_order. orderNo is {}", depositPayEntity.getRentalPackageOrderNo());
                            throw new BizException("300000", "数据有误");
                        }
                        Integer status = electricityTradeOrder.getStatus();
                        if (ElectricityTradeOrder.STATUS_INIT.equals(status) || ElectricityTradeOrder.STATUS_FAIL.equals(status)) {
                            log.warn("refundDepositCreate faild. t_electricity_trade_order status is wrong. orderNo is {}", depositPayEntity.getRentalPackageOrderNo());
                            throw new BizException("300000", "数据有误");
                        }
    
                        // 调用微信支付，进行退款
                        RefundOrder refundOrder = RefundOrder.builder().orderId(electricityTradeOrder.getOrderNo()).payAmount(electricityTradeOrder.getTotalFee())
                                .refundOrderNo(refundDepositInsertEntity.getOrderNo()).refundAmount(refundDepositInsertEntity.getRealAmount()).build();
                        log.info("refundDepositCreate, Call WeChat refund. params is {}", JsonUtil.toJson(refundOrder));
                        BasePayOrderRefundDTO wxRefundDto = refund(refundOrder);
                        log.info("refundDepositCreate, Call WeChat refund. result is {}", JsonUtil.toJson(wxRefundDto));
    
                    } catch (PayException e) {
                        log.error("refundDepositCreate failed.", e);
                        
                        // 缓存问题，事务在管理其中没有提交，但是缓存已经存在，所以需要删除一次缓存
                        carRentalPackageMemberTermService.deleteCache(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                        throw new BizException("PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
                    }
                }
                
                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    Integer payStatus = freeDepositAlipayHistoryService.queryPayingByOrderId(freeDepositOrderNo);
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.warn("refundDepositCreate failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }
                    if (Objects.nonNull(freeDepositOrder.getPayStatus()) && FreeDepositOrder.PAY_STATUS_DEALING.equals(payStatus)){
                        throw new BizException("300000", "当前有正在执行中的免押代扣，无法退押");
                    }
                    if (BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).compareTo(realAmount) < 0){
                        throw new BizException("300000", "退款金额不能大于剩余可代扣金额");
                    }
                    Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(
                            UnFreeDepositOrderQuery.builder().uid(freeDepositOrder.getUid()).tenantId(depositPayEntity.getTenantId()).subject("租车套餐免押解冻")
                                    .orderId(freeDepositOrderNo).channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).amount(realAmount.toString()).build());
                    if (!triple.getLeft()){
                        throw new BizException(triple.getMiddle(), String.valueOf(triple.getRight()));
                    }
                    
                    FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                    freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                    freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
                    freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    freeDepositOrderService.update(freeDepositOrderUpdate);
                }
            }
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
        }
        
        return true;
    }
    
    
    
    /**
     * 审批退还押金申请单
     *
     * @param refundDepositOrderNo 退押申请单
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @param compelOffLine        强制线下退款
     * @return
     */
    @Override
    public boolean approveRefundDepositOrder(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, BigDecimal refundAmount,
            Integer compelOffLine) {
        if (!ObjectUtils.allNotNull(refundDepositOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 退押订单
        CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(refundDepositOrderNo);
        if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.PENDING_APPROVAL.getCode().equals(depositRefundEntity.getRefundState())) {
            log.warn("approveRefundDepositOrder faild. not find car_rental_package_deposit_refund or status error. refundDepositOrderNo is {}", refundDepositOrderNo);
            throw new BizException("300000", "数据有误");
        }
        
        Integer payType = depositRefundEntity.getPayType();
        if ((PayTypeEnum.ON_LINE.getCode().equals(payType) || PayTypeEnum.OFF_LINE.getCode().equals(payType)) && approveFlag && ObjectUtils.isEmpty(refundAmount)) {
            throw new BizException("300029", "退押金额不能为空");
        }
        
        // 租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode().equals(memberTermEntity.getStatus())) {
            log.warn("approveRefundRentOrder faild. not find t_car_rental_package_member_term or status error. uid is {}", depositRefundEntity.getUid());
            throw new BizException("300000", "数据有误");
        }
        
        // 押金缴纳编码
        String orderNo = memberTermEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.warn("approveRefundRentOrder faild. not find t_car_rental_package_deposit_pay or payState error. orderNo is {}", orderNo);
            throw new BizException("300000", "数据有误");
        }
        
        // TX 事务落库
        saveApproveRefundDepositOrderTx(refundDepositOrderNo, approveFlag, apploveDesc, apploveUid, depositRefundEntity, refundAmount, depositPayEntity, compelOffLine);
        
        return true;
    }
    
    /**
     * 退押审批，TX事务处理
     *
     * @param refundDepositOrderNo 退押申请订单号
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @param depositRefundEntity  退押申请单信息
     * @param depositPayEntity     押金缴纳信息
     * @param compelOffLine        强制线下退押
     */
    public void saveApproveRefundDepositOrderTx(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid,
            CarRentalPackageDepositRefundPo depositRefundEntity, BigDecimal refundAmount, CarRentalPackageDepositPayPo depositPayEntity, Integer compelOffLine) {
        
        CarRentalPackageDepositRefundPo depositRefundUpdateEntity = new CarRentalPackageDepositRefundPo();
        depositRefundUpdateEntity.setOrderNo(refundDepositOrderNo);
        depositRefundUpdateEntity.setAuditTime(System.currentTimeMillis());
        depositRefundUpdateEntity.setRemark(apploveDesc);
        depositRefundUpdateEntity.setUpdateUid(apploveUid);
        depositRefundUpdateEntity.setRealAmount(refundAmount);
        
        // 审核通过
        if (approveFlag) {
            // 交易方式
            Integer payType = depositRefundEntity.getPayType();
            
            // 强制线下退款
            if (ObjectUtils.isNotEmpty(compelOffLine) && YesNoEnum.YES.getCode().equals(compelOffLine) && PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                payType = PayTypeEnum.OFF_LINE.getCode();
                depositRefundUpdateEntity.setPayType(payType);
                depositRefundUpdateEntity.setCompelOffLine(compelOffLine);
            }
            Integer payStatus = freeDepositAlipayHistoryService.queryPayingByOrderId(depositPayEntity.getOrderNo());
            // 非零元退押
            if (BigDecimal.ZERO.compareTo(refundAmount) < 0) {
                // 赋值退款单状态：审核通过
                depositRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_PASS.getCode());
                
                // 线下
                if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                    // 赋值退款单状态：退款成功
                    depositRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                    // 作废所有的套餐购买订单（未使用、使用中）、
                    carRentalPackageOrderService.refundDepositByUid(depositPayEntity.getTenantId(), depositPayEntity.getUid(), apploveUid);
                    // 查询用户保险
                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                    // 按照人+类型，作废保险
                    insuranceUserInfoService.deleteByUidAndType(depositPayEntity.getUid(), depositRefundEntity.getRentalPackageType());
                    // 作废保险订单
                    if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                        insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
                    }
                    // 删除会员期限表信息
                    carRentalPackageMemberTermService.delByUidAndTenantId(depositPayEntity.getTenantId(), depositPayEntity.getUid(), apploveUid);
                    // 清理user信息/解绑车辆/解绑电池
                    userBizService.depositRefundUnbind(depositPayEntity.getTenantId(), depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                    // 车电一体押金，同步删除电池那边的数据
                    if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(depositPayEntity.getRentalPackageType())) {
                        log.info("saveApproveRefundDepositOrderTx, delete from battery member info. depositPayOrderNo is {}", depositRefundEntity.getOrderNo());
                        userBatteryTypeService.deleteByUid(depositPayEntity.getUid());
                        userBatteryDepositService.deleteByUid(depositPayEntity.getUid());
                    }
                    
                    // 删除用户分组
                    userInfoGroupDetailService.handleAfterRefundDeposit(depositPayEntity.getUid());
                }
                
                // 线上，调用微信退款
                if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                    // 线上退款
                    this.onLineRefund(depositRefundEntity, depositRefundUpdateEntity, depositPayEntity, refundDepositOrderNo, refundAmount);
                    // 已经提前处理，无需继续执行
                    return;
                }
                
                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    // 赋值退款单状态：退款中
                    depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
                    
                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.warn("saveApproveRefundDepositOrderTx failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }
                    if (Objects.nonNull(freeDepositOrder.getPayStatus()) && FreeDepositOrder.PAY_STATUS_DEALING.equals(payStatus)){
                        throw new BizException("300000", "当前有正在执行中的免押代扣，无法退押");
                    }
                    if (BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).compareTo(refundAmount) < 0){
                        throw new BizException("300000", "退款金额不能大于剩余可代扣金额");
                    }
                    Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(
                            UnFreeDepositOrderQuery.builder().uid(freeDepositOrder.getUid()).tenantId(depositPayEntity.getTenantId()).subject("租车套餐免押解冻")
                                    .orderId(freeDepositOrderNo).channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).amount(refundAmount.toString()).build());
                    if (!triple.getLeft()){
                        throw new BizException(triple.getMiddle(), String.valueOf(triple.getRight()));
                    }
                    
                    FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                    freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                    freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
                    freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    freeDepositOrderService.update(freeDepositOrderUpdate);
                }
                
            } else {
                // 零元退押
                depositRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                
                // 线下、线上
                if (PayTypeEnum.ON_LINE.getCode().equals(payType) || PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                    // 作废所有的套餐购买订单（未使用、使用中）、
                    carRentalPackageOrderService.refundDepositByUid(depositPayEntity.getTenantId(), depositPayEntity.getUid(), null);
                    // 查询用户保险
                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                    // 按照人+类型，作废保险
                    insuranceUserInfoService.deleteByUidAndType(depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                    // 作废保险订单
                    if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                        insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
                    }
                    // 删除会员期限表信息
                    carRentalPackageMemberTermService.delByUidAndTenantId(depositPayEntity.getTenantId(), depositPayEntity.getUid(), null);
                    // 清理user信息/解绑车辆/解绑电池
                    userBizService.depositRefundUnbind(depositPayEntity.getTenantId(), depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                    // 车电一体押金，同步删除电池那边的数据
                    if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(depositPayEntity.getRentalPackageType())) {
                        log.info("saveApproveRefundDepositOrderTx, delete from battery member info. depositPayOrderNo is {}", depositRefundEntity.getOrderNo());
                        userBatteryTypeService.deleteByUid(depositPayEntity.getUid());
                        userBatteryDepositService.deleteByUid(depositPayEntity.getUid());
                    }
                    
                    // 删除用户分组
                    userInfoGroupDetailService.handleAfterRefundDeposit(depositPayEntity.getUid());
                }
                
                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    // 赋值退款单状态：退款中
                    depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
                    
                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.warn("saveApproveRefundDepositOrderTx failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }
                    if (Objects.nonNull(freeDepositOrder.getPayStatus()) && FreeDepositOrder.PAY_STATUS_DEALING.equals(payStatus)){
                        throw new BizException("300000", "当前有正在执行中的免押代扣，无法退押");
                    }
                    if (BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).compareTo(refundAmount) < 0){
                        throw new BizException("300000", "退款金额不能大于剩余可代扣金额");
                    }
                    Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(
                            UnFreeDepositOrderQuery.builder().uid(freeDepositOrder.getUid()).tenantId(depositPayEntity.getTenantId()).subject("租车套餐免押解冻")
                                    .orderId(freeDepositOrderNo).channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).amount(refundAmount.toString()).build());
                    if (!triple.getLeft()){
                        throw new BizException(triple.getMiddle(), String.valueOf(triple.getRight()));
                    }
                    
                    FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                    freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                    freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
                    freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    freeDepositOrderService.update(freeDepositOrderUpdate);
                }
            }
            
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);
        } else {
            
            // 1. 更新退押申请单状态
            depositRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_REJECT.getCode());
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);
            
            // 2. 更新会员期限
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(),
                    apploveUid);
        }
    }
    
    /**
     * 线上退款单处理
     *
     * @param depositPayEntity
     * @author caobotao.cbt
     * @date 2024/8/14 18:32
     */
    private void onLineRefund(CarRentalPackageDepositRefundPo depositRefundEntity, CarRentalPackageDepositRefundPo depositRefundUpdateEntity,
            CarRentalPackageDepositPayPo depositPayEntity, String refundDepositOrderNo, BigDecimal refundAmount) {
        
        String rentalPackageOrderNo = depositPayEntity.getRentalPackageOrderNo();
        if (StringUtils.isBlank(rentalPackageOrderNo)) {
            log.warn("onLineRefund fail. not find t_electricity_trade_order. orderNo is {}", rentalPackageOrderNo);
            throw new BizException("300000", "数据有误");
        }
        try {
            // 根据购买订单编码获取当初的支付流水
            ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(rentalPackageOrderNo);
            if (ObjectUtils.isEmpty(electricityTradeOrder)) {
                log.warn("saveApproveRefundDepositOrderTx faild. not find t_electricity_trade_order. orderNo is {}", rentalPackageOrderNo);
                throw new BizException("300000", "数据有误");
            }
            Integer status = electricityTradeOrder.getStatus();
            if (ElectricityTradeOrder.STATUS_INIT.equals(status) || ElectricityTradeOrder.STATUS_FAIL.equals(status)) {
                log.warn("saveApproveRefundDepositOrderTx faild. t_electricity_trade_order status is wrong. orderNo is {}", rentalPackageOrderNo);
                throw new BizException("300000", "数据有误");
            }
            
            // 调用微信支付，进行退款
            RefundOrder refundOrder = RefundOrder.builder().orderId(electricityTradeOrder.getOrderNo()).payAmount(electricityTradeOrder.getTotalFee())
                    .refundOrderNo(refundDepositOrderNo).refundAmount(refundAmount).build();
            log.info("saveApproveRefundDepositOrderTx, Call WeChat refund. params is {}", JsonUtil.toJson(refundOrder));
            BasePayOrderRefundDTO wxRefundDto = refund(refundOrder);
            log.info("saveApproveRefundDepositOrderTx, Call WeChat refund. result is {}", JsonUtil.toJson(wxRefundDto));
    
            // 赋值退款单状态：退款中
            depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
            //            depositRefundUpdateEntity.setPaymentChannel(depositPayEntity.getPaymentChannel());
            //此处新开事物提前提交，解决异步通知订单状态更新先后顺序问题
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);
            
        } catch (PayException e) {
            log.error("saveApproveRefundDepositOrderTx failed.", e);
            // 缓存问题，事务在管理其中没有提交，但是缓存已经存在，所以需要删除一次缓存
            carRentalPackageMemberTermService.deleteCache(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());
            throw new BizException("PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
        }
        
    }
    
    
    /**
     * 用户名下的押金信息(单车、车电一体)
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 押金缴纳信息
     */
    @Override
    public CarRentalPackageDepositPayVo selectUnRefundCarDeposit(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.warn("selectUnRefundCarDeposit, not found car_rental_package_member_term, tenantId is {}, uid is {}", tenantId, uid);
            return null;
        }
        
        Integer status = 0;
        String rejectReason = StringUtils.EMPTY;
        if (MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode().equals(memberTermEntity.getStatus())) {
            status = 1;
            // 申请退押，查询退押订单信息
            CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(memberTermEntity.getDepositPayOrderNo());
            if (ObjectUtils.isEmpty(depositRefundEntity)) {
                log.warn("selectUnRefundCarDeposit, not found t_car_rental_package_order_rent_refund, tenantId is {}, uid is {}", tenantId, uid);
                throw new BizException("300000", "数据有误");
            }
            if (RefundStateEnum.REFUNDING.getCode().equals(depositRefundEntity.getRefundState())) {
                status = 2;
            }
            
        }
        
        // 押金缴纳信息
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(memberTermEntity.getDepositPayOrderNo());
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            return null;
        }
        
        // 免押、未支付  调用第三方，二次查询
        if (PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType()) && PayStateEnum.UNPAID.getCode().equals(depositPayEntity.getPayState())) {
//            // 调用第三方查询
//            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
//            if (ObjectUtils.isEmpty(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
//                log.info("selectUnRefundCarDeposit, pxzConfig configuration error. tenantId is {}", tenantId);
//                return null;
//            }
//
//            String orderNo = depositPayEntity.getOrderNo();
//
//            PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
//            query.setAesSecret(pxzConfig.getAesKey());
//            query.setDateTime(System.currentTimeMillis());
//            query.setSessionId(orderNo);
//            query.setMerchantCode(pxzConfig.getMerchantCode());
//
//            PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
//            request.setTransId(orderNo);
//            query.setData(request);
//
//            PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
//            try {
//                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder params is {}", JsonUtil.toJson(query));
//                pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
//            } catch (PxzFreeDepositException e) {
//                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder error. ", e);
//                return null;
//            }
//            log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder result is {}", JsonUtil.toJson(pxzQueryOrderRsp));
//
//            if (ObjectUtils.isEmpty(pxzQueryOrderRsp) || !pxzQueryOrderRsp.isSuccess() || ObjectUtils.isEmpty(pxzQueryOrderRsp.getData())) {
//                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder failed. orderNo is {}", orderNo);
//                return null;
//            }
//
//            // 未冻结
//            PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
//            if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
//                log.warn("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder is not auth_frozen. orderNo is {}, uid is {}", orderNo, uid);
//                return null;
//            }
//
            // 查询免押记录信息
            String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(depositPayOrderNo);
            if (ObjectUtils.isEmpty(freeDepositOrder) || !freeDepositOrder.getAuthStatus().equals(FreeDepositOrder.AUTH_FROZEN)) {
                log.warn("selectUnRefundCarDeposit failed. not found t_free_deposit_order. depositPayOrderNo is {}", depositPayOrderNo);
                return null;
            }
//
//            saveFreeDepositSuccessTx(depositPayEntity, freeDepositOrder, queryOrderRspData);
        }
        
        // 获取套餐信息
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(depositPayEntity.getRentalPackageId());
        
        // 拼装返回数据
        CarRentalPackageDepositPayVo depositPayVo = new CarRentalPackageDepositPayVo();
        depositPayVo.setOrderNo(depositPayEntity.getOrderNo());
        depositPayVo.setDeposit(depositPayEntity.getDeposit());
        depositPayVo.setRentalPackageType(depositPayEntity.getRentalPackageType());
        depositPayVo.setPayState(PayStateEnum.SUCCESS.getCode());
        depositPayVo.setPayType(depositPayEntity.getPayType());
        depositPayVo.setStoreId(depositPayEntity.getStoreId());
        depositPayVo.setCarModelId(rentalPackageEntity.getCarModelId());
        depositPayVo.setRentalPackageDeposit(depositPayEntity.getRentalPackageDeposit());
        
        //查询当前订单是否存在退押的状态
        CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(memberTermEntity.getDepositPayOrderNo());
        //如果存在退押的订单并且为拒绝状态，则设置状态信息及拒绝原因
        if (Objects.nonNull(depositRefundEntity) && RefundStateEnum.AUDIT_REJECT.getCode().equals(depositRefundEntity.getRefundState())) {
            status = RefundStateEnum.AUDIT_REJECT.getCode();
            rejectReason = depositRefundEntity.getRemark();
        }
        depositPayVo.setStatus(status);
        depositPayVo.setRejectReason(rejectReason);
        
        return depositPayVo;
    }
    
}
