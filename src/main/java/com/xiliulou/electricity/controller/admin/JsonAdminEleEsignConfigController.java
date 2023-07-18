package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleEsignConfigQuery;
import com.xiliulou.electricity.service.EleEsignConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/8 10:06
 * @Description:
 */

@RestController
@Slf4j
public class JsonAdminEleEsignConfigController extends BaseController {

    @Autowired
    private EleEsignConfigService eleEsignConfigService;

    /**
     * 根据租户id查询当前签名配置信息
     */
    @GetMapping("/admin/eleEsignConfig/retrieve")
    public R retrieveByTenantId() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE retrieve esign config ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(this.eleEsignConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId()));
    }

    /**
     * 修改签署配置信息
     */
    @PutMapping("/admin/eleEsignConfig/updateEsignConfig")
    public R updateEsignConfig(@RequestBody @Validated EleEsignConfigQuery esignConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE update esign config ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(this.eleEsignConfigService.insertOrUpdate(esignConfigQuery));
    }

}
