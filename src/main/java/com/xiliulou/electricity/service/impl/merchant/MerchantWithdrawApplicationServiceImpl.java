package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.util.IdUtil;
import com.huaweicloud.sdk.core.utils.JsonUtils;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawConstant;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationMapper;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationRecordService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import com.xiliulou.pay.weixinv3.dto.WechatTransferBatchOrderQueryCommonResult;
import com.xiliulou.pay.weixinv3.dto.WechatTransferBatchOrderQueryResult;
import com.xiliulou.pay.weixinv3.dto.WechatTransferOrderQueryResult;
import com.xiliulou.pay.weixinv3.dto.WechatTransferOrderResult;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatTransferBatchOrderDetailQuery;
import com.xiliulou.pay.weixinv3.query.WechatTransferBatchOrderQuery;
import com.xiliulou.pay.weixinv3.query.WechatTransferBatchOrderRecordQuery;
import com.xiliulou.pay.weixinv3.query.WechatTransferOrderRecordQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3TransferService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private WechatV3TransferService wechatV3TransferService;
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
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
            log.error("merchant user not found, uid = {}", merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120003", "商户不存在");
        }
        
        //查询商户余额表，是否存在商户账户
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(merchantWithdrawApplicationRequest.getUid());
        if (Objects.isNull(merchantUserAmount)) {
            log.error("merchant user balance account not found, uid = {}", merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120010", "商户余额账户不存在");
        }
        
        //单词提现金额限制：大于1， 小于等于500
        if (merchantWithdrawApplicationRequest.getAmount().compareTo(BigDecimal.ONE) < 0
                || merchantWithdrawApplicationRequest.getAmount().compareTo(new BigDecimal(MerchantWithdrawConstant.WITHDRAW_MAX_AMOUNT)) > 0) {
            return Triple.of(false, "120011", "单次提现金额范围（1-500）");
        }
        
        //检查余额表中的余额是否满足提现金额
        if (merchantUserAmount.getBalance().compareTo(merchantWithdrawApplicationRequest.getAmount()) < 0) {
            log.error("merchant user balance amount not enough, amount = {}, uid = {}", merchantWithdrawApplicationRequest.getAmount(),
                    merchantWithdrawApplicationRequest.getUid());
            return Triple.of(false, "120012", "提现金额不足");
        }
        
        //查询银行卡信息，检查银行卡是否存在，并且检查该银行卡是否支持转账，若为微信转账，则不需要银行卡信息
        //计算手续费， 微信申请无手续费，若为其他方式提现，则需要考虑
        
        //插入提现表
        MerchantWithdrawApplication merchantWithdrawApplication = new MerchantWithdrawApplication();
        merchantWithdrawApplication.setAmount(merchantWithdrawApplicationRequest.getAmount());
        merchantWithdrawApplication.setUid(merchantWithdrawApplicationRequest.getUid());
        merchantWithdrawApplication.setTenantId(merchantWithdrawApplicationRequest.getTenantId());
        merchantWithdrawApplication.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW, merchantWithdrawApplicationRequest.getUid()));
        merchantWithdrawApplication.setWithdrawType(MerchantWithdrawConstant.WITHDRAW_TYPE_WECHAT);
        merchantWithdrawApplication.setStatus(MerchantWithdrawConstant.REVIEW_IN_PROGRESS);
        merchantWithdrawApplication.setDelFlag(CommonConstant.DEL_N);
        merchantWithdrawApplication.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setUpdateTime(System.currentTimeMillis());
        
        Integer result = merchantWithdrawApplicationMapper.insertOne(merchantWithdrawApplication);
        
        //扣除商户账户余额表中的余额
        merchantUserAmountService.withdrawAmount(merchantWithdrawApplicationRequest.getAmount(), merchantWithdrawApplicationRequest.getUid(),
                merchantWithdrawApplicationRequest.getTenantId().longValue());
        
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
        if (!MerchantWithdrawConstant.REVIEW_REFUSED.equals(reviewWithdrawApplicationRequest.getStatus()) && !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(
                reviewWithdrawApplicationRequest.getStatus())) {
            log.error("Illegal parameter error for approve withdraw application,  status = {}", reviewWithdrawApplicationRequest.getStatus());
            return Triple.of(false, "120014", "参数不合法");
        }
        
        //检查提现审核参数状态，是否为待审核状态
        MerchantWithdrawApplication merchantWithdrawApplication = merchantWithdrawApplicationMapper.selectById(reviewWithdrawApplicationRequest.getId());
        if (Objects.isNull(merchantWithdrawApplication)) {
            return Triple.of(false, "120015", "提现申请不存在");
        }
        
        //检查提现状态是否为审核中的状态
        if (!MerchantWithdrawConstant.REVIEW_IN_PROGRESS.equals(merchantWithdrawApplication.getStatus())) {
            return Triple.of(false, "120016", "不能重复审核");
        }
        
        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("review Merchant withdraw application error, not found pay params for tenant. tenantId = {}", tenantId);
            return Triple.of(false, "120017", "未配置支付参数");
        }
        
        //生成提现批次单号
        String batchNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH, user.getUid());
        String batchDetailNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH_DETAIL, user.getUid());
        
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setId(reviewWithdrawApplicationRequest.getId());
        merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
        merchantWithdrawApplicationUpdate.setRemark(reviewWithdrawApplicationRequest.getRemark());
        merchantWithdrawApplicationUpdate.setCheckTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        
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
    
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(merchantWithdrawApplication.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("review Merchant withdraw application error, not found user auth bind info for merchant user. uid = {}", merchantWithdrawApplication.getUid());
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
        
        //TODO 发起微信第三方提现申请
        WechatTransferBatchOrderQuery wechatTransferBatchOrderQuery = new WechatTransferBatchOrderQuery();
        wechatTransferBatchOrderQuery.setAppid(electricityPayParams.getMerchantAppletId());
        //转账批次号
        wechatTransferBatchOrderQuery.setOutBatchNo(batchNo);
        wechatTransferBatchOrderQuery.setTotalAmount(merchantWithdrawApplication.getAmount().multiply(new BigDecimal(100)).intValue());
        wechatTransferBatchOrderQuery.setTotalNum(BigDecimal.ONE.intValue());
        wechatTransferBatchOrderQuery.setTenantId(merchantWithdrawApplication.getTenantId());
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
            WechatTransferOrderResult wechatTransferOrderResult = wechatV3TransferService.transferBatch(wechatTransferBatchOrderQuery);
            log.info("wechat response data for single review, result = {}", wechatTransferOrderResult);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getBatchId());
                merchantWithdrawApplicationUpdate.setResponse(JsonUtil.toJson(wechatTransferOrderResult));
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
            log.error("transfer amount for merchant withdraw review error, e = {}", e);
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            merchantWithdrawApplicationUpdate.setResponse(e.getMessage());
            merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            
            merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
            merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);
            
            //回滚商户余额表中的提现金额
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(),
                    merchantWithdrawApplication.getTenantId().longValue());
            
            return Triple.of(false, "120019", "提现失败");
        }
        
        return Triple.of(true, null, result);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> batchReviewMerchantWithdrawApplication(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
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
        if (!MerchantWithdrawConstant.REVIEW_REFUSED.equals(batchReviewWithdrawApplicationRequest.getStatus()) && !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(
                batchReviewWithdrawApplicationRequest.getStatus())) {
            log.error("Illegal parameter error for approve withdraw application,  status = {}", batchReviewWithdrawApplicationRequest.getStatus());
            return Triple.of(false, "120014", "参数不合法");
        }
        
        //检查审批条数是否超过100条，如果超过100条，则提示错误信息
        if (batchReviewWithdrawApplicationRequest.getIds().size() > 100) {
            log.error("batch handle withdraw record is more than 100, ids = {}", batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120025", "提现申请数量不能超过100条");
        }
        
        List<MerchantWithdrawApplication> merchantWithdrawApplications = merchantWithdrawApplicationMapper.selectListByIds(batchReviewWithdrawApplicationRequest.getIds(),
                tenantId.longValue());
        
        if (CollectionUtils.isEmpty(merchantWithdrawApplications) || !Objects.equals(merchantWithdrawApplications.size(), batchReviewWithdrawApplicationRequest.getIds().size())) {
            log.error("batch handle withdraw record is not exists, ids = {}", batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "120015", "提现申请不存在");
        }
        
        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("review Merchant withdraw application error, not found pay params for tenant. tenantId = {}", tenantId);
            return Triple.of(false, "120017", "未配置支付参数");
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
        //生成提现发起的批次号
        String batchNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH, merchantWithdrawApplicationUpdate.getOperator());
        BigDecimal totalAmount = merchantWithdrawApplications.stream().map(MerchantWithdrawApplication::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //设置提现批次号
        merchantWithdrawApplicationUpdate.setBatchNo(batchNo);
        
        //创建转账明细记录
        List<WechatTransferBatchOrderDetailQuery> wechatTransferBatchOrderDetailQueryList = new ArrayList<>();
    
        AtomicInteger suffixId = new AtomicInteger();
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
            withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
            withdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
            withdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
            
            merchantWithdrawApplicationRecords.add(withdrawApplicationRecord);
            
            //创建转账批次明细单
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(merchantWithdrawApplication.getUid(), merchantWithdrawApplication.getTenantId());
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("batch review Merchant withdraw application error, not found user auth bind info for merchant user. uid = {}, tenant id = {}",
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
        
        //发起微信第三方提现申请
        //创建调用第三方参数信息
        WechatTransferBatchOrderQuery wechatTransferBatchOrderQuery = new WechatTransferBatchOrderQuery();
        wechatTransferBatchOrderQuery.setAppid(electricityPayParams.getMerchantAppletId());
        //转账批次号
        wechatTransferBatchOrderQuery.setOutBatchNo(batchNo);
        wechatTransferBatchOrderQuery.setTotalAmount(totalAmount.multiply(new BigDecimal(100)).intValue());
        wechatTransferBatchOrderQuery.setTotalNum(wechatTransferBatchOrderDetailQueryList.size());
        wechatTransferBatchOrderQuery.setTenantId(tenantId);
        wechatTransferBatchOrderQuery.setBatchName(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);
        wechatTransferBatchOrderQuery.setBatchRemark(
                DateUtils.getYearAndMonthAndDayByTimeStamps(System.currentTimeMillis()) + MerchantWithdrawConstant.WECHAT_TRANSFER_BATCH_NAME_SUFFIX);
        
        wechatTransferBatchOrderQuery.setTransferDetailList(wechatTransferBatchOrderDetailQueryList);
        
        Integer result;
        try {
            log.info("wechat transfer for batch review start. request = {}", wechatTransferBatchOrderQuery);
            WechatTransferOrderResult wechatTransferOrderResult = wechatV3TransferService.transferBatch(wechatTransferBatchOrderQuery);
            log.info("wechat response data for batch review, result = {}", wechatTransferOrderResult);
            if (Objects.nonNull(wechatTransferOrderResult)) {
                //更新提现申请状态为已审核，并且修改提现批次明细记录表中的提现状态为提现中。
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
                merchantWithdrawApplicationUpdate.setTransactionBatchId(wechatTransferOrderResult.getBatchId());
                merchantWithdrawApplicationUpdate.setResponse(JsonUtil.toJson(wechatTransferOrderResult));
                
            } else {
                //若返回为空，调用微信第三方错误，将提现状态设置为提现失败
                merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                
                merchantWithdrawApplicationRecords.forEach(withdrawApplicationRecord -> {
                    withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
                });
    
                //回滚提现金额至提现余额表
                merchantWithdrawApplications.forEach(withdrawApplication -> {
                    merchantUserAmountService.rollBackWithdrawAmount(withdrawApplication.getAmount(), withdrawApplication.getUid(),
                            withdrawApplication.getTenantId().longValue());
                });
                
            }
    
            //批量创建批次记录
            merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
            result = merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());
            
            log.info("wechat transfer for batch review end. batch no = {}", batchNo);
            
        } catch (WechatPayException e) {
            log.error("batch review merchant withdraw application error, wechat pay exception. e = {}", e);
            //将提现申请状态为提现失败，并且修改提现批次明细记录表中的提现状态为提现失败。
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            merchantWithdrawApplicationUpdate.setResponse(e.getMessage());
            
            merchantWithdrawApplicationRecords.forEach(withdrawApplicationRecord -> {
                withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_FAIL);
            });
            
            merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
            merchantWithdrawApplicationMapper.updateByIds(merchantWithdrawApplicationUpdate, batchReviewWithdrawApplicationRequest.getIds());
            
            //回滚提现金额至提现余额表
            merchantWithdrawApplications.forEach(withdrawApplication -> {
                merchantUserAmountService.rollBackWithdrawAmount(withdrawApplication.getAmount(), withdrawApplication.getUid(),
                        withdrawApplication.getTenantId().longValue());
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
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMerchantWithdrawStatus() {
        String traceId = IdUtil.simpleUUID();
        MDC.put(CommonConstant.TRACE_ID, traceId);
        
        log.info("Merchant withdraw application update status task start. trace id = {}", traceId);
        
        try {
            //获取审核状态为提现中且审核时间为30分钟前的提现申请记录
            Long checkTime = System.currentTimeMillis() - 30 * 60 * 1000L;
            int offset = 0;
            int size = 200;
            
            while (true) {
                List<MerchantWithdrawApplication> merchantWithdrawApplications = merchantWithdrawApplicationMapper.selectListForWithdrawInProgress(checkTime, offset, size);
                if (CollectionUtils.isEmpty(merchantWithdrawApplications)) {
                    return;
                }
                
                //根据批次号循环调用第三方接口查询提现结果状态
                merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
                    String batchNo = merchantWithdrawApplication.getBatchNo();
                    Integer tenantId = merchantWithdrawApplication.getTenantId();
                    
                    if (Objects.isNull(batchNo)) {
                        return;
                    }
                    
                    //调用第三方接口查询提现结果状态
                    WechatTransferBatchOrderRecordQuery wechatTransferBatchOrderRecordQuery = new WechatTransferBatchOrderRecordQuery();
                    wechatTransferBatchOrderRecordQuery.setBatchId(batchNo);
                    wechatTransferBatchOrderRecordQuery.setTenantId(tenantId);
                    wechatTransferBatchOrderRecordQuery.setNeedQueryDetail(true);
                    wechatTransferBatchOrderRecordQuery.setDetailStatus("ALL");
                    
                    try {
                        WechatTransferBatchOrderQueryResult wechatTransferBatchOrderQueryResult = wechatV3TransferService.queryTransferBatchOrder(
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
                        updateWithdrawApplication.setResponse(JsonUtil.toJson(wechatTransferBatchOrderQueryResult));
                        
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
                        log.error("query batch wechat transfer order info error, e = {}", e);
                        
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
        });
        
        return merchantWithdrawApplicationVOS;
    }
    
    @Override
    public Integer selectRecordListCount(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        return merchantWithdrawApplicationMapper.selectRecordListCount(merchantWithdrawApplicationRequest);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleBatchDetailsInfo(String batchNo, Integer tenantId, WechatTransferBatchOrderQueryResult wechatTransferBatchOrderQueryResult) {
        //查询当前批次的明细记录，并查询每条明细的处理结果是否为成功状态，若失败，则记录失败原因。
        List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords = merchantWithdrawApplicationRecordService.selectListByBatchNo(batchNo, tenantId);
        if (CollectionUtils.isEmpty(merchantWithdrawApplicationRecords)) {
            log.info("query batch wechat transfer order detail info, merchant withdraw application record is null, batchNo = {}, tenant id = {}", batchNo, tenantId);
            return;
        }
        
        merchantWithdrawApplicationRecords.forEach(merchantWithdrawApplicationRecord -> {
            WechatTransferOrderRecordQuery wechatTransferOrderRecordQuery = new WechatTransferOrderRecordQuery();
            wechatTransferOrderRecordQuery.setOutBatchNo(merchantWithdrawApplicationRecord.getBatchNo());
            wechatTransferOrderRecordQuery.setOutDetailNo(merchantWithdrawApplicationRecord.getBatchDetailNo());
            wechatTransferOrderRecordQuery.setTenantId(merchantWithdrawApplicationRecord.getTenantId());
            
            try {
                //第三方查询提现结果详细信息
                WechatTransferOrderQueryResult wechatTransferOrderQueryResult = wechatV3TransferService.queryTransferOrder(wechatTransferOrderRecordQuery);
                
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
                merchantWithdrawApplicationUpdate.setResponse(JsonUtils.toJSON(wechatTransferBatchOrderQueryResult));
                merchantWithdrawApplicationUpdate.setOrderNo(merchantWithdrawApplicationRecord.getOrderNo());
                merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
                
                MerchantWithdrawApplicationRecord withdrawApplicationRecordUpdate = new MerchantWithdrawApplicationRecord();
                withdrawApplicationRecordUpdate.setBatchNo(batchNo);
                withdrawApplicationRecordUpdate.setBatchDetailNo(merchantWithdrawApplicationRecord.getBatchDetailNo());
                withdrawApplicationRecordUpdate.setTenantId(tenantId);
                withdrawApplicationRecordUpdate.setUpdateTime(System.currentTimeMillis());
                withdrawApplicationRecordUpdate.setTransactionBatchId(wechatTransferOrderQueryResult.getBatchId());
                withdrawApplicationRecordUpdate.setTransactionBatchDetailId(wechatTransferOrderQueryResult.getDetailId());
                withdrawApplicationRecordUpdate.setResponse(JsonUtils.toJSON(wechatTransferOrderQueryResult));
                
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
                log.error("query wechat transfer order detail info error, e = {}", e);
            }
            
        });
        
    }
}
