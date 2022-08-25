package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * zgw
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetTrafficController {

    @Autowired
    ElectricityCabinetTrafficService electricityCabinetTrafficService;

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetTraffic/list")
    public R queryList(@RequestParam("size") Long size,
        @RequestParam("offset") Long offset,
        @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
        @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
        @RequestParam(value = "beginTime", required = false) Long beginTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "date", required = false) LocalDate date) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        return electricityCabinetTrafficService
            .queryList(size, offset, electricityCabinetId, electricityCabinetName, date, beginTime,
                endTime);
    }

    //流量导出报表
    @GetMapping("/admin/electricityCabinetTraffic/exportExcel")
    public void exportExcel(@RequestParam("size") Long size,
        @RequestParam("offset") Long offset,
        @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
        @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
        @RequestParam(value = "beginTime", required = false) Long beginTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "date", required = false) LocalDate date,
        HttpServletResponse response) {

        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 92) {
            throw new CustomBusinessException("搜索日期不能大于3个月");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("未找到用户");
        }

        //限制解锁权限
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
            && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            log.info("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
            throw new CustomBusinessException("用户权限不足");
        }

        electricityCabinetTrafficService
            .exportExcel(size, offset, electricityCabinetId, electricityCabinetName, date,
                beginTime,
                endTime, response);
    }
}
