package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FaceidConfigQuery;
import com.xiliulou.electricity.service.FaceidConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * (FaceidConfig)表控制层
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
@RestController
@Slf4j
public class JsonAdminFaceidConfigController extends BaseController {

    @Autowired
    private FaceidConfigService faceidConfigService;

    /**
     * 根据租户id查询最新单条数据
     */
    @GetMapping("/admin/faceidConfig/selectByTenantId")
    public R selectByTenantId() {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin() || !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            return R.ok();
        }

        return R.ok(this.faceidConfigService.selectLatestByTenantId(TenantContextHolder.getTenantId()));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/faceidConfig/insertOrUpdate")
    public R insertOrUpdate(@RequestBody @Validated FaceidConfigQuery faceidConfigQuery) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin() || !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            return R.ok();
        }

        return R.ok(this.faceidConfigService.insertOrUpdate(faceidConfigQuery));
    }

}
