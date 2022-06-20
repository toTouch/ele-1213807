package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleBindCarRecordQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.EleBindCarRecordService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2022-06-08 15:09:08
 */
@RestController
@Slf4j
public class JsonAdminEleBindCarRecordController {

    @Autowired
    EleBindCarRecordService eleBindCarRecordService;


    //列表查询
    @GetMapping(value = "/admin/bindCarRecord/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "operateName", required = false) String operateName,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "bindTime", required = false) Long bindTime,
                       @RequestParam(value = "id", required = false) Integer id,
                       @RequestParam(value = "carId", required = false) Integer carId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        EleBindCarRecordQuery eleBindCarRecordQuery = EleBindCarRecordQuery.builder()
                .offset(offset)
                .size(size)
                .bindTime(bindTime)
                .operateName(operateName)
                .phone(phone)
                .sn(sn)
                .id(id)
				.carId(carId)
                .tenantId(tenantId).build();

        return eleBindCarRecordService.queryList(eleBindCarRecordQuery);
    }

	//列表查询
	@GetMapping(value = "/admin/bindCarRecord/queryCount")
	public R queryCount(@RequestParam(value = "operateName", required = false) String operateName,
					   @RequestParam(value = "sn", required = false) String sn,
					   @RequestParam(value = "phone", required = false) String phone,
					   @RequestParam(value = "bindTime", required = false) Long bindTime,
					   @RequestParam(value = "id", required = false) Integer id,
					   @RequestParam(value = "carId", required = false) Integer carId) {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户区分
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}


		EleBindCarRecordQuery eleBindCarRecordQuery = EleBindCarRecordQuery.builder()
				.bindTime(bindTime)
				.operateName(operateName)
				.phone(phone)
				.sn(sn)
				.id(id)
				.carId(carId)
				.tenantId(tenantId).build();

		return eleBindCarRecordService.queryCount(eleBindCarRecordQuery);
	}

}
