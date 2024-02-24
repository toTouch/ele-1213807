package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName : JsonUserMerchantPromotionFee
 * @Description : 小程序推广费
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@RestController
@Slf4j
public class JsonUserMerchantPromotionFeeController {
    
    @Resource
    private MerchantPromotionFeeService merchantPromotionFeeService;
    
    @GetMapping("/user/merchant/promotionFee/availableWithdrawAmount")
    public R queryMerchantAvailableWithdrawAmount() {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return merchantPromotionFeeService.queryMerchantAvailableWithdrawAmount(user.getUid());
    }
    
    @GetMapping("/user/merchant/promotionFee/income")
    public R queryMerchantPromotionFeeIncome(@RequestParam("type") Integer type, @RequestParam("uid") Long uid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.queryMerchantPromotionFeeIncome(type, uid);
    }
    
    @GetMapping("/user/merchant/promotionFee/scanCodeCount")
    public R queryMerchantPromotionScanCode(@RequestParam("type") Integer type, @RequestParam("uid") Long uid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return merchantPromotionFeeService.queryMerchantPromotionScanCode(type, uid);
    }
    
    
    @GetMapping("/user/merchant/promotionFee/renewal")
    public R queryMerchantPromotionRenewal(@RequestParam("type") Integer type, @RequestParam("uid") Long uid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return merchantPromotionFeeService.queryMerchantPromotionRenewal(type, uid);
    }
    
    @GetMapping("/user/merchant/promotionFee/statistic/merchantIncome")
    public R promotionFeeStatisticAnalysisIncome(@RequestParam("type") Integer type, @RequestParam("uid") Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.statisticMerchantIncome(type, uid, beginTime, endTime);
    }
    
    @GetMapping("/user/merchant/promotionFee/statistic/user")
    public R promotionFeeStatisticAnalysisUser(@RequestParam("type") Integer type, @RequestParam("uid") Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.statisticUser(type, uid, beginTime, endTime);
    }
    
    @GetMapping("/user/merchant/promotionFee/statistic/channelEmployeeMerchant")
    public R promotionFeeStatisticAnalysisChannelEmployeeMerchant(@RequestParam("type") Integer type, @RequestParam("uid") Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.statisticChannelEmployeeMerchant(type, uid, beginTime, endTime);
    }
    
    @GetMapping("/user/merchant/promotion/employee/details/page")
    public R promotionEmployeeDetails(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam("merchantId") Long merchantId) {
        if (size < 0 || size > 5) {
            size = 5L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        MerchantPromotionEmployeeDetailQueryModel queryModel = MerchantPromotionEmployeeDetailQueryModel.builder().size(size).offset(offset).merchantUid(merchantId)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return merchantPromotionFeeService.selectMerchantEmployeeDetailList(queryModel);
    }
    
    
    /**
     *
     *  推广数据概览展示
     * @param size
     * @param offset
     * @param uid
     * @param type
     * @param queryTime
     * @param status
     * @return
     */
    @GetMapping("/user/merchant/promotion/data/")
    public R promotionDataPage(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam(value = "uid",required = false) Long uid, @RequestParam(value = "type",required = false) Integer type,
            @RequestParam(value = "queryTime",required = false) Long queryTime) {
        if (size < 0 || size > 10) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        MerchantPromotionDataDetailQueryModel queryModel = MerchantPromotionDataDetailQueryModel.builder().size(size).offset(offset).uid(uid).type(type)
                .tenantId(TenantContextHolder.getTenantId()).startTime(queryTime).endTime(DateUtils.getMonthEndTimeStampByDate(queryTime)).build();
        return merchantPromotionFeeService.selectPromotionData(queryModel);
    }
    
    
    /**
     *
     *  推广数据列表展示
     * @param size 页码大小
     * @param offset 页码
     * @param uid uid
     * @param type 类型
     * @param queryTime 查询时间
     * @param status 状态
     * @return 推广数据列表
     */
    @GetMapping("/user/merchant/promotion/data/detail/page")
    public R promotionDataPage(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam(value = "uid",required = false) Long uid, @RequestParam(value = "type",required = false) Integer type,
            @RequestParam(value = "queryTime",required = false) Long queryTime,@RequestParam(value = "status",required = false) Integer status) {
        if (size < 0 || size > 10) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        MerchantPromotionDataDetailQueryModel queryModel = MerchantPromotionDataDetailQueryModel.builder().size(size).offset(offset).uid(uid).type(type)
                .tenantId(TenantContextHolder.getTenantId()).startTime(queryTime).endTime(DateUtils.getMonthEndTimeStampByDate(queryTime)).status(status).build();
        return merchantPromotionFeeService.selectPromotionDataDetail(queryModel);
    }
    
    
    
    
}
