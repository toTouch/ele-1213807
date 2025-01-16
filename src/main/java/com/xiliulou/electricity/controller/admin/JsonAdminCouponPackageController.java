package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.service.CouponPackageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @Description: 优惠券包Controller
 * @Author: renhang
 * @Date: 2025/01/16
 */

@RestController
@RequestMapping("/admin/couponPackage")
public class JsonAdminCouponPackageController {

    @Resource
    private CouponPackageService couponPackageService;


    /**
     * 新增优惠券包
     *
     * @param query query
     * @return R
     */
    @PostMapping(value = "add")
    public R add(@RequestBody @Validated CouponPackageEditQuery query) {
        return couponPackageService.add(query);
    }


    /**
     * 编辑优惠券包
     *
     * @param query query
     * @return R
     */
    @PostMapping(value = "edit")
    public R edit(@RequestBody @Validated CouponPackageEditQuery query) {
        return couponPackageService.edit(query);
    }

}
