package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.query.InvitationActivityUserSaveQuery;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-05-16:12
 */
@Slf4j
@RestController
public class JsonAdminInvitationActivityUserController extends BaseController {

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;

    @Autowired
    private UserDataScopeService userDataScopeService;

    @GetMapping("/admin/invitationActivityUser/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "phone", required = false) String phone,
                  @RequestParam(value = "userName", required = false) String userName,
                  @RequestParam(value = "activityName", required = false) String activityName) {
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

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InvitationActivityUserQuery query = InvitationActivityUserQuery.builder()
                .size(size)
                .offset(offset)
                .userName(userName)
                .uid(uid)
                .tenantId(TenantContextHolder.getTenantId())
                .phone(phone)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .activityName(activityName)
                .build();

        return R.ok(invitationActivityUserService.selectByPage(query));
    }

    @GetMapping("/admin/invitationActivityUser/queryCount")
    public R count(@RequestParam(value = "phone", required = false) String phone,
                   @RequestParam(value = "uid", required = false) Long uid,
                   @RequestParam(value = "userName", required = false) String userName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        InvitationActivityUserQuery query = InvitationActivityUserQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .userName(userName)
                .phone(phone)
                .uid(uid)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .build();

        return R.ok(invitationActivityUserService.selectByPageCount(query));
    }

    @PostMapping("/admin/invitationActivityUser/save")
    public R save(@RequestBody @Validated(CreateGroup.class) InvitationActivityUserSaveQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(invitationActivityUserService.saveInvitationUser(query));
    }

    @DeleteMapping("/admin/invitationActivityUser/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(invitationActivityUserService.delete(id));
    }
}
