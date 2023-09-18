package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FreeDepositDataQuery;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
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
 * 免押次数充值
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-20-15:48
 */
@Slf4j
@RestController
public class JsonAdminFreeDepositDataController extends BaseController {

    @Autowired
    FreeDepositDataService freeDepositDataService;

    @PutMapping("/admin/freeDepositData/recharge")
    public R recharge(@RequestBody @Validated(UpdateGroup.class) FreeDepositDataQuery freeDepositDataQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.error("ELE ERROR! update faceRecognizeData no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(this.freeDepositDataService.recharge(freeDepositDataQuery));
    }


    @GetMapping("/admin/freeDeposit/capacity")
    public R capacity() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return R.ok(this.freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId()));
    }

}
