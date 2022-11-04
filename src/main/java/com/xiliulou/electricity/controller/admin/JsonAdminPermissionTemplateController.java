package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.PermissionTemplateQuery;
import com.xiliulou.electricity.service.PermissionTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色默认权限
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-09-29-15:12
 */
@RestController
@Slf4j
public class JsonAdminPermissionTemplateController extends BaseController {
    @Autowired
    private PermissionTemplateService permissionTemplateService;

    /**
     *获取角色已分配权限
     */
    @GetMapping(value = "/admin/permissionTemplate/list/{type}")
    public R selectPermission(@PathVariable("type") Integer type) {
        return R.ok(permissionTemplateService.selectByType(type));
    }

    /**
     * 保存角色分配的权限
     */
    @PostMapping(value="/admin/permissionTemplate/insert")
    public R insertPermission(@RequestBody @Validated PermissionTemplateQuery permissionTemplateQuery){
        return R.ok(permissionTemplateService.insertPermissionTemplate(permissionTemplateQuery));
    }

}
