package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.CouponIssueOperateRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponIssueOperateRecordMapper;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.query.CouponIssueOperateRecordQuery;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.CouponIssueOperateRecordService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

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

    @Slave
    @Override
    public R queryList(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryList(couponIssueOperateRecordQuery));
    }

    @Slave
    @Override
    public R queryCount(CouponIssueOperateRecordQuery couponIssueOperateRecordQuery) {
        return R.ok(couponIssueOperateRecordMapper.queryCount(couponIssueOperateRecordQuery));
    }
}
