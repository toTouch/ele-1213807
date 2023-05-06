package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-24-16:24
 */
@Slf4j
@RestController
public class JsonAdminDivisionAccountRecordController extends BaseController {

    @Autowired
    private DivisionAccountRecordService divisionAccountRecordService;

    /**
     * 分页列表
     */
    @GetMapping("/admin/division/account/record/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
                  @RequestParam(value = "membercardName", required = false) String membercardName,
                  @RequestParam(value = "source", required = false) Integer source,
                  @RequestParam(value = "divisionAccountConfigId", required = false) Long divisionAccountConfigId,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
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
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountRecordQuery query = DivisionAccountRecordQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .membercardName(membercardName)
                .divisionAccountConfigId(divisionAccountConfigId)
                .source(source)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();

        return R.ok(divisionAccountRecordService.selectByPage(query));
    }

    /**
     * 分页总数
     */
    @GetMapping("/admin/division/account/record/count")
    public R pageCount(@RequestParam(value = "membercardName", required = false) String membercardName,
                       @RequestParam(value = "source", required = false) Integer source,
                       @RequestParam(value = "divisionAccountConfigId", required = false) Long divisionAccountConfigId,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountRecordQuery query = DivisionAccountRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .membercardName(membercardName)
                .divisionAccountConfigId(divisionAccountConfigId)
                .source(source)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();

        return R.ok(divisionAccountRecordService.selectByPageCount(query));
    }

    /**
     * 分帐记录统计
     */
    @GetMapping("/admin/division/account/statistic/page")
    public R statisticPage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                           @RequestParam(value = "membercardName", required = false) String membercardName,
                           @RequestParam(value = "divisionAccountConfigId", required = false) Long divisionAccountConfigId,
                           @RequestParam(value = "beginTime", required = false) Long beginTime,
                           @RequestParam(value = "endTime", required = false) Long endTime) {
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
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountRecordQuery query = DivisionAccountRecordQuery.builder()
                .size(size)
                .offset(offset)
                .tenantId(TenantContextHolder.getTenantId())
                .membercardName(membercardName)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();

        return R.ok(divisionAccountRecordService.selectStatisticByPage(query));
    }


    @GetMapping("/admin/division/account/statistic/count")
    public R statisticPageCount(@RequestParam(value = "membercardName", required = false) String membercardName,
                                @RequestParam(value = "divisionAccountConfigId", required = false) Long divisionAccountConfigId,
                                @RequestParam(value = "beginTime", required = false) Long beginTime,
                                @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        DivisionAccountRecordQuery query = DivisionAccountRecordQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .membercardName(membercardName)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();

        return R.ok(divisionAccountRecordService.selectStatisticByPageCount(query));
    }
    
    /**
     * 分帐 （预留接口）
     */
    @PostMapping("/admin/division/account/compensation")
    public R divisionAccountCompensation(@RequestParam("orderId") String orderId, @RequestParam("type") Integer type) {
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        return  returnTripleResult(divisionAccountRecordService.divisionAccountCompensation(orderId,type));
    }
    
    
    

}
