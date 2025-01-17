package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.query.CouponPackagePageQuery;
import com.xiliulou.electricity.vo.CouponPackageDetailsVO;

import java.util.List;

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


    /**
     * 编辑回显获取详情
     *
     * @param packageId packageId
     * @return: @return {@link CouponPackageDetailsVO }
     */

    CouponPackageDetailsVO editEcho(Long packageId);

    /**
     * 删除优惠券包
     *
     * @param packageId packageId
     * @return:
     */
    void del(Long packageId);

    /**
     * 分页list
     *
     * @param query query
     * @return: @return {@link R }
     */

    R pageList(CouponPackagePageQuery query);

    /**
     * 分页count
     *
     * @param query query
     * @return: @return {@link R }
     */
    R<Integer> pageCount(CouponPackagePageQuery query);
}
