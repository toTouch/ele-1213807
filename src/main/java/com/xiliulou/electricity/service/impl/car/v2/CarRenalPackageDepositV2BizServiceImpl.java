package com.xiliulou.electricity.service.impl.car.v2;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.PxzConfig;
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
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.v2.CarRenalPackageDepositV2BizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3RefundRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3JsapiInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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
    private UserInfoService userInfoService;
    
    @Resource
    private CarRentalPackageService carRentalPackageService;
    
    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FreeDepositService freeDepositService;
    
    /**
     * 创建免押订单，生成二维码<br /> 创建押金缴纳订单、生成免押记录
     *
     * @param tenantId          租户ID
     * @param uid               C端用户ID
     * @param freeDepositOptReq 免押数据申请
     */
    @Override
    public String createFreeDeposit(Integer tenantId, Long uid, FreeDepositOptReq freeDepositOptReq) {
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
        
        if (Boolean.FALSE.equals(useFreeDepositStatusResult.getLeft())) {
            throw new BizException(useFreeDepositStatusResult.getMiddle(), useFreeDepositStatusResult.getRight().toString());
        }
        
        //查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid);
        if (Objects.isNull(useFreeDepositStatusResult.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result from cache for car rental. uid = {}, result = {}", uid, result);
            
            // 此时代表：在5分钟内用户调用了取消订单的接口且二次申请免押，则需要创建租车会员信息
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 查询最后一次的免押订单信息
                CarRentalPackageDepositPayPo carRentalPackageDepositPayOri = carRentalPackageDepositPayService.queryLastFreeOrderByUid(tenantId, uid);
                if (ObjectUtils.isEmpty(carRentalPackageDepositPayOri)) {
                    log.error("t_car_rental_package_deposit_pay not found. uid = {}", uid);
                    throw new BizException("300015", "押金订单状态异常");
                }
                
                CarRentalPackageMemberTermPo memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, carRentalPackage, carRentalPackageDepositPayOri.getOrderNo(),
                        memberTermEntity);
                carRentalPackageMemberTermService.insert(memberTermInsertEntity);
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
                        .payAmount(BigDecimal.valueOf(freeDepositOrder.getTransAmt()))
                        .freeDepositOrderId(freeDepositOrder.getOrderId()).phoneNumber(freeDepositOrder.getPhone()).uid(uid).build());
        if (Boolean.FALSE.equals(triple.getLeft())){
            throw new BizException(triple.getMiddle(), triple.getRight().toString());
        }
        FreeDepositOrderDTO freeDepositOrderDTO = (FreeDepositOrderDTO) triple.getRight();
        // TX 事务落库
        saveFreeDepositTx(carRentalPackageDepositPayInsert, freeDepositOrder, memberTermInsertOrUpdateEntity);
        
        log.info("generate free deposit data from pxz for car rental, data = {}", triple.getRight());
        //保存pxz返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid, UriUtils.encode(freeDepositOrderDTO.getData(), StandardCharsets.UTF_8),
                300 * 1000L, false);
        
        return freeDepositOrderDTO.getData();
    }
    
    /**
     * 免押申请数据落库事务处理
     *
     * @param carRentalPackageDepositPay 车辆押金缴纳订单
     * @param freeDepositOrder           免押记录
     * @param memberTermEntity           新增的会员期限信息
     */
    //@Transactional(rollbackFor = Exception.class)
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
    private CarRentalPackageMemberTermPo buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePo packageEntity, String depositPayOrderNo,
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
    
}
