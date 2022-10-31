package com.xiliulou.electricity.controller.admin;

import com.xiliulou.electricity.entity.ReportManagement;
import com.xiliulou.electricity.service.ReportManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 报表管理(ReportManagement)表控制层
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */
@RestController
@RequestMapping("reportManagement")
public class JsonAdminReportManagementController {

    @Autowired
    private ReportManagementService reportManagementService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("select")
    public ReportManagement selectOne(Long id) {
        return this.reportManagementService.selectById(id);
    }

}
