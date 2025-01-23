package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.UUID;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.CouponPackageItemBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.CouponTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.CouponPackageMapper;
import com.xiliulou.electricity.query.CouponPackageEditQuery;
import com.xiliulou.electricity.query.CouponPackagePageQuery;
import com.xiliulou.electricity.request.CouponPackageAppointReleaseRequest;
import com.xiliulou.electricity.request.CouponPackageBatchReleaseRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CouponPackageBatchReleaseVO;
import com.xiliulou.electricity.vo.CouponPackageDetailsVO;
import com.xiliulou.electricity.vo.CouponPackagePageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author : renhang
 * @description CouponPackageServiceImpl
 * @date : 2025-01-16 14:59
 **/
@Slf4j
@Service
@SuppressWarnings("all")
public class CouponPackageServiceImpl implements CouponPackageService {

    @Resource
    private CouponPackageMapper couponPackageMapper;

    @Resource
    private CouponService couponService;

    @Resource
    private CouponPackageItemService packageItemService;

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private RedisService redisService;

    TtlXllThreadPoolExecutorServiceWrapper execute = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("COUPON_PACKAGE_RELEASE_POOL", 2, "coupon_package_release_thread"));


    private void checkCouponAndBuildPackageItem(List<CouponPackageEditQuery.CouponPackageItemQuery> list, Long franchiseeId, List<CouponPackageItem> itemList, AtomicReference<Integer> sumCount) {
        list.forEach(item -> {
            Coupon coupon = couponService.queryByIdFromCache(item.getCouponId().intValue());
            if (Objects.isNull(coupon)) {
                throw new BizException("ELECTRICITY.0085", "未找到优惠券，请检查");
            }

            String errorMsgName = String.format("[%s]", coupon.getName());
            if (Objects.isNull(coupon.getFranchiseeId()) || !Objects.equals(Long.valueOf(coupon.getFranchiseeId()), franchiseeId)) {
                throw new BizException("402021", errorMsgName + "优惠券加盟商不一致");
            }

            if (Objects.equals(coupon.getEnabledState(), Coupon.COUPON_UNABLE_STATUS)) {
                throw new BizException("402028", errorMsgName + "优惠券已禁用，请检查选择的优惠券状态");
            }
            if (Objects.equals(coupon.getDelFlag(), Coupon.DEL_DEL)) {
                throw new BizException("402022", errorMsgName + "优惠券已删除，请检查选择的优惠券状态");
            }

            CouponPackageItem.CouponPackageItemBuilder itemBuilder = CouponPackageItem.builder().couponId(item.getCouponId())
                    .couponName(coupon.getName()).discountType(coupon.getDiscountType()).superposition(coupon.getSuperposition())
                    .days(coupon.getDays()).count(item.getCount()).tenantId(coupon.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis());
            if (Objects.equals(coupon.getDiscountType(), Coupon.FULL_REDUCTION)) {
                // 减免券
                itemBuilder.discount(coupon.getAmount().doubleValue()).effect(String.format("减免%s元", coupon.getAmount()));
            } else if (Objects.equals(coupon.getDiscountType(), Coupon.DAY_VOUCHER)) {
                // 天数券
                itemBuilder.discount(coupon.getCount().doubleValue()).effect(String.format("赠送%d天", coupon.getCount()));
            } else {
                throw new BizException("402023", "不支持的优惠券");
            }
            itemList.add(itemBuilder.build());

            sumCount.updateAndGet(v -> v + item.getCount());
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addOrEdit(CouponPackageEditQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(query.getIsCanBuy(), CouponPackage.CAN_BUY) && Objects.isNull(query.getAmount())) {
            return R.fail("402020", "可购买时，购买金额不能为空");
        }

        // 优惠券校验（加盟商，禁用，删除）
        List<CouponPackageItem> itemList = CollUtil.newArrayList();
        AtomicReference<Integer> sumCount = new AtomicReference<>(0);
        checkCouponAndBuildPackageItem(query.getItemList(), query.getFranchiseeId(), itemList, sumCount);

        if (CollUtil.isEmpty(itemList)) {
            return R.fail("402024", "优惠券为空");
        }

        CouponPackage.CouponPackageBuilder packageBuilder = CouponPackage.builder().name(query.getName()).couponCount(sumCount.get()).isCanBuy(query.getIsCanBuy())
                .amount(Objects.equals(query.getIsCanBuy(), CouponPackage.CAN_BUY) ? BigDecimal.valueOf(query.getAmount()) : BigDecimal.ZERO)
                .userName(user.getUsername()).tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(query.getFranchiseeId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis());

        Long packageId = null;
        if (Objects.isNull(query.getId())) {
            CouponPackage couponPackage = packageBuilder.build();
            couponPackageMapper.saveCouponPackage(couponPackage);
            packageId = couponPackage.getId();
        } else {
            CouponPackage couponPackage = queryByIdFromCache(query.getId());
            if (Objects.isNull(couponPackage)) {
                return R.fail("402025", "优惠券包不存在");
            }
            packageId = couponPackage.getId();

            CouponPackage updateCouponPackage = packageBuilder.id(couponPackage.getId()).build();
            couponPackageMapper.updateCouponPackage(updateCouponPackage);
            // 删除优惠券包下的优惠券
            packageItemService.deletePackItemByPackageId(couponPackage.getId());
        }

        // 批量insert coupon item
        Long finalPackageId = packageId;
        itemList.forEach(item -> item.setPackageId(finalPackageId));
        packageItemService.batchSavePackItem(itemList);

        redisService.delete(CacheConstant.COUPON_PACKAGE_CACHE_KEY + packageId);
        return R.ok();
    }

    @Override
    public CouponPackageDetailsVO editEcho(Long packageId) {
        CouponPackage couponPackage = queryByIdFromCache(packageId);
        if (Objects.isNull(couponPackage)) {
            throw new BizException("402025", "优惠券包不存在");
        }
        // CouponPackageDetailsVO
        CouponPackageDetailsVO couponPackageDetailsVO = CouponPackageDetailsVO.builder().id(couponPackage.getId()).name(couponPackage.getName()).count(couponPackage.getCouponCount())
                .franchiseeId(couponPackage.getFranchiseeId()).isCanBuy(couponPackage.getIsCanBuy()).amount(couponPackage.getAmount().doubleValue()).build();

        Franchisee franchisee = franchiseeService.queryByIdFromCache(couponPackage.getFranchiseeId());
        couponPackageDetailsVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);

        List<CouponPackageItem> couponPackageItemList = packageItemService.listCouponPackageItemByPackageId(packageId);
        if (CollUtil.isEmpty(couponPackageItemList)) {
            return couponPackageDetailsVO;
        }
        couponPackageDetailsVO.setItemDetailsVOList(couponPackageItemList.stream().map(item -> BeanUtil.copyProperties(item, CouponPackageDetailsVO.CouponPackageItemDetailsVO.class)).collect(Collectors.toList()));

        return couponPackageDetailsVO;
    }

    @Override
    public void del(Long packageId) {
        CouponPackage couponPackage = queryByIdFromCache(packageId);
        if (Objects.isNull(couponPackage)) {
            throw new BizException("402025", "优惠券包不存在");
        }

        couponPackageMapper.deleteCouponPackageById(packageId);
        packageItemService.deletePackItemByPackageId(packageId);

        redisService.delete(CacheConstant.COUPON_PACKAGE_CACHE_KEY + packageId);
    }

    @Override
    public R pageList(CouponPackagePageQuery query) {
        Pair<Boolean, List<Long>> checkPermission = preCheckPermission();
        if (!checkPermission.getLeft()) {
            return R.ok(CollUtil.newArrayList());
        }

        query.setFranchiseeIds(checkPermission.getRight());
        query.setTenantId(TenantContextHolder.getTenantId());

        List<CouponPackage> packageList = couponPackageMapper.selectPageCouponPackage(query);
        if (CollUtil.isEmpty(packageList)) {
            return R.ok(Collections.emptyList());
        }

        // 聚合
        List<Long> packageIdList = packageList.stream().map(CouponPackage::getId).collect(Collectors.toList());
        List<CouponPackageItemBO> packageItemList = packageItemService.listCouponPackageItemByPackageIds(packageIdList);
        Map<Long, CouponPackageItemBO> packageItemBoMap = new HashMap<>(10);
        if (CollUtil.isNotEmpty(packageItemList)) {
            packageItemBoMap = packageItemList.stream().collect(Collectors.toMap(CouponPackageItemBO::getPackageId, item -> item, (k1, k2) -> k1));
        }

        Map<Long, CouponPackageItemBO> finalPackageItemBoMap = packageItemBoMap;
        return R.ok(packageList.stream().map(item -> {
            CouponPackagePageVO vo = BeanUtil.copyProperties(item, CouponPackagePageVO.class);

            CouponPackageItemBO itemBO = finalPackageItemBoMap.get(item.getId());
            if (Objects.nonNull(itemBO)) {
                vo.setCouponNameStr(itemBO.getCouponNameStr());
                vo.setEffectStr(itemBO.getEffectStr());
            }

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            vo.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());

            return vo;
        }).collect(Collectors.toList()));
    }


    @Override
    @Slave
    public R<Integer> pageCount(CouponPackagePageQuery query) {
        Pair<Boolean, List<Long>> checkPermission = preCheckPermission();
        if (!checkPermission.getLeft()) {
            return R.ok(NumberConstant.ZERO);
        }
        query.setFranchiseeIds(checkPermission.getRight());
        query.setTenantId(TenantContextHolder.getTenantId());
        return R.ok(couponPackageMapper.selectCountCouponPackage(query));
    }


    private Pair<Boolean, List<Long>> preCheckPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return Pair.of(false, Collections.emptyList());
        }
        return assertPermissionService.assertPermissionByPair(user);
    }


    @Override
    public R batchRelease(CouponPackageBatchReleaseRequest request) {
        Set<String> phoneSet = new HashSet<>(JsonUtil.fromJsonArray(request.getJsonPhones(), String.class));
        if (CollectionUtils.isEmpty(phoneSet)) {
            return R.fail("ELECTRICITY.0007", "手机号不可以为空");
        }

        //增加优惠劵发放人Id
        Long operateUid = SecurityUtils.getUid();
        if (Objects.isNull(operateUid)) {
            log.warn("CouponPackage BatchRelease Warn! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User operateUser = userService.queryByUidFromCache(operateUid);
        if (Objects.isNull(operateUser)) {
            log.warn("CouponPackage BatchRelease Warn! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CouponPackage couponPackage = queryByIdFromCache(request.getPackageId());
        if (Objects.isNull(couponPackage)) {
            R.fail("402025", "优惠券包不存在");
        }

        List<CouponPackageItem> packageItemList = packageItemService.listCouponPackageItemByPackageId(couponPackage.getId());
        if (CollUtil.isEmpty(packageItemList)) {
            R.fail("402026", "优惠券包下的优惠券为空");
        }

        Set<User> existUserSet = new ConcurrentHashSet<>();
        Set<String> notExistUserSet = new ConcurrentHashSet<>();

        Integer tenantId = TenantContextHolder.getTenantId();
        phoneSet.parallelStream().forEach(e -> {
            User user = userService.queryByUserPhone(e, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            if (Objects.isNull(user)) {
                notExistUserSet.add(e);
            } else {
                UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
                user.setName(userInfo.getName());
                existUserSet.add(user);
            }
        });

        CouponPackageBatchReleaseVO vo = CouponPackageBatchReleaseVO.builder()
                .sessionId(UUID.fastUUID().toString(true))
                .notExistPhones(notExistUserSet)
                .isRequest(existUserSet.isEmpty() ? CouponPackageBatchReleaseVO.IS_REQUEST_NO : CouponPackageBatchReleaseVO.IS_REQUEST_YES)
                .build();
        execute.execute(() -> batchReleaseHandler(existUserSet, packageItemList, vo.getSessionId()));

        return R.ok(vo);
    }


    private void batchReleaseHandler(Set<User> existsPhone, List<CouponPackageItem> packageItemList, String sessionId) {
        Iterator<User> iterator = existsPhone.iterator();

        List<UserCoupon> userCouponList = new ArrayList<>();
        int maxSize = 500;

        while (iterator.hasNext()) {
            // 按照最大优惠包下面20个优惠券算，一次可以插入是25个用户数据
            if (userCouponList.size() >= maxSize) {
                userCouponService.batchInsert(userCouponList);
                userCouponList.clear();
                continue;
            }
            User user = iterator.next();
            packageItemList.stream().forEach(item -> {
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setSource(UserCoupon.TYPE_SOURCE_ADMIN_SEND);
                userCoupon.setCouponId(item.getCouponId().intValue());
                userCoupon.setName(item.getCouponName());
                userCoupon.setDiscountType(item.getDiscountType());
                userCoupon.setUid(user.getUid());
                userCoupon.setPhone(user.getPhone());
                userCoupon.setDeadline(TimeUtils.convertTimeStamp(LocalDateTime.now().plusDays(item.getDays())));
                userCoupon.setCreateTime(System.currentTimeMillis());
                userCoupon.setUpdateTime(System.currentTimeMillis());
                userCoupon.setStatus(UserCoupon.STATUS_UNUSED);
                userCoupon.setDelFlag(UserCoupon.DEL_NORMAL);
                userCoupon.setVerifiedUid(UserCoupon.INITIALIZE_THE_VERIFIER);
                userCoupon.setTenantId(user.getTenantId());
                userCoupon.setCouponType(CouponTypeEnum.COUPON_PACKAGE.getCode());
                userCoupon.setCouponWay(item.getPackageId());
                userCouponList.add(userCoupon);
            });
        }

        if (CollUtil.isNotEmpty(userCouponList)) {
            userCouponService.batchInsert(userCouponList);
        }

        log.info("Coupon Package Batch Release Success! sessionId is {} size is {}", sessionId, existsPhone.size());
        redisService.set(String.format(CacheConstant.COUPON_PACKAGE_BATCH_RELEASE_RESULT_KEY, sessionId), "1", 60L, TimeUnit.SECONDS);
    }


    @Override
    public R appointRelease(CouponPackageAppointReleaseRequest request) {
        TokenUser operateUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(operateUser)) {
            log.warn("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        CouponPackage couponPackage = queryByIdFromCache(request.getPackageId());
        if (Objects.isNull(couponPackage)) {
            R.fail("402025", "优惠券包不存在");
        }

        List<CouponPackageItem> packageItemList = packageItemService.listCouponPackageItemByPackageId(couponPackage.getId());
        if (CollUtil.isEmpty(packageItemList)) {
            R.fail("402026", "优惠券包下的优惠券为空");
        }

        request.getUid().parallelStream().forEach(uid -> {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("Coupon Package Appoint Release Warn! userInfo is null , uid is {}", uid);
                return;
            }
            List<UserCoupon> userCouponList = packageItemList.parallelStream().map(item -> {
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setSource(UserCoupon.TYPE_SOURCE_ADMIN_SEND);
                userCoupon.setCouponId(item.getCouponId().intValue());
                userCoupon.setName(item.getCouponName());
                userCoupon.setDiscountType(item.getDiscountType());
                userCoupon.setUid(userInfo.getUid());
                userCoupon.setPhone(userInfo.getPhone());
                userCoupon.setDeadline(TimeUtils.convertTimeStamp(LocalDateTime.now().plusDays(item.getDays())));
                userCoupon.setCreateTime(System.currentTimeMillis());
                userCoupon.setUpdateTime(System.currentTimeMillis());
                userCoupon.setStatus(UserCoupon.STATUS_UNUSED);
                userCoupon.setDelFlag(UserCoupon.DEL_NORMAL);
                userCoupon.setVerifiedUid(UserCoupon.INITIALIZE_THE_VERIFIER);
                userCoupon.setTenantId(userInfo.getTenantId());
                userCoupon.setCouponType(CouponTypeEnum.COUPON_PACKAGE.getCode());
                userCoupon.setCouponWay(couponPackage.getId());
                return userCoupon;
            }).collect(Collectors.toList());

            userCouponService.batchInsert(userCouponList);
        });

        return R.ok();
    }

    @Override
    public R queryBatchReleaseStatus(String sessionId) {
        if (!redisService.hasKey(String.format(CacheConstant.COUPON_PACKAGE_BATCH_RELEASE_RESULT_KEY, sessionId))) {
            return R.fail("402027", "券包正在发送中，请稍后再试");
        }
        return R.ok();
    }


    @Override
    public CouponPackage queryByIdFromCache(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        //先查缓存
        CouponPackage couponPackage = redisService.getWithHash(CacheConstant.COUPON_PACKAGE_CACHE_KEY + id, CouponPackage.class);
        if (Objects.nonNull(couponPackage)) {
            return couponPackage;
        }

        //缓存没有再查数据库
        CouponPackage couponPackageDb = couponPackageMapper.selectCouponPackageById(id);
        if (Objects.isNull(couponPackageDb)) {
            return null;
        }

        //放入缓存
        redisService.saveWithHash(CacheConstant.COUPON_PACKAGE_CACHE_KEY + id, couponPackageDb);
        return couponPackageDb;
    }
}
