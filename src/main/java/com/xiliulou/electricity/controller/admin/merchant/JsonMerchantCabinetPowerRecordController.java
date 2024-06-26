package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantPowerRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 场地电费统计
 * @date 2024/2/24 18:07:05
 */
@Slf4j
@RestController
public class JsonMerchantCabinetPowerRecordController extends BaseController {
    
    @Resource
    private MerchantCabinetPowerMonthRecordService merchantCabinetPowerMonthRecordService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/merchant/power/record/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "monthDate", required = false) String monthDate,
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok(Collections.emptyList());
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        MerchantPowerRequest request = MerchantPowerRequest.builder().size(size).offset(offset).monthDate(monthDate).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId)
                .build();
        
        return R.ok(merchantCabinetPowerMonthRecordService.listByPage(request));
    }
    
    @GetMapping("/admin/merchant/power/record/pageCount")
    public R pageCount(@RequestParam(value = "monthDate", required = false) String monthDate, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }
        
        MerchantPowerRequest request = MerchantPowerRequest.builder().monthDate(monthDate).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantCabinetPowerMonthRecordService.countTotal(request));
    }
    
    /**
     * @param monthDate 格式要求：yyyy-MM 2024-02
     */
    @GetMapping("/admin/merchant/power/record/exportExcel")
    public void exportExcel(@RequestParam(value = "monthDate") String monthDate, HttpServletResponse response,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return;
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return;
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return;
            }
        }
        
        MerchantPowerRequest request = MerchantPowerRequest.builder().monthDate(monthDate).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        
        merchantCabinetPowerMonthRecordService.exportExcel(request, response);
    }
}
