package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRecordQuery;
import com.xiliulou.electricity.service.EnterpriseCloudBeanOrderService;
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
 * @date 2023-09-15-9:37
 */
@Slf4j
@RestController
public class JsonAdminEnterpriseCloudBeanOrderController extends BaseController {

    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/enterpriseCloudBeanOrder/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "orderId", required = false) String orderId,
                  @RequestParam(value = "uid", required = false) Long uid,
                  @RequestParam(value = "type", required = false) Integer type,
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

        EnterpriseCloudBeanOrderQuery query = EnterpriseCloudBeanOrderQuery.builder()
                .size(size)
                .offset(offset)
                .enterpriseId(enterpriseId)
                .orderId(orderId)
                .uid(uid)
                .type(type)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseCloudBeanOrderService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/enterpriseCloudBeanOrder/count")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "enterpriseId", required = false) Long enterpriseId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        EnterpriseCloudBeanOrderQuery query = EnterpriseCloudBeanOrderQuery.builder()
                .enterpriseId(enterpriseId)
                .orderId(orderId)
                .uid(uid)
                .type(type)
                .franchiseeId(franchiseeId)
                .tenantId(TenantContextHolder.getTenantId())
                .build();

        return R.ok(enterpriseCloudBeanOrderService.selectByPageCount(query));
    }


}
