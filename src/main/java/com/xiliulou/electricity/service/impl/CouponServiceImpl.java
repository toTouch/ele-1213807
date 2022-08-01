package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.query.CouponQuery;
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
 * 优惠券规则表(TCoupon)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@Service("couponService")
@Slf4j
public class CouponServiceImpl implements CouponService {
    @Resource
    private CouponMapper couponMapper;

    @Autowired
    RedisService redisService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Coupon queryByIdFromCache(Integer id) {
        //先查缓存
        Coupon couponCache = redisService.getWithHash(CacheConstant.COUPON_CACHE + id, Coupon.class);
        if (Objects.nonNull(couponCache)) {
            return couponCache;
        }


        //缓存没有再查数据库
        Coupon coupon = couponMapper.selectById(id);
        if (Objects.isNull(coupon)) {
            return null;
        }


        //放入缓存
        redisService.saveWithHash(CacheConstant.COUPON_CACHE + id, coupon);
        return coupon;
    }

    /**
     * 新增数据
     *
     * @param coupon 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(Coupon coupon) {
        //创建账号
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("Coupon  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        //判断参数
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            coupon.setType(Coupon.TYPE_FRANCHISEE);
            if (Objects.isNull(coupon.getFranchiseeId())) {
                log.error("Coupon  ERROR! not found FranchiseeId ");
                return R.fail("ELECTRICITY.0094", "加盟商不能为空");
            }
        } else {
            if (Objects.equals(coupon.getType(), Coupon.TYPE_FRANCHISEE)) {
                if (Objects.isNull(coupon.getFranchiseeId())) {
                    log.error("Coupon  ERROR! not found FranchiseeId ");
                    return R.fail("ELECTRICITY.0094", "加盟商不能为空");
                }
            }
        }


        //参数判断
        if (Objects.equals(coupon.getDiscountType(), Coupon.FULL_REDUCTION)) {
            if (Objects.isNull(coupon.getAmount())) {
                return R.fail("ELECTRICITY.0072", "减免金额不能为空");
            }
        }

        if (Objects.equals(coupon.getDiscountType(), Coupon.DISCOUNT)) {
            if (Objects.isNull(coupon.getDiscount())) {
                return R.fail("ELECTRICITY.0073", "打折折扣不能为空");
            }
        }

        if (Objects.equals(coupon.getDiscountType(), Coupon.EXPERIENCE)) {
            if (Objects.isNull(coupon.getCount())) {
                return R.fail("ELECTRICITY.0074", "天数不能为空");
            }
        }


        coupon.setUid(user.getUid());
        coupon.setUserName(user.getUsername());
        coupon.setCreateTime(System.currentTimeMillis());
        coupon.setUpdateTime(System.currentTimeMillis());
        coupon.setTenantId(tenantId);

        if(Objects.isNull(coupon.getStatus())){
            coupon.setStatus(Coupon.STATUS_OFF);
        }



        //先默认为自营活动 以后需要前端传值 TODO
        if (Objects.isNull(coupon.getType())) {
            coupon.setType(Coupon.TYPE_SYSTEM);
        }


        int insert = couponMapper.insert(coupon);


        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 修改数据
     *
     * @param coupon 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(Coupon coupon) {
        Coupon oldCoupon = queryByIdFromCache(coupon.getId());
        if (Objects.isNull(oldCoupon)) {
            log.error("update Coupon  ERROR! not found coupon ! couponId:{} ", coupon.getId());
            return R.fail("ELECTRICITY.00104", "找不到优惠券");
        }


        BeanUtil.copyProperties(coupon, oldCoupon);
        oldCoupon.setUpdateTime(System.currentTimeMillis());


        int update = couponMapper.updateById(oldCoupon);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.COUPON_CACHE + oldCoupon.getId(), oldCoupon);
            return null;
        });


        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");

    }


    @Override
    public R queryList(CouponQuery couponQuery) {
        return R.ok(couponMapper.queryList(couponQuery));
    }


    @Override
    public R queryCount(CouponQuery couponQuery) {
        return R.ok(couponMapper.queryCount(couponQuery));
    }

}
