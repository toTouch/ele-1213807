package com.xiliulou.electricity.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CouponPackageOperateRecord;
import com.xiliulou.electricity.mapper.CouponPackageOperateRecordMapper;
import com.xiliulou.electricity.query.CouponPackageOperateRecordQuery;
import com.xiliulou.electricity.service.CouponPackageOperateRecordService;
import com.xiliulou.electricity.vo.CouponPackageOperateRecordPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : renhang
 * @description CouponPackageOperateRecordServiceImpl
 * @date : 2025-02-05 09:42
 **/
@Slf4j
@Service
public class CouponPackageOperateRecordServiceImpl implements CouponPackageOperateRecordService {

    @Resource
    private CouponPackageOperateRecordMapper couponPackageOperateRecordMapper;

    @Override
    public void batchInsert(List<CouponPackageOperateRecord> packageOperateRecords) {
        if (CollUtil.isEmpty(packageOperateRecords)) {
            log.error("CouponPackageOperateRecord BatchInsert Error! packageOperateRecords is null");
            return;
        }
        couponPackageOperateRecordMapper.batchInsertRecord(packageOperateRecords);
    }

    @Override
    public R queryRecordList(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery) {
        List<CouponPackageOperateRecord> recordList = couponPackageOperateRecordMapper.queryPageRecordList(couponPackageOperateRecordQuery);
        if (CollUtil.isEmpty(recordList)) {
            return R.ok(ListUtil.empty());
        }

        return R.ok(BeanUtil.copyToList(recordList, CouponPackageOperateRecordPageVO.class));
    }

    @Override
    public R queryRecordCount(CouponPackageOperateRecordQuery couponPackageOperateRecordQuery) {
        return R.ok(couponPackageOperateRecordMapper.queryRecordCount(couponPackageOperateRecordQuery));
    }

}
