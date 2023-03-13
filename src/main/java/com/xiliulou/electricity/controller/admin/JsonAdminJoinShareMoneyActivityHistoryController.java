package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminJoinShareMoneyActivityHistoryController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;


    /**
     * 用户参与记录admin
     */
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam("id") Long id, @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

		JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery.builder()
                .offset(offset).size(size).id(id).joinName(joinName).beginTime(beginTime).endTime(endTime)
                .status(status).tenantId(TenantContextHolder.getTenantId()).build();
		return joinShareMoneyActivityHistoryService.queryList(jsonShareMoneyActivityHistoryQuery);
	}


    /**
     * 用户参与记录admin
     */
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/queryCount")
    public R queryCount(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {
    
        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery
                .builder().id(id).joinName(joinName).beginTime(beginTime).endTime(endTime).status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();
		return joinShareMoneyActivityHistoryService.queryCount(jsonShareMoneyActivityHistoryQuery);
	}
    
    @GetMapping(value = "/admin/joinShareMoneyActivityHistory/exportExcel")
    public R queryExportExcel(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, HttpServletResponse response) {
        JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery
                .builder().id(id).joinName(joinName).beginTime(beginTime).endTime(endTime).status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return joinShareMoneyActivityHistoryService.queryExportExcel(jsonShareMoneyActivityHistoryQuery, response);
    }
}































