package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FranchiseeBindCardBindQuery;
import com.xiliulou.electricity.service.FranchiseeBindCardService;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加盟商套餐绑定表(TFranchiseeBindCard)表控制层
 *
 * @author makejava
 * @since 2021-04-16 15:12:51
 */
@RestController
public class JsonAdminFranchiseeBindCardController {
    /**
     * 服务对象
     */
    @Autowired
    private FranchiseeBindCardService franchiseeBindCardService;

    //加盟商绑定、解绑套餐
    @PostMapping(value = "/admin/franchiseeBindCard/bindCard")
    public R bindCard(@RequestBody @Validated(value = CreateGroup.class) FranchiseeBindCardBindQuery franchiseeBindCardBindQuery) {
        return franchiseeBindCardService.bindCard(franchiseeBindCardBindQuery);
    }

    //加盟商查询套餐(根据加盟商id)
    @GetMapping(value = "/admin/franchiseeBindCard/queryBindCard/{id}")
    public R queryBindCard(@PathVariable("id") Integer id ) {
        return franchiseeBindCardService.queryBindCard(id);
    }

}
