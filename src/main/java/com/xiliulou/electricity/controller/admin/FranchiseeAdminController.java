package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindFranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;


/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class FranchiseeAdminController {
    /**
     * 服务对象
     */
    @Autowired
    FranchiseeService franchiseeService;

    //新增加盟商
    @PostMapping(value = "/admin/franchisee")
    public R save(@RequestBody @Validated(value = CreateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.save(franchiseeAddAndUpdate);
    }

    //修改加盟商
    @PutMapping(value = "/admin/franchisee")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.edit(franchiseeAddAndUpdate);
    }

    //删除加盟商
    @DeleteMapping(value = "/admin/franchisee/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return franchiseeService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/franchisee/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        List<Integer> idList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            List<Franchisee> franchiseeList = franchiseeService.queryByUid(user.getUid());
            if (ObjectUtil.isNotEmpty(franchiseeList)) {
                for (Franchisee franchisee:franchiseeList) {
                    idList.add(franchisee.getId());
                }

            }
        }



        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .idList(idList).build();

        return franchiseeService.queryList(franchiseeQuery);
    }

    //分配电池
    @PostMapping(value = "/admin/store/bindElectricityBattery")
    public R bindElectricityBattery(@RequestBody @Validated(value = CreateGroup.class) BindElectricityBatteryQuery bindElectricityBatteryQuery){
        return franchiseeService.bindElectricityBattery(bindElectricityBatteryQuery);
    }


    //分配门店
    @PostMapping(value = "/admin/store/bindStore")
    public R bindStore(@RequestBody @Validated(value = CreateGroup.class) BindFranchiseeQuery bindFranchiseeQuery){
        return franchiseeService.bindStore(bindFranchiseeQuery);
    }

    //查询电池
    @GetMapping(value = "/admin/store/getElectricityBatteryList/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Integer id){
        return franchiseeService.getElectricityBatteryList(id);
    }

    //查询门店
    @GetMapping(value = "/admin/store/getStoreList/{id}")
    public R getStoreList(@PathVariable("id") Integer id){
        return franchiseeService.getStoreList(id);
    }





}
