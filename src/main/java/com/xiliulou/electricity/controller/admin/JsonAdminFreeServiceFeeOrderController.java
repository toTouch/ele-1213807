package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FreeServiceFeePageQuery;
import com.xiliulou.electricity.service.FreeServiceFeeOrderService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author : renhang
 * @description JsonAdminFreeServiceFeeOrderController
 * @date : 2025-03-28 09:10
 **/
@RestController
@RequestMapping("admin/freeServiceFee")
public class JsonAdminFreeServiceFeeOrderController {

    @Resource
    private FreeServiceFeeOrderService freeServiceFeeOrderService;


    /**
     * pageList
     *
     * @param query query
     * @return: @return {@link R }
     */

    @PostMapping("page")
    public R pageList(@RequestBody FreeServiceFeePageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(freeServiceFeeOrderService.pageList(query));
    }

    /**
     * count
     *
     * @param query query
     * @return: @return {@link R }
     */

    @PostMapping("count")
    public R count(@RequestBody FreeServiceFeePageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(freeServiceFeeOrderService.count(query));
    }
}
