package com.xiliulou.electricity.controller.admin.thirdParty;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.thirdparty.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.request.thirdParty.MeiTuanRiderMallConfigRequest;
import com.xiliulou.electricity.service.thirdParty.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.thirdParty.MeiTuanRiderMallConfigVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息
 * @date 2023/11/28 09:42:34
 */
@RestController
@Slf4j
public class JsonAdminMeiTuanRiderMallConfigController {
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    
    @PostMapping("/admin/meiTuanRiderMall/config/update")
    public R insertOrUpdate(@RequestBody @Validated MeiTuanRiderMallConfigRequest meiTuanRiderMallConfigRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return R.ok(meiTuanRiderMallConfigService.insertOrUpdate(meiTuanRiderMallConfigRequest));
    }
    
    @GetMapping("/admin/meiTuanRiderMall/config/query")
    public R queryByTenantId() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            return R.ok();
        }
        
        MeiTuanRiderMallConfigVO vo = new MeiTuanRiderMallConfigVO();
        BeanUtils.copyProperties(meiTuanRiderMallConfig, vo);
        
        return R.ok(vo);
    }
    
}
