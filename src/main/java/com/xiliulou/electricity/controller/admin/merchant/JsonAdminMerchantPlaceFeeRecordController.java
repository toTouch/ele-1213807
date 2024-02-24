package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRecordPageRequest;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/15 21:06
 * @desc 场地费记录
 */
@Slf4j
@RestController
public class JsonAdminMerchantPlaceFeeRecordController extends BaseController {
    @Resource
    private MerchantPlaceFeeRecordService merchantPlaceFeeRecordService;
    
    /**
     * @param
     * @description 场地费列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/fee/pageCount")
    public R pageCount(@RequestParam(value = "cabinetId") Integer cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
    
        MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest = MerchantPlaceFeeRecordPageRequest.builder().cabinetId(cabinetId).tenantId(tenantId)
                .tenantId(tenantId).build();
        
        return R.ok(merchantPlaceFeeRecordService.countTotal(merchantPlacePageRequest));
    }
    
    /**
     * @param
     * @description 场地费列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchant/place/fee/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "cabinetId") Integer cabinetId) {
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
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
        
        MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest = MerchantPlaceFeeRecordPageRequest.builder().cabinetId(cabinetId).size(size).offset(offset).tenantId(tenantId).build();
        
        return R.ok(merchantPlaceFeeRecordService.listByPage(merchantPlacePageRequest));
    }
}
