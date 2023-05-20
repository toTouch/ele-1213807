package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.OldUserActivity;
import com.xiliulou.electricity.mapper.OldUserActivityMapper;
import com.xiliulou.electricity.query.OldUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.OldUserActivityQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.OldUserActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.OldUserActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("oldUserActivityService")
@Slf4j
public class OldUserActivityServiceImpl implements OldUserActivityService {
    @Resource
    OldUserActivityMapper oldUserActivityMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    CouponService couponService;

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public OldUserActivity queryByIdFromCache(Integer id) {
        //先查缓存
        OldUserActivity oldUserActivityCache = redisService.getWithHash(CacheConstant.OLD_USER_ACTIVITY_CACHE + id, OldUserActivity.class);
        if (Objects.nonNull(oldUserActivityCache)) {
            return oldUserActivityCache;
        }

        //缓存没有再查数据库
        OldUserActivity oldUserActivity = oldUserActivityMapper.selectById(id);
        if (Objects.isNull(oldUserActivity)) {
            return null;
        }

        //放入缓存
        redisService.saveWithHash(CacheConstant.OLD_USER_ACTIVITY_CACHE + id, oldUserActivity);
        return oldUserActivity;
    }

    /**
     * 新增数据
     *
     * @param oldUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery) {
        //创建账号
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("Coupon  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        OldUserActivity oldUserActivity = new OldUserActivity();
        BeanUtils.copyProperties(oldUserActivityAddAndUpdateQuery, oldUserActivity);
        oldUserActivity.setUid(user.getUid());
        oldUserActivity.setUserName(user.getUsername());
        oldUserActivity.setCreateTime(System.currentTimeMillis());
        oldUserActivity.setUpdateTime(System.currentTimeMillis());
        oldUserActivity.setTenantId(tenantId);

        if (Objects.isNull(oldUserActivity.getType())) {
            oldUserActivity.setType(OldUserActivity.SYSTEM);
        }

        int insert = oldUserActivityMapper.insert(oldUserActivity);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.OLD_USER_ACTIVITY_CACHE + oldUserActivity.getId(), oldUserActivity);
            return null;
        });

        if (insert > 0) {
            return R.ok(oldUserActivity.getId());
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 修改数据(暂只支持上下架）
     *
     * @param oldUserActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(OldUserActivityAddAndUpdateQuery oldUserActivityAddAndUpdateQuery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        OldUserActivity oldOldUserActivity = queryByIdFromCache(oldUserActivityAddAndUpdateQuery.getId());

        if (Objects.isNull(oldOldUserActivity)) {
            log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", oldOldUserActivity.getId());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

        if (!Objects.equals(tenantId, oldOldUserActivity.getTenantId())) {
            return R.ok();
        }


        OldUserActivity oldUserActivity = new OldUserActivity();
        BeanUtil.copyProperties(oldUserActivityAddAndUpdateQuery, oldUserActivity);
        oldUserActivity.setUpdateTime(System.currentTimeMillis());

        int update = oldUserActivityMapper.updateById(oldUserActivity);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.NEW_USER_ACTIVITY_CACHE + oldOldUserActivity.getId());

            //解绑套餐活动
            electricityMemberCardService.unbindActivity(oldUserActivity.getId());

            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Slave
    @Override
    public R queryList(OldUserActivityQuery oldUserActivityQuery) {
        List<OldUserActivity> oldUserActivityList = oldUserActivityMapper.queryList(oldUserActivityQuery);
        if (ObjectUtil.isEmpty(oldUserActivityList)) {
            return R.ok(oldUserActivityList);
        }

        List<OldUserActivityVO> oldUserActivityVOList = new ArrayList<>();
        for (OldUserActivity oldUserActivity : oldUserActivityList) {
            OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
            BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);

            if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON)) {
                if (Objects.isNull(oldUserActivity.getCouponId())) {
                    continue;
                }

                Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                if (Objects.isNull(coupon)) {
                    log.error("queryInfo Activity  ERROR! not found coupon ! couponId:{} ", oldUserActivity.getCouponId());
                    continue;
                }

                oldUserActivityVO.setCoupon(coupon);
            }
            oldUserActivityVOList.add(oldUserActivityVO);

        }
        return R.ok(oldUserActivityVOList);

    }

    @Slave
    @Override
    public R queryCount(OldUserActivityQuery oldUserActivityQuery) {
        Integer count = oldUserActivityMapper.queryCount(oldUserActivityQuery);
        return R.ok(count);
    }


    @Override
    public R queryInfo(Integer id) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

        OldUserActivity oldUserActivity = queryByIdFromCache(id);
        if (Objects.isNull(oldUserActivity)) {
            log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

		if (!Objects.equals(tenantId, oldUserActivity.getTenantId())) {
			return R.ok();
		}

		if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON)) {
            if (Objects.isNull(oldUserActivity.getCouponId())) {
                return R.ok(oldUserActivity);
            }


            Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("queryInfo Activity  ERROR! not found coupon ! couponId:{} ", oldUserActivity.getCouponId());
                return R.ok(oldUserActivity);
            }

            OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
            BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);
            oldUserActivityVO.setCoupon(coupon);

            return R.ok(oldUserActivityVO);

        }

        return R.ok(oldUserActivity);

    }

    @Override
    public void handleActivityExpired() {
        //分页只修改200条
        List<OldUserActivity> oldUserActivityList = oldUserActivityMapper.getExpiredActivity(System.currentTimeMillis(), 0, 200);
        if (!DataUtil.collectionIsUsable(oldUserActivityList)) {
            return;
        }

        for (OldUserActivity oldUserActivity : oldUserActivityList) {
            oldUserActivity.setStatus(OldUserActivity.STATUS_OFF);
            oldUserActivity.setUpdateTime(System.currentTimeMillis());
            oldUserActivityMapper.updateById(oldUserActivity);
        }
    }

}

