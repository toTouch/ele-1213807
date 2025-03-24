package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawApplicationBO;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawApplicationRecordBO;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawSendBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.config.merchant.MerchantConfig;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawApplicationConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawApplicationRecordConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawConstant;
import com.xiliulou.electricity.entity.ElectricityConfigExtra;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawApplicationStateEnum;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawSceneEnum;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawTypeEnum;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationMapper;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.merchant.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawConfirmReceiptVO;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawProcessVO;
import com.xiliulou.pay.weixinv3.dto.*;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatTransferBatchOrderDetailQuery;
import com.xiliulou.pay.weixinv3.query.WechatTransferSceneReportInfoQuery;
import com.xiliulou.pay.weixinv3.v2.query.*;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3TransferInvokeService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:50
 */

@Slf4j
@Service("merchantWithdrawApplicationService")
public class MerchantWithdrawApplicationServiceImpl implements MerchantWithdrawApplicationService {
    
    @Resource
    UserOauthBindService userOauthBindService;
    
    @Resource
    private MerchantWithdrawApplicationMapper merchantWithdrawApplicationMapper;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private MerchantUserAmountService merchantUserAmountService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantWithdrawApplicationRecordService merchantWithdrawApplicationRecordService;
    
    @Resource
    private WechatV3TransferInvokeService wechatV3TransferInvokeService;
    
    @Resource
    private MerchantConfig merchantConfig;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Autowired
    private SiteMessagePublish siteMessagePublish;
    
    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private WechatConfig wechatConfig;

    @Resource
    private MerchantWithdrawOldConfigInfoService merchantWithdrawOldConfigInfoService;

    @Resource
    private ElectricityConfigExtraService electricityConfigExtraService;

    @Resource
    private WeChatAppTemplateService weChatAppTemplateService;

    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("MERCHANT-WITHDRAW-THREAD-POOL", 6, "merchantWithdrawThread");
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        //限频
        Boolean getLockSuccess = redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION + merchantWithdrawApplicationRequest.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "000000", "操作频繁,请稍后再试");
        }
        
        //检查商户是否存在
        Merchant queryMerchant = merchantService.queryByUid(merchantWithdrawApplicationRequest.getUid());
        if (Objects.isNull(queryMerchant)) {
            log.warn("merchant user not found, uid = {}", merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120003", "商户不存在");
        }
        
        //查询商户余额表，是否存在商户账户
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(merchantWithdrawApplicationRequest.getUid());
        if (Objects.isNull(merchantUserAmount)) {
            log.warn("merchant user balance account not found, uid = {}", merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120010", "商户余额账户不存在");
        }

        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(queryMerchant.getTenantId(), queryMerchant.getFranchiseeId());
        } catch (Exception e) {
            log.error("check merchant old withdraw config error!, get wechat pay params details error, tenantId = {}, franchiseeId={}", queryMerchant.getTenantId(), queryMerchant.getFranchiseeId(), e);
            throw new CustomBusinessException("支付配置有误，请检查相关配置");
        }

        if (Objects.isNull(wechatPayParamsDetails) || Objects.isNull(wechatPayParamsDetails.getFranchiseeId())) {
            return Triple.of(false, "120017", "未配置支付参数");
        }

        boolean checkMerchantOldWithdrawConfigInfo = merchantWithdrawOldConfigInfoService.existsMerchantOldWithdrawConfigInfo(wechatPayParamsDetails.getTenantId(), wechatPayParamsDetails.getFranchiseeId());
        Integer limitAmount = MerchantWithdrawConstant.WITHDRAW_MAX_AMOUNT;
        Integer type = MerchantWithdrawTypeEnum.OLD.getCode();
        if (!checkMerchantOldWithdrawConfigInfo) {
            // 新流程
            limitAmount = MerchantWithdrawConstant.WITHDRAW_MAX_AMOUNT_V2;
            type = MerchantWithdrawTypeEnum.NEW.getCode();
        }

        //单词提现金额限制：大于1， 小于等于500
        if (merchantWithdrawApplicationRequest.getAmount().compareTo(BigDecimal.ONE) < 0
                || merchantWithdrawApplicationRequest.getAmount().compareTo(new BigDecimal(limitAmount)) > 0) {
            return Triple.of(false, "120011", "单次提现金额范围（1-500）");
        }
        
        //检查余额表中的余额是否满足提现金额
        if (merchantUserAmount.getBalance().compareTo(merchantWithdrawApplicationRequest.getAmount()) < 0) {
            log.warn("merchant user balance amount not enough, amount = {}, uid = {}", merchantWithdrawApplicationRequest.getAmount(),
                    merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120012", "提现金额不足");
        }
        
        //查询银行卡信息，检查银行卡是否存在，并且检查该银行卡是否支持转账，若为微信转账，则不需要银行卡信息
        //计算手续费， 微信申请无手续费，若为其他方式提现，则需要考虑
        
        String businessOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW, merchantWithdrawApplicationRequest.getUid());
        //插入提现表
        MerchantWithdrawApplication merchantWithdrawApplication = new MerchantWithdrawApplication();
        merchantWithdrawApplication.setAmount(merchantWithdrawApplicationRequest.getAmount());
        merchantWithdrawApplication.setUid(merchantWithdrawApplicationRequest.getUid());
        merchantWithdrawApplication.setTenantId(merchantWithdrawApplicationRequest.getTenantId());
        merchantWithdrawApplication.setOrderNo(businessOrderId);
        merchantWithdrawApplication.setWithdrawType(MerchantWithdrawConstant.WITHDRAW_TYPE_WECHAT);
        merchantWithdrawApplication.setStatus(MerchantWithdrawConstant.REVIEW_IN_PROGRESS);
        merchantWithdrawApplication.setDelFlag(CommonConstant.DEL_N);
        merchantWithdrawApplication.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setFranchiseeId(queryMerchant.getFranchiseeId());
        merchantWithdrawApplication.setType(type);
        
        Integer result = merchantWithdrawApplicationMapper.insertOne(merchantWithdrawApplication);
        
        //扣除商户账户余额表中的余额
        merchantUserAmountService.withdrawAmount(merchantWithdrawApplicationRequest.getAmount(), merchantWithdrawApplicationRequest.getUid(),
                merchantWithdrawApplicationRequest.getTenantId().longValue());
        //发送站内信
        User user = userService.queryByUidFromCache(merchantWithdrawApplicationRequest.getUid());
        siteMessagePublish.publish(SiteMessageEvent.builder(this).tenantId(merchantWithdrawApplicationRequest.getTenantId().longValue()).code(SiteMessageType.MERCHANT_WITHDRAWAL)
                .notifyTime(System.currentTimeMillis()).addContext("name", user.getName()).addContext("phone", user.getPhone())
                .addContext("orderNo", businessOrderId).build());
        
        return Triple.of(true, null, result);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> reviewMerchantWithdrawApplication(ReviewWithdrawApplicationRequest reviewWithdrawApplicationRequest) {
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION_REVIEW + reviewWithdrawApplicationRequest.getId(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁");
        }
        
        log.info("review withdraw application, request = {}", reviewWithdrawApplicationRequest);
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "120013", "未找到用户");
        }
        
        //检查入参中的状态是否为同意或者拒绝状态，若为其他状态，则提示错误。
