package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ReportManagementQuery;
import com.xiliulou.electricity.service.ReportManagementService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 报表管理(ReportManagement)表控制层
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */
@RestController
@RequestMapping("/admin/reportManagement/")
public class JsonAdminReportManagementController extends BaseController {

    @Autowired
    private ReportManagementService reportManagementService;

    /**
     * 分页
     */
    @GetMapping("page")
    public R page(@RequestParam("size") Long size,
                  @RequestParam("offset") Long offset,
                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                  @RequestParam(value = "endTime", required = false) Long endTime) {
        ReportManagementQuery query = new ReportManagementQuery();
        query.setOffset(offset);
        query.setSize(size);
        query.setStartTime(beginTime);
        query.setEndTime(endTime);

        if (!SecurityUtils.isAdmin()) {
            query.setTenantId(TenantContextHolder.getTenantId());
        }

         return R.ok(this.reportManagementService.selectByPage(query));
    }

    /**
     * 总条数
     */
    @GetMapping("count")
    public R selectOne(
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {

        ReportManagementQuery query = new ReportManagementQuery();
        query.setStartTime(beginTime);
        query.setEndTime(endTime);

        if (!SecurityUtils.isAdmin()) {
            query.setTenantId(TenantContextHolder.getTenantId());
        }
        return R.ok(this.reportManagementService.selectByPageCount(query));
    }

    @DeleteMapping("delete/{id}")
    public R delete(@PathVariable("id") Long id){
        ReportManagementQuery query = new ReportManagementQuery();
        query.setId(id);
        if (!SecurityUtils.isAdmin()) {
            query.setTenantId(TenantContextHolder.getTenantId());
        }

        return R.ok(this.reportManagementService.deleteByQuery(query));
    }

}
