package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarCouponNamePO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.mapper.CouponPackageItemMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.SearchVo;
import com.xiliulou.electricity.vo.activity.CouponActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    
    @Autowired
    private OperateRecordUtil operateRecordUtil;
    
    @Autowired
    private CouponActivityPackageService couponActivityPackageService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private CouponPackageItemService packageItemService;
    @Autowired
    private CouponPackageItemMapper couponPackageItemMapper;

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
    
    @Override
    public Coupon queryByIdFromDB(Integer id) {
        return couponMapper.selectById(id);
    }
    
    /**
     * 新增数据
     *
     * @param couponQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(CouponQuery couponQuery) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        couponQuery.setTenantId(tenantId);
        couponQuery.setType(Coupon.TYPE_FRANCHISEE);
        
        // 加盟商一致性校验
        Long franchiseeId = couponQuery.getFranchiseeId();
        List<Long> franchiseeIds = couponQuery.getFranchiseeIds();
        if (CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId)) {
            log.warn("Insert coupon WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        //参数判断
        if (Objects.equals(couponQuery.getDiscountType(), Coupon.FULL_REDUCTION)) {
            if (Objects.isNull(couponQuery.getAmount())) {
                return R.fail("ELECTRICITY.0072", "减免金额不能为空");
            }
            if (Objects.isNull(couponQuery.getSuperposition())){
                return R.fail("ELECTRICITY.0071", "优惠券叠加使用方式不能为空");
            }
        }
        
        if (Objects.equals(couponQuery.getDiscountType(), Coupon.DISCOUNT)) {
            if (Objects.isNull(couponQuery.getDiscount())) {
                return R.fail("ELECTRICITY.0073", "折扣不能为空");
            }
        }
        
        if (Objects.equals(couponQuery.getDiscountType(), Coupon.DAY_VOUCHER)) {
            if (Objects.isNull(couponQuery.getCount())) {
                return R.fail("ELECTRICITY.0074", "赠送天数不能为空");
            }
            if (Objects.isNull(couponQuery.getUseScope())) {
                return R.fail("ELECTRICITY.0075", "使用范围不能为空");
            }
            couponQuery.setSuperposition(Coupon.SUPERPOSITION_YES);
        }
        
        //检查优惠券名称是否已经存在
        if (isExistCouponName(couponQuery.getName(), tenantId)) {
            return R.fail("000075", "优惠券名称不能重复");
        }
        
        // 可叠加时不能指定套餐
        if (!Objects.equals(couponQuery.getDiscountType(), Coupon.DAY_VOUCHER)) {
            if (Coupon.SUPERPOSITION_YES.equals(couponQuery.getSuperposition()) && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())) {
                return R.fail("SYSTEM.0002", "参数不合法");
            }
            //判断若选择不可叠加优惠券，则需要检查是否选择了套餐
            if (Coupon.SUPERPOSITION_NO.equals(couponQuery.getSuperposition()) && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())) {
                //获取页面传递进来的套餐信息
                Triple<Boolean, String, Object> packagesResult = verifyPackages(couponQuery);
                if (Boolean.FALSE.equals(packagesResult.getLeft())) {
                    return R.fail("000076", (String) packagesResult.getRight());
                }
            }
        }
        
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponQuery, coupon);
        coupon.setCreateTime(System.currentTimeMillis());
        coupon.setUpdateTime(System.currentTimeMillis());
        coupon.setTenantId(tenantId);
        coupon.setDays(Integer.parseInt(couponQuery.getValidDays()));
        coupon.setFranchiseeId(franchiseeId.intValue());
        
        if (Objects.isNull(coupon.getStatus())) {
            coupon.setStatus(Coupon.STATUS_OFF);
        }
        
        int insert = couponMapper.insert(coupon);
        
        //将该优惠券对应的套餐信息保存到数据库中, 优惠券类型为不可叠加，并且为指定得套餐使用 ,且不是天数券
        //log.error("check issue, get coupon id when create coupon. coupon id = {}", coupon.getId());
        if (!Objects.equals(couponQuery.getDiscountType(), Coupon.DAY_VOUCHER)) {
            if (Coupon.SUPERPOSITION_NO.equals(couponQuery.getSuperposition()) && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())) {
                List<CouponActivityPackage> couponActivityPackages = getPackagesFromCoupon(coupon.getId().longValue(), couponQuery);
                if (!CollectionUtils.isEmpty(couponActivityPackages)) {
                    couponActivityPackageService.addCouponActivityPackages(couponActivityPackages);
                }
            }
        }
        
        if (insert > 0) {
            operateRecordUtil.record(null, coupon);
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }
    
    /**
     * 检查是否存在同名优惠券
     *
     * @param couponName couponName
     * @param tenantId tenantId
     * @return boolean
     */
    private boolean isExistCouponName(String couponName, Integer tenantId) {
        Coupon coupon = new Coupon();
        coupon.setName(couponName);
        coupon.setTenantId(tenantId);
        List<Coupon> result = couponMapper.selectCouponByQuery(coupon);
        if (!CollectionUtils.isEmpty(result)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    private Triple<Boolean, String, Object> verifyPackages(CouponQuery couponQuery) {
        //检查是否有选择（换电,租车,车电一体）套餐信息
        if (CollectionUtils.isEmpty(couponQuery.getBatteryPackages()) && CollectionUtils.isEmpty(couponQuery.getCarRentalPackages()) && CollectionUtils.isEmpty(
                couponQuery.getCarWithBatteryPackages())) {
            return Triple.of(false, "000201", "请选择套餐信息");
        }
        
        List<Long> electricityPackages = couponQuery.getBatteryPackages();
        for (Long packageId : electricityPackages) {
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "000202", "换电套餐不存在");
            }
        }
        
        List<Long> carRentalPackages = couponQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000203", "租车套餐不存在");
            }
        }
        
        List<Long> carElectricityPackages = couponQuery.getCarWithBatteryPackages();
        for (Long packageId : carElectricityPackages) {
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000204", "车电一体套餐不存在");
            }
        }
        return Triple.of(true, "", null);
    }
    
    private List<CouponActivityPackage> getPackagesFromCoupon(Long couponId, CouponQuery couponQuery) {
        List<CouponActivityPackage> couponActivityPackages = Lists.newArrayList();
        List<Long> electricityPackages = couponQuery.getBatteryPackages();
        for (Long packageId : electricityPackages) {
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode(),
                    couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }
        
        List<Long> carRentalPackages = couponQuery.getCarRentalPackages();
        for (Long packageId : carRentalPackages) {
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode(),
                    couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }
        
        List<Long> carElectricityPackages = couponQuery.getCarWithBatteryPackages();
        for (Long packageId : carElectricityPackages) {
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode(),
                    couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }
        
        return couponActivityPackages;
    }
    
    private CouponActivityPackage buildCouponActivityPackage(Long couponId, Long packageId, Integer packageType, Integer tenantId) {
        CouponActivityPackage couponActivityPackage = new CouponActivityPackage();
        couponActivityPackage.setCouponId(couponId);
        couponActivityPackage.setPackageId(packageId);
        couponActivityPackage.setPackageType(packageType);
        couponActivityPackage.setTenantId(tenantId.longValue());
        couponActivityPackage.setDelFlag(CommonConstant.DEL_N);
        couponActivityPackage.setCreateTime(System.currentTimeMillis());
        couponActivityPackage.setUpdateTime(System.currentTimeMillis());
        
        return couponActivityPackage;
    }
    
    
    /**
     * 修改数据
     *
     * @param couponQuery 实例对象
     * @return 实例对象
     */
    @Override
    public R update(CouponQuery couponQuery) {
        Coupon oldCoupon = queryByIdFromCache(couponQuery.getId());
        if (Objects.isNull(oldCoupon) || !Objects.equals(oldCoupon.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("Update coupon WARN! not found coupon ! couponId={} ", couponQuery.getId());
            return R.fail("120124", "找不到优惠券");
        }
        
        // 加盟商一致性校验
        Integer franchiseeId = oldCoupon.getFranchiseeId();
        List<Long> franchiseeIds = couponQuery.getFranchiseeIds();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId.longValue())) {
            log.warn("Update coupon WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return R.fail("120240", "当前加盟商无权限操作");
        }
        
        //检查优惠券是否已经绑定用户
        /*List<UserCoupon> userCoupons = userCouponService.selectCouponUserCountById(couponQuery.getId().longValue());
        if (!CollectionUtils.isEmpty(userCoupons)) {
            log.warn("Update coupon WARN! this coupon already bound user ! couponId={} ", couponQuery.getId());
            return R.fail("000205", "优惠券已有用户领取");
        }*/

        Integer days = Integer.parseInt(couponQuery.getValidDays());
        Integer incrementDays = 0;

        //参数判断
        if (Objects.equals(oldCoupon.getDiscountType(), Coupon.FULL_REDUCTION) || Objects.equals(oldCoupon.getDiscountType(), Coupon.DAY_VOUCHER)) {
            if (days < oldCoupon.getDays()) {
                // 检测有效期
                return R.fail("120164", "有效期限只能增大不可减小");
            }

            if (days > oldCoupon.getDays()) {
                incrementDays = days - oldCoupon.getDays();
            }
        }

        couponQuery.setTenantId(TenantContextHolder.getTenantId());

        // 可叠加时不能指定套餐
        if (!Objects.equals(oldCoupon.getDiscountType(), Coupon.DAY_VOUCHER)) {
            //判断若选择不可叠加优惠券，则需要检查是否选择了套餐
            if (Coupon.SUPERPOSITION_NO.equals(oldCoupon.getSuperposition()) && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())) {
                //获取页面传递进来的套餐信息
                Triple<Boolean, String, Object> packagesResult = verifyPackages(couponQuery);
                if (Boolean.FALSE.equals(packagesResult.getLeft())) {
                    return R.fail("000076", (String) packagesResult.getRight());
                }
            }
        }

        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(couponQuery.getId());
       /* couponUpdate.setSuperposition(coupon.getSuperposition());
        couponUpdate.setName(coupon.getName());
        couponUpdate.setDelFlag(coupon.getDelFlag());*/
        couponUpdate.setDescription(couponQuery.getDescription());
        couponUpdate.setUpdateTime(System.currentTimeMillis());
        couponUpdate.setDays(days);
        couponUpdate.setSpecificPackages(couponQuery.getSpecificPackages());
        
        int update = couponMapper.updateById(couponUpdate);

        if (update > 0) {
            // 维护指定套餐新关系
            if (!Objects.equals(oldCoupon.getDiscountType(), Coupon.DAY_VOUCHER)) {
                if (Coupon.SUPERPOSITION_NO.equals(oldCoupon.getSuperposition())) {
                    // 删除套餐与优惠券的关联关系
                    couponActivityPackageService.deleteByCouponId(couponQuery.getId(), TenantContextHolder.getTenantId());

                    if (SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())) {
                        List<CouponActivityPackage> couponActivityPackages = getPackagesFromCoupon(couponQuery.getId().longValue(), couponQuery);
                        if (!CollectionUtils.isEmpty(couponActivityPackages)) {
                            couponActivityPackageService.addCouponActivityPackages(couponActivityPackages);
                        }
                    }
                }
            }

            if (incrementDays > 0) {
                // 更新优惠券的时限
                userCouponService.asyncBatchUpdateIncreaseDeadline(couponQuery.getId(), incrementDays, TenantContextHolder.getTenantId());
            }

            //更新缓存
            redisService.delete(CacheConstant.COUPON_CACHE + oldCoupon.getId());

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
    public R queryCouponList(CouponQuery couponQuery) {
        List<Coupon> couponList = couponMapper.queryList(couponQuery);
        List<CouponActivityVO> couponActivityVOList = Lists.newArrayList();
        for (Coupon coupon : couponList) {
            CouponActivityVO couponActivityVO = new CouponActivityVO();
            BeanUtils.copyProperties(coupon, couponActivityVO);
            couponActivityVO.setValidDays(String.valueOf(coupon.getDays()));
            
            Integer franchiseeId = coupon.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                couponActivityVO.setFranchiseeId(franchiseeId.longValue());
                couponActivityVO.setFranchiseeName(
                        Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            
            couponActivityVOList.add(couponActivityVO);
        }
        
        return R.ok(couponActivityVOList);
    }
    
    @Slave
    @Override
    public R queryCount(CouponQuery couponQuery) {
        return R.ok(couponMapper.queryCount(couponQuery));
    }
    
    
    @Override
    public List<SearchVo> search(CouponQuery query) {
        return couponMapper.search(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> findCouponById(Long id) {
        Coupon coupon = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, null, "优惠券信息不存在");
        }
        CouponActivityVO couponActivityVO = new CouponActivityVO();
        BeanUtils.copyProperties(coupon, couponActivityVO);
        couponActivityVO.setValidDays(String.valueOf(coupon.getDays()));
        
        if (Coupon.SUPERPOSITION_NO.equals(coupon.getSuperposition())) {
            
            //判断是否指定了使用套餐，如果是1，则查询指定的套餐，如果是2，则拉取所有的套餐信息
            
            if (SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(coupon.getSpecificPackages())) {
                couponActivityVO.setBatteryPackages(getBatteryPackages(id));
                couponActivityVO.setCarRentalPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                couponActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            } else {
//                couponActivityVO.setBatteryPackages(getAllBatteryPackages());
//                couponActivityVO.setCarRentalPackages(getAllCarBatteryPackages(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
//                couponActivityVO.setCarWithBatteryPackages(getAllCarBatteryPackages(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }
        }
        
        Integer franchiseeId = coupon.getFranchiseeId();
        if (Objects.nonNull(franchiseeId)) {
            couponActivityVO.setFranchiseeName(
                    Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId.longValue())).map(Franchisee::getName).orElse(StringUtils.EMPTY));
        }
        
        return Triple.of(true, null, couponActivityVO);
    }
    
    @Slave
    @Override
    public List<CarCouponNamePO> queryListByIdsFromCache(List<Long> couponId) {
        if (CollectionUtils.isEmpty(couponId)) {
            return ListUtil.empty();
        }
        
        List<CarCouponNamePO> result = new ArrayList<>();
        for (Long id : couponId) {
            Coupon coupon = queryByIdFromCache(id.intValue());
            if (!Objects.isNull(coupon)) {
                CarCouponNamePO couponVO = new CarCouponNamePO();
                couponVO.setName(coupon.getName());
                couponVO.setId(coupon.getId().longValue());
                couponVO.setAmount(coupon.getAmount());
                couponVO.setCount(coupon.getCount());
                couponVO.setDiscountType(coupon.getDiscountType());
                result.add(couponVO);
            }
        }
        return result;
    }
    
    @Override
    public Boolean isSameFranchisee(Integer couponFranchiseeId, Long targetFranchiseeId) {
        if (Objects.isNull(couponFranchiseeId) || Objects.equals(couponFranchiseeId, NumberConstant.ZERO) || Objects.isNull(targetFranchiseeId) || Objects.equals(
                targetFranchiseeId, NumberConstant.ZERO_L)) {
            return true;
        }
        
        return Objects.equals(couponFranchiseeId.longValue(), targetFranchiseeId);
    }
    
    @Override
    public List<Coupon> queryListByIdsFromDB(List<Long> couponIds) {
        if (CollectionUtils.isEmpty(couponIds)) {
            return List.of();
        }
        return couponMapper.selectListByIdsFromDB(couponIds);
    }
    
    public List<BatteryMemberCardVO> getAllBatteryPackages() {
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder().delFlag(BatteryMemberCard.DEL_NORMAL).status(BatteryMemberCard.STATUS_UP)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return batteryMemberCardService.selectByQuery(query);
    }
    
    public List<BatteryMemberCardVO> getAllCarBatteryPackages(Integer packageType) {
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setTenantId(TenantContextHolder.getTenantId());
        qryModel.setStatus(UpDownEnum.UP.getCode());
        
        if (PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(packageType)) {
            qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
        } else if (PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(packageType)) {
            qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
        }
        
        return batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel);
        
    }
    
    private List<BatteryMemberCardVO> getBatteryPackages(Long couponId) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        
        for (CouponActivityPackage couponActivityPackage : couponActivityPackages) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(couponActivityPackage.getPackageId());
            if (Objects.nonNull(batteryMemberCard) && CommonConstant.DEL_N.equals(batteryMemberCard.getDelFlag())) {
                BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
                memberCardVOList.add(batteryMemberCardVO);
            }
        }
        
        return memberCardVOList;
    }
    
    private List<BatteryMemberCardVO> getCarBatteryPackages(Long couponId, Integer packageType) {
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, packageType);
        for (CouponActivityPackage couponActivityPackage : couponActivityPackages) {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(couponActivityPackage.getPackageId());
            if (Objects.nonNull(carRentalPackagePO) && CommonConstant.DEL_N.equals(carRentalPackagePO.getDelFlag())) {
                batteryMemberCardVO.setId(carRentalPackagePO.getId());
                batteryMemberCardVO.setName(carRentalPackagePO.getName());
                batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
                memberCardVOList.add(batteryMemberCardVO);
            }
        }
        
        return memberCardVOList;
    }
    
    @Override
    public Triple<Boolean, String, Object> deleteById(Long id, List<Long> franchiseeIds) {
        Coupon coupon = this.queryByIdFromCache(id.intValue());
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        // 加盟商一致性校验
        Integer franchiseeId = coupon.getFranchiseeId();
        if (Objects.nonNull(franchiseeId) && CollectionUtils.isNotEmpty(franchiseeIds) && !franchiseeIds.contains(franchiseeId.longValue())) {
            log.warn("Update coupon WARN! Franchisees are different, franchiseeIds={}, franchiseeId={}", franchiseeIds, franchiseeId);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
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
        if (Objects.nonNull(oldUserActivity)) {
            return Triple.of(false, "", "删除失败，优惠券已绑定套餐活动");
        }
        
        NewUserActivity newUserActivity = newUserActivityService.selectByCouponId(id);
        if (Objects.nonNull(newUserActivity)) {
            return Triple.of(false, "", "删除失败，优惠券已绑定新用户活动");
        }
        
        //需要增加套餐绑定检验，是指在创建套餐时是否指定了套餐关联的优惠券。并非是新建优惠券时所关联的套餐。
        //检查是否绑定到换电套餐
        List<BatteryMemberCard> batteryMemberCardList = batteryMemberCardService.selectListByCouponId(coupon.getId().longValue());
        if (!CollectionUtils.isEmpty(batteryMemberCardList)) {
            log.warn("find the battery packages related to coupon, cannot delete. coupon id = {}", coupon.getId());
            return Triple.of(false, "", "删除失败，优惠券已绑定套餐");
        }
        //检查是否绑定到租车或车电一体套餐
        List<CarRentalPackagePo> carRentalPackagePos = carRentalPackageService.findByCouponId(coupon.getId().longValue());
        if (!CollectionUtils.isEmpty(carRentalPackagePos)) {
            log.warn("find the car rental packages related to coupon, cannot delete. coupon id = {}", coupon.getId());
            return Triple.of(false, "", "删除失败，优惠券已绑定套餐");
        }
        // 优惠券已绑定优惠券包
        Integer existsCouponBindPackage = couponPackageItemMapper.existsCouponBindPackage(id);
        if (Objects.nonNull(existsCouponBindPackage)) {
            return Triple.of(false, "402019", "删除失败，优惠券已绑定优惠券包");
        }

        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(id.intValue());
        couponUpdate.setDelFlag(Coupon.DEL_DEL);
        couponUpdate.setUpdateTime(System.currentTimeMillis());
        
        int update = couponMapper.updateById(couponUpdate);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.saveWithHash(CacheConstant.COUPON_CACHE + couponUpdate.getId(), couponUpdate);
            return null;
        });
        operateRecordUtil.record(null, coupon);
        return Triple.of(true, "", "删除成功！");
    }


    @Override
    public R editEnablesState(Long id, Integer enabledState) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        Coupon coupon = queryByIdFromCache(id.intValue());
        if (Objects.isNull(coupon) || !Objects.equals(coupon.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.fail("120124", "找不到优惠券");
        }

        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(user);
        if (!pair.getLeft()) {
            log.warn("editEnablesState WARN! Franchisees is null");
            return R.ok();
        }

        Integer franchiseeId = coupon.getFranchiseeId();
        if (Objects.nonNull(franchiseeId) && CollUtil.isNotEmpty(pair.getRight()) && !pair.getRight().contains(franchiseeId.longValue())) {
            return R.fail("120240", "当前加盟商无权限操作");
        }

        ShareActivityRule shareActivityRule = shareActivityRuleService.selectByCouponId(id);
        if (Objects.nonNull(shareActivityRule)) {
            return R.fail("402029", "禁用失败，优惠券已绑定用户邀请活动");
        }

        // 注册活动
        NewUserActivity newUserActivity = newUserActivityService.selectByCouponId(id);
        if (Objects.nonNull(newUserActivity)) {
            return R.fail("402029", "禁用失败，优惠券已绑定用户邀请活动");
        }

        //检查是否绑定到换电套餐
        List<BatteryMemberCard> batteryMemberCardList = batteryMemberCardService.selectListByCouponId(id);
        if (CollUtil.isNotEmpty(batteryMemberCardList)) {
            return R.fail("402017", "禁用失败，优惠券已绑定套餐");
        }

        //检查是否绑定到租车或车电一体套餐
        List<CarRentalPackagePo> carRentalPackagePos = carRentalPackageService.findByCouponId(id);
        if (CollUtil.isNotEmpty(carRentalPackagePos)) {
            return R.fail("402017", "禁用失败，优惠券已绑定套餐");
        }

        // 已绑定优惠券包，提示“禁用失败，优惠券已绑定优惠券包
        Integer existsCouponBindPackage = packageItemService.existsCouponBindPackage(id);
        if (Objects.nonNull(existsCouponBindPackage)) {
            return R.fail("402018", "禁用失败，优惠券已绑定优惠券包");
        }


        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(id.intValue());
        couponUpdate.setEnabledState(enabledState);
        couponUpdate.setUpdateTime(System.currentTimeMillis());
        couponMapper.updateById(couponUpdate);

        redisService.delete(CacheConstant.COUPON_CACHE + id);
        return R.ok();

    }
}
