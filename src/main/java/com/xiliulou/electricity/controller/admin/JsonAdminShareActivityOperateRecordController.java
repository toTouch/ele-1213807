package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.ShareActivityOperateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-24-15:00
 */
@Slf4j
@RestController
public class JsonAdminShareActivityOperateRecordController extends BaseController {

    @Autowired
    private ShareActivityOperateRecordService shareActivityOperateRecordService;

    @GetMapping(value = "/admin/shareActivityOperate/list")
    public R page(@RequestParam("size") long size,
                  @RequestParam("offset") long offset,
                  @RequestParam("shareActivityId") int shareActivityId,
                  @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }

        ShareActivityQuery query = ShareActivityQuery.builder()
                .offset(offset)
                .size(size)
                .id(shareActivityId)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(shareActivityOperateRecordService.page(query));
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivityOperate/count")
    public R count(@RequestParam("shareActivityId") int shareActivityId,
                   @RequestParam(value = "name", required = false) String name) {

        Triple<Boolean, String, Object> verifyPermissionResult = verifyPermission();
        if (Boolean.FALSE.equals(verifyPermissionResult.getLeft())) {
            return returnTripleResult(verifyPermissionResult);
        }

        ShareActivityQuery query = ShareActivityQuery.builder()
                .id(shareActivityId)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(shareActivityOperateRecordService.count(query));
    }

    private Triple<Boolean, String, Object> verifyPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户!");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return Triple.of(false, "ELECTRICITY.0066", "用户权限不足");
        }

        return Triple.of(true, "", null);
    }

}
