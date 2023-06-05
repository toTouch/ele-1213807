package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.InvitationActivityService;
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
 * @date 2023-06-01-15:57
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityController extends BaseController {

    @Autowired
    private InvitationActivityService invitationActivityService;


    /**
     * 新增
     */
    @PostMapping("/admin/invitation/activity/save")
    public R save(@RequestBody @Validated(CreateGroup.class) InvitationActivityQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(invitationActivityService.save(query));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/invitation/activity/update")
    public R update(@RequestBody @Validated(UpdateGroup.class) InvitationActivityQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(invitationActivityService.modify(query));
    }

    /**
     * 上架/下架
     */
    @PutMapping("/admin/invitation/activity/shelf")
    public R updateStatus(@RequestBody @Validated InvitationActivityStatusQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(invitationActivityService.updateStatus(query));
    }

    @GetMapping("/admin/invitationActivity/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        InvitationActivityQuery query = InvitationActivityQuery.builder().size(size).offset(offset).name(name)
                .tenantId(TenantContextHolder.getTenantId()).status(status).build();

        return R.ok(invitationActivityService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivity/queryCount")
    public R count(@RequestParam(value = "status", required = false) Integer status,
                   @RequestParam(value = "name", required = false) String name) {

        InvitationActivityQuery query = InvitationActivityQuery.builder()
                .tenantId(TenantContextHolder.getTenantId()).name(name).status(status).build();

        return R.ok(invitationActivityService.selectByPageCount(query));
    }


}
