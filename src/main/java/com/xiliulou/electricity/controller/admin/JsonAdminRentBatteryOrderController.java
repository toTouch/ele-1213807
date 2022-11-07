package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
@Slf4j
public class JsonAdminRentBatteryOrderController {
    /**
     * 服务对象
     */
    @Autowired
    private RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    UserTypeFactory userTypeFactory;

    //列表查询
    @GetMapping(value = "/admin/rentBatteryOrder/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "orderId", required = false) String orderId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByDataType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .type(type)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return rentBatteryOrderService.queryList(rentBatteryOrderQuery);
    }

	@GetMapping(value = "/admin/rentBatteryOrder/list/super")
	public R querySuperList(@RequestParam("size") Long size,
							@RequestParam("offset") Long offset,
							@RequestParam(value = "status", required = false) String status,
							@RequestParam(value = "type", required = false) Integer type,
							@RequestParam(value = "name", required = false) String name,
							@RequestParam(value = "phone", required = false) String phone,
							@RequestParam(value = "beginTime", required = false) Long beginTime,
							@RequestParam(value = "endTime", required = false) Long endTime,
							@RequestParam(value = "orderId", required = false) String orderId) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//用户区分
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (user.getTenantId() != 1) {
			return R.fail("权限不足");
		}

		RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.phone(phone)
				.beginTime(beginTime)
				.endTime(endTime)
				.status(status)
				.orderId(orderId)
				.type(type)
				.eleIdList(null)
				.tenantId(null).build();

		return rentBatteryOrderService.queryList(rentBatteryOrderQuery);
	}

	@GetMapping(value = "/admin/rentBatteryOrder/queryCount/super")
	public R querySuperCount(@RequestParam(value = "status", required = false) String status,
							 @RequestParam(value = "type", required = false) Integer type,
							 @RequestParam(value = "name", required = false) String name,
							 @RequestParam(value = "phone", required = false) String phone,
							 @RequestParam(value = "beginTime", required = false) Long beginTime,
							 @RequestParam(value = "endTime", required = false) Long endTime,
							 @RequestParam(value = "orderId", required = false) String orderId) {

		//用户区分
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (user.getTenantId() != 1) {
			return R.fail("权限不足");
		}

		RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
				.name(name)
				.phone(phone)
				.beginTime(beginTime)
				.endTime(endTime)
				.status(status)
				.orderId(orderId)
				.type(type)
				.eleIdList(null)
				.tenantId(null).build();

		return rentBatteryOrderService.queryCount(rentBatteryOrderQuery);
	}


	//列表查询
	@GetMapping(value = "/admin/rentBatteryOrder/queryCount")
	public R queryCount(@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "type", required = false) Integer type,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "orderId", required = false) String orderId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }

            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (CollectionUtils.isEmpty(eleIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .type(type)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return rentBatteryOrderService.queryCount(rentBatteryOrderQuery);
    }

    //租电池订单导出报表
    @GetMapping("/admin/rentBatteryOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "type", required = false) Integer type,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime,
                            @RequestParam(value = "orderId", required = false) String orderId, HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }

        List<Integer> eleIdList = null;
        if ( Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)&& Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                throw new CustomBusinessException("查不到订单");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            throw new CustomBusinessException("查不到订单");
        }

        RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .type(type)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId()).build();

        rentBatteryOrderService.exportExcel(rentBatteryOrderQuery, response);
    }

    //结束异常订单
    @PostMapping(value = "/admin/rentBatteryOrder/endOrder")
    public R endOrder(@RequestParam("orderId") String orderId) {
        return rentBatteryOrderService.endOrder(orderId);
    }

}
