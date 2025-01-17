package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponPackageEditQuery;

/**
 * @Description: CouponPackageService
 * @Author: renhang
 * @Date: 2025/01/16
 */

public interface CouponPackageService {

    /**
     * 新增/编辑优惠券包
     *
     * @param query query
     * @return: @return {@link R }
     */

    R addOrEdit(CouponPackageEditQuery query);


}
