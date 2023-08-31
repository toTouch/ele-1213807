package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.mapper.EnableMemberCardRecordMapper;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
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
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("enableMemberCardRecordService")
@Slf4j
public class EnableMemberCardRecordServiceImpl implements EnableMemberCardRecordService {
    @Resource
    private EnableMemberCardRecordMapper enableMemberCardRecordMapper;

    @Autowired
    RedisService redisService;


    @Override
    public R insert(EnableMemberCardRecord enableMemberCardRecord) {
        return R.ok(enableMemberCardRecordMapper.insert(enableMemberCardRecord));
    }

    @Override
    public Integer update(EnableMemberCardRecord enableMemberCardRecord) {
        return enableMemberCardRecordMapper.updateById(enableMemberCardRecord);
    }

    @Slave
    @Override
    public R queryList(EnableMemberCardRecordQuery enableMemberCardRecordQuery) {
        return R.ok(enableMemberCardRecordMapper.queryList(enableMemberCardRecordQuery));
    }

    @Slave
    @Override
    public R queryCount(EnableMemberCardRecordQuery enableMemberCardRecordQuery) {
        return R.ok(enableMemberCardRecordMapper.queryCount(enableMemberCardRecordQuery));
    }

    @Override
    public EnableMemberCardRecord queryByDisableCardNO(String disableCardNO, Integer tenantId) {
        return enableMemberCardRecordMapper.selectOne(new LambdaQueryWrapper<EnableMemberCardRecord>().eq(EnableMemberCardRecord::getDisableMemberCardNo, disableCardNO).eq(EnableMemberCardRecord::getTenantId, tenantId));
    }

    @Override
    public EnableMemberCardRecord selectLatestByUid(Long uid) {
        return enableMemberCardRecordMapper.selectLatestByUid(uid);
    }
}
