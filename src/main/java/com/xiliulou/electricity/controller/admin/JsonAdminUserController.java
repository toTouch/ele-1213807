package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.query.UserSourceUpdateQuery;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.electricity.web.query.PasswordQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author: eclair
 * @Date: 2020/11/30 16:47
 * @Description:
 */
@RestController()
@RequestMapping("/admin")
@Slf4j
public class JsonAdminUserController extends BaseController {
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    UserDataScopeService userDataScopeService;


    @PostMapping("/user/register")
    public R createUser(@Validated(value = CreateGroup.class) @RequestBody AdminUserQuery adminUserQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }
        return returnTripleResult(userService.addAdminUser(adminUserQuery));
    }
    
    @GetMapping("/user/search")
    public R search(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
    
        UserInfoQuery query = UserInfoQuery.builder().size(size).offset(offset).name(name).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(userService.search(query));
    }

    @GetMapping("/user/list")
    public R listUser(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                      @RequestParam(value = "uid", required = false) Long uid,
                      @RequestParam(value = "name", required = false) String name,
                      @RequestParam(value = "phone", required = false) String phone,
                      @RequestParam(value = "type", required = false) Integer type,
                      @RequestParam(value = "beginTime", required = false) Long startTime,
                      @RequestParam(value = "endTime", required = false) Long endTime) {
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
            return R.ok(Collections.EMPTY_LIST);
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if (SecurityUtils.isAdmin() && Objects.nonNull(type) && type == -1) {
            tenantId = null;
        }

        return returnPairResult(userService.queryListUser(uid, size, offset, name, phone, type, startTime, endTime,tenantId));
    }


    @GetMapping("/user/queryCount")
    public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "beginTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if(SecurityUtils.isAdmin()&&Objects.nonNull(type)&&type==-1){
            tenantId=null;
        }

        return returnPairResult(userService.queryCount(uid,  name, phone, type, startTime, endTime,tenantId));
    }

    @GetMapping("/user/scope/{uid}")
    public R UserDataScope(@PathVariable("uid") Long uid) {
        return R.ok(userDataScopeService.selectByUid(uid));
    }

    @PutMapping("/user")
    @Log(title = "修改用户")
    public R updateAdminUser(@Validated(value = UpdateGroup.class) @RequestBody AdminUserQuery adminUserQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(userService.updateAdminUser(adminUserQuery));
    }

    @DeleteMapping("/user/{uid}")
    @Log(title = "删除用户")
    public R deleteAdminUser(@PathVariable("uid") Long uid) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(userService.deleteAdminUser(uid));
    }

    @PostMapping("/user/role/bind")
    public R bindUserRole(@RequestParam("uid") Long uid, @RequestParam("roleIds") String jsonRoleIds) {
        List<Long> roleIds = JsonUtil.fromJsonArray(jsonRoleIds, Long.class);
        if (!DataUtil.collectionIsUsable(roleIds)) {
            return R.fail("SYSTEM.0002", "参数不合法");
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(roleService.bindUserRole(uid, roleIds));
    }

    @GetMapping("/user/menu")
    public R getUserMenu() {
        return returnPairResult(roleService.getMenuByUid());
    }

    @PostMapping("/user/updatePassword")
    @Log(title = "修改登录密码")
    public R updatePassword(@Validated(value = CreateGroup.class)  PasswordQuery passwordQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }
        return returnTripleResult(userService.updatePassword(passwordQuery));
    }


    //结束限制订单
//    @Deprecated
//    @PutMapping(value = "/user/endLimitUser")
//    public R endLimitUser(@RequestParam("uid") Long uid) {
//        return userService.endLimitUser(uid);
//    }

    @DeleteMapping("/user/del/{uid}")
    @Log(title = "删除普通用户")
    public R deleteNormalUser(@PathVariable("uid") Long uid) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnTripleResult(userService.deleteNormalUser(uid));
    }

    @GetMapping("/user/auth/generationCode")
    public R userAutoCodeGeneration() {
        return userService.userAutoCodeGeneration();
    }

    @PostMapping("user/auth/checkCode")
    public R userAutoCodeCheck(@RequestParam("autoCode")String autoCode) {
        return userService.userAutoCodeCheck(autoCode);
    }


    /**
     * 修改用户来源
     */
    @PutMapping(value = "/user/userSource")
    public R updateUserSource(@RequestBody @Validated UserSourceUpdateQuery query) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        userService.updateUserByUid(query);
        return R.ok();
    }


}
