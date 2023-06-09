package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
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
    private UserCouponService userCouponService;
    @Autowired
    RedisService redisService;
    @Autowired
    private ShareActivityRuleService shareActivityRuleService;
    @Autowired
    private OldUserActivityService oldUserActivityService;
    @Autowired
    private NewUserActivityService newUserActivityService;


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


//        //判断参数
//        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
//            coupon.setType(Coupon.TYPE_FRANCHISEE);
//            if (Objects.isNull(coupon.getFranchiseeId())) {
//                log.error("Coupon  ERROR! not found FranchiseeId ");
//                return R.fail("ELECTRICITY.0094", "加盟商不能为空");
//            }
//        } else {
//            if (Objects.equals(coupon.getType(), Coupon.TYPE_FRANCHISEE)) {
//                if (Objects.isNull(coupon.getFranchiseeId())) {
//                    log.error("Coupon  ERROR! not found FranchiseeId ");
//                    return R.fail("ELECTRICITY.0094", "加盟商不能为空");
//                }
//            }
//        }
    
    
        //判断参数
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
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
        if (Objects.isNull(oldCoupon) || !Objects.equals(oldCoupon.getTenantId() ,TenantContextHolder.getTenantId())) {
            log.error("update Coupon  ERROR! not found coupon ! couponId={} ", coupon.getId());
            return R.fail("ELECTRICITY.00104", "找不到优惠券");
        }

        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(coupon.getId());
        couponUpdate.setSuperposition(coupon.getSuperposition());
        couponUpdate.setName(coupon.getName());
        couponUpdate.setDelFlag(coupon.getDelFlag());
        couponUpdate.setUpdateTime(System.currentTimeMillis());

        int update = couponMapper.updateById(couponUpdate);
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

    @Slave
    @Override
    public R queryList(CouponQuery couponQuery) {
        return R.ok(couponMapper.queryList(couponQuery));
    }

    @Slave
    @Override
    public R queryCount(CouponQuery couponQuery) {
        return R.ok(couponMapper.queryCount(couponQuery));
    }

    @Override
    public Triple<Boolean, String, Object> deleteById(Long id) {
        Coupon coupon = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }

        List<UserCoupon> userCoupons = userCouponService.selectCouponUserCountById(id);
        if (!CollectionUtils.isEmpty(userCoupons)) {
            return Triple.of(false, "", "删除失败，优惠券已有用户领取");
        }

        ShareActivityRule shareActivityRule = shareActivityRuleService.selectByCouponId(id);
        if (Objects.nonNull(shareActivityRule)) {
            return Triple.of(false, "", "删除失败，优惠券已绑定邀请好友活动");
        }

        OldUserActivity oldUserActivity = oldUserActivityService.selectByCouponId(id);
        if(Objects.nonNull(oldUserActivity)){
            return Triple.of(false, "", "删除失败，优惠券已绑定套餐活动");
        }

        NewUserActivity newUserActivity=newUserActivityService.selectByCouponId(id);
        if(Objects.nonNull(newUserActivity)){
            return Triple.of(false, "", "删除失败，优惠券已绑定新用户活动");
        }

        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(id.intValue());
        couponUpdate.setDelFlag(Coupon.DEL_DEL);
        couponUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(couponUpdate);

        return Triple.of(true, "", "删除成功！");
    }
}
