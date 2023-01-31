package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 发起邀请活动记录(ShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@RestController
public class JsonAdminShareActivityRecordController {

	@Autowired
	private ShareActivityRecordService shareActivityRecordService;

	//列表查询
	@GetMapping(value = "/admin/shareActivityRecord/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		ShareActivityRecordQuery shareActivityRecordQuery = ShareActivityRecordQuery.builder()
				.offset(offset)
				.size(size)
				.phone(phone).name(name).tenantId(tenantId).startTime(beginTime).endTime(endTime).build();

		return shareActivityRecordService.queryList(shareActivityRecordQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/shareActivityRecord/queryCount")
	public R queryCount(@RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		ShareActivityRecordQuery shareActivityRecordQuery = ShareActivityRecordQuery.builder()
				.phone(phone).name(name).tenantId(tenantId).startTime(beginTime).endTime(endTime).build();

		return shareActivityRecordService.queryCount(shareActivityRecordQuery);
    }
    
    @GetMapping(value = "/admin/shareActivityRecord/exportExcel")
    public void shareActivityRecordExportExcel(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
	
	    ShareActivityRecordQuery shareActivityRecordQuery = ShareActivityRecordQuery.builder()
                .phone(phone).name(name).tenantId(tenantId).startTime(beginTime).endTime(endTime).build();
        
        shareActivityRecordService.shareActivityRecordExportExcel(shareActivityRecordQuery, response);
    }
}

