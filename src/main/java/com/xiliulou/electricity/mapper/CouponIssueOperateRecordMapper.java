package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.query.CouponQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
public interface CouponIssueOperateRecordMapper extends BaseMapper<CouponIssueOperateRecord>{


    List<CouponIssueOperateRecord> queryList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);

    Integer queryCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);

}
