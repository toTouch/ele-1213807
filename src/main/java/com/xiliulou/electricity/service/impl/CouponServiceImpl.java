package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.SpecificPackagesEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.mapper.CouponMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.SearchVo;
import com.xiliulou.electricity.vo.activity.CouponActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
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
    @Autowired
    private CarRentalPackageService carRentalPackageService;
    @Autowired
    private CouponActivityPackageService couponActivityPackageService;
    @Autowired
    BatteryMemberCardService batteryMemberCardService;


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
     * @param couponQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(CouponQuery couponQuery) {
        //创建账号
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("Coupon  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        couponQuery.setTenantId(tenantId);

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
            couponQuery.setType(Coupon.TYPE_FRANCHISEE);
            if (Objects.isNull(couponQuery.getFranchiseeId())) {
                log.error("Coupon  ERROR! not found FranchiseeId ");
                return R.fail("ELECTRICITY.0094", "加盟商不能为空");
            }
        } else {
            if (Objects.equals(couponQuery.getType(), Coupon.TYPE_FRANCHISEE)) {
                if (Objects.isNull(couponQuery.getFranchiseeId())) {
                    log.error("Coupon  ERROR! not found FranchiseeId ");
                    return R.fail("ELECTRICITY.0094", "加盟商不能为空");
                }
            }
        }


        //参数判断
        if (Objects.equals(couponQuery.getDiscountType(), Coupon.FULL_REDUCTION)) {
            if (Objects.isNull(couponQuery.getAmount())) {
                return R.fail("ELECTRICITY.0072", "减免金额不能为空");
            }
        }

        if (Objects.equals(couponQuery.getDiscountType(), Coupon.DISCOUNT)) {
            if (Objects.isNull(couponQuery.getDiscount())) {
                return R.fail("ELECTRICITY.0073", "打折折扣不能为空");
            }
        }

        if (Objects.equals(couponQuery.getDiscountType(), Coupon.EXPERIENCE)) {
            if (Objects.isNull(couponQuery.getCount())) {
                return R.fail("ELECTRICITY.0074", "天数不能为空");
            }
        }

        //检查优惠券名称是否已经存在
        if(isExistCouponName(couponQuery.getName(), tenantId)){
            return R.fail("000075", "优惠券名称不能重复");
        }

        //判断若选择不可叠加优惠券，则需要检查是否选择了套餐
        if(Coupon.SUPERPOSITION_NO.equals(couponQuery.getSuperposition())
                && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())){
            //获取页面传递进来的套餐信息
            Triple<Boolean, String, Object> packagesResult = verifyPackages(couponQuery);
            if (Boolean.FALSE.equals(packagesResult.getLeft())) {
                return R.fail("000076", (String) packagesResult.getRight());
            }
        }

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponQuery, coupon);
        coupon.setUid(user.getUid());
        coupon.setUserName(user.getUsername());
        coupon.setCreateTime(System.currentTimeMillis());
        coupon.setUpdateTime(System.currentTimeMillis());
        coupon.setTenantId(tenantId);
        coupon.setDays(Integer.parseInt(couponQuery.getValidDays()));

        if(Objects.isNull(coupon.getStatus())){
            coupon.setStatus(Coupon.STATUS_OFF);
        }

        //先默认为自营活动 以后需要前端传值 TODO
        if (Objects.isNull(coupon.getType())) {
            coupon.setType(Coupon.TYPE_SYSTEM);
        }


        int insert = couponMapper.insert(coupon);

        //将该优惠券对应的套餐信息保存到数据库中, 优惠券类型为不可叠加，并且为指定得套餐使用
        //log.error("check issue, get coupon id when create coupon. coupon id = {}", coupon.getId());
        if(Coupon.SUPERPOSITION_NO.equals(couponQuery.getSuperposition())
                && SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(couponQuery.getSpecificPackages())){
            List<CouponActivityPackage> couponActivityPackages = getPackagesFromCoupon(coupon.getId().longValue(), couponQuery);
            if(!CollectionUtils.isEmpty(couponActivityPackages)){
                couponActivityPackageService.addCouponActivityPackages(couponActivityPackages);
            }
        }

        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 检查是否存在同名优惠券
     * @param couponName
     * @param tenantId
     * @return
     */
    private boolean isExistCouponName(String couponName, Integer tenantId){
        Coupon coupon = new Coupon();
        coupon.setName(couponName);
        coupon.setTenantId(tenantId);
        Coupon result = couponMapper.selectCouponByQuery(coupon);
        if(Objects.nonNull(result)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private Triple<Boolean, String, Object> verifyPackages(CouponQuery couponQuery){
        //检查是否有选择（换电,租车,车电一体）套餐信息
        if(CollectionUtils.isEmpty(couponQuery.getBatteryPackages())
                && CollectionUtils.isEmpty(couponQuery.getCarRentalPackages())
                && CollectionUtils.isEmpty(couponQuery.getCarWithBatteryPackages())){
            return Triple.of(false, "000201", "请选择套餐信息");
        }

        List<Long> electricityPackages = couponQuery.getBatteryPackages();
        for(Long packageId : electricityPackages){
            //检查所选套餐是否存在，并且可用
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, "000202", "换电套餐不存在");
            }
        }

        List<Long> carRentalPackages = couponQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000203", "租车套餐不存在");
            }
        }

        List<Long> carElectricityPackages = couponQuery.getCarWithBatteryPackages();
        for(Long packageId : carElectricityPackages){
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(packageId);
            if (Objects.isNull(carRentalPackagePO)) {
                return Triple.of(false, "000204", "车电一体套餐不存在");
            }
        }
        return Triple.of(true, "", null);
    }

    private List<CouponActivityPackage> getPackagesFromCoupon(Long couponId, CouponQuery couponQuery){
        List<CouponActivityPackage> couponActivityPackages = Lists.newArrayList();
        List<Long> electricityPackages = couponQuery.getBatteryPackages();
        for(Long packageId : electricityPackages){
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode(), couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }

        List<Long> carRentalPackages = couponQuery.getCarRentalPackages();
        for(Long packageId : carRentalPackages){
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode(), couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }

        List<Long> carElectricityPackages = couponQuery.getCarWithBatteryPackages();
        for(Long packageId : carElectricityPackages){
            CouponActivityPackage couponActivityPackage = buildCouponActivityPackage(couponId, packageId, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode(), couponQuery.getTenantId());
            couponActivityPackages.add(couponActivityPackage);
        }

        return couponActivityPackages;
    }

    private CouponActivityPackage buildCouponActivityPackage(Long couponId, Long packageId, Integer packageType, Integer tenantId){
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
    @Transactional(rollbackFor = Exception.class)
    public R update(CouponQuery couponQuery) {
        Coupon oldCoupon = queryByIdFromCache(couponQuery.getId());
        if (Objects.isNull(oldCoupon) || !Objects.equals(oldCoupon.getTenantId() ,TenantContextHolder.getTenantId())) {
            log.error("update Coupon  ERROR! not found coupon ! couponId={} ", couponQuery.getId());
            return R.fail("ELECTRICITY.00104", "找不到优惠券");
        }

        //检查优惠券是否已经绑定用户
        List<UserCoupon> userCoupons = userCouponService.selectCouponUserCountById(couponQuery.getId().longValue());
        if (!CollectionUtils.isEmpty(userCoupons)) {
            log.error("update Coupon  ERROR! this coupon already bound user ! couponId={} ", couponQuery.getId());
            return R.fail("000205", "优惠券已有用户领取");
        }

        Coupon couponUpdate = new Coupon();
        couponUpdate.setId(couponQuery.getId());
       /* couponUpdate.setSuperposition(coupon.getSuperposition());
        couponUpdate.setName(coupon.getName());
        couponUpdate.setDelFlag(coupon.getDelFlag());*/
        couponUpdate.setDescription(couponQuery.getDescription());
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
    public R queryCouponList(CouponQuery couponQuery) {
        List<Coupon> couponList = couponMapper.queryList(couponQuery);
        List<CouponActivityVO> couponActivityVOList = Lists.newArrayList();
        for(Coupon coupon : couponList){
            CouponActivityVO couponActivityVO = new CouponActivityVO();
            BeanUtils.copyProperties(coupon, couponActivityVO);
            couponActivityVO.setValidDays(String.valueOf(coupon.getDays()));
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

        if(Coupon.SUPERPOSITION_NO.equals(coupon.getSuperposition())){

            //判断是否指定了使用套餐，如果是1，则查询指定的套餐，如果是2，则拉取所有的套餐信息

            if(SpecificPackagesEnum.SPECIFIC_PACKAGES_YES.getCode().equals(coupon.getSpecificPackages())){
                couponActivityVO.setBatteryPackages(getBatteryPackages(id));
                couponActivityVO.setCarRentalPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                couponActivityVO.setCarWithBatteryPackages(getCarBatteryPackages(id, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }else{
                couponActivityVO.setBatteryPackages(getAllBatteryPackages());
                couponActivityVO.setCarRentalPackages(getAllCarBatteryPackages(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
                couponActivityVO.setCarWithBatteryPackages(getAllCarBatteryPackages(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));
            }
        }
        return Triple.of(true, null, couponActivityVO);
    }

    public List<BatteryMemberCardVO> getAllBatteryPackages(){
        BatteryMemberCardQuery query = BatteryMemberCardQuery.builder()
                .delFlag(BatteryMemberCard.DEL_NORMAL)
                .status(BatteryMemberCard.STATUS_UP)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return batteryMemberCardService.selectByQuery(query);
    }

    public List<BatteryMemberCardVO> getAllCarBatteryPackages(Integer packageType){
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setTenantId(TenantContextHolder.getTenantId());
        qryModel.setStatus(UpDownEnum.UP.getCode());

        if(PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode().equals(packageType)){
            qryModel.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode());
        }else if(PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode().equals(packageType)){
            qryModel.setType(RentalPackageTypeEnum.CAR.getCode());
        }

        return batteryMemberCardService.selectCarRentalAndElectricityPackages(qryModel);

    }

    private List<BatteryMemberCardVO> getBatteryPackages(Long couponId){
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());

        for(CouponActivityPackage couponActivityPackage : couponActivityPackages){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(couponActivityPackage.getPackageId());
            BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
            memberCardVOList.add(batteryMemberCardVO);
        }

        return memberCardVOList;
    }

    private List<BatteryMemberCardVO> getCarBatteryPackages(Long couponId, Integer packageType){
        List<BatteryMemberCardVO> memberCardVOList = Lists.newArrayList();
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findPackagesByCouponIdAndType(couponId, packageType);
        for(CouponActivityPackage couponActivityPackage : couponActivityPackages){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            CarRentalPackagePo carRentalPackagePO = carRentalPackageService.selectById(couponActivityPackage.getPackageId());
            batteryMemberCardVO.setId(carRentalPackagePO.getId());
            batteryMemberCardVO.setName(carRentalPackagePO.getName());
            batteryMemberCardVO.setCreateTime(carRentalPackagePO.getCreateTime());
            memberCardVOList.add(batteryMemberCardVO);
        }

        return memberCardVOList;
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

        //需要增加套餐绑定检验， 优惠券会和套餐进行绑定
        List<CouponActivityPackage> couponActivityPackages = couponActivityPackageService.findActivityPackagesByCouponId(id.longValue());
        if(!CollectionUtils.isEmpty(couponActivityPackages)){
            return Triple.of(false, "", "删除失败，优惠券已绑定套餐");
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

        return Triple.of(true, "", "删除成功！");
    }
}
