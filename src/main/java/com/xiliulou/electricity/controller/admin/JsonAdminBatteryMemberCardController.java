package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
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
 * @date 2023-07-07-15:18
 */
@Slf4j
@RestController
public class JsonAdminBatteryMemberCardController extends BaseController {

    @Autowired
    private BatteryMemberCardService batteryMemberCardService;
//
//    /**
//     * 分页列表
//     */
//    @GetMapping("/admin/battery/memberCard/page")
//    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
//                  @RequestParam(value = "batteryType", required = false) String batteryType) {
//        if (size < 0 || size > 50) {
//            size = 10L;
//        }
//
//        if (offset < 0) {
//            offset = 0L;
//        }
//
//        BatteryModelQuery query = BatteryModelQuery.builder()
//                .size(size)
//                .offset(offset)
//                .tenantId(TenantContextHolder.getTenantId())
//                .batteryType(batteryType)
//                .build();
//
//        return R.ok(batteryMemberCardService.selectByPage(query));
//    }
//
//    /**
//     * 分页总数
//     */
//    @GetMapping("/admin/battery/model/queryCount")
//    public R pageCount(@RequestParam(value = "batteryType", required = false) String batteryType) {
//
//        BatteryModelQuery query = BatteryModelQuery.builder()
//                .batteryType(batteryType)
//                .tenantId(TenantContextHolder.getTenantId())
//                .build();
//
//        return R.ok(batteryMemberCardService.selectByPageCount(query));
//    }
//
//    /**
//     * 新增
//     */
//    @PostMapping("/admin/battery/model")
//    public R save(@RequestBody @Validated(CreateGroup.class) BatteryModelQuery batteryModelQuery) {
//        TokenUser user = SecurityUtils.getUserInfo();
//        if (Objects.isNull(user)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//        }
//
//        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
//            return R.fail("ELECTRICITY.0066", "用户权限不足");
//        }
//
//        return returnTripleResult(batteryMemberCardService.save(batteryModelQuery));
//    }
//
//    /**
//     * 修改
//     */
//    @PutMapping("/admin/battery/model")
//    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryModelQuery batteryModelQuery) {
//        TokenUser user = SecurityUtils.getUserInfo();
//        if (Objects.isNull(user)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//        }
//
//        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
//            return R.fail("ELECTRICITY.0066", "用户权限不足");
//        }
//
//        return R.ok(batteryMemberCardService.modify(batteryModelQuery));
//    }
//
//    /**
//     * 删除
//     */
//    @DeleteMapping("/admin/battery/model/{id}")
//    public R delete(@PathVariable("id") Long id) {
//        TokenUser user = SecurityUtils.getUserInfo();
//        if (Objects.isNull(user)) {
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//        }
//
//        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
//            return R.fail("ELECTRICITY.0066", "用户权限不足");
//        }
//
//        return returnTripleResult(batteryMemberCardService.delete(id));
//    }



}
