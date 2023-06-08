package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminJoinShareActivityHistoryController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareActivityHistoryService joinShareActivityHistoryService;


	/**
	 * 用户参与记录admin
	 */
	@GetMapping(value = "/admin/joinShareActivityHistory/list")
	public R joinActivity(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset, @RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
				.offset(offset)
				.size(size).tenantId(tenantId).id(id).joinName(joinName).status(status)
                .startTime(beginTime).endTime(endTime)
                .build();
		return joinShareActivityHistoryService.queryList(jsonShareActivityHistoryQuery);
	}
    
    
    @GetMapping(value = "/admin/joinShareActivityHistory/queryCount")
    public R joinActivityCount(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
                .tenantId(tenantId).id(id).joinName(joinName).status(status)
                .startTime(beginTime).endTime(endTime).build();
        return joinShareActivityHistoryService.queryCount(jsonShareActivityHistoryQuery);
    }
    
    
    @GetMapping(value = "/admin/joinShareActivityHistory/exportExcel")
    public void joinActivityExportExcel(@RequestParam("id") Long id,
            @RequestParam(value = "joinName", required = false) String joinName,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status, HttpServletResponse response) {
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        JsonShareActivityHistoryQuery jsonShareActivityHistoryQuery = JsonShareActivityHistoryQuery.builder()
                .tenantId(tenantId).id(id).joinName(joinName).status(status).startTime(beginTime).endTime(endTime)
                .build();
        joinShareActivityHistoryService.queryExportExcel(jsonShareActivityHistoryQuery, response);
    }
}































