package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.query.asset.BatteryModelQueryModel;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-11-11:35
 */
@Slf4j
@RestController
public class JsonAdminBatteryModelController extends BaseController {

    @Autowired
    private BatteryModelService batteryModelService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/battery/model/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "batteryType", required = false) String batteryType,
                  @RequestParam(value = "brandName", required = false) String brandName) {
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

        BatteryModelQuery query = BatteryModelQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .batteryType(batteryType)
                .brandName(brandName)
                .build();

        return R.ok(batteryModelService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/battery/model/count")
    public R pageCount(@RequestParam(value = "batteryType", required = false) String batteryType,
                       @RequestParam(value = "brandName", required = false) String brandName) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        BatteryModelQuery query = BatteryModelQuery.builder()
                .batteryType(batteryType)
                .brandName(brandName)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(batteryModelService.selectByPageCount(query));
    }

    /**
     * 获取租户所有电池型号
     */
    @GetMapping("/admin/battery/model/all")
    public R selectBatteryTypeAll() {
        return R.ok(batteryModelService.selectBatteryTypeAll());
    }

    /**
     * 获取租户所有电池型号，包含标准型号
     */
    @GetMapping("/admin/simple/battery/model/list")
    public R simpleBatteryTypeList() {
        return R.ok(batteryModelService.simpleBatteryTypeList());
    }

    @GetMapping("/admin/simple/battery/model/all")
    public R selectBatteryVAll() {
        return R.ok(batteryModelService.selectBatteryVAll());
    }

    /**
     * 获取用户自定义的电池型号
     *
     * @param batteryType
     * @return
     */
    @GetMapping("/admin/battery/type/customize")
    public R customizeBatteryType(@RequestParam(value = "batteryType", required = false) String batteryType) {

        BatteryModelQuery query = BatteryModelQuery.builder()
                .batteryType(batteryType)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(batteryModelService.selectCustomizeBatteryType(query));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/battery/model")
    public R save(@RequestBody @Validated(CreateGroup.class) BatteryModelQuery batteryModelQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(batteryModelService.save(batteryModelQuery));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/battery/model")
    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryModelQuery batteryModelQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return R.ok(batteryModelService.modify(batteryModelQuery));
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/battery/model/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(batteryModelService.delete(id));
    }
    
    /**
     * 根据modelId查询电池
     */
    @GetMapping("/admin/battery/model/{id}")
    public R queryBatteryModelById(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(batteryModelService.queryBatteryModelById(id));
    }
    
    /**
     * 根据modelId查询电池
     */
    @GetMapping("/admin/battery/brandAndModel/list")
    public R queryBatteryBrandAndModel(@RequestParam("size") Long size, @RequestParam("offset") Long offset) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        BatteryModelQueryModel batteryModelQueryModel = BatteryModelQueryModel.builder().offset(offset).size(size).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(batteryModelService.listBatteryBrandAndModel(batteryModelQueryModel));
    }
}