//        if (!MerchantWithdrawConstant.REVIEW_REFUSED.equals(reviewWithdrawApplicationRequest.getStatus()) && !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(
//                reviewWithdrawApplicationRequest.getStatus())) {
//            log.warn("Illegal parameter warn for approve withdraw application,  status = {}", reviewWithdrawApplicationRequest.getStatus());
//            return Triple.of(false, "120014", "参数不合法");
//        }
        
        //检查提现审核参数状态，是否为待审核状态
        MerchantWithdrawApplication merchantWithdrawApplication = merchantWithdrawApplicationMapper.selectById(reviewWithdrawApplicationRequest.getId());
        if (Objects.isNull(merchantWithdrawApplication)) {
            return Triple.of(false, "120015", "提现申请不存在");
        }
        
        Merchant merchant = merchantService.queryByUid(merchantWithdrawApplication.getUid());
        if (Objects.isNull(merchant) || Objects.isNull(merchant.getFranchiseeId())) {
            return Triple.of(false, "120212", "商户不存在");
        }
        
        if (ObjectUtils.isNotEmpty(reviewWithdrawApplicationRequest.getBindFranchiseeIdList()) && !reviewWithdrawApplicationRequest.getBindFranchiseeIdList().contains(merchant.getFranchiseeId())) {
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        //检查提现状态是否为审核中的状态
        if (!MerchantWithdrawConstant.REVIEW_IN_PROGRESS.equals(merchantWithdrawApplication.getStatus())) {
            return Triple.of(false, "120016", "不能重复审核");
        }

        //生成提现批次单号
        String batchNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH, user.getUid());
        String batchDetailNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH_DETAIL, user.getUid());

        //  线下转账拦截
        if (Objects.equals(reviewWithdrawApplicationRequest.getStatus(), MerchantWithdrawConstant.OFF_LINE_TRANSFER)){
            offlineTransferHandler(reviewWithdrawApplicationRequest, batchNo, user, merchantWithdrawApplication, batchDetailNo);
            // 生产提现记录
            return Triple.of(true, " ", 1);
        }
        
        WechatPayParamsDetails wechatPayParamsDetails = null;
        
        try {
            //查询支付配置详情
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, merchant.getFranchiseeId());
        } catch (Exception e) {
            log.error("review Merchant withdraw application error, get wechat pay params details error, tenantId = {}, franchiseeId={}", tenantId, merchant.getFranchiseeId(), e);
            
            return Triple.of(false, "PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }

        if (Objects.isNull(wechatPayParamsDetails) || Objects.isNull(wechatPayParamsDetails.getFranchiseeId())) {
            return Triple.of(false, "120017", "未配置支付参数");
        }

        // 查询流程信息
        boolean oldProcessFlag = merchantWithdrawOldConfigInfoService.existsMerchantOldWithdrawConfigInfo(wechatPayParamsDetails.getTenantId(), wechatPayParamsDetails.getFranchiseeId());
        Integer type = MerchantWithdrawTypeEnum.OLD.getCode();
        if (!oldProcessFlag) {
            // 新流程
            type = MerchantWithdrawTypeEnum.NEW.getCode();
        }
        
        wechatPayParamsDetails.setMerchantAppletId(merchantConfig.getMerchantAppletId());
        
        // 支付配置类型
        Integer payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_DEFAULT;
        if (!Objects.equals(wechatPayParamsDetails.getFranchiseeId(), NumberConstant.ZERO_L)) {
            payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE;
        }
        

        
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setId(reviewWithdrawApplicationRequest.getId());
        merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
        merchantWithdrawApplicationUpdate.setRemark(reviewWithdrawApplicationRequest.getRemark());
        merchantWithdrawApplicationUpdate.setCheckTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        merchantWithdrawApplicationUpdate.setPayConfigType(payConfigType);
        merchantWithdrawApplicationUpdate.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        merchantWithdrawApplicationUpdate.setType(type);
        
        //若为拒绝提现，则修改提现状态为已拒绝，并且修改提现记录表中的提现状态为已拒绝。
        if (MerchantWithdrawConstant.REVIEW_REFUSED.equals(reviewWithdrawApplicationRequest.getStatus())) {
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_REFUSED);
            merchantWithdrawApplicationUpdate.setRemark(reviewWithdrawApplicationRequest.getRemark());
            
            Integer result = merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);
            //将商户余额表中的提现金额重新加回去
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                    merchantWithdrawApplication.getTenantId().longValue());
            
            return Triple.of(true, null, result);
        }
        
        UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(merchantWithdrawApplication.getUid(), tenantId,UserOauthBind.SOURCE_WX_PRO);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.warn("review Merchant withdraw application warn, not found user auth bind info for merchant user. uid = {}", merchantWithdrawApplication.getUid());
            return Triple.of(false, "120018", "未找到用户的第三方授权信息");
        }
        
        //若为同意提现，则修改提现状态为已审核，并且修改提现记录表中的提现状态为已审核。
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_SUCCESS);
        
        //创建一条批次提现记录，用于记录当前提现申请的批次号。如果为批量提现，则需要创建多条批次记录
        MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
        merchantWithdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
        merchantWithdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
        merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
        //提现发起的批次号
        merchantWithdrawApplicationRecord.setBatchNo(batchNo);
        merchantWithdrawApplicationRecord.setBatchDetailNo(batchDetailNo);
        merchantWithdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
        merchantWithdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
        merchantWithdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecord.setPayConfigType(payConfigType);
        merchantWithdrawApplicationRecord.setFranchiseeId(merchantWithdrawApplication.getFranchiseeId());
        merchantWithdrawApplicationRecord.setType(type);

        if (oldProcessFlag) {
            // 执行旧流程
            return handleOldProcessTransfer(wechatPayParamsDetails, batchNo, merchantWithdrawApplication, userOauthBind, batchDetailNo, merchantWithdrawApplicationUpdate, merchantWithdrawApplicationRecord);
        }

        return handleNewProcessTransfer(wechatPayParamsDetails, batchNo, merchantWithdrawApplication, userOauthBind, batchDetailNo, merchantWithdrawApplicationUpdate, merchantWithdrawApplicationRecord);
    }

    private void offlineTransferHandler(ReviewWithdrawApplicationRequest reviewWithdrawApplicationRequest, String batchNo, TokenUser user, MerchantWithdrawApplication merchantWithdrawApplication, String batchDetailNo) {
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setId(reviewWithdrawApplicationRequest.getId());
        merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
        merchantWithdrawApplicationUpdate.setCheckTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.OFF_LINE_TRANSFER);
        merchantWithdrawApplicationUpdate.setRemark(reviewWithdrawApplicationRequest.getRemark());
        merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

        //创建一条批次提现记录，用于记录当前提现申请的批次号。如果为批量提现，则需要创建多条批次记录
        MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
        merchantWithdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
        merchantWithdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
        // 线下转账
        merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.OFF_LINE_TRANSFER);
        merchantWithdrawApplicationRecord.setBatchNo(batchNo);
        merchantWithdrawApplicationRecord.setBatchDetailNo(batchDetailNo);
        merchantWithdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
        merchantWithdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
        merchantWithdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecord.setPayConfigType(null);
        merchantWithdrawApplicationRecord.setFranchiseeId(merchantWithdrawApplication.getFranchiseeId());
        merchantWithdrawApplicationRecord.setType(1);


        //merchantWithdrawApplicationRecord.setTransactionBatchId(wechatTransferOrderResult.getTransferBillNo());
        //merchantWithdrawApplicationRecord.setTransactionBatchDetailId(wechatTransferOrderResult.getTransferBillNo());

        merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
    }

    /**
     * 新流程转账发起
     * @param wechatPayParamsDetails
     * @param batchNo
     * @param merchantWithdrawApplication
     * @param userOauthBind
     * @param batchDetailNo
     * @param merchantWithdrawApplicationUpdate
     * @param merchantWithdrawApplicationRecord
     * @return
     */
    private Triple<Boolean, String, Object> handleNewProcessTransfer(WechatPayParamsDetails wechatPayParamsDetails, String batchNo, MerchantWithdrawApplication merchantWithdrawApplication, UserOauthBind userOauthBind, String batchDetailNo, MerchantWithdrawApplication merchantWithdrawApplicationUpdate, MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord) {
        // 发起微信第三方提现申请
        WechatTransferOrderRequestV2 wechatTransferOrderQuery = new WechatTransferOrderRequestV2();
        WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails);
        wechatTransferOrderQuery.setCommonRequest(wechatV3CommonRequest);
        wechatTransferOrderQuery.setAppid(wechatPayParamsDetails.getMerchantAppletId());
        //转账批次号
        wechatTransferOrderQuery.setOutBillNo(batchDetailNo);
        wechatTransferOrderQuery.setTransferSceneId(MerchantWithdrawSceneEnum.DISTRIBUTION_REBATE.getCode().toString());
        wechatTransferOrderQuery.setOpenid(userOauthBind.getThirdId());
        wechatTransferOrderQuery.setTransferAmount(merchantWithdrawApplication.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatTransferOrderQuery.setTransferRemark(MerchantWithdrawConstant.WITHDRAW_TRANSFER_REMARK);
        wechatTransferOrderQuery.setNotifyUrl(wechatConfig.getMerchantWithdrawCallBackUrl() + wechatPayParamsDetails.getTenantId() + "/" + wechatPayParamsDetails.getFranchiseeId());
        wechatTransferOrderQuery.setTransferSceneReportInfos(getWechatTransferSceneReportInfos(wechatTransferOrderQuery.getTransferSceneId()));


        Integer result;
        try {
            log.info("wechat transfer for single review start new. request = {}", wechatTransferOrderQuery);
            WechatTransferOrderResultV2 wechatTransferOrderResult = wechatV3TransferInvokeService.transferV2(wechatTransferOrderQuery);
            log.info("wechat response data for single review new, result  = {}", wechatTransferOrderResult);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getTransferBillNo());
                if (Objects.isNull(MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderResult.getState()))) {
                    log.error("wechat transfer for single review new, result  = {}", wechatTransferOrderQuery);
                } else {
                    merchantWithdrawApplicationUpdate.setState(MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderResult.getState()).getCode());
                }
                merchantWithdrawApplicationUpdate.setPackageInfo(wechatTransferOrderResult.getPackageInfo());

                //更新明细批次记录状态为提现中
                merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationRecord.setTransactionBatchId(wechatTransferOrderResult.getTransferBillNo());
                merchantWithdrawApplicationRecord.setTransactionBatchDetailId(wechatTransferOrderResult.getTransferBillNo());
            } else {
                //若返回为空，则调用微信接口失败，将提现状态设置为提现失败。需要商户重新发起提现
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                //merchantWithdrawApplicationUpdate.setRemark();
                merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

                //回滚商户余额表中的提现金额
                merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                        merchantWithdrawApplication.getTenantId().longValue());
            }

            merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
            result = merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            // 检测并且发送通知
            executorService.execute(() -> {
                checkAndSendNotify(merchantWithdrawApplicationUpdate.getState(), merchantWithdrawApplication);
            });

            log.info("wechat transfer for single review new end. batch no = {}", batchNo);
        } catch (WechatPayException e) {
            //throw new RuntimeException(e);
            log.error("transfer amount for merchant withdraw review new error", e);
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

            merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
            merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            //回滚商户余额表中的提现金额
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                    merchantWithdrawApplication.getTenantId().longValue());

            return Triple.of(false, "120019", "提现失败");
        }

        return Triple.of(true, "", result);
    }

    private void checkAndSendNotify(Integer state, MerchantWithdrawApplication merchantWithdrawApplication) {
        // 状态是否用户待确认
        if (!(Objects.equals(state, MerchantWithdrawApplicationStateEnum.WAIT_USER_CONFIRM.getCode()) || Objects.equals(state, MerchantWithdrawApplicationStateEnum.TRANSFERING.getCode()))) {
            return;
        }

        // 发送通知
        MerchantWithdrawSendBO merchantWithdrawSendBO = new MerchantWithdrawSendBO();
        merchantWithdrawSendBO.setUid(merchantWithdrawApplication.getUid());
        merchantWithdrawSendBO.setAmount(merchantWithdrawApplication.getAmount());
        merchantWithdrawSendBO.setCreateTime(merchantWithdrawApplication.getCreateTime());

        sendNotify(merchantWithdrawSendBO, merchantWithdrawApplication.getTenantId());
    }

    private List<WechatTransferSceneReportInfoQuery> getWechatTransferSceneReportInfos(String transferSceneId) {
        List<WechatTransferSceneReportInfoQuery> list = new ArrayList<>();
        if (Objects.equals(transferSceneId, MerchantWithdrawSceneEnum.DISTRIBUTION_REBATE.getCode().toString())) {
            WechatTransferSceneReportInfoQuery wechatTransferSceneReportInfoQueryJob = new WechatTransferSceneReportInfoQuery();
            wechatTransferSceneReportInfoQueryJob.setInfoType(MerchantWithdrawConstant.WITHDRAW_TRANSFER_SCENE_JOB);
            wechatTransferSceneReportInfoQueryJob.setInfoContent(MerchantWithdrawConstant.WITHDRAW_TRANSFER_SCENE_JOB_CONTENT);

            WechatTransferSceneReportInfoQuery wechatTransferSceneReportInfoQueryReward = new WechatTransferSceneReportInfoQuery();
            wechatTransferSceneReportInfoQueryReward.setInfoType(MerchantWithdrawConstant.WITHDRAW_TRANSFER_SCENE_REWARD);
            wechatTransferSceneReportInfoQueryReward.setInfoContent(MerchantWithdrawConstant.WITHDRAW_TRANSFER_REMARK);
            list.add(wechatTransferSceneReportInfoQueryReward);
            list.add(wechatTransferSceneReportInfoQueryJob);
        }

        return list;
    }

    private Triple<Boolean, String, Object> handleOldProcessTransfer(WechatPayParamsDetails wechatPayParamsDetails, String batchNo, MerchantWithdrawApplication merchantWithdrawApplication, UserOauthBind userOauthBind, String batchDetailNo, MerchantWithdrawApplication merchantWithdrawApplicationUpdate, MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord) {
        // 发起微信第三方提现申请
        WechatTransferBatchOrderRequest wechatTransferBatchOrderQuery = new WechatTransferBatchOrderRequest();
        WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails);
        wechatTransferBatchOrderQuery.setCommonRequest(wechatV3CommonRequest);
        wechatTransferBatchOrderQuery.setAppid(wechatPayParamsDetails.getMerchantAppletId());
        //转账批次号
        wechatTransferBatchOrderQuery.setOutBatchNo(batchNo);
        wechatTransferBatchOrderQuery.setTotalAmount(merchantWithdrawApplication.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatTransferBatchOrderQuery.setTotalNum(BigDecimal.ONE.intValue());
        wechatTransferBatchOrderQuery.setBatchName(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);
        wechatTransferBatchOrderQuery.setBatchRemark(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);

        WechatTransferBatchOrderDetailQuery wechatTransferBatchOrderDetailQuery = new WechatTransferBatchOrderDetailQuery();
        wechatTransferBatchOrderDetailQuery.setOpenId(userOauthBind.getThirdId());
        //转账批次单下不同转账明细单的唯一标识
        wechatTransferBatchOrderDetailQuery.setOutDetailNo(batchDetailNo);
        //设置转账金额，单位为分
        wechatTransferBatchOrderDetailQuery.setTransferAmount(merchantWithdrawApplication.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatTransferBatchOrderDetailQuery.setTransferRemark(MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_REMARK_SUFFIX);

        List<WechatTransferBatchOrderDetailQuery> wechatTransferBatchOrderDetailQueryList = new ArrayList<>();
        wechatTransferBatchOrderDetailQueryList.add(wechatTransferBatchOrderDetailQuery);

        wechatTransferBatchOrderQuery.setTransferDetailList(wechatTransferBatchOrderDetailQueryList);

        Integer result;
        try {
            log.info("wechat transfer for single review start. request = {}", wechatTransferBatchOrderQuery);
            WechatTransferOrderResult wechatTransferOrderResult = wechatV3TransferInvokeService.transferBatch(wechatTransferBatchOrderQuery);
            log.info("wechat response data for single review, result = {}", wechatTransferOrderResult);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getBatchId());
                //更新明细批次记录状态为提现中
                merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
            } else {
                //若返回为空，则调用微信接口失败，将提现状态设置为提现失败。需要商户重新发起提现
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                //merchantWithdrawApplicationUpdate.setRemark();
                merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

                //回滚商户余额表中的提现金额
                merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                        merchantWithdrawApplication.getTenantId().longValue());
            }

            merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
            result = merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            log.info("wechat transfer for single review end. batch no = {}", batchNo);
        } catch (WechatPayException e) {
            //throw new RuntimeException(e);
            log.error("transfer amount for merchant withdraw review error", e);
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

            merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
            merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            //回滚商户余额表中的提现金额
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                    merchantWithdrawApplication.getTenantId().longValue());

            return Triple.of(false, "120019", "提现失败");
        }

        return Triple.of(true, "", result);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object>  batchReviewMerchantWithdrawApplication(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "120013", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION_REVIEW + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "000000", "操作频繁");
        }
        
        log.info("batch review withdraw application, request = {}", batchReviewWithdrawApplicationRequest);
        
        //检查入参中的状态是否为同意或者拒绝状态，若为其他状态，则提示错误。
