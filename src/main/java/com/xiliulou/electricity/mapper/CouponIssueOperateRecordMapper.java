package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.vo.CouponIssueOperateRecordVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 优惠券规则表(t_coupon_issue_operate_record)表服务接口
 *
 * @author makejava
 * @since 2022-08-19 09:28:22
 */
public interface CouponIssueOperateRecordMapper extends BaseMapper<CouponIssueOperateRecord> {


    List<CouponIssueOperateRecord> queryList(@Param("query") CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);

    Integer queryCount(@Param("query") CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);
    List<CouponIssueOperateRecordVO> queryRecordList(@Param("query") CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);
    Integer queryRecordCount(@Param("query") CouponIssueOperateRecordQuery couponIssueOperateRecordQuery);
    
    /**
     * 根据更换手机号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
}
