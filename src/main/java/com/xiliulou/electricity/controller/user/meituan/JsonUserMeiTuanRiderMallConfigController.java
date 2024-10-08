package com.xiliulou.electricity.controller.user.meituan;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息
 * @date 2024/8/29 13:49:50
 */

@Slf4j
@RestController
public class JsonUserMeiTuanRiderMallConfigController {
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    /**
     * 是否显示“美团商城”
     */
    @GetMapping("/user/meiTuanRiderMall/isShow")
    public R isShow() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        
        return R.ok(Objects.nonNull(meiTuanRiderMallConfigService.checkEnableMeiTuanRiderMall(TenantContextHolder.getTenantId())));
    }
}
