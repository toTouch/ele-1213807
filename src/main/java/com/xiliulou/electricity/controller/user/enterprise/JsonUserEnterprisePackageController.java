package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 15:52
 */

@RestController
@Slf4j
public class JsonUserEnterprisePackageController extends BaseController {

    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;

    @PostMapping("/user/enterprise/addUser")
    public R addUser(@RequestBody @Validated(CreateGroup.class) EnterpriseChannelUserQuery query) {

       return R.ok(enterpriseChannelUserService.save(query));

    }


}
