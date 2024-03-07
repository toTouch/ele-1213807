package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;

import java.util.List;

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
    
    R queryRecordList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);
    
    R queryRecordCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    Integer batchInsert(List<CouponIssueOperateRecord> couponIssueOperateRecords);
}
