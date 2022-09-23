package com.xiliulou.electricity.service;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.query.CouponQuery;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
public interface CouponIssueOperateRecordService {

    void insert(CouponIssueOperateRecord couponIssueOperateRecord);

    R queryList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);

    R queryCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);

}
