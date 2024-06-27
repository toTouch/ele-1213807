package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/5 10:49
 */

@Slf4j
@RestController
public class JsonMerchantWithdrawController extends BaseController {
    
    @Resource
    UserDataScopeService userDataScopeService;
    
    @Resource
    MerchantWithdrawApplicationService merchantWithdrawApplicationService;
    
    
    /**
     * 提现申请列表查询
     *
     * @param size
     * @param offset
     * @param merchantUid
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    @GetMapping(value = "/admin/merchant/withdraw/page")
    public R queryMerchantWithdrawList(@RequestParam(value = "size", required = true) Long size, @RequestParam(value = "offset", required = true) Long offset,
            @RequestParam(value = "merchantUid", required = false) Long merchantUid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder().tenantId(user.getTenantId())
                .franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).size(size).offset(offset).uid(merchantUid).status(status).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(merchantWithdrawApplicationService.queryMerchantWithdrawApplicationList(merchantWithdrawApplicationRequest));
        
    }
    
    @GetMapping(value = "/admin/merchant/withdraw/pageCount")
    public R queryMerchantWithdrawCount(@RequestParam(value = "merchantUid", required = false) Long merchantUid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder().tenantId(user.getTenantId())
                .franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).uid(merchantUid).status(status).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(merchantWithdrawApplicationService.countMerchantWithdrawApplication(merchantWithdrawApplicationRequest));
    }
    
    @PostMapping(value = "/admin/merchant/withdraw/review")
    public R reviewMerchantWithdrawApplication(@Validated @RequestBody ReviewWithdrawApplicationRequest reviewWithdrawApplicationRequest) {
        // 临时处理，暂时对外不开放此功能
        return returnTripleResult(Triple.of(false, "000000", "该功能需要微信商户开通“企业付款到零钱”功能，请确认开通后使用"));
        
        // return returnTripleResult(merchantWithdrawApplicationService.reviewMerchantWithdrawApplication(reviewWithdrawApplicationRequest));
    }
    
    @PostMapping(value = "/admin/merchant/withdraw/batchReview")
    public R batchReviewMerchantWithdrawApplication(@Validated @RequestBody BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest) {
        // 临时处理，暂时对外不开放此功能
        return returnTripleResult(Triple.of(false, "000000", "该功能需要微信商户开通“企业付款到零钱”功能，请确认开通后使用"));
        
        // return returnTripleResult(merchantWithdrawApplicationService.batchReviewMerchantWithdrawApplication(batchReviewWithdrawApplicationRequest));
    }
    
    /**
     * 查询商户提现记录
     *
     * @param size
     * @param offset
     * @param merchantUid
     * @param beginTime
     * @param endTime
     * @param status
     * @return
     */
    @GetMapping(value = "/admin/merchant/withdraw/recordList")
    public R queryRecordList(@RequestParam(value = "size") Long size, @RequestParam(value = "offset") Long offset,
            @RequestParam(value = "merchantUid", required = false) Long merchantUid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "transactionBatchId", required = false) String transactionBatchId,
            @RequestParam(value = "transactionDetailId", required = false) String transactionDetailId, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId ,
            @RequestParam(value = "checkTimeStart", required = false) Long checkTimeStart, @RequestParam(value = "checkTimeEnd", required = false) Long checkTimeEnd) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder().tenantId(user.getTenantId()).size(size).offset(offset)
                .uid(merchantUid).status(status).beginTime(beginTime).endTime(endTime).transactionBatchId(transactionBatchId).transactionDetailId(transactionDetailId)
                .checkTimeStart(checkTimeStart).checkTimeEnd(checkTimeEnd).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantWithdrawApplicationService.selectRecordList(merchantWithdrawApplicationRequest));
        
    }
    
    @GetMapping(value = "/admin/merchant/withdraw/recordListCount")
    public R queryRecordListCount(@RequestParam(value = "merchantUid", required = false) Long merchantUid, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "transactionBatchId", required = false) String transactionBatchId,
            @RequestParam(value = "transactionDetailId", required = false) String transactionDetailId, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "checkTimeStart", required = false) Long checkTimeStart, @RequestParam(value = "checkTimeEnd", required = false) Long checkTimeEnd) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder().tenantId(user.getTenantId()).uid(merchantUid)
                .status(status).beginTime(beginTime).endTime(endTime).transactionBatchId(transactionBatchId).transactionDetailId(transactionDetailId).checkTimeStart(checkTimeStart)
                .checkTimeEnd(checkTimeEnd).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantWithdrawApplicationService.selectRecordListCount(merchantWithdrawApplicationRequest));
        
    }
    
    
}
