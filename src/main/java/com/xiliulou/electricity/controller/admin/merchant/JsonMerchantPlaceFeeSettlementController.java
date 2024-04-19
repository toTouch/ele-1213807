package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeSettlementService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Objects;

/**
 * @ClassName : JsonAdminMerchantPlaceFeeSettlementController
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */

@RestController
public class JsonMerchantPlaceFeeSettlementController extends BaseController {
    
    @Resource
    private MerchantPlaceFeeSettlementService merchantPlaceFeeSettlementService;
    
    
    @GetMapping("/admin/merchant/placeFee/settlement/exportExcel")
    public void export(@RequestParam("monthDate") String monthDate,HttpServletResponse response) {
        merchantPlaceFeeSettlementService.export(monthDate, response);
    }
    
    /**
     *
     * @param size 条数
     * @param offset 偏移量
     * @param monthDate 月份
     * @return 查询到的记录
     */
    @GetMapping("/admin/merchant/placeFee/settlement/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "monthDate", required = false) String monthDate) {
        if (size < 0 || size > 50) {
            size = 50L;
        }
    
        if (offset < 0) {
            offset = 0L;
        }
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.emptyList());
        }
    
        MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel = MerchantPlaceFeeMonthSummaryRecordQueryModel.builder().size(size).offset(offset).monthDate(monthDate).tenantId(
                TenantContextHolder.getTenantId()).build();
        
        return merchantPlaceFeeSettlementService.page(queryModel);
    }
    
    /**
     *
     * @param monthDate 月份
     * @return 查询到的记录条数
     */
    @GetMapping("/admin/merchant/placeFee/settlement/pageCount")
    public R pageCount(@RequestParam(value = "monthDate", required = false) String monthDate) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }
        
        MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel = MerchantPlaceFeeMonthSummaryRecordQueryModel.builder().monthDate(monthDate).tenantId(
                TenantContextHolder.getTenantId()).build();
        
        return merchantPlaceFeeSettlementService.pageCount(queryModel);
    }
    
}
