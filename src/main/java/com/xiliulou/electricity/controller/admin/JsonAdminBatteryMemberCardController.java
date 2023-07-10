package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
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

    @Autowired
    UserDataScopeService userDataScopeService;

    /**
     * 搜索
     *
     */
    @GetMapping("/admin/battery/memberCard/search")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "name", required = false) String name,
                  @RequestParam(value = "display", required = false) Integer display,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .status(status)
                .display(display)
                .name(name)
                .build();

        return R.ok(batteryMemberCardService.search(query));
    }

    /**
     * 分页列表
     */
    @GetMapping("/admin/battery/memberCard/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                  @RequestParam(value = "status", required = false) Integer status,
                  @RequestParam(value = "rentType", required = false) Integer rentType,
                  @RequestParam(value = "name", required = false) String name) {
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

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .status(status)
                .rentType(rentType)
                .name(name)
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .build();

        return R.ok(batteryMemberCardService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/battery/memberCard/queryCount")
    public R pageCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "rentType", required = false) Integer rentType,
                       @RequestParam(value = "name", required = false) String name) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .status(status)
                .rentType(rentType)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId())
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .build();

        return R.ok(batteryMemberCardService.selectByPageCount(query));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/battery/memberCard")
    public R save(@RequestBody @Validated(CreateGroup.class) BatteryMemberCardQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(batteryMemberCardService.save(query));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/battery/memberCard")
    public R update(@RequestBody @Validated(UpdateGroup.class) BatteryMemberCardQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return R.ok(batteryMemberCardService.modify(query));
    }

    /**
     * 上下架
     */
    @PutMapping("/admin/battery/memberCard/shelf")
    public R update(@RequestBody @Validated BatteryMemberCardStatusQuery batteryModelQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(batteryMemberCardService.updateStatus(batteryModelQuery));
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/battery/memberCard/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return returnTripleResult(batteryMemberCardService.delete(id));
    }


}
