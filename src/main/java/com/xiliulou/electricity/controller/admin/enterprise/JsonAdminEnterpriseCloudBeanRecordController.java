package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRecordQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-13:53
 */
@Slf4j
@RestController
public class JsonAdminEnterpriseCloudBeanRecordController extends BaseController {

    @Autowired
    private EnterpriseCloudBeanRecordService enterpriseCloudBeanRecordService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/enterpriseCloudBeanRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                  @RequestParam(value = "enterpriseId", required = false) Long enterpriseId) {
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

        EnterpriseCloudBeanRecordQuery query = EnterpriseCloudBeanRecordQuery.builder()
                .size(size)
                .offset(offset)
                .enterpriseId(enterpriseId)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseCloudBeanRecordService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/enterpriseCloudBeanRecord/count")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "enterpriseId", required = false) Long enterpriseId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        EnterpriseCloudBeanRecordQuery query = EnterpriseCloudBeanRecordQuery.builder()
                .enterpriseId(enterpriseId)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseCloudBeanRecordService.selectByPageCount(query));
    }
}
