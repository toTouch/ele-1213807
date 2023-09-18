package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @date 2023-09-14 11:32
 */

@RestController
@Slf4j
public class JsonAdminEnterprisePackageController extends BaseController {

    @Resource
    private BatteryMemberCardService batteryMemberCardService;

    @Resource
    private EnterpriseBatteryPackageService enterpriseBatteryPackageService;

    @Resource
    UserDataScopeService userDataScopeService;


    @PostMapping("/admin/battery/enterprise/addPackage")
    public R addPackage(@RequestBody @Validated(CreateGroup.class) EnterpriseMemberCardQuery query) {

        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(userInfo.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(userInfo.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        enterpriseBatteryPackageService.save(query);

        return null;

    }

}
