package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.query.DivisionAccountConfigStatusQuery;
import com.xiliulou.electricity.service.DivisionAccountConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-23-18:12
 */
@Slf4j
@RestController
public class JsonAdminDivisionAccountConfigController extends BaseController {

    @Autowired
    private DivisionAccountConfigService divisionAccountConfigService;

    @GetMapping("/admin/division/account/config/search")
    public R configSearch(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                         @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 20) {
            size = 20L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        return R.ok(divisionAccountConfigService.configSearch(size, offset, name, TenantContextHolder.getTenantId()));
    }

    /**
     * 分页列表
     */
    @GetMapping("/admin/division/account/config/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "name", required = false) String name,
                  @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountConfigQuery query = DivisionAccountConfigQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .name(name)
                .status(status)
                .build();

        return R.ok(divisionAccountConfigService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/division/account/config/count")
    public R pageCount(@RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "status", required = false) Integer status) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountConfigQuery query = DivisionAccountConfigQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .name(name)
                .status(status)
                .build();

        return R.ok(divisionAccountConfigService.selectByPageCount(query));
    }

    /**
     * 详情
     *
     * @return
     */
    @GetMapping("/admin/division/account/config/{id}")
    public R customizeBatteryType(@PathVariable(value = "id", required = false) Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(divisionAccountConfigService.selectInfoById(id));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/division/account/config")
    public R save(@RequestBody @Validated(CreateGroup.class) DivisionAccountConfigQuery divisionAccountConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(divisionAccountConfigService.save(divisionAccountConfigQuery));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/division/account/config")
    public R update(@RequestBody @Validated(UpdateGroup.class) DivisionAccountConfigQuery divisionAccountConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(divisionAccountConfigService.modify(divisionAccountConfigQuery));
    }

    /**
     * 启用/禁用
     */
    @PutMapping("/admin/division/account/config/status")
    public R updateStatus(@RequestBody @Validated DivisionAccountConfigStatusQuery divisionAccountConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(divisionAccountConfigService.updateStatus(divisionAccountConfigQuery));
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/division/account/config/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(divisionAccountConfigService.delete(id));
    }



}
