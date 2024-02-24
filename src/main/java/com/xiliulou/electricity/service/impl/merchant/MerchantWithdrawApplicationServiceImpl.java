package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawConstant;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationMapper;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationRecordService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest) {
        
        //限频
        Boolean getLockSuccess = redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION + merchantWithdrawApplicationRequest.getMerchantUid(), "1", 3L, false);
        if (!getLockSuccess) {
            return Triple.of(false, null, "操作频繁,请稍后再试");
        }

        //检查商户是否存在
        Merchant queryMerchant = merchantService.queryByUid(merchantWithdrawApplicationRequest.getMerchantUid());
        if(Objects.isNull(queryMerchant)){
            return Triple.of(false, "", "商户不存在");
        }
    
        //查询商户余额表，是否存在商户账户
        MerchantUserAmount merchantUserAmount = merchantUserAmountService.queryByUid(merchantWithdrawApplicationRequest.getMerchantUid());
        if(Objects.isNull(merchantUserAmount)){
            return Triple.of(false, "", "商户余额账户不存在");
        }
        
        //单词提现金额限制：大于0， 小于等于500
        if(merchantWithdrawApplicationRequest.getAmount().compareTo(BigDecimal.ZERO) < 0 || merchantWithdrawApplicationRequest.getAmount().compareTo(new BigDecimal(MerchantWithdrawConstant.WITHDRAW_MAX_AMOUNT)) > 0){
            return Triple.of(false, "", "单次提现金额范围（0-500）");
        }
        
        //检查余额表中的余额是否满足提现金额
        if(merchantUserAmount.getBalance().compareTo(merchantWithdrawApplicationRequest.getAmount()) < 0){
            return Triple.of(false, "", "提现金额不足");
        }

        //查询银行卡信息，检查银行卡是否存在，并且检查该银行卡是否支持转账，若为微信转账，则不需要银行卡信息
        //计算手续费， 微信申请无手续费，若为其他方式提现，则需要考虑

        //插入提现表
        MerchantWithdrawApplication merchantWithdrawApplication = new MerchantWithdrawApplication();
        merchantWithdrawApplication.setAmount(merchantWithdrawApplicationRequest.getAmount());
        merchantWithdrawApplication.setUid(merchantWithdrawApplicationRequest.getMerchantUid());
        merchantWithdrawApplication.setTenantId(merchantWithdrawApplicationRequest.getTenantId());
        merchantWithdrawApplication.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW, merchantWithdrawApplicationRequest.getMerchantUid()));
        merchantWithdrawApplication.setWithdrawType(MerchantWithdrawConstant.WITHDRAW_TYPE_WECHAT);
        merchantWithdrawApplication.setStatus(MerchantWithdrawConstant.REVIEW_IN_PROGRESS);
        merchantWithdrawApplication.setDelFlag(CommonConstant.DEL_N);
        merchantWithdrawApplication.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplication.setUpdateTime(System.currentTimeMillis());
        
        Integer result = merchantWithdrawApplicationMapper.insertOne(merchantWithdrawApplication);

        //扣除商户账户余额表中的余额
        merchantUserAmountService.withdrawAmount(merchantWithdrawApplicationRequest.getAmount(), merchantWithdrawApplicationRequest.getMerchantUid(), merchantWithdrawApplicationRequest.getTenantId().longValue());
        
        return Triple.of(true, null, result);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> reviewMerchantWithdrawApplication(ReviewWithdrawApplicationRequest reviewWithdrawApplicationRequest) {
    
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION_REVIEW + reviewWithdrawApplicationRequest.getId(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "", "操作频繁");
        }
    
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return  Triple.of(false, "", "未找到用户");
        }
        
        //检查入参中的状态是否为同意或者拒绝状态，若为其他状态，则提示错误。
        if(!MerchantWithdrawConstant.REVIEW_REFUSED.equals(reviewWithdrawApplicationRequest.getStatus()) || !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(reviewWithdrawApplicationRequest.getStatus())){
            log.error("Illegal parameter error for approve withdraw application,  status = {}", reviewWithdrawApplicationRequest.getStatus());
            return Triple.of(false, "", "参数不合法");
        }

        //检查提现审核参数状态，是否为待审核状态
        MerchantWithdrawApplication merchantWithdrawApplication = merchantWithdrawApplicationMapper.selectById(reviewWithdrawApplicationRequest.getId());
        if(Objects.isNull(merchantWithdrawApplication)){
            return Triple.of(false, "", "提现申请不存在");
        }

        //检查提现状态是否为审核中的状态
        if (!MerchantWithdrawConstant.REVIEW_IN_PROGRESS.equals(merchantWithdrawApplication.getStatus())) {
            return Triple.of(false, "", "不能重复审核");
        }
    
        MerchantWithdrawApplication merchantWithdrawApplicationUpdate = new MerchantWithdrawApplication();
        merchantWithdrawApplicationUpdate.setId(reviewWithdrawApplicationRequest.getId());
        merchantWithdrawApplicationUpdate.setUpdateTime(System.currentTimeMillis());
        merchantWithdrawApplicationUpdate.setOperator(user.getUid());
        
        //若为拒绝提现，则修改提现状态为已拒绝，并且修改提现记录表中的提现状态为已拒绝。
        if(MerchantWithdrawConstant.REVIEW_REFUSED.equals(reviewWithdrawApplicationRequest.getStatus())){
            merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_REFUSED);
            merchantWithdrawApplicationUpdate.setRemark(reviewWithdrawApplicationRequest.getRemark());
           
            Integer result = merchantWithdrawApplicationMapper.updateOne(merchantWithdrawApplicationUpdate);
            //将商户余额表中的提现金额重新加回去
            merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(), merchantWithdrawApplication.getTenantId().longValue());
            
            return Triple.of(true, null, result);
        }
        
        //若为同意提现，则修改提现状态为已审核，并且修改提现记录表中的提现状态为已审核。
        merchantWithdrawApplicationUpdate.setStatus(MerchantWithdrawConstant.REVIEW_SUCCESS);

        //创建一条批次提现记录，用于记录当前提现申请的批次号。如果为批量提现，则需要创建多条批次记录
        MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
        merchantWithdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
        merchantWithdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
        //提现发起的批次号
        merchantWithdrawApplicationRecord.setBatchNo(OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH, user.getUid()));
        merchantWithdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
        merchantWithdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
        merchantWithdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
        merchantWithdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
        merchantWithdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
        
        merchantWithdrawApplicationRecordService.insertOne(merchantWithdrawApplicationRecord);
        
        //TODO 发起微信第三方提现申请

        return null;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> batchReviewMerchantWithdrawApplication(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return  Triple.of(false, "", "未找到用户");
        }
    
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_WITHDRAW_APPLICATION_REVIEW + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "", "操作频繁");
        }
    
        //检查入参中的状态是否为同意或者拒绝状态，若为其他状态，则提示错误。
        if(!MerchantWithdrawConstant.REVIEW_REFUSED.equals(batchReviewWithdrawApplicationRequest.getStatus()) || !MerchantWithdrawConstant.REVIEW_SUCCESS.equals(batchReviewWithdrawApplicationRequest.getStatus())){
            log.error("Illegal parameter error for approve withdraw application,  status = {}", batchReviewWithdrawApplicationRequest.getStatus());
            return Triple.of(false, "", "参数不合法");
        }
    
        List<MerchantWithdrawApplication> merchantWithdrawApplications = merchantWithdrawApplicationMapper.selectListByIds(batchReviewWithdrawApplicationRequest.getIds(), tenantId.longValue());
    
        if (CollectionUtils.isEmpty(merchantWithdrawApplications) || !Objects.equals(merchantWithdrawApplications.size(), batchReviewWithdrawApplicationRequest.getIds().size())) {
            log.error("batch handle withdraw record is not exists, ids = {}", batchReviewWithdrawApplicationRequest.getIds());
            return Triple.of(false, "", "提现记录不存在");
        }
    
        // 过滤已经审核的提现订单
        List<MerchantWithdrawApplication> alreadyAuditList = new ArrayList<>();
    
        Set<Long> uidList = new HashSet<>();
    
        merchantWithdrawApplications.forEach(withdrawRecord -> {
            if (!Objects.equals(withdrawRecord.getStatus(), MerchantWithdrawConstant.REVIEW_IN_PROGRESS)) {
                alreadyAuditList.add(withdrawRecord);
            }
            uidList.add(withdrawRecord.getUid());
        });
    
        if (!CollectionUtils.isEmpty(alreadyAuditList)) {
            return Triple.of(false, "", "网络不佳，请刷新重试操作");
        }
    
        batchReviewWithdrawApplicationRequest.setCheckTime(System.currentTimeMillis());
        batchReviewWithdrawApplicationRequest.setUpdateTime(System.currentTimeMillis());
        batchReviewWithdrawApplicationRequest.setTenantId(tenantId);
        batchReviewWithdrawApplicationRequest.setOperator(user.getUid());
    
        //若为拒绝提现，则修改提现状态为已拒绝，并且修改提现记录表中的提现状态为已拒绝。
        if(MerchantWithdrawConstant.REVIEW_REFUSED.equals(batchReviewWithdrawApplicationRequest.getStatus())){
            batchReviewWithdrawApplicationRequest.setStatus(MerchantWithdrawConstant.REVIEW_REFUSED);
            batchReviewWithdrawApplicationRequest.setRemark(batchReviewWithdrawApplicationRequest.getRemark());
            
            //批量修改商户提现记录信息为拒绝状态
            Integer result = merchantWithdrawApplicationMapper.updateByIds(batchReviewWithdrawApplicationRequest);
            
            //TODO 将商户余额表中的提现金额重新加回去
            List<MerchantUserAmount> merchantUserAmountList = null; //merchantUserAmountService.queryList();
            if(CollectionUtils.isEmpty(merchantUserAmountList)){
                return Triple.of(false, "", "所选商户账户不存在");
            }
    
            Map<Long, MerchantUserAmount> merchantUserAmountMap = merchantUserAmountList.stream().collect(Collectors.toMap(MerchantUserAmount::getUid, Function.identity()));
    
            merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
                //回退余额
                MerchantUserAmount merchantUserAmount = merchantUserAmountMap.get(merchantWithdrawApplication.getUid());
                if(Objects.nonNull(merchantUserAmount)){
                    merchantUserAmountService.rollBackWithdrawAmount(merchantWithdrawApplication.getAmount(), merchantWithdrawApplication.getUid(), merchantWithdrawApplication.getTenantId().longValue());
                }
                
            });
            
            return Triple.of(true, "", result);
        }
        
        //若为同意提现，则修改提现状态为已审核，并且修改提现记录表中的提现状态为已审核。
        batchReviewWithdrawApplicationRequest.setStatus(MerchantWithdrawConstant.REVIEW_SUCCESS);
    
        //根据提现申请，创建批次提现记录，用于记录当前提现申请的批次号。（注意：若后期放开每个商户每次提现金额超过500元，则这里需要修改。要添加将提现金额按照每条500元拆分，然后再创建多个批次提现记录）
        List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords = new ArrayList<>();
        //生成提现发起的批次号
        String batchNo = OrderIdUtil.generateBusinessOrderId(BusinessType.MERCHANT_WITHDRAW_BATCH, batchReviewWithdrawApplicationRequest.getOperator());
        merchantWithdrawApplications.forEach(merchantWithdrawApplication -> {
            MerchantWithdrawApplicationRecord withdrawApplicationRecord = new MerchantWithdrawApplicationRecord();
            withdrawApplicationRecord.setUid(merchantWithdrawApplication.getUid());
            withdrawApplicationRecord.setOrderNo(merchantWithdrawApplication.getOrderNo());
            //设置提现发起的批次号
            withdrawApplicationRecord.setBatchNo(batchNo);
            withdrawApplicationRecord.setAmount(merchantWithdrawApplication.getAmount());
            withdrawApplicationRecord.setStatus(MerchantWithdrawConstant.WITHDRAW_IN_PROGRESS);
            withdrawApplicationRecord.setTenantId(merchantWithdrawApplication.getTenantId());
            withdrawApplicationRecord.setCreateTime(System.currentTimeMillis());
            withdrawApplicationRecord.setUpdateTime(System.currentTimeMillis());
    
            merchantWithdrawApplicationRecords.add(withdrawApplicationRecord);
            
        });
        
        //批量创建批次记录
        merchantWithdrawApplicationRecordService.batchInsert(merchantWithdrawApplicationRecords);
        
        //TODO 发起微信第三方提现申请
        
        return null;
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
}
