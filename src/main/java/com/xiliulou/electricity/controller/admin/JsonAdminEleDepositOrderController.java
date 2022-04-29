package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class JsonAdminEleDepositOrderController {
    /**
     * 服务对象
     */
    @Autowired
    EleDepositOrderService eleDepositOrderService;

    @Autowired
    FranchiseeService franchiseeService;

    //列表查询
    @GetMapping(value = "/admin/eleDepositOrder/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "orderId", required = false) String orderId,
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
            franchiseeId = franchisee.getId();
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .tenantId(tenantId)
                .franchiseeName(franchiseeName)
                .franchiseeId(franchiseeId).build();

        return eleDepositOrderService.queryList(eleDepositOrderQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/eleDepositOrder/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "franchiseeName", required = false) String franchiseeName) {

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
                return R.ok(0);
            }
            franchiseeId = franchisee.getId();
        }

        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .tenantId(tenantId)
                .franchiseeName(franchiseeName)
                .franchiseeId(franchiseeId).build();

        return eleDepositOrderService.queryCount(eleDepositOrderQuery);
    }

    //押金订单导出报表
    @GetMapping("/admin/eleDepositOrder/exportExcel")
    public void exportExcel(@RequestParam(value = "status", required = false) Integer status,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "orderId", required = false) String orderId,
                            @RequestParam(value = "beginTime", required = false) Long beginTime,
                            @RequestParam(value = "endTime", required = false) Long endTime, HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }

        Long franchiseeId = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.isNull(franchisee)) {
                throw new CustomBusinessException("查不到订单");
            }
            franchiseeId = franchisee.getId();
        }


        EleDepositOrderQuery eleDepositOrderQuery = EleDepositOrderQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .orderId(orderId)
                .tenantId(tenantId)
                .franchiseeId(franchiseeId).build();
        eleDepositOrderService.exportExcel(eleDepositOrderQuery, response);
    }


}
