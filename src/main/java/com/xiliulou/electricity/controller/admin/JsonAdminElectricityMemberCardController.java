package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:25
 **/
@RestController
@Slf4j
public class JsonAdminElectricityMemberCardController {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;

    @Autowired
    FranchiseeService franchiseeService;

    /**
     * 新增
     *
     * @return
     */
    @PostMapping("admin/electricityMemberCard")
    public R add(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        return electricityMemberCardService.add(electricityMemberCard);
    }

    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/electricityMemberCard")
    public R update(@RequestBody @Validated ElectricityMemberCard electricityMemberCard) {
        if (Objects.isNull(electricityMemberCard)) {
            return R.failMsg("请求参数不能为空!");
        }
        return electricityMemberCardService.update(electricityMemberCard);
    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("admin/electricityMemberCard/{id}")
    public R delete(@PathVariable(value = "id") Integer id) {
        return electricityMemberCardService.delete(id);
    }

    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/list")
    public R getElectricityMemberCardPage(@RequestParam(value = "offset") Long offset,
                                          @RequestParam(value = "size") Long size,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();
        return electricityMemberCardService.queryList(offset, size, status, type,tenantId);
    }



    /**
     * 分页数量
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/queryCount")
    public R getElectricityMemberCardPage(@RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();
        return electricityMemberCardService.queryCount(status, type,tenantId);
    }


    /**
     * 加盟商分页
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "offset") Long offset,
            @RequestParam(value = "size") Long size,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(Objects.isNull(franchisee)){
            return R.ok();
        }


        return electricityMemberCardService.listByFranchisee(offset, size, status, type,tenantId,franchisee.getId());
    }

    /**
     * 加盟商分页数量
     *
     * @return
     */
    @GetMapping("admin/electricityMemberCard/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(Objects.isNull(franchisee)){
            return R.ok();
        }


        return electricityMemberCardService.listCountByFranchisee(status, type,tenantId,franchisee.getId());
    }


    //查询换电套餐根据加盟商
    @GetMapping(value = "/admin/electricityMemberCard/queryByFranchisee/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Integer id){
        return R.ok(electricityMemberCardService.queryByFranchisee(id));
    }

}