//        if (!MerchantWithdrawConstant.REVIEW_REFUSED.equals(batchReviewWithdrawApplicationRequest.getStatus()) && !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(
//                batchReviewWithdrawApplicationRequest.getStatus())) {
//            log.warn("Illegal parameter warn for approve withdraw application,  status = {}", batchReviewWithdrawApplicationRequest.getStatus());
//            return Triple.of(false, "120014", "参数不合法");
//        }
        
        //检查审批条数是否超过100条，如果超过100条，则提示错误信息
        if (batchReviewWithdrawApplicationRequest.getIds().size() > 100) {
            log.warn("batch handle withdraw record is more than 100, ids = {}", batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120025", "提现申请数量不能超过100条");
        }
        
        List<MerchantWithdrawApplication> merchantWithdrawApplications = merchantWithdrawApplicationMapper.selectListByIds(batchReviewWithdrawApplicationRequest.getIds(),
                tenantId.longValue());
        
        if (CollectionUtils.isEmpty(merchantWithdrawApplications) || !Objects.equals(merchantWithdrawApplications.size(), batchReviewWithdrawApplicationRequest.getIds().size())) {
            log.warn("batch handle withdraw record is not exists, ids = {}", batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120015", "提现申请不存在");
        }
        
        // 检测审核记录中是否存在不同的加盟商提现审核数据
        List<Long> franchiseeIdList = merchantWithdrawApplicationMapper.selectListFranchiseeIdByIds(batchReviewWithdrawApplicationRequest.getIds(), tenantId.longValue());
        if (ObjectUtils.isEmpty(franchiseeIdList)) {
            return Triple.of(false, "120027", "根据加盟商查询提现记录不存在");
        }
        
        // 检测审核的提现订单中是否存在不同加盟商的提现订单
        if (franchiseeIdList.size() > NumberConstant.ONE) {
            log.info("batch handle withdraw record info, franchisee only select one uid={}, ids = {}", user.getUid(), batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120026", "请选择同一加盟商的提现订单进行审核");
        }
        
        Long franchiseeId = franchiseeIdList.get(NumberConstant.ZERO);
        
        // 登录用户的加盟商id和提现订单对应的加盟商id是否相等
        if (ObjectUtils.isNotEmpty(batchReviewWithdrawApplicationRequest.getBindFranchiseeIdList()) && !batchReviewWithdrawApplicationRequest.getBindFranchiseeIdList().contains(franchiseeId)) {
            log.info("batch handle withdraw record info, franchisee id is not equal, user uid={}, franchisee id={}, ids = {}", user.getUid(), franchiseeId, batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }

        //生成提现发起的批次号
        String batchNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH,  user.getUid());

        // 提前处理线下付款
        if (Objects.equals(batchReviewWithdrawApplicationRequest.getStatus(), MerchantWithdrawConstant.OFF_LINE_TRANSFER)){
            List<MerchantWithdrawApplication> list = merchantWithdrawApplications.stream().filter(e -> {
                return Objects.equals(e.getStatus(), MerchantWithdrawConstant.REVIEW_IN_PROGRESS);
            }).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(list)) {
                return Triple.of(false, "120026", "只可选择审核中状态的数据项，请重新选择后操作");
            }

            batchOfflineTransferHandler(batchReviewWithdrawApplicationRequest, tenantId, user, merchantWithdrawApplications, batchNo);
            return Triple.of(true, "", merchantWithdrawApplications.size());
        }

        //查询支付配置详情
        WechatPayParamsDetails wechatPayParamsDetails = null;
        
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
        } catch (Exception e) {
            log.error("review batch merchant withdraw application error, get wechat pay params details error, tenantId = {}, franchiseeId={}", tenantId, franchiseeId, e);
            return Triple.of(false, "PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        
        if (Objects.isNull(wechatPayParamsDetails) || Objects.isNull(wechatPayParamsDetails.getFranchiseeId())) {
            return Triple.of(false, "120017", "未配置支付参数");
        }

        // 查询流程信息
        boolean oldProcessFlag = merchantWithdrawOldConfigInfoService.existsMerchantOldWithdrawConfigInfo(wechatPayParamsDetails.getTenantId(), wechatPayParamsDetails.getFranchiseeId());
        Integer type;
        if (!oldProcessFlag) {
            // 新流程
            type = MerchantWithdrawTypeEnum.NEW.getCode();
        } else {
            type = MerchantWithdrawTypeEnum.OLD.getCode();
        }

        wechatPayParamsDetails.setMerchantAppletId(merchantConfig.getMerchantAppletId());
        
        // 支付配置类型
        Integer payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_DEFAULT;
        if (!Objects.equals(wechatPayParamsDetails.getFranchiseeId(), NumberConstant.ZERO_L)) {
            payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE;
        }
        
        // 过滤已经审核的提现订单
        List<MerchantWithdrawApplication> alreadyReviewList = new ArrayList<>();
        Set<Long> uids = new HashSet<>();
        
        merchantWithdrawApplications.forEach(withdrawRecord -> {
            if (!Objects.equals(withdrawRecord.getStatus(), MerchantWithdrawConstant.REVIEW_IN_PROGRESS)) {
                alreadyReviewList.add(withdrawRecord);
            }
            uids.add(withdrawRecord.getUid());
        });
        
        if (!CollectionUtils.isEmpty(alreadyReviewList)) {
            return Triple.of(false, "120020", "只可选择审核中状态的数据项，请重新选择后操作");
        }

        //根据传入的参数，创建待更新对象
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setStatus(batchReviewWithdrawApplicationRequest.getStatus());
        merchantWithdrawApplicationUpdate.setRemark(batchReviewWithdrawApplicationRequest.getRemark());
        merchantWithdrawApplicationUpdate.setCheckTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setTenantId(tenantId);
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        merchantWithdrawApplicationUpdate.setPayConfigType(payConfigType);
        merchantWithdrawApplicationUpdate.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        merchantWithdrawApplicationUpdate.setType(type);
        
        //若为拒绝提现，则修改提现状态为已拒绝，并且修改提现记录表中的提现状态为已拒绝。
        if (MerchantWithdrawConstant.REVIEW_REFUSED.equals(batchReviewWithdrawApplicationRequest.getStatus())) {
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_REFUSED);
            merchantWithdrawApplicationUpdate.setRemark(batchReviewWithdrawApplicationRequest.getRemark());
            
            //批量修改商户提现记录信息为拒绝状态
            Integer result = merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());
            
            //将商户余额表中的提现金额重新加回去
            List<Long> uidList = uids.stream().collect(Collectors.toList());
            List<MerchantUserAmount> merchantUserAmountList = merchantUserAmountService.queryUserAmountList(uidList, tenantId.longValue());
            if (CollectionUtils.isEmpty(merchantUserAmountList)) {
                return Triple.of(false, "120021", "所选商户账户不存在");
            }
            
            Map<Long, MerchantUserAmount> merchantUserAmountMap = merchantUserAmountList.stream().collect(Collectors.toMap(MerchantUserAmount::getUid, Function.identity()));
            
            merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
                //回退余额
                MerchantUserAmount merchantUserAmount = merchantUserAmountMap.get(merchantWithdrawApplication.getUid());
                if (Objects.nonNull(merchantUserAmount)) {
                    merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                            merchantWithdrawApplication.getTenantId().longValue());
                }

            });
            
            return Triple.of(true, "", result);
        }
        
        //若为同意提现，则修改提现状态为已审核，并且修改提现记录表中的提现状态为已审核。
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_SUCCESS);
        
        //根据提现申请，创建批次提现记录，用于记录当前提现申请的批次号。（注意：若后期放开每个商户每次提现金额超过500元，则这里需要修改。要添加将提现金额按照每条500元拆分，然后再创建多个批次提现记录）
        List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords = new ArrayList<>();

        //设置提现批次号
        merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
        
        //创建转账明细记录
        List<WechatTransferBatchOrderDetailQuery> wechatTransferBatchOrderDetailQueryList = new ArrayList<>();
        
        AtomicInteger suffixId = new AtomicInteger();
        Integer finalPayConfigType = payConfigType;
        merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
            //生成提现明细的批次号
            String batchDetailNo = OrderIdUtil.generateBusinessId(BusinessType.MERCHANT_WITHDRAW_BATCH_DETAIL, merchantWithdrawApplication.getUid()) + suffixId.getAndIncrement();
            MerchantWithdrawApplicationRecord withdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
            withdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
            withdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
            //设置提现发起的批次号
            withdrawApplicationRecord.setBatchNo(batchNo);
            withdrawApplicationRecord.setBatchDetailNo(batchDetailNo);
            withdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
            // 旧流程提现中，新流程审核通过
            withdrawApplicationRecord.setStatus(oldProcessFlag ? MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS : MerchantWithdrawConstant.REVIEW_SUCCESS);
            withdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
            withdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setPayConfigType(finalPayConfigType);
            withdrawApplicationRecord.setFranchiseeId(merchantWithdrawApplication.getFranchiseeId());
            withdrawApplicationRecord.setType(type);
            
            merchantWithdrawApplicationRecords.add(withdrawApplicationRecord);
            
            //创建转账批次明细单
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(merchantWithdrawApplication.getUid(), merchantWithdrawApplication.getTenantId(),UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("batch review Merchant withdraw application warn, not found user auth bind info for merchant user. uid = {}, tenant id = {}",
                        merchantWithdrawApplication.getUid(), merchantWithdrawApplication.getTenantId());
            }
            
            WechatTransferBatchOrderDetailQuery wechatTransferBatchOrderDetailQuery = new WechatTransferBatchOrderDetailQuery();
            if (Objects.nonNull(userOauthBind)) {
                wechatTransferBatchOrderDetailQuery.setOpenId(userOauthBind.getThirdId());
            }
            
            //转账批次单下不同转账明细单的唯一标识
            wechatTransferBatchOrderDetailQuery.setOutDetailNo(batchDetailNo);
            wechatTransferBatchOrderDetailQuery.setTransferAmount(merchantWithdrawApplication.getAmount().multiply(new BigDecimal(100)).intValue());
            wechatTransferBatchOrderDetailQuery.setTransferRemark(MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_REMARK_SUFFIX);
            wechatTransferBatchOrderDetailQueryList.add(wechatTransferBatchOrderDetailQuery);
            
        });

        if (oldProcessFlag) {
            return handleOldProcessBatchTransfer(wechatPayParamsDetails, batchNo, wechatTransferBatchOrderDetailQueryList, merchantWithdrawApplicationUpdate,
                    merchantWithdrawApplicationRecords, merchantWithdrawApplications, batchReviewWithdrawApplicationRequest);
        }

        return handleNewProcessBatchTransfer(merchantWithdrawApplicationRecords, merchantWithdrawApplications, merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest);
    }

    private void batchOfflineTransferHandler(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest, Integer tenantId, TokenUser user, List<MerchantWithdrawApplication> merchantWithdrawApplications, String batchNo) {
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setStatus(batchReviewWithdrawApplicationRequest.getStatus());
        merchantWithdrawApplicationUpdate.setRemark(batchReviewWithdrawApplicationRequest.getRemark());
        merchantWithdrawApplicationUpdate.setCheckTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setTenantId(tenantId);
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        merchantWithdrawApplicationUpdate.setPayConfigType(null);
        merchantWithdrawApplicationUpdate.setWechatMerchantId(null);
        merchantWithdrawApplicationUpdate.setType(1);
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.OFF_LINE_TRANSFER);
        merchantWithdrawApplicationUpdate.setRemark(batchReviewWithdrawApplicationRequest.getRemark());
        //批量修改商户提现记录信息为拒绝状态
        merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());

        AtomicInteger suffixId = new AtomicInteger();
        List<MerchantWithdrawApplicationRecord> recordList = merchantWithdrawApplications.stream().map(merchantWithdrawApplication -> {
            //生成提现明细的批次号
            String batchDetailNo = OrderIdUtil.generateBusinessId(BusinessType.MERCHANT_WITHDRAW_BATCH_DETAIL, merchantWithdrawApplication.getUid()) + suffixId.getAndIncrement();
            MerchantWithdrawApplicationRecord withdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
            withdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
            withdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
            //设置提现发起的批次号
            withdrawApplicationRecord.setBatchNo(batchNo);
            withdrawApplicationRecord.setBatchDetailNo(batchDetailNo);
            withdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
            // 旧流程提现中，新流程审核通过
            withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.OFF_LINE_TRANSFER);
            withdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
            withdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setPayConfigType(null);
            withdrawApplicationRecord.setFranchiseeId(merchantWithdrawApplication.getFranchiseeId());
            withdrawApplicationRecord.setType(1);

            return withdrawApplicationRecord;
        }).collect(Collectors.toList());

        //批量创建批次记录
        merchantWithdrawApplicationRecordService.batchInsert(recordList);
    }

    private Triple<Boolean, String, Object> handleNewProcessBatchTransfer(List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords, List<MerchantWithdrawApplication> merchantWithdrawApplications, MerchantWithdrawApplication merchantWithdrawApplicationUpdate, BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
        //批量创建批次记录
        merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
        Integer result = merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());

        return Triple.of(true, null, result);
    }

    private Triple<Boolean, String, Object> handleOldProcessBatchTransfer(WechatPayParamsDetails wechatPayParamsDetails, String batchNo, List<WechatTransferBatchOrderDetailQuery> wechatTransferBatchOrderDetailQueryList, MerchantWithdrawApplication merchantWithdrawApplicationUpdate,
                                                                          List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords, List<MerchantWithdrawApplication> merchantWithdrawApplications, BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
        //发起微信第三方提现申请
        //创建调用第三方参数信息
        BigDecimal totalAmount = merchantWithdrawApplications.stream().map(MerchantWithdrawApplication::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        WechatTransferBatchOrderRequest wechatTransferBatchOrderQuery = new WechatTransferBatchOrderRequest();
        WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails);
        wechatTransferBatchOrderQuery.setCommonRequest(wechatV3CommonRequest);
        wechatTransferBatchOrderQuery.setAppid(wechatPayParamsDetails.getMerchantAppletId());
        //转账批次号
        wechatTransferBatchOrderQuery.setOutBatchNo(batchNo);
        wechatTransferBatchOrderQuery.setTotalAmount(totalAmount.multiply(new BigDecimal(100)).intValue());
        wechatTransferBatchOrderQuery.setTotalNum(wechatTransferBatchOrderDetailQueryList.size());
        wechatTransferBatchOrderQuery.setBatchName(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);
        wechatTransferBatchOrderQuery.setBatchRemark(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);

        wechatTransferBatchOrderQuery.setTransferDetailList(wechatTransferBatchOrderDetailQueryList);

        Integer result;
        try {
            log.info("wechat transfer for batch review start. request = {}", wechatTransferBatchOrderQuery);
            WechatTransferOrderResult wechatTransferOrderResult = wechatV3TransferInvokeService.transferBatch(wechatTransferBatchOrderQuery);
            log.info("wechat response data for batch review, result = {}", wechatTransferOrderResult);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                //更新提现申请状态为已审核，并且修改提现批次明细记录表中的提现状态为提现中。
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getBatchId());

            } else {
                //若返回为空，调用微信第三方错误，将提现状态设置为提现失败
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

                merchantWithdrawApplicationRecords.forEach(withdrawApplicationRecord -> {
                    withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                });

                //回滚提现金额至提现余额表
                merchantWithdrawApplications.forEach(withdrawApplication -> {
                    merchantUserAmountService.rollBackWithdrawAmount(withdrawApplication.getAmount(), withdrawApplication.getUid(), withdrawApplication.getTenantId().longValue());
                });

            }

            //批量创建批次记录
            merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
            result = merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());

            log.info("wechat transfer for batch review end. batch no = {}", batchNo);

        } catch (WechatPayException e) {
            log.error("batch review merchant withdraw application error, wechat pay exception", e);
            //将提现申请状态为提现失败，并且修改提现批次明细记录表中的提现状态为提现失败。
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

            merchantWithdrawApplicationRecords.forEach(withdrawApplicationRecord -> {
                withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            });

            merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
            merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());

            //回滚提现金额至提现余额表
            merchantWithdrawApplications.forEach(withdrawApplication -> {
                merchantUserAmountService.rollBackWithdrawAmount(withdrawApplication.getAmount(), withdrawApplication.getUid(), withdrawApplication.getTenantId().longValue());
            });

            return Triple.of(false, "120022", "批量提现失败");
        }

        return Triple.of(true, null, result);
    }

    @Override
    public Integer removeMerchantWithdrawApplication(Long id) {
        return null;
    }
    
    @Slave
    @Override
    public Integer countMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        
        return merchantWithdrawApplicationMapper.countByCondition(merchantWithdrawApplicationRequest);
    }
    
    @Slave
    @Override
    public List<MerchantWithdrawApplicationVO> queryMerchantWithdrawApplicationList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        List<MerchantWithdrawApplicationVO> merchantWithdrawApplicationVOList = merchantWithdrawApplicationMapper.selectListByCondition(merchantWithdrawApplicationRequest);
        
        merchantWithdrawApplicationVOList.forEach(merchantWithdrawApplicationVO -> {
            log.info("query merchant withdraw application, result = {}", merchantWithdrawApplicationVO);
            MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord = merchantWithdrawApplicationRecordService.selectByOrderNo(
                    merchantWithdrawApplicationVO.getOrderNo(), merchantWithdrawApplicationVO.getTenantId());
            if (Objects.nonNull(merchantWithdrawApplicationRecord)) {
                merchantWithdrawApplicationVO.setFailReason(MerchantWithdrawConstant.WITHDRAW_FAILED_COMMON_REASON);
                merchantWithdrawApplicationVO.setRealReason(merchantWithdrawApplicationRecord.getRemark());
            }
            
            // 查询加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantWithdrawApplicationVO.getFranchiseeId());
            Optional.ofNullable(franchisee).ifPresent(f -> {
                merchantWithdrawApplicationVO.setFranchiseeName(f.getName());
            });
        });
        
        return merchantWithdrawApplicationVOList;
    }
    
    @Slave
    @Override
    public MerchantWithdrawApplication queryMerchantWithdrawApplication(Long id) {
        return merchantWithdrawApplicationMapper.selectById(id);
    }
    
    @Slave
    @Override
    public BigDecimal sumByStatus(Integer tenantId, Integer status, Long uid) {
        return merchantWithdrawApplicationMapper.sumByStatus(tenantId, status, uid);
    }
    
    @Override
    public void updateMerchantWithdrawStatus() {
        String traceId = IdUtil.simpleUUID();
        MDC.put(CommonConstant.TRACE_ID, traceId);
        
        try {
            //获取审核状态为提现中且审核时间为30分钟前的提现申请记录
            Long checkTime = System.currentTimeMillis() - 30 * 60 * 1000L;
            int offset = 0;
            int size = 200;
            
            while (true) {
                List<MerchantWithdrawApplication> merchantWithdrawApplications = merchantWithdrawApplicationMapper.selectListForWithdrawInProgress(checkTime, offset, size, MerchantWithdrawTypeEnum.OLD.getCode());
                if (CollectionUtils.isEmpty(merchantWithdrawApplications)) {
                    return;
                }
                
                List<String> batchNoList = merchantWithdrawApplications.parallelStream().map(MerchantWithdrawApplication::getBatchNo).collect(Collectors.toList());
                
                // 检测当前批次号是否存在加盟商不同的提现订单如果存在则查询使用的是默认的配置
                List<MerchantWithdrawApplicationBO> merchantWithdrawApplicationBOS = merchantWithdrawApplicationMapper.selectListByBatchNoList(batchNoList);
                if (CollectionUtils.isEmpty(merchantWithdrawApplicationBOS)) {
                    log.warn("Merchant withdraw application update status task warn, withdraw application is empty, batchNoList = {}", batchNoList);
                    return;
                }
                
                // 支付配置类型map
                Map<String, Integer> payConfigTypeMap = new HashMap<>();
                
                Map<String, List<Long>> franchiseeIdMap = merchantWithdrawApplicationBOS.stream().collect(Collectors.groupingBy(MerchantWithdrawApplicationBO::getBatchNo,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().map(MerchantWithdrawApplicationBO::getFranchiseeId).distinct().collect(Collectors.toList()))));
                
                Map<String, String> weChatMerchantIdMap = new HashMap<>();
                
                merchantWithdrawApplicationBOS.stream().forEach(merchantWithdrawApplicationBO -> {
                    payConfigTypeMap.put(merchantWithdrawApplicationBO.getBatchNo(), merchantWithdrawApplicationBO.getPayConfigType());
                    weChatMerchantIdMap.put(merchantWithdrawApplicationBO.getBatchNo(), merchantWithdrawApplicationBO.getWechatMerchantId());
                    
                });
                
                //根据批次号循环调用第三方接口查询提现结果状态
                merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
                    String batchNo = merchantWithdrawApplication.getBatchNo();
                    Integer tenantId = merchantWithdrawApplication.getTenantId();
                    
                    if (Objects.isNull(batchNo)) {
                        return;
                    }
                    
                    if (ObjectUtils.isEmpty(franchiseeIdMap.get(batchNo))) {
                        log.warn("update merchant withdraw status warn!, franchisee id is empty, batch no = {}", batchNo);
                        return;
                    }
                    
                    if (ObjectUtils.isEmpty(payConfigTypeMap.get(batchNo))) {
                        log.warn("update merchant withdraw status warn!, pay config type is empty, batch no = {}", batchNo);
                        return;
                    }
                    
                    Long franchiseeId = MultiFranchiseeConstant.DEFAULT_FRANCHISEE;
                    
                    // 批量提现审核的时候使用的支付配置为加盟商的配置
                    if (Objects.equals(payConfigTypeMap.get(batchNo), MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE)) {
                        franchiseeId = franchiseeIdMap.get(batchNo).get(0);
                    }
                    
                    //查询支付配置详情
                    WechatPayParamsDetails details = null;
                    
                    try {
                        details = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
                    } catch (Exception e) {
                        log.error("update merchant withdraw status error get wechat pay params details error, tenantId = {}, franchiseeId={}", tenantId, franchiseeId, e);
                        return;
                    }
                    
                    if (Objects.isNull(details)) {
                        log.warn("update merchant withdraw status warn! , wechat pay params details is null, batchNo = {}, tenantId = {}, franchiseeId={}", batchNo, tenantId, franchiseeId);
                        return;
                    }
                    
                    // 判断支付配置对应的商户号是否发送改变
                    String wechatMerchantId = weChatMerchantIdMap.get(batchNo);
                    if (StringUtils.isNotEmpty(wechatMerchantId) && (!Objects.equals(wechatMerchantId, details.getWechatMerchantId()) || !Objects.equals(details.getFranchiseeId(),
                            franchiseeId))) {
                        MerchantWithdrawApplication updateWithdrawApplicationUpdate = new MerchantWithdrawApplication();
                        updateWithdrawApplicationUpdate.setBatchNo(batchNo);
                        updateWithdrawApplicationUpdate.setTenantId(tenantId);
                        updateWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
                        // 支付配置发送了改变
                        updateWithdrawApplicationUpdate.setPayConfigWhetherChange(MerchantWithdrawApplicationConstant.PAY_CONFIG_WHETHER_CHANGE_YES);
                        merchantWithdrawApplicationMapper.updatePayConfigWhetherChangeByBatchNo(updateWithdrawApplicationUpdate);
                        
                        log.warn("update merchant withdraw status warn! , wechat merchant id is not equal, batchNo = {}, tenantId = {}, franchiseeId={}, oldWechatMerchantId = {}, newWechatMerchantId", batchNo, tenantId, franchiseeId, merchantWithdrawApplication.getWechatMerchantId(), details.getWechatMerchantId());
                        return;
                    }
                    
                    //转换支付接口调用参数
                    WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(details);
                    
                    //调用第三方接口查询提现结果状态
                    WechatTransferBatchOrderRecordRequest wechatTransferBatchOrderRecordQuery = new WechatTransferBatchOrderRecordRequest();
                    wechatTransferBatchOrderRecordQuery.setBatchId(batchNo);
                    wechatTransferBatchOrderRecordQuery.setNeedQueryDetail(true);
                    wechatTransferBatchOrderRecordQuery.setDetailStatus("ALL");
                    wechatTransferBatchOrderRecordQuery.setCommonRequest(wechatV3CommonRequest);
                    
                    try {
                        WechatTransferBatchOrderQueryResult wechatTransferBatchOrderQueryResult = wechatV3TransferInvokeService.queryTransferBatchOrder(
                                wechatTransferBatchOrderRecordQuery);
                        if (Objects.isNull(wechatTransferBatchOrderQueryResult) && Objects.isNull(wechatTransferBatchOrderQueryResult.getTransferBatch())) {
                            log.info("query batch wechat transfer order info, response is null, batchNo = {}, tenant id = {}, response = {}",
                                    merchantWithdrawApplication.getBatchNo(), merchantWithdrawApplication.getTenantId(), wechatTransferBatchOrderQueryResult);
                            return;
                        }
                        
                        log.info("query batch wechat transfer order result, result = {}, tenant id = {}", wechatTransferBatchOrderQueryResult,
                                merchantWithdrawApplication.getTenantId());
                        //获取该批次记录状态结果
                        WechatTransferBatchOrderQueryCommonResult wechatTransferBatchOrderQueryCommonResult = wechatTransferBatchOrderQueryResult.getTransferBatch();
                        String batchStatus = wechatTransferBatchOrderQueryCommonResult.getBatchStatus();
                        
                        //待更新的提现申请
                        MerchantWithdrawApplication updateWithdrawApplication = new MerchantWithdrawApplication();
                        updateWithdrawApplication.setBatchNo(batchNo);
                        updateWithdrawApplication.setTenantId(tenantId);
                        updateWithdrawApplication.setUpdateTime(System.currentTimeMillis());
                        
                        //如果当前批次结果为已完成，则将提现申请状态修改为提现成功，如果当前批次结果为已关闭，则将提现申请状态修改为提现失败。
                        if (MerchantWithdrawConstant.WECHAT_BATCH_STATUS_FINISHED.equals(batchStatus)) {
                            
                            handleBatchDetailsInfo(batchNo, tenantId, wechatTransferBatchOrderQueryResult);
                            
                        } else if (MerchantWithdrawConstant.WECHAT_BATCH_STATUS_CLOSED.equals(batchStatus)) {
                            //若为关闭状态，则代表等待商户管理员确认付款超过时间限制，或锁订商户资金失败。
                            log.info("batch wechat transfer closed by wechat, batchNo = {}, tenant id = {}", batchNo, tenantId);
                            //更新当前批次提现申请表状态为提现失败
                            updateWithdrawApplication.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                            merchantWithdrawApplicationMapper.updateMerchantWithdrawStatus(updateWithdrawApplication);
                            //merchantWithdrawApplicationMapper.updateApplicationStatusByBatchNo(MerchantWithdrawConstant.WITHDRAW_FAIL, System.currentTimeMillis(), batchNo, tenantId);
                            
                            //更新当前批次提现申请详细中的记状态为提现失败
                            merchantWithdrawApplicationRecordService.updateApplicationRecordStatusByBatchNo(MerchantWithdrawConstant.WITHDRAW_FAIL, batchNo, tenantId);
                            
                            //回滚提现金额至提现余额表
                            List<MerchantWithdrawApplication> merchantWithdrawApplicationList = merchantWithdrawApplicationMapper.selectListByBatchNo(batchNo, tenantId);
                            if (!CollectionUtils.isEmpty(merchantWithdrawApplicationList)) {
                                merchantWithdrawApplicationList.forEach(withdrawApplication -> {
                                    merchantUserAmountService.rollBackWithdrawAmount(withdrawApplication.getAmount(), withdrawApplication.getUid(),
                                            withdrawApplication.getTenantId().longValue());
                                });
                            }
                            
                        } else {
                            //更新查询结果到提现记录表中, 也可以不更新，方便查看转账暂时未成功原因
                            updateWithdrawApplication.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                            merchantWithdrawApplicationMapper.updateMerchantWithdrawStatus(updateWithdrawApplication);
                        }
                        
                    } catch (WechatPayException e) {
                        log.error("query batch wechat transfer order info error, e = {}, params = {}", e, wechatTransferBatchOrderRecordQuery);
                        
                    }
                    
                });
                offset += size;
            }
            
        } catch (Exception e) {
            log.error("update merchant withdraw status error, e = {}", e);
        } finally {
            MDC.clear();
        }
    }
    
    @Slave
    @Override
    public List<MerchantWithdrawApplicationVO> selectRecordList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        List<MerchantWithdrawApplicationVO> merchantWithdrawApplicationVOS = merchantWithdrawApplicationMapper.selectRecordList(merchantWithdrawApplicationRequest);
        if (CollectionUtils.isEmpty(merchantWithdrawApplicationVOS)) {
            return Collections.emptyList();
        }
        
        merchantWithdrawApplicationVOS.stream().forEach(e -> {
            if (StringUtils.isNotEmpty(e.getUid())) {
                User user = userService.queryByUidFromCache(Long.valueOf(e.getUid()));
                e.setPhone(ObjectUtils.isNotEmpty(user) ? user.getPhone() : null);
            }
    
            // 查询加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(e.getFranchiseeId());
            Optional.ofNullable(franchisee).ifPresent(f -> {
                e.setFranchiseeName(f.getName());
            });
        });
        
        return merchantWithdrawApplicationVOS;
    }
    
    @Slave
    @Override
    public Integer selectRecordListCount(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        return merchantWithdrawApplicationMapper.selectRecordListCount(merchantWithdrawApplicationRequest);
    }

    @Override
    @Slave
    public Triple<Boolean, String, Object> getMerchantWithdrawProcess(Long uid) {
        MerchantWithdrawProcessVO merchantWithdrawProcessVO = new MerchantWithdrawProcessVO();
        merchantWithdrawProcessVO.setWithdrawAmountLimit(MerchantWithdrawConstant.WITHDRAW_TRANSFER_DEFAULT);

        //检查商户是否存在
        Merchant queryMerchant = merchantService.queryByUid(uid);
        if (Objects.isNull(queryMerchant)) {
            log.warn("merchant user not found, uid = {}", uid);
            return Triple.of(false, "商户不存在", null);
        }

        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(queryMerchant.getTenantId(), queryMerchant.getFranchiseeId());
        } catch (Exception e) {
            log.error("check merchant old withdraw config error!, get wechat pay params details error, tenantId = {}, franchiseeId={}", queryMerchant.getTenantId(), queryMerchant.getFranchiseeId(), e);
            throw new CustomBusinessException("支付配置有误，请检查相关配置");
        }

        if (Objects.isNull(wechatPayParamsDetails) || Objects.isNull(wechatPayParamsDetails.getFranchiseeId())) {
            log.warn("wechat pay params details is null, tenantId = {}, franchiseeId = {}", queryMerchant.getTenantId(), queryMerchant.getFranchiseeId());
            return Triple.of(false, "120017", "未配置支付参数");
        }

        boolean oldWithdrawConfigInfo = merchantWithdrawOldConfigInfoService.existsMerchantOldWithdrawConfigInfo(wechatPayParamsDetails.getTenantId(), wechatPayParamsDetails.getFranchiseeId());
        merchantWithdrawProcessVO.setType(oldWithdrawConfigInfo ? MerchantWithdrawTypeEnum.OLD.getCode() : MerchantWithdrawTypeEnum.NEW.getCode());

        ElectricityConfigExtra electricityConfigExtra = electricityConfigExtraService.queryByTenantIdFromCache(queryMerchant.getTenantId());
        if (Objects.nonNull(electricityConfigExtra)) {
            merchantWithdrawProcessVO.setWithdrawAmountLimit(electricityConfigExtra.getWithdrawAmountLimit());
        }

        return Triple.of(true, "", merchantWithdrawProcessVO);
    }

    @Override
    @Slave
    public List<MerchantWithdrawSendBO> listAuditSuccess(Integer tenantId, Long size, Long startId, Integer type) {
        return merchantWithdrawApplicationMapper.selectListAuditSuccess(tenantId, size, startId, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> sendTransfer(MerchantWithdrawSendBO merchantWithdrawSendBO, String userThird, WechatPayParamsDetails wechatPayParamsDetails, Integer payConfigType) {
        String batchDetailNo = merchantWithdrawSendBO.getBatchDetailNo();

        // 发起微信第三方提现申请
        WechatTransferOrderRequestV2 wechatTransferOrderQuery = new WechatTransferOrderRequestV2();
        WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(wechatPayParamsDetails);
        wechatTransferOrderQuery.setCommonRequest(wechatV3CommonRequest);
        wechatTransferOrderQuery.setAppid(merchantConfig.getMerchantAppletId());
        //转账批次号
        wechatTransferOrderQuery.setOutBillNo(batchDetailNo);
        wechatTransferOrderQuery.setTransferSceneId(MerchantWithdrawSceneEnum.DISTRIBUTION_REBATE.getCode().toString());
        wechatTransferOrderQuery.setOpenid(userThird);
        wechatTransferOrderQuery.setTransferAmount(merchantWithdrawSendBO.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatTransferOrderQuery.setTransferRemark(MerchantWithdrawConstant.WITHDRAW_TRANSFER_REMARK);
        wechatTransferOrderQuery.setNotifyUrl(wechatConfig.getMerchantWithdrawCallBackUrl() + wechatPayParamsDetails.getTenantId() + "/" + wechatPayParamsDetails.getFranchiseeId());
        wechatTransferOrderQuery.setTransferSceneReportInfos(getWechatTransferSceneReportInfos(wechatTransferOrderQuery.getTransferSceneId()));

        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setId(merchantWithdrawSendBO.getApplicationId());
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setPayConfigType(payConfigType);
        merchantWithdrawApplicationUpdate.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());

        MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecordUpdate = new MerchantWithdrawApplicationRecord();
        merchantWithdrawApplicationRecordUpdate.setId(merchantWithdrawSendBO.getRecordId());
        merchantWithdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
        merchantWithdrawApplicationRecordUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecordUpdate.setPayConfigType(payConfigType);


        Integer result;
        try {
            log.info("wechat transfer for single start new. request = {}", wechatTransferOrderQuery);
            WechatTransferOrderResultV2 wechatTransferOrderResult = wechatV3TransferInvokeService.transferV2(wechatTransferOrderQuery);
            log.info("wechat response data for single new, result  = {}", wechatTransferOrderQuery);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getTransferBillNo());
                if (Objects.isNull(MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderResult.getState()))) {
                    log.error("wechat transfer for single review new, result  = {}", wechatTransferOrderQuery);
                } else {
                    merchantWithdrawApplicationUpdate.setState(MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderResult.getState()).getCode());
                }
                merchantWithdrawApplicationUpdate.setPackageInfo(wechatTransferOrderResult.getPackageInfo());

                //更新明细批次记录状态为提现中
                merchantWithdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationRecordUpdate.setTransactionBatchId(wechatTransferOrderResult.getTransferBillNo());
                merchantWithdrawApplicationRecordUpdate.setTransactionBatchDetailId(wechatTransferOrderResult.getTransferBillNo());
            } else {
                //若返回为空，则调用微信接口失败，将提现状态设置为提现失败。需要商户重新发起提现
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                //merchantWithdrawApplicationUpdate.setRemark();
                merchantWithdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

                //回滚商户余额表中的提现金额
                merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawSendBO.getAmount(), merchantWithdrawSendBO.getUid(),
                        merchantWithdrawSendBO.getTenantId().longValue());
            }

            merchantWithdrawApplicationRecordService.updateById(merchantWithdrawApplicationRecordUpdate);
            result = merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            MerchantWithdrawApplication merchantWithdrawApplication = MerchantWithdrawApplication.builder().amount(merchantWithdrawSendBO.getAmount())
                    .uid(merchantWithdrawSendBO.getUid()).tenantId(merchantWithdrawSendBO.getTenantId()).createTime(merchantWithdrawSendBO.getCreateTime()).build();

            checkAndSendNotify(merchantWithdrawApplicationUpdate.getState(), merchantWithdrawApplication);

            log.info("wechat transfer for single review new end. batchDetailNo = {}", batchDetailNo);
        } catch (WechatPayException e) {
            //throw new RuntimeException(e);
            log.error("transfer amount for merchant withdraw review new task error", e);
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            merchantWithdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);

            merchantWithdrawApplicationRecordService.updateById(merchantWithdrawApplicationRecordUpdate);
            merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);

            //回滚商户余额表中的提现金额
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawSendBO.getAmount(), merchantWithdrawSendBO.getUid(),
                    merchantWithdrawSendBO.getTenantId().longValue());

            return Triple.of(false, "120019", "提现失败");
        }

        return Triple.of(true, "", result);
    }

    @Override
    @Slave
    public List<MerchantWithdrawSendBO> listWithdrawingByMerchantId(Long uid, Long size, Long startId, Long checkTime) {
        return merchantWithdrawApplicationMapper.selectListWithdrawingByMerchantId(uid, size, startId, checkTime);
    }

    @Override
    public Integer batchUpdatePayConfigChangeByIdList(List<Long> idList, Integer payConfigWhetherChangeYes) {
        return merchantWithdrawApplicationMapper.batchUpdatePayConfigChangeByIdList(idList, payConfigWhetherChangeYes, System.currentTimeMillis());
    }

    @Override
    public Integer updateStateById(Long applicationId, Integer state) {
        return merchantWithdrawApplicationMapper.updateStateById(applicationId, state, System.currentTimeMillis());
    }

    @Override
    @Slave
    public MerchantWithdrawApplication queryByOrderNo(String orderNo, String batchNo) {
        return merchantWithdrawApplicationMapper.selectByOrderNo(orderNo, batchNo);
    }

    @Override
    public Integer updateById(MerchantWithdrawApplication merchantWithdrawApplicationUpdate) {
        return merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);
    }

    @Override
    public void sendNotify(MerchantWithdrawSendBO merchantWithdrawSendBO, Integer tenantId) {
        UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(merchantWithdrawSendBO.getUid(), tenantId, UserOauthBind.SOURCE_WX_PRO);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.warn("send notify for merchant withdraw warn, not found user auth bind info for merchant user. uid = {}, batchDetailNo={}", merchantWithdrawSendBO.getUid(), merchantWithdrawSendBO.getBatchDetailNo());
            return;
        }

        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setAppId(merchantConfig.getMerchantAppletId());
        appTemplateQuery.setSecret(merchantConfig.getMerchantAppletSecret());
        appTemplateQuery.setTouser(userOauthBind.getThirdId());
        appTemplateQuery.setTemplateId(merchantConfig.getTemplateId());
        appTemplateQuery.setPage(merchantConfig.getPage());
        appTemplateQuery.setMiniProgramState(merchantConfig.getMiniProgramState());
        appTemplateQuery.setData(this.getData(merchantWithdrawSendBO));

        try {
            weChatAppTemplateService.sendMsg(appTemplateQuery);
        } catch (AlipayApiException e) {
            log.error("send notify for merchant withdraw error! batchDetailNo={}", merchantWithdrawSendBO.getBatchDetailNo(), e);
        }
    }

    @Override
    @Slave
    public Triple<Boolean, String, Object> getConfirmReceiptInfo(Long uid, Long id) {
        //检查商户是否存在
        Merchant queryMerchant = merchantService.queryByUid(uid);
        if (Objects.isNull(queryMerchant)) {
            log.warn("merchant user not found, uid = {}", uid);
            return Triple.of(false, "商户不存在", null);
        }

        MerchantWithdrawApplication merchantWithdrawApplication = merchantWithdrawApplicationMapper.selectById(id);
        if (Objects.isNull(merchantWithdrawApplication)) {
            return Triple.of(false, "120015", "提现申请不存在");
        }

        if (!Objects.equals(queryMerchant.getUid(), uid)) {
            return Triple.of(false, "120016", "提现申请不存在");
        }

        MerchantWithdrawConfirmReceiptVO receiptVO = MerchantWithdrawConfirmReceiptVO.builder().
                packageInfo(merchantWithdrawApplication.getPackageInfo()).mchId(merchantWithdrawApplication.getWechatMerchantId())
                .appId(merchantConfig.getMerchantAppletId()).build();

        return Triple.of(true, "", receiptVO);
    }

    private Map<String, Object> getData(MerchantWithdrawSendBO merchantWithdrawSendBO) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount1", Objects.nonNull(merchantWithdrawSendBO.getAmount()) ? merchantWithdrawSendBO.getAmount().toString() : "");
        data.put("phrase2", MerchantWithdrawConstant.PHRASE2);
        data.put("time3", DateUtil.format(new Date(merchantWithdrawSendBO.getCreateTime()), DateFormatConstant.MONTH_DATE_TIME_FORMAT));
        data.put("thing4", MerchantWithdrawConstant.RECEIVE_REMARK);
        return data;
    }

    public void handleBatchDetailsInfo(String batchNo, Integer tenantId, WechatTransferBatchOrderQueryResult wechatTransferBatchOrderQueryResult) {
        //查询当前批次的明细记录，并查询每条明细的处理结果是否为成功状态，若失败，则记录失败原因。
        List<MerchantWithdrawApplicationRecordBO> merchantWithdrawApplicationRecords = merchantWithdrawApplicationRecordService.selectListByBatchNo(batchNo, tenantId);
        if (CollectionUtils.isEmpty(merchantWithdrawApplicationRecords)) {
            log.info("query batch wechat transfer order detail info, merchant withdraw application record is null, batchNo = {}, tenant id = {}", batchNo, tenantId);
            return;
        }
        
        merchantWithdrawApplicationRecords.forEach(merchantWithdrawApplicationRecord -> {
            WechatTransferOrderRecordRequest wechatTransferOrderRecordQuery = new WechatTransferOrderRecordRequest();
            wechatTransferOrderRecordQuery.setOutBatchNo(merchantWithdrawApplicationRecord.getBatchNo());
            wechatTransferOrderRecordQuery.setOutDetailNo(merchantWithdrawApplicationRecord.getBatchDetailNo());
            
            Long franchiseeId = MultiFranchiseeConstant.DEFAULT_FRANCHISEE;
            
            // 如果支付配置类型为默认，则查询租户的默认配置
            if (Objects.equals(merchantWithdrawApplicationRecord.getPayConfigType(), MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE)) {
                franchiseeId = merchantWithdrawApplicationRecord.getFranchiseeId();
            }
            
            //查询支付配置详情
            WechatPayParamsDetails details = null;
            
            try {
                details = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
            } catch (Exception e) {
                log.error("handle batch withdraw application detail error, get wechat pay params details error, batchNo = {}, tenantId = {}, franchiseeId={}", batchNo, tenantId,
                        franchiseeId, e);
                return;
            }
            
            if (Objects.isNull(details)) {
                log.warn("handle batch withdraw application detail error , wechat pay params details is null, batchNo = {}, tenantId = {}, franchiseeId={}", batchNo, tenantId, franchiseeId);
                return;
            }
            
            //转换支付接口调用参数
            WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(details);
            wechatTransferOrderRecordQuery.setCommonRequest(wechatV3CommonRequest);
            
            try {
                //第三方查询提现结果详细信息
                WechatTransferOrderQueryResult wechatTransferOrderQueryResult = wechatV3TransferInvokeService.queryTransferOrder(wechatTransferOrderRecordQuery);
                
                //并将失败原因更新至提现详细表中
                if (Objects.isNull(wechatTransferOrderQueryResult)) {
                    log.info("query batch wechat transfer order detail info, response is null, batchNo = {}, tenant id = {}", batchNo, tenantId);
                    return;
                }
                
                log.info("query transfer order detail info, result = {}", wechatTransferOrderQueryResult);
                WechatTransferBatchOrderQueryCommonResult wechatTransferBatchOrderQueryCommonResult = wechatTransferBatchOrderQueryResult.getTransferBatch();
                
                String detailStatus = wechatTransferOrderQueryResult.getDetailStatus();
                String failReason = wechatTransferOrderQueryResult.getFailReason();
                
                MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
                merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
                merchantWithdrawApplicationUpdate.setTenantId(tenantId);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferBatchOrderQueryCommonResult.getBatchId());
                merchantWithdrawApplicationUpdate.setOrderNo(merchantWithdrawApplicationRecord.getOrderNo());
                merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
                
                MerchantWithdrawApplicationRecord withdrawApplicationRecordUpdate = new MerchantWithdrawApplicationRecord();
                withdrawApplicationRecordUpdate.setBatchNo(batchNo);
                withdrawApplicationRecordUpdate.setBatchDetailNo(merchantWithdrawApplicationRecord.getBatchDetailNo());
                withdrawApplicationRecordUpdate.setTenantId(tenantId);
                withdrawApplicationRecordUpdate.setUpdateTime(System.currentTimeMillis());
                withdrawApplicationRecordUpdate.setTransactionBatchId(wechatTransferOrderQueryResult.getBatchId());
                withdrawApplicationRecordUpdate.setTransactionBatchDetailId(wechatTransferOrderQueryResult.getDetailId());
                
                if (MerchantWithdrawConstant.WECHAT_BATCH_DETAIL_STATUS_SUCCESS.equals(detailStatus)) {
                    //更新单条提现申请和单条详细记录为提现成功状态
                    merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_SUCCESS);
                    merchantWithdrawApplicationUpdate.setReceiptTime(System.currentTimeMillis());
                    withdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_SUCCESS);
                    
                } else if (MerchantWithdrawConstant.WECHAT_BATCH_DETAIL_STATUS_FAIL.equals(detailStatus)) {
                    //更新单条提现申请和单条详细记录为提现失败状态
                    merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                    withdrawApplicationRecordUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                    withdrawApplicationRecordUpdate.setRemark(failReason);
                    
                    //失败则需回滚提现金额至提现余额表
                    merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplicationRecord.getAmount(), merchantWithdrawApplicationRecord.getUid(),
                            merchantWithdrawApplicationRecord.getTenantId().longValue());
                }
                
                //更新提现记录表以及详细表
                merchantWithdrawApplicationMapper.updateMerchantWithdrawStatus(merchantWithdrawApplicationUpdate);
                merchantWithdrawApplicationRecordService.updateMerchantWithdrawRecordStatus(withdrawApplicationRecordUpdate);
                
            } catch (WechatPayException e) {
                log.error("query wechat transfer order detail info error,  params = {}", wechatTransferOrderRecordQuery, e);
            }
            
        });
        
    }
}
