package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.query.CouponPackagePageQuery;
import com.xiliulou.electricity.service.CouponPackageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * 新增/编辑优惠券包
     *
     * @param query query
     * @return R
     */
    @PostMapping(value = "addOrEdit")
    public R addOrEdit(@RequestBody @Validated CouponPackageEditQuery query) {
        return couponPackageService.addOrEdit(query);
    }


    /**
     * 编辑回显
     *
     * @param packageId query
     * @return R
     */
    @GetMapping(value = "editEcho")
    public R editEcho(@RequestParam("packageId") Long packageId) {
        return R.ok(couponPackageService.editEcho(packageId));
    }


    /**
     * 删除
     *
     * @param packageId query
     * @return R
     */
    @GetMapping(value = "del")
    public R del(@RequestParam("packageId") Long packageId) {
        couponPackageService.del(packageId);
        return R.ok();
    }


    /**
     * 分页list
     *
     * @param query query
     * @return: @return {@link R }
     */
    @PostMapping(value = "pageList")
    public R pageList(@RequestBody CouponPackagePageQuery query) {
        return couponPackageService.pageList(query);
    }


    /**
     * 分页count
     *
     * @param query query
     * @return: @return {@link R }
     */
    @PostMapping(value = "pageCount")
    public R pageCount(@RequestBody CouponPackagePageQuery query) {
        return couponPackageService.pageCount(query);
    }

}
