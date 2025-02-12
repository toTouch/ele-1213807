package com.xiliulou.electricity.mapper;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CouponPackageOperateRecord;
import com.xiliulou.electricity.query.CouponPackageOperateRecordQuery;

import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageOperateRecordMapper
 * @date : 2025-02-05 09:44
 **/
public interface CouponPackageOperateRecordMapper {

    void batchInsertRecord(List<CouponPackageOperateRecord> couponPackageOperateRecord);

    List<CouponPackageOperateRecord> queryPageRecordList(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery);

    Integer queryRecordCount(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery);
}
