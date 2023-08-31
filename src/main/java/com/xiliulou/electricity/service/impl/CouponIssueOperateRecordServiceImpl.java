package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.mapper.CouponIssueOperateRecordMapper;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.service.CouponIssueOperateRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
@Service("couponIssueOperateRecordService")
@Slf4j
public class CouponIssueOperateRecordServiceImpl implements CouponIssueOperateRecordService {


    @Resource
    CouponIssueOperateRecordMapper couponIssueOperateRecordMapper;

    @Override
    public void insert(CouponIssueOperateRecord couponIssueOperateRecord) {
        couponIssueOperateRecordMapper.insert(couponIssueOperateRecord);
    }

    @Deprecated
    @Slave
    @Override
    public R queryList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryList(couponIssueOperateRecordQuery));
    }

    @Deprecated
    @Slave
    @Override
    public R queryCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryCount(couponIssueOperateRecordQuery));
    }
    @Slave
    @Override
    public R queryRecordList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryRecordList(couponIssueOperateRecordQuery));
    }
    @Slave
    @Override
    public R queryRecordCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryRecordCount(couponIssueOperateRecordQuery));
    }
}
