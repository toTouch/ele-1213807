package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 保险订单表(TInsuranceOrder)表控制层
 *
 * @author makejava
 * @since 2022-11-04 10:56:56
 */
@RestController
@Slf4j
public class JsonAdminInsuranceOrderController {
    /**
     * 服务对象
     */
    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    FranchiseeService franchiseeService;

    //保险订单查询
    @GetMapping("/admin/insuranceOrder/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "userName", required = false) String userName) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long franchiseeId = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.isNull(franchisee)) {
                return R.ok(new ArrayList<>());
            }
            franchiseeId=franchisee.getId();
        }

        InsuranceOrderQuery insuranceOrderQuery=InsuranceOrderQuery.builder()
                .orderId(orderId)
                .beginTime(beginTime)
                .endTime(endTime)
                .franchiseeId(franchiseeId)
                .franchiseeName(franchiseeName)
                .tenantId(tenantId)
                .phone(phone)
                .status(status)
                .userName(userName)
                .offset(offset)
                .size(size).build();


        return insuranceOrderService.queryList(insuranceOrderQuery);
    }


    //保险订单查询
    @GetMapping("/admin/insuranceOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "userName", required = false) String userName) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long franchiseeId = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.isNull(franchisee)) {
                return R.ok(new ArrayList<>());
            }
            franchiseeId=franchisee.getId();
        }

        InsuranceOrderQuery insuranceOrderQuery=InsuranceOrderQuery.builder()
                .orderId(orderId)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .franchiseeId(franchiseeId)
                .franchiseeName(franchiseeName)
                .tenantId(tenantId)
                .phone(phone)
                .userName(userName).build();


        return insuranceOrderService.queryCount(insuranceOrderQuery);
    }


}
