package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.BindElectricityCabinetQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserService;
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

import java.util.ArrayList;
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
    FranchiseeService franchiseeService;

    @PostMapping("/user/register")
    public R createUser(@Validated(value = CreateGroup.class) @RequestBody AdminUserQuery adminUserQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }
        return returnTripleResult(userService.addAdminUser(adminUserQuery));
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
        return returnPairResult(userService.queryListUser(uid, size, offset, name, phone, type, startTime, endTime));
    }

    @GetMapping("/user/listByFranchisee")
    public R listByFranchisee(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
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
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Franchisee> franchiseeList=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchiseeList)){
            return R.ok();
        }

        List<Integer> cidList=new ArrayList<>();


        for (Franchisee franchisee:franchiseeList) {
            cidList.add(franchisee.getCid());
        }
        if(ObjectUtil.isEmpty(cidList)){
            return R.ok();
        }

        return returnPairResult(userService.listByFranchisee(uid, size, offset, name, phone, type, startTime, endTime,cidList));
    }

    @PutMapping("/user")
    public R updateAdminUser(@Validated(value = UpdateGroup.class) @RequestBody AdminUserQuery adminUserQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }

        return returnPairResult(userService.updateAdminUser(adminUserQuery));
    }

    @DeleteMapping("/user/{uid}")
    public R deleteAdminUser(@PathVariable("uid") Long uid) {
        return returnPairResult(userService.deleteAdminUser(uid));
    }

    @PostMapping("/user/role/bind")
    public R bindUserRole(@RequestParam("uid") Long uid, @RequestParam("roleIds") String jsonRoleIds) {
        List<Long> roleIds = JsonUtil.fromJsonArray(jsonRoleIds, Long.class);
        if (!DataUtil.collectionIsUsable(roleIds)) {
            return R.fail("SYSTEM.0002", "参数不合法");
        }

        return returnPairResult(roleService.bindUserRole(uid, roleIds));
    }

    @GetMapping("/user/menu")
    public R getUserMenu() {
        return returnPairResult(roleService.getMenuByUid());
    }

    @PostMapping("/user/updatePassword")
    public R updatePassword(@Validated(value = CreateGroup.class)  PasswordQuery passwordQuery, BindingResult result) {
        if (result.hasFieldErrors()) {
            return R.fail("SYSTEM.0002", result.getFieldError().getDefaultMessage());
        }
        return returnTripleResult(userService.updatePassword(passwordQuery));
    }

    //电柜负责人绑定电柜
    @PostMapping(value = "/user/bindElectricityCabinet")
    public R bindElectricityCabinet(@RequestBody @Validated(value = CreateGroup.class) BindElectricityCabinetQuery bindElectricityCabinetQuery){
        return userService.bindElectricityCabinet(bindElectricityCabinetQuery);
    }

    //电柜负责人绑定电柜查询
    @PostMapping(value = "/user/queryElectricityCabinetList/{uid}")
    public R queryElectricityCabinetList(@PathVariable("uid") Long uid){
        return userService.queryElectricityCabinetList(uid);
    }



}
