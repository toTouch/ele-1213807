package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.request.merchant.RebateConfigRequest;
import com.xiliulou.electricity.service.merchant.RebateConfigService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 返利配置
 */
@Slf4j
@RestController
public class JsonMerchantRebateConfigController extends BaseController {
    
    @Autowired
    private RebateConfigService rebateConfigService;
    
    /**
     * 获取返利配置列表
     *
     * @return
     */
    @GetMapping("/admin/rebateConfig/list")
    public R getRebateConfigList(@RequestParam("franchiseeId") Long franchiseeId, @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "mid", required = false) Long mid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        RebateConfigRequest rebateConfigRequest = RebateConfigRequest.builder().mid(mid).level(level).delFlag(CommonConstant.DEL_N).franchiseeId(franchiseeId).build();
        
        return R.ok(rebateConfigService.listByPage(rebateConfigRequest));
    }
    
    /**
     * 保存返利配置
     *
     * @return
     */
    @PutMapping("/admin/rebateConfig/save")
    @Log(title = "新增返利配置")
    public R save(@RequestBody @Validated(CreateGroup.class) RebateConfigRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(rebateConfigService.save(request));
    }
    
    /**
     * 修改返利配置
     *
     * @return
     */
    @PutMapping("/admin/rebateConfig/update")
    @Log(title = "修改返利配置")
    public R modify(@RequestBody @Validated(UpdateGroup.class) RebateConfigRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return returnTripleResult(rebateConfigService.modify(request));
    }
}
