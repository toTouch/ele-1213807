package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
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
 * @date 2023-09-14-10:27
 */
@RestController
@Slf4j
public class JsonAdminEnterpriseInfoController extends BaseController {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/enterpriseInfo/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
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

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.EMPTY_LIST);
        }

        EnterpriseInfoQuery query = EnterpriseInfoQuery.builder()
                .size(size)
                .offset(offset)
                .name(name)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseInfoService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/enterpriseInfo/count")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "name", required = false) String name) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        EnterpriseInfoQuery query = EnterpriseInfoQuery.builder()
                .name(name)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseInfoService.selectByPageCount(query));
    }

    /**
     * 新增
     */
    @PostMapping("/admin/enterpriseInfo")
    public R save(@RequestBody @Validated(CreateGroup.class) EnterpriseInfoQuery enterpriseInfoQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnTripleResult(enterpriseInfoService.save(enterpriseInfoQuery));
    }

    /**
     * 修改
     */
    @PutMapping("/admin/enterpriseInfo")
    public R update(@RequestBody @Validated(UpdateGroup.class) EnterpriseInfoQuery enterpriseInfoQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnTripleResult(enterpriseInfoService.modify(enterpriseInfoQuery));
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/enterpriseInfo/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnTripleResult(enterpriseInfoService.delete(id));
    }

    /**
     * 云豆充值
     */
    @PutMapping("/admin/enterpriseInfo/recharge")
    public R recharge(@RequestBody @Validated EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnTripleResult(enterpriseInfoService.recharge(enterpriseCloudBeanRechargeQuery));
    }

}
