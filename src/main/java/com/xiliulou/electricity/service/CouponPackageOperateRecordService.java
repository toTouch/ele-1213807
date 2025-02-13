package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CouponPackageOperateRecord;
import com.xiliulou.electricity.query.CouponPackageOperateRecordQuery;

import java.util.List;

/**
 * 优惠券规则表(t_coupon_package_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
public interface CouponPackageOperateRecordService {

    /**
     * insert
     *
     * @param packageOperateRecords packageOperateRecords
     * @return:
     */

    void batchInsert(List<CouponPackageOperateRecord> packageOperateRecords);

    /**
     * 查询列表
     *
     * @param couponPackageOperateRecordQuery couponPackageOperateRecordQuery
     * @return R
     */
    R queryRecordList(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery);

    /**
     * 查询列表
     *
     * @param couponPackageOperateRecordQuery couponPackageOperateRecordQuery
     * @return R
     */
    R queryRecordCount(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery);
}
