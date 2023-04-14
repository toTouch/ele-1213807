package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryMaterialQuery;
import com.xiliulou.electricity.service.BatteryMaterialService;
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
 * @date 2023-04-11-11:36
 */
@Slf4j
@RestController
public class JsonAdminBatteryMaterialController extends BaseController {

    @Autowired
    private BatteryMaterialService batteryMaterialService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/battery/material/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        BatteryMaterialQuery query = BatteryMaterialQuery.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .build();

        return R.ok(batteryMaterialService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/battery/material/count")
    public R pageCount(@RequestParam(value = "name", required = false) String name) {

        BatteryMaterialQuery query = BatteryMaterialQuery.builder()
                .name(name)
                .build();
        return R.ok(batteryMaterialService.selectByPageCount(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/battery/material/search")
    public R search(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                    @RequestParam(value = "name", required = false) String name) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        BatteryMaterialQuery query = BatteryMaterialQuery.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .build();
        return R.ok(batteryMaterialService.selectBySearch(query));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/battery/material")
    public R save(@RequestBody @Validated(CreateGroup.class) BatteryMaterialQuery batteryMaterialQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! add batteryMaterial no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return R.ok(batteryMaterialService.save(batteryMaterialQuery));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/battery/material")
    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryMaterialQuery batteryMaterialQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! update batteryMaterial no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/battery/material/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("ELE ERROR! delete batteryMaterial no authority!");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return R.ok(batteryMaterialService.delete(id));
    }

}
