package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageDepositRefundQryModel;
import com.xiliulou.electricity.reqparam.opt.deposit.FreeDepositOptReq;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.*;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
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
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 租车套餐押金业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageDepositBizServiceImpl implements CarRenalPackageDepositBizService {

    @Resource
    private InsuranceOrderService insuranceOrderService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;

    @Resource(name = "wxRefundPayCarDepositServiceImpl")
    private WxRefundPayService wxRefundPayService;

    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private FreeDepositOrderService freeDepositOrderService;

    @Resource
    private PxzDepositService pxzDepositService;

    @Resource
    private PxzConfigService pxzConfigService;

    @Resource
    private FreeDepositDataService freeDepositDataService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private WechatConfig wechatConfig;

    @Resource
    private WechatV3JsapiService wechatV3JsapiService;

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    /**
     * 免押退押处理逻辑<br />
     * 用于定时任务
     *
     * @param offset 偏移量
     * @param size   取值数量
     */
    @Override
    public void freeDepositRefundHandler(Integer offset, Integer size) {
        // 初始化定义
        offset = ObjectUtils.isEmpty(offset) ? 0: offset;
        size = ObjectUtils.isEmpty(size) ? 500: size;

        boolean lookFlag = true;

        while (lookFlag) {
            // 1. 查询免押退押订单，退款中
            CarRentalPackageDepositRefundQryModel qryModel = new CarRentalPackageDepositRefundQryModel();
            qryModel.setOffset(offset);
            qryModel.setSize(size);
            qryModel.setPayType(PayTypeEnum.EXEMPT.getCode());
            qryModel.setRefundState(RefundStateEnum.REFUNDING.getCode());
            List<CarRentalPackageDepositRefundPo> depositRefundEntityList = carRentalPackageDepositRefundService.page(qryModel);
            if (CollectionUtils.isEmpty(depositRefundEntityList)) {
                log.info("freeDepositRefundHandler, The data is empty and does not need to be processed");
                lookFlag = false;
                break;
            }

            for (CarRentalPackageDepositRefundPo depositRefundEntity : depositRefundEntityList) {
                Integer tenantId = depositRefundEntity.getTenantId();

                // 调用第三方查询
                PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
                if (ObjectUtils.isEmpty(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
                    log.info("freeDepositRefundHandler, pxzConfig configuration error. tenantId is {}", tenantId);
                    continue;
                }

                String orderNo = depositRefundEntity.getDepositPayOrderNo();
                Long uid = depositRefundEntity.getUid();

                PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
                query.setAesSecret(pxzConfig.getAesKey());
                query.setDateTime(System.currentTimeMillis());
                query.setSessionId(orderNo);
                query.setMerchantCode(pxzConfig.getMerchantCode());

                PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
                request.setTransId(orderNo);
                query.setData(request);

                PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
                try {
                    log.info("freeDepositRefundHandler, pxzDepositService.queryFreeDepositOrder params is {}", JsonUtil.toJson(query));
                    pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
                } catch (PxzFreeDepositException e) {
                    log.info("freeDepositRefundHandler, pxzDepositService.queryFreeDepositOrder error. ", e);
                    continue;
                }
                log.info("freeDepositRefundHandler, pxzDepositService.queryFreeDepositOrder result is {}", JsonUtil.toJson(pxzQueryOrderRsp));

                if (ObjectUtils.isEmpty(pxzQueryOrderRsp) || !pxzQueryOrderRsp.isSuccess() || ObjectUtils.isEmpty(pxzQueryOrderRsp.getData())) {
                    log.info("freeDepositRefundHandler, pxzDepositService.queryFreeDepositOrder failed. orderNo is {}", orderNo);
                    continue;
                }

                // 已解冻
                PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
                if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
                    log.info("freeDepositRefundHandler, pxzDepositService.queryFreeDepositOrder is not auth_un_frozen. orderNo is {}, uid is {}", orderNo, uid);
                    continue;
                }
                saveFreeDepositRefundHandlerTx(depositRefundEntity);
            }

            offset += size;
        }
    }

    /**
     * 免押退押处理逻辑，事务处理
     * @param depositRefundEntity
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFreeDepositRefundHandlerTx(CarRentalPackageDepositRefundPo depositRefundEntity) {
        // 更改押金状态
        CarRentalPackageDepositRefundPo depositRefundUpdateEntity = new CarRentalPackageDepositRefundPo();
        depositRefundUpdateEntity.setOrderNo(depositRefundEntity.getOrderNo());
        depositRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
        depositRefundUpdateEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);
        // 作废所有的套餐购买订单（未使用、使用中）、
        carRentalPackageOrderService.refundDepositByUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), null);
        // 查询用户保险
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());
        // 按照人+类型，作废保险
        insuranceUserInfoService.deleteByUidAndType(depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());
        // 作废保险订单
        if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
            insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
        }
        // 删除会员期限表信息
        carRentalPackageMemberTermService.delByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), null);
        // 清理user信息/解绑车辆/解绑电池
        userBizService.depositRefundUnbind(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());

    }

    /**
     * 根据押金缴纳订单编号获取套餐ID
     *
     * @param depositPayOrderNo 押金缴纳订单编码
     * @return 套餐ID
     */
    @Override
    public Long queryRentalPackageIdByDepositPayOrderNo(String depositPayOrderNo) {
        if (StringUtils.isBlank(depositPayOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 获取押金缴纳信息
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("CarRenalPackageDepositBizService.queryRentalPackageIdByDepositPayOrderNo failed. not found t_car_rental_package_deposit_pay. depositPayOrderNo is {}", depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }

        return depositPayEntity.getRentalPackageId();
    }

    /**
     * 查询免押状态
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return true(成功)、false(失败)
     */
    @Override
    public FreeDepositUserInfoVo queryFreeDepositStatus(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 定义返回信息
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();

        // 检测用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userInfo)) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_user_info. uid is {}", uid);
            return null;
        }

        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_car_rental_package_member_term. uid is {}", uid);
            return null;
        }

        // 查询免押记录信息
        String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(depositPayOrderNo);
        if (ObjectUtils.isEmpty(freeDepositOrder)) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_free_deposit_order. depositPayOrderNo is {}", depositPayOrderNo);
            return null;
        }

        // 成功返回判定，前端按照时间比对轮询
        Integer authStatus = freeDepositOrder.getAuthStatus();
        Long createTime = freeDepositOrder.getCreateTime();
        Integer depositType = freeDepositOrder.getDepositType();
        if (Objects.equals(authStatus, FreeDepositOrder.AUTH_FROZEN)) {
            if (FreeDepositOrder.DEPOSIT_TYPE_CAR.equals(depositType)) {
                freeDepositUserInfoVo.setApplyCarDepositTime(createTime);
                freeDepositUserInfoVo.setCarDepositAuthStatus(authStatus);
            }
            if (FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY.equals(depositType)) {
                freeDepositUserInfoVo.setApplyCarBatteryDepositTime(createTime);
                freeDepositUserInfoVo.setCarDepositAuthStatus(authStatus);
            }
            return freeDepositUserInfoVo;
        }

        // 查询押金缴纳信息
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. not found t_car_rental_package_deposit_pay. depositPayOrderNo is {}", depositPayOrderNo);
            return null;
        }

        if (!PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType())) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. t_car_rental_package_deposit_pay payType is wrong. depositPayOrderNo is {}", depositPayOrderNo);
            return null;
        }

        // 查询第三方
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
        if (ObjectUtils.isEmpty(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("CarRenalPackageDepositBizService.queryFreeDepositStatus failed. Incorrect functional configuration. tenantId is {}", tenantId);
            return null;
        }
        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(depositPayOrderNo);
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(depositPayOrderNo);
        query.setData(request);

        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            log.info("CarRenalPackageDepositBizService queryFreeDepositStatus, pxzDepositService.queryFreeDepositOrder params is {}. ", JsonUtil.toJson(query));
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("CarRenalPackageDepositBizService queryFreeDepositStatus, pxzDepositService.queryFreeDepositOrder failed. ", e);
            return null;
        }
        log.info("CarRenalPackageDepositBizService queryFreeDepositStatus, pxzDepositService.queryFreeDepositOrder result is {}", JsonUtil.toJson(pxzQueryOrderRsp));

        // 返回值判定
        if (ObjectUtils.isEmpty(pxzQueryOrderRsp) || !pxzQueryOrderRsp.isSuccess() || ObjectUtils.isEmpty(pxzQueryOrderRsp.getData())) {
            log.info("CarRenalPackageDepositBizService queryFreeDepositStatus, pxzDepositService.queryFreeDepositOrder failed. depositPayOrderNo is {}", depositPayOrderNo);
            return null;
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        // 免押成功
        if (Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            saveFreeDepositSuccessTx(depositPayEntity, freeDepositOrder, queryOrderRspData);
        }

        // 查询结果没有免押成功
        if (FreeDepositOrder.DEPOSIT_TYPE_CAR.equals(depositType)) {
            freeDepositUserInfoVo.setApplyCarDepositTime(createTime);
            freeDepositUserInfoVo.setCarDepositAuthStatus(authStatus);
        }
        if (FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY.equals(depositType)) {
            freeDepositUserInfoVo.setApplyCarBatteryDepositTime(createTime);
            freeDepositUserInfoVo.setCarDepositAuthStatus(authStatus);
        }
        return freeDepositUserInfoVo;
    }

    /**
     * 免押成功查询，成功之后的事务操作
     * @param depositPayEntity 押金缴纳订单数据
     * @param freeDepositOrder 免押记录数据
     * @param queryOrderRspData 请求三方查询数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFreeDepositSuccessTx(CarRentalPackageDepositPayPo depositPayEntity, FreeDepositOrder freeDepositOrder, PxzQueryOrderRsp queryOrderRspData) {
        Integer tenantId = depositPayEntity.getTenantId();
        Integer franchiseeId = depositPayEntity.getFranchiseeId();
        Integer storeId = depositPayEntity.getStoreId();
        Long uid = depositPayEntity.getUid();
        Integer rentalPackageType = depositPayEntity.getRentalPackageType();
        String depositPayOrderNo = depositPayEntity.getOrderNo();

        // 1. 更新免押记录的状态
        FreeDepositOrder freeDepositOrderModify = new FreeDepositOrder();
        freeDepositOrderModify.setId(freeDepositOrder.getId());
        freeDepositOrderModify.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderModify.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderModify.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderModify);
        // 2. 成功之后更新各种状态
        if (FreeDepositOrder.AUTH_FROZEN.equals(queryOrderRspData.getAuthStatus())) {
            // 1. 扣减免押次数
            freeDepositDataService.deductionFreeDepositCapacity(tenantId, 1);
            // 2. 更新押金缴纳订单数据
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
            // 3. 更新租车会员信息状态
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), uid);
            // 4. 更新用户表押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(Long.valueOf(franchiseeId));
            userInfoUpdate.setStoreId(Long.valueOf(storeId));
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            if (RentalPackageTypeEnum.CAR.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarBatteryDepositStatus(YesNoEnum.YES.getCode());
            }
        }
        // 3. 超时关闭之后更新状态
        if (FreeDepositOrder.AUTH_TIMEOUT.equals(queryOrderRspData.getAuthStatus())) {
            // 1. 更新押金缴纳订单数据
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.FAILED.getCode());
            // 2. 删除会员期限表数据
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }
    }

    /**
     * 创建免押订单，生成二维码<br />
     * 创建押金缴纳订单、生成免押记录
     *
     * @param tenantId        租户ID
     * @param uid             C端用户ID
     * @param freeDepositOptReq 免押数据申请
     */
    @Override
    public String createFreeDeposit(Integer tenantId, Long uid, FreeDepositOptReq freeDepositOptReq) {
        if (!ObjectUtils.allNotNull(tenantId, uid, freeDepositOptReq, freeDepositOptReq.getRentalPackageId(), freeDepositOptReq.getPhoneNumber(), freeDepositOptReq.getRealName(), freeDepositOptReq.getIdCard())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        //检测租户
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_CAR) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. The deposit exemption function is not enabled. tenantId is {}", tenantId);
            throw new BizException("100418", "押金免押功能未开启,请联系客服处理");
        }

        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(tenantId);
        if (ObjectUtils.isEmpty(freeDepositData)) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. not found t_free_deposit_data, tenantId is {}", tenantId);
            throw new BizException("100404", "免押次数未充值，请联系管理员");
        }

        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. The number of times to waive charges is 0. tenantId is {}", tenantId);
            throw new BizException("100405", "免押次数已用完，请联系管理员");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
        if (ObjectUtils.isEmpty(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. Incorrect functional configuration. tenantId is {}", tenantId);
            throw new BizException("100400", "免押功能未配置相关信息，请联系客服处理");
        }

        // 检测用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userInfo)) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. not found t_user_info. uid is {}", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. user is disable. uid is {}", uid);
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. user not auth. uid is {}", uid);
            throw new BizException("ELECTRICITY.0041", "未实名认证");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(memberTermEntity) && !MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. Deposit paid. uid is {}", uid);
            throw new BizException("300028", "已缴纳押金");
        }

        // 查询套餐信息
        Long rentalPackageId = freeDepositOptReq.getRentalPackageId();
        CarRentalPackagePo carRentalPackage = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(carRentalPackage) || UpDownEnum.DOWN.getCode().equals(carRentalPackage.getStatus())) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. not found t_car_rental_package or status is wrong. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }
        if (!carRentalPackage.getTenantId().equals(tenantId)) {
            log.error("CarRenalPackageDepositBizService.createFreeDeposit failed. Tenant mismatch. rentalPackage tenantId is {}, param tenantId is {}", carRentalPackage.getTenantId(), tenantId);
            throw new BizException("300000", "数据有误");
        }

        // 创建押金缴纳订单
        CarRentalPackageDepositPayPo carRentalPackageDepositPayInsert = buildCarRentalPackageDepositPayEntity(tenantId, uid, carRentalPackage, YesNoEnum.YES.getCode(), PayTypeEnum.EXEMPT.getCode());
        // 创建免押记录
        FreeDepositOrder freeDepositOrder = buildFreeDepositOrderEntity(tenantId, uid, carRentalPackageDepositPayInsert, freeDepositOptReq);
        // 创建租车会员信息
        CarRentalPackageMemberTermPo memberTermInsertOrUpdateEntity = buildCarRentalPackageMemberTerm(tenantId, uid, carRentalPackage, carRentalPackageDepositPayInsert.getOrderNo(), memberTermEntity);

        // 调用第三方
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeDepositOrder.getPhone());
        request.setSubject("租车套餐免押");
        request.setRealName(freeDepositOrder.getRealName());
        request.setIdNumber(freeDepositOrder.getIdCard());
        request.setTransId(freeDepositOrder.getOrderId());
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
        PxzCommonRsp<String> callPxzRsp = null;
        try {
            log.info("CarRenalPackageDepositBizService createFreeDeposit, pxzDepositService.freeDepositOrder params is {}", JsonUtil.toJson(query));
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("CarRenalPackageDepositBizService createFreeDeposit, pxzDepositService.freeDepositOrder failed. ", e);
            throw new BizException("100401", "免押生成失败");
        }
        log.info("CarRenalPackageDepositBizService createFreeDeposit, pxzDepositService.freeDepositOrder result is {}", JsonUtil.toJson(callPxzRsp));

        if (ObjectUtils.isEmpty(callPxzRsp)) {
            log.error("CarRenalPackageDepositBizService createFreeDeposit, pxzDepositService.freeDepositOrder", uid, freeDepositOrder.getOrderId());
            throw new BizException("100401", "免押生成失败");
        }

        if (!callPxzRsp.isSuccess()) {
            log.error("CarRenalPackageDepositBizService createFreeDeposit, pxzDepositService.freeDepositOrder", uid, freeDepositOrder.getOrderId());
            throw new BizException("100401", callPxzRsp.getRespDesc());
        }

        // TX 事务落库
        saveFreeDepositTx(carRentalPackageDepositPayInsert, freeDepositOrder, memberTermInsertOrUpdateEntity);

        return callPxzRsp.getData();
    }

    /**
     * 免押申请数据落库事务处理
     * @param carRentalPackageDepositPay 车辆押金缴纳订单
     * @param freeDepositOrder 免押记录
     * @param memberTermEntity 新增的会员期限信息
     */
    @Transactional(rollbackFor = Exception.class)
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
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageEntity 租车套餐信息
     * @param depositPayOrderNo 押金缴纳订单编码
     * @param memberTermEntity DB层的会员期限数据
     * @return 将要新增或修改的租车会员期限信息
     */
    private CarRentalPackageMemberTermPo buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePo packageEntity, String depositPayOrderNo, CarRentalPackageMemberTermPo memberTermEntity) {
        CarRentalPackageMemberTermPo carRentalPackageMemberTermEntity = new CarRentalPackageMemberTermPo();
        carRentalPackageMemberTermEntity.setUid(uid);
        carRentalPackageMemberTermEntity.setRentalPackageType(packageEntity.getType());
        carRentalPackageMemberTermEntity.setRentalPackageConfine(packageEntity.getConfine());
        carRentalPackageMemberTermEntity.setStatus(MemberTermStatusEnum.PENDING_EFFECTIVE.getCode());
        carRentalPackageMemberTermEntity.setDeposit(packageEntity.getDeposit());
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
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param carRentalPackageDepositPayInsert 租车套餐押金订单
     * @param freeDepositOptReq 免押申请数据
     * @return 免押记录订单
     */
    private FreeDepositOrder buildFreeDepositOrderEntity(Integer tenantId, Long uid, CarRentalPackageDepositPayPo carRentalPackageDepositPayInsert, FreeDepositOptReq freeDepositOptReq) {
        Integer depositType = FreeDepositOrder.DEPOSIT_TYPE_CAR;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackageDepositPayInsert.getRentalPackageType())) {
            depositType = FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY;
        }
        return FreeDepositOrder.builder()
                .uid(uid)
                .authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE)
                .idCard(freeDepositOptReq.getIdCard())
                .orderId(carRentalPackageDepositPayInsert.getOrderNo())
                .phone(freeDepositOptReq.getPhoneNumber())
                .realName(freeDepositOptReq.getRealName())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .payStatus(FreeDepositOrder.PAY_STATUS_INIT)
                .tenantId(tenantId)
                .transAmt(carRentalPackageDepositPayInsert.getDeposit().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO)
                .depositType(depositType)
                .build();
    }

    /**
     * 生成押金缴纳订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param carRentalPackage 租车套餐信息
     * @return 租车套餐押金订单
     */
    private CarRentalPackageDepositPayPo buildCarRentalPackageDepositPayEntity(Integer tenantId, Long uid, CarRentalPackagePo carRentalPackage, Integer freeDeposit, Integer payType) {
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
        return carRentalPackageDepositPay;
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
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.info("selectUnRefundCarDeposit, not found car_rental_package_member_term, tenantId is {}, uid is {}", tenantId, uid);
            return null;
        }

        Integer status = 0;
        if (MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode().equals(memberTermEntity.getStatus())) {
            status = 1;
            // 申请退押，查询退押订单信息
            CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(memberTermEntity.getDepositPayOrderNo());
            if (ObjectUtils.isEmpty(depositRefundEntity)) {
                log.error("selectUnRefundCarDeposit, not found t_car_rental_package_order_rent_refund, tenantId is {}, uid is {}", tenantId, uid);
                throw new BizException("300000", "数据有误");
            }
            if (RefundStateEnum.REFUNDING.getCode().equals(depositRefundEntity.getRefundState())) {
                status = 2;
            }
        }

        // 押金缴纳信息
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectLastByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            return null;
        }

        // 免押、未支付  调用第三方，二次查询
        if (PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType()) && PayStateEnum.UNPAID.getCode().equals(depositPayEntity.getPayState())) {
            // 调用第三方查询
            PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(tenantId);
            if (ObjectUtils.isEmpty(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
                log.info("selectUnRefundCarDeposit, pxzConfig configuration error. tenantId is {}", tenantId);
                return null;
            }

            String orderNo = depositPayEntity.getOrderNo();

            PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
            query.setAesSecret(pxzConfig.getAesKey());
            query.setDateTime(System.currentTimeMillis());
            query.setSessionId(orderNo);
            query.setMerchantCode(pxzConfig.getMerchantCode());

            PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
            request.setTransId(orderNo);
            query.setData(request);

            PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
            try {
                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder params is {}", JsonUtil.toJson(query));
                pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
            } catch (PxzFreeDepositException e) {
                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder error. ", e);
                return null;
            }
            log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder result is {}", JsonUtil.toJson(pxzQueryOrderRsp));

            if (ObjectUtils.isEmpty(pxzQueryOrderRsp) || !pxzQueryOrderRsp.isSuccess() || ObjectUtils.isEmpty(pxzQueryOrderRsp.getData())) {
                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder failed. orderNo is {}", orderNo);
                return null;
            }

            // 未冻结
            PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
            if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
                log.info("selectUnRefundCarDeposit, pxzDepositService.queryFreeDepositOrder is not auth_frozen. orderNo is {}, uid is {}", orderNo, uid);
                return null;
            }

            // 查询免押记录信息
            String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(depositPayOrderNo);
            if (ObjectUtils.isEmpty(freeDepositOrder)) {
                log.error("selectUnRefundCarDeposit failed. not found t_free_deposit_order. depositPayOrderNo is {}", depositPayOrderNo);
                return null;
            }

            saveFreeDepositSuccessTx(depositPayEntity, freeDepositOrder, queryOrderRspData);
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
        depositPayVo.setStatus(status);

        return depositPayVo;
    }

    /**
     * 运营商端创建退押
     *
     * @param optModel 租户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundDepositCreate(CarRentalPackageDepositRefundOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getTenantId(), optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = optModel.getTenantId();
        Long uid = optModel.getUid();
        String depositPayOrderNo = optModel.getDepositPayOrderNo();
        BigDecimal realAmount = optModel.getRealAmount();

        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRenalPackageDepo" +
                    "sitBizService.checkRefundDeposit failed. car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.error("CarRenalPackageDepositBizService.refundDepositCreate failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}", uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);

        Integer payType = depositPayEntity.getPayType();

        // 生成退押申请单
        CarRentalPackageDepositRefundPo refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo,
                SystemDefinitionEnum.BACKGROUND, false, payType, realAmount);


        // 待审核
        if (RefundStateEnum.REFUNDING.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 实际退款0元，则直接成功，不调用退款接口
            if (BigDecimal.ZERO.compareTo(realAmount) == 0) {
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
                            log.error("refundDepositCreate faild. not find t_electricity_trade_order. orderNo is {}", depositPayEntity.getRentalPackageOrderNo());
                            throw new BizException("300000", "数据有误");
                        }
                        Integer status = electricityTradeOrder.getStatus();
                        if (ElectricityTradeOrder.STATUS_INIT.equals(status) || ElectricityTradeOrder.STATUS_FAIL.equals(status)) {
                            log.error("refundDepositCreate faild. t_electricity_trade_order status is wrong. orderNo is {}", depositPayEntity.getRentalPackageOrderNo());
                            throw new BizException("300000", "数据有误");
                        }

                        // 调用微信支付，进行退款
                        RefundOrder refundOrder = RefundOrder.builder()
                                .orderId(electricityTradeOrder.getOrderNo())
                                .payAmount(electricityTradeOrder.getTotalFee())
                                .refundOrderNo(refundDepositInsertEntity.getOrderNo())
                                .refundAmount(refundDepositInsertEntity.getRealAmount()).build();
                        log.info("refundDepositCreate, Call WeChat refund. params is {}", JsonUtil.toJson(refundOrder));
                        WechatJsapiRefundResultDTO wxRefundDto = wxRefund(refundOrder);
                        log.info("refundDepositCreate, Call WeChat refund. result is {}", JsonUtil.toJson(wxRefundDto));

                    } catch (WechatPayException e) {
                        log.error("refundDepositCreate failed.", e);
                        throw new BizException(e.getMessage());
                    }
                }

                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.error("refundDepositCreate failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }

                    PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(depositPayEntity.getTenantId());
                    if(ObjectUtils.isEmpty(pxzConfig)) {
                        log.error("refundDepositCreate failed. not found t_pxz_config. tenantId is {}", depositPayEntity.getTenantId());
                        throw new BizException("300000", "数据有误");
                    }

                    PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
                    query.setAesSecret(pxzConfig.getAesKey());
                    query.setDateTime(System.currentTimeMillis());
                    query.setSessionId(freeDepositOrderNo);
                    query.setMerchantCode(pxzConfig.getMerchantCode());

                    PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
                    queryRequest.setRemark("租车套餐免押解冻");
                    queryRequest.setTransId(freeDepositOrderNo);
                    query.setData(queryRequest);

                    PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
                    try {
                        log.info("refundDepositCreate, pxzDepositService.unfreezeDeposit params query is {}", JsonUtil.toJson(query));
                        pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
                    } catch (Exception e) {
                        log.error("refundDepositCreate failed. pxzDepositService.unfreezeDeposit failed.", e);
                        throw new BizException("100406", "免押解冻失败");
                    }
                    log.info("refundDepositCreate, pxzDepositService.unfreezeDeposit result is {}", JsonUtil.toJson(pxzDepositUnfreezeRspPxzCommonRsp));

                    if (ObjectUtils.isEmpty(pxzDepositUnfreezeRspPxzCommonRsp) || !pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
                        throw new BizException("100406", "免押解冻失败");
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
     * @return
     */
    @Override
    public boolean approveRefundDepositOrder(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, BigDecimal refundAmount) {
        if (!ObjectUtils.allNotNull(refundDepositOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 退押订单
        CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(refundDepositOrderNo);
        if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.PENDING_APPROVAL.getCode().equals(depositRefundEntity.getRefundState())) {
            log.error("approveRefundDepositOrder faild. not find car_rental_package_deposit_refund or status error. refundDepositOrderNo is {}", refundDepositOrderNo);
            throw new BizException("300000", "数据有误");
        }

        Integer payType = depositRefundEntity.getPayType();
        if ((PayTypeEnum.ON_LINE.getCode().equals(payType) || PayTypeEnum.OFF_LINE.getCode().equals(payType)) && approveFlag && ObjectUtils.isEmpty(refundAmount)) {
            log.error("approveRefundDepositOrder faild. not find car_rental_package_deposit_refund or status error. refundDepositOrderNo is {}", refundDepositOrderNo);
            throw new BizException("300029", "退押金额不能为空");
        }

        // 租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode().equals(memberTermEntity.getStatus())) {
            log.error("approveRefundRentOrder faild. not find t_car_rental_package_member_term or status error. uid is {}", depositRefundEntity.getUid());
            throw new BizException("300000", "数据有误");
        }

        // 押金缴纳编码
        String orderNo = memberTermEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.error("approveRefundRentOrder faild. not find t_car_rental_package_deposit_pay or payState error. orderNo is {}", orderNo);
            throw new BizException("300000", "数据有误");
        }

        // TX 事务落库
        saveApproveRefundDepositOrderTx(refundDepositOrderNo, approveFlag, apploveDesc, apploveUid, depositRefundEntity, refundAmount, depositPayEntity);

        return true;
    }

    /**
     * 退押审批，TX事务处理
     * @param refundDepositOrderNo 退押申请订单号
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @param depositRefundEntity         退押申请单信息
     * @param depositPayEntity         押金缴纳信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveRefundDepositOrderTx(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, CarRentalPackageDepositRefundPo depositRefundEntity,
                                                BigDecimal refundAmount, CarRentalPackageDepositPayPo depositPayEntity) {

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

                }

                // 线上，调用微信退款
                if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                    String rentalPackageOrderNo = depositPayEntity.getRentalPackageOrderNo();
                    if (StringUtils.isBlank(rentalPackageOrderNo)) {
                        log.error("saveApproveRefundDepositOrderTx faild. not find t_electricity_trade_order. orderNo is {}", rentalPackageOrderNo);
                        throw new BizException("300000", "数据有误");
                    }
                    try {
                        // 根据购买订单编码获取当初的支付流水
                        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(rentalPackageOrderNo);
                        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
                            log.error("saveApproveRefundDepositOrderTx faild. not find t_electricity_trade_order. orderNo is {}", rentalPackageOrderNo);
                            throw new BizException("300000", "数据有误");
                        }
                        Integer status = electricityTradeOrder.getStatus();
                        if (ElectricityTradeOrder.STATUS_INIT.equals(status) || ElectricityTradeOrder.STATUS_FAIL.equals(status)) {
                            log.error("saveApproveRefundDepositOrderTx faild. t_electricity_trade_order status is wrong. orderNo is {}", rentalPackageOrderNo);
                            throw new BizException("300000", "数据有误");
                        }

                        // 调用微信支付，进行退款
                        RefundOrder refundOrder = RefundOrder.builder()
                                .orderId(electricityTradeOrder.getOrderNo())
                                .payAmount(electricityTradeOrder.getTotalFee())
                                .refundOrderNo(refundDepositOrderNo)
                                .refundAmount(refundAmount).build();
                        log.info("saveApproveRefundDepositOrderTx, Call WeChat refund. params is {}", JsonUtil.toJson(refundOrder));
                        WechatJsapiRefundResultDTO wxRefundDto = wxRefund(refundOrder);
                        log.info("saveApproveRefundDepositOrderTx, Call WeChat refund. result is {}", JsonUtil.toJson(wxRefundDto));

                        // 赋值退款单状态：退款中
                        depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());

                    } catch (WechatPayException e) {
                        log.error("saveApproveRefundDepositOrderTx failed.", e);
                        throw new BizException(e.getMessage());
                    }
                }

                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    // 赋值退款单状态：退款中
                    depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());

                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.error("saveApproveRefundDepositOrderTx failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }

                    PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(depositPayEntity.getTenantId());
                    if(ObjectUtils.isEmpty(pxzConfig)) {
                        log.error("saveApproveRefundDepositOrderTx failed. not found t_pxz_config. tenantId is {}", depositPayEntity.getTenantId());
                        throw new BizException("300000", "数据有误");
                    }

                    PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
                    query.setAesSecret(pxzConfig.getAesKey());
                    query.setDateTime(System.currentTimeMillis());
                    query.setSessionId(freeDepositOrderNo);
                    query.setMerchantCode(pxzConfig.getMerchantCode());

                    PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
                    queryRequest.setRemark("租车套餐免押解冻");
                    queryRequest.setTransId(freeDepositOrderNo);
                    query.setData(queryRequest);

                    PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
                    try {
                        log.info("saveApproveRefundDepositOrderTx, pxzDepositService.unfreezeDeposit params query is {}", JsonUtil.toJson(query));
                        pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
                    } catch (Exception e) {
                        log.error("saveApproveRefundDepositOrderTx failed. pxzDepositService.unfreezeDeposit failed.", e);
                        throw new BizException("100406", "免押解冻失败");
                    }
                    log.info("saveApproveRefundDepositOrderTx, pxzDepositService.unfreezeDeposit result is {}", JsonUtil.toJson(pxzDepositUnfreezeRspPxzCommonRsp));

                    if (ObjectUtils.isEmpty(pxzDepositUnfreezeRspPxzCommonRsp) || !pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
                        throw new BizException("100406", "免押解冻失败");
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
                }

                // 免押
                if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                    // 赋值退款单状态：退款中
                    depositRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());

                    String freeDepositOrderNo = depositPayEntity.getOrderNo();
                    FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(freeDepositOrderNo);
                    if (ObjectUtils.isEmpty(freeDepositOrder)) {
                        log.error("saveApproveRefundDepositOrderTx failed. not found t_free_deposit_order. orderId is {}", freeDepositOrderNo);
                        throw new BizException("300000", "数据有误");
                    }

                    PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(depositPayEntity.getTenantId());
                    if(ObjectUtils.isEmpty(pxzConfig)) {
                        log.error("saveApproveRefundDepositOrderTx failed. not found t_pxz_config. tenantId is {}", depositPayEntity.getTenantId());
                        throw new BizException("300000", "数据有误");
                    }

                    PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
                    query.setAesSecret(pxzConfig.getAesKey());
                    query.setDateTime(System.currentTimeMillis());
                    query.setSessionId(freeDepositOrderNo);
                    query.setMerchantCode(pxzConfig.getMerchantCode());

                    PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
                    queryRequest.setRemark("租车套餐免押解冻");
                    queryRequest.setTransId(freeDepositOrderNo);
                    query.setData(queryRequest);

                    PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
                    try {
                        log.info("saveApproveRefundDepositOrderTx, pxzDepositService.unfreezeDeposit params query is {}", JsonUtil.toJson(query));
                        pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
                    } catch (Exception e) {
                        log.error("saveApproveRefundDepositOrderTx failed. pxzDepositService.unfreezeDeposit failed.", e);
                        throw new BizException("100406", "免押解冻失败");
                    }
                    log.info("saveApproveRefundDepositOrderTx, pxzDepositService.unfreezeDeposit result is {}", JsonUtil.toJson(pxzDepositUnfreezeRspPxzCommonRsp));

                    if (ObjectUtils.isEmpty(pxzDepositUnfreezeRspPxzCommonRsp) || !pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
                        throw new BizException("100406", "免押解冻失败");
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
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        }
    }


    /**
     * 调用微信支付
     * @param refundOrder
     * @return
     * @throws WechatPayException
     */
    private WechatJsapiRefundResultDTO wxRefund(RefundOrder refundOrder) throws WechatPayException {
        //第三方订单号
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
            log.error("CarRenalPackageDepositBizService.wxRefund failed, not found t_electricity_trade_order. orderId is {}", refundOrder.getOrderId());
            throw new BizException("300000", "数据有误");
        }

        //调用退款
        WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
        wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setTotal(electricityTradeOrder.getTotalFee().intValue());
        wechatV3RefundQuery.setRefund(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundQuery.setReason("押金退款");
        wechatV3RefundQuery.setOrderId(electricityTradeOrder.getTradeOrderNo());
        wechatV3RefundQuery.setNotifyUrl(wechatConfig.getCarDepositRefundCallBackUrl() + electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setCurrency("CNY");
        wechatV3RefundQuery.setRefundId(refundOrder.getRefundOrderNo());

        return wechatV3JsapiService.refund(wechatV3RefundQuery);
    }

    /**
     * C端退押申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @return
     */
    @Override
    public boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, depositPayOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRenalPackageDepositBizService.checkRefundDeposit failed. car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.error("CarRenalPackageDepositBizService.refundDeposit failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}", uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }

        Integer payType = depositPayEntity.getPayType();
        if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
            log.error("CarRenalPackageDepositBizService.refundDeposit failed. t_car_rental_package_deposit_pay pay type is {}, depositPayOrderNo is {}", payType, depositPayOrderNo);
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
                depositAuditFlag, payType, null);

        // 待审核
        if (RefundStateEnum.PENDING_APPROVAL.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
        }

        return true;
    }

    /**
     * 退押申请事务处理
     * @param refundDepositInsertEntity 退押申请单数据
     * @param memberTermEntity 会员期限实体数据
     * @param optId 操作人ID
     * @param delFlag 删除标识
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

        }
    }

    /**
     * 构建退押申请单数据
     * @param memberTermEntity 会员期限信息
     * @param depositPayOrderNo 押金缴纳订单编码
     * @param systemDefinition 操作系统
     * @param depositAuditFlag 退押审批标识
     * @param depositAuditFlag 支付方式
     * @param depositAuditFlag 实际退款金额(可为空，后端操作不能为空)
     * @return
     */
    private CarRentalPackageDepositRefundPo budidCarRentalPackageOrderRentRefund(CarRentalPackageMemberTermPo memberTermEntity, String depositPayOrderNo, SystemDefinitionEnum systemDefinition,
                                                                                 boolean depositAuditFlag, Integer payType, BigDecimal refundAmount) {
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

        // 设置退款状态
        if (SystemDefinitionEnum.BACKGROUND.getCode().equals(systemDefinition.getCode())) {
            refundDepositInsertEntity.setRealAmount(refundAmount);

            // 线上，退款中
            if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
            }
            // 线下，退款成功
            if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            }
            // 免押，退款中
            if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
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
     * 退押检测
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    private void checkRefundDeposit(Integer tenantId, Long uid, Integer rentalPackageType, String depositPayOrderNo) {

        // 是否存在正常的退押申请单
        CarRentalPackageDepositRefundPo depositRefundPo = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(depositPayOrderNo);
        if (ObjectUtils.isNotEmpty(depositRefundPo) && !RefundStateEnum.getRefundStateList().contains(depositRefundPo.getRefundState())) {
            throw new BizException("100031", "不能重复退押金");
        }

        // 检测是否存在滞纳金
        if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
            log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There is a Late fee, please pay first. uid is {}", uid);
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // 查询设备(车辆)
        ElectricityCar userCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(userCar) && StringUtils.isNotBlank(userCar.getSn())) {
            log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There are vehicles that have not been returned. uid is {}", uid);
            throw new BizException("300041", "需先退还资产再退押金");
        }

        // 车电一体，查询设备(电池)
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There are unreturned batteries. uid is {}", uid);
                throw new BizException("300041", "需先退还资产再退押金");
            }
        }
    }
}
