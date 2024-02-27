package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;
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
public class JsonUserMerchantPromotionFeeController extends BaseController {
    
    @Resource
    private MerchantPromotionFeeService merchantPromotionFeeService;
    
    
    /**
     * 获取商户下的场地员工(商户首页筛选条件)
     *
     * @return 可提现金额/user/merchant/promotion/employee/details/page
     */
    @GetMapping("/user/merchant/promotionFee/merchantEmployee")
    public R queryMerchantEmployees(@RequestParam("merchantUid") Long merchantUid) {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return merchantPromotionFeeService.queryMerchantEmployees(merchantUid);
    }
    
    /**
     * 可提现金额
     *
     * @return 可提现金额
     */
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
    
    /**
     * 商户首页 推广费收入统计
     *
     * @param type 用户类型
     * @param uid  用户uid
     * @return 推广费收入统计
     */
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
    
    /**
     * 商户首页 推广费扫码统计
     *
     * @param type 用户类型
     * @param uid  用户uid
     * @return 推广费扫码统计
     */
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
    
    /**
     * 商户首页 推广费续费情况
     *
     * @param type 用户类型
     * @param uid  用户uid
     * @return 推广费续费情况
     */
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
    
    /**
     * 收入分析
     *
     * @param type      用户类型
     * @param uid       用户uid
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 收入分析
     */
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
    
    /**
     * 用户分析
     *
     * @param type      用户类型
     * @param uid       用户uid
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 用户分析
     */
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
    
    /**
     * 渠道员商户分析
     *
     * @param type      用户类型
     * @param uid       用户uid
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 渠道员商户分析
     */
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
    
    /**
     * 商户首页 商户下的推广详情概览-商户数据(单独处理)
     *
     * @param merchantUid 商户id
     * @return 推广详情概览
     */
    @GetMapping("/user/merchant/promotion/merchant/detail")
    public R promotionMerchantDetail(@RequestParam("merchantUid") Long merchantUid) {
        
        MerchantPromotionEmployeeDetailQueryModel queryModel = MerchantPromotionEmployeeDetailQueryModel.builder().merchantUid(merchantUid)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return merchantPromotionFeeService.selectPromotionMerchantDetail(queryModel);
    }
    
    
    /**
     * 商户首页 商户下的推广详情概览
     *
     * @param size       页面显示条数
     * @param offset     偏移量
     * @param merchantId 商户id
     * @return 推广详情概览
     */
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
     * 推广详情
     *
     * @param size      页面显示条数
     * @param offset    偏移量
     * @param uid       用户uid
     * @param status    状态
     * @param queryTime 查询时间
     * @return 推广详情
     */
    @GetMapping("/user/merchant/promotion/employee/details/specifics")
    public R promotionEmployeeDetailList(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam("uid") Long uid,
            @RequestParam("status") Integer status, @RequestParam("queryTime") Long queryTime) {
        if (size < 0 || size > 10) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 10L;
        }
        
        MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel = MerchantPromotionEmployeeDetailSpecificsQueryModel.builder().size(size).offset(offset).uid(uid)
                .status(status).startTime(queryTime).endTime(DateUtils.getMonthEndTimeStampByDate(queryTime)).tenantId(TenantContextHolder.getTenantId()).build();
        
        return merchantPromotionFeeService.selectPromotionEmployeeDetailList(queryModel);
    }
    
    
    /**
     * 推广数据概览展示
     *
     * @param size
     * @param offset
     * @param uid
     * @param type
     * @param queryTime
     * @return
     */
    @GetMapping("/user/merchant/promotion/data")
    public R promotionDataPage(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "queryTime", required = false) Long queryTime) {
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
     * 推广数据列表展示
     *
     * @param size      页码大小
     * @param offset    页码
     * @param uid       uid
     * @param type      类型
     * @param queryTime 查询时间
     * @param status    状态
     * @return 推广数据列表
     */
    @GetMapping("/user/merchant/promotion/data/detail/page")
    public R promotionDataPage(@RequestParam("size") long size, @RequestParam("offset") Long offset, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "queryTime", required = false) Long queryTime,
            @RequestParam(value = "status", required = false) Integer status) {
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
