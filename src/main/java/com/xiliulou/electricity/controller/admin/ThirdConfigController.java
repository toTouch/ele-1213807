package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ThirdConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ThirdConfigQuery;
import com.xiliulou.electricity.service.ThirdConfigService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 * (ThirdConfig)表控制层
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
@RestController
@Slf4j
public class ThirdConfigController extends BaseController {

    @Autowired
    private ThirdConfigService thirdConfigService;

    /**
     * 根据租户id查询最新单条数据
     */
    @GetMapping("/admin/thirdConfig/selectByTenantId")
    public R selectByTenantId() {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin() || !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            return R.ok();
        }

        return R.ok(this.thirdConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId()));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/thirdConfig/insertOrUpdate")
    public R insertOrUpdate(@RequestBody @Validated ThirdConfigQuery thirdConfigQuery) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin() || !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            return R.ok();
        }

        return R.ok(this.thirdConfigService.insertOrUpdate(thirdConfigQuery));
    }

}
