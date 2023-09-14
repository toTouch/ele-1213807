package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-16:02
 */
@RestController
@Slf4j
public class JsonUserEnterpriseInfoController extends BaseController {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 获取用户是否属于企业渠道
     */
    @GetMapping("/user/EnterpriseInfo/check")
    public R enterpriseInfoCheck() {
        return R.ok(enterpriseInfoService.checkUserType());
    }


}
