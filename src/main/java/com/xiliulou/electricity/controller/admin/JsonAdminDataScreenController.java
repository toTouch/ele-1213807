package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.DataScreenService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 数据大屏
 *
 * @author hrp
 * @date 2022/3/22 10:35
 * @mood
 */
@RestController
@Slf4j
public class JsonAdminDataScreenController {

    @Autowired
    DataScreenService dataScreenService;

    /**
     * 大屏订单统计
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/admin/dataScreen/order")
    public R queryOrderStatistics(@RequestParam(value = "tenantId", required = false) Integer tenantId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryOrderStatistics(tenantId);
    }


    /**
     * 大屏订单统计
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/admin/dataScreen/dataBrowsing")
    public R dataBrowsing(@RequestParam(value = "tenantId", required = false) Integer tenantId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryDataBrowsing(tenantId);
    }

    /**
     * 大屏地图省份统计
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/admin/dataScreen/map")
    public R mapProvince(@RequestParam(value = "tenantId", required = false) Integer tenantId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryMapProvince(tenantId);
    }

    /**
     * 大屏地图城市统计
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/admin/dataScreen/mapCity")
    public R mapCity(@RequestParam(value = "tenantId", required = false) Integer tenantId, @RequestParam(value = "pid") Integer pid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryMapCity(tenantId, pid);
    }

    /**
     * 大屏优惠券统计
     *
     * @param tenantId
     * @return
     */
    @GetMapping("/admin/dataScreen/coupon")
    public R couponStatistic(@RequestParam(value = "tenantId", required = false) Integer tenantId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryCoupon(tenantId);
    }

    @GetMapping("/admin/dataScreen/turnoverAndUser")
    public R TurnoverAndUserStatistic(@RequestParam(value = "tenantId", required = false) Integer tenantId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("dataScreen  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.error("dataScreen  ERROR! user not permissions userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return dataScreenService.queryTurnoverAndUser(tenantId);
    }

}
