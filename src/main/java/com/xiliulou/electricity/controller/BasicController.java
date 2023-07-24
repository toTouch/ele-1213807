package com.xiliulou.electricity.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基础Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
public class BasicController {

    @Resource
    private CouponService couponService;

    @Resource
    private StoreService storeService;

    @Resource
    private ElectricityCabinetService electricityCabinetService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private ElectricityCarModelService electricityCarModelService;

    @Resource
    private UserDataScopeService userDataScopeService;

    /**
     * 根据优惠券ID集获取优惠券信息<br />
     * K：优惠券ID<br />
     * V：优惠券信息
     * @param couponIdList 优惠券ID集
     * @return
     */
    protected Map<Long, Coupon> queryCouponForMapByIds(List<Long> couponIdList) {
        if (CollectionUtils.isEmpty(couponIdList)) {
            return Collections.emptyMap();
        }
        CouponQuery couponQuery = CouponQuery.builder()
                .ids(couponIdList)
                .build();

        R couponResult = couponService.queryList(couponQuery);
        if (!couponResult.isSuccess()) {
            return Collections.emptyMap();
        }

        List<Coupon> couponList = (List<Coupon>) couponResult.getData();

        Map<Long, Coupon> couponMap = new HashMap<>();
        couponList.forEach(coupon -> {
            couponMap.put(Long.valueOf(coupon.getId()), coupon);
        });

        return couponMap;
    }

    /**
     * 根据柜机ID集获取柜机信息<br />
     * K：柜机ID<br />
     * V：柜机信息
     * @param cabinetIds 柜机ID集
     * @return
     */
    protected Map<Integer, ElectricityCabinet> getCabinetByIdsForMap(Set<Integer> cabinetIds) {
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return Collections.emptyMap();
        }

        List<ElectricityCabinet> electricityCabinets = electricityCabinetService.listByIds(cabinetIds);

        if (CollectionUtils.isEmpty(electricityCabinets)) {
            return Collections.emptyMap();
        }

        Map<Integer, ElectricityCabinet> cabinetMap = electricityCabinets.stream()
                .collect(Collectors.toMap(ElectricityCabinet::getId, Function.identity(), (k1, k2) -> k1));

        return cabinetMap;
    }

    /**
     * 根据租车套餐ID集获取套餐信息<br />
     * K：租车套餐ID<br />
     * V：租车套餐信息
     * @param carRentalPackageIds 租车套餐ID集
     * @return 租车套餐ID:租车套餐信息
     */
    protected Map<Long, CarRentalPackagePO> getCarRentalPackageByIdsForMap(Set<Long> carRentalPackageIds) {

        List<CarRentalPackagePO> resData = carRentalPackageService.selectByIds(new ArrayList<>(carRentalPackageIds));
        if (CollectionUtils.isEmpty(resData)) {
            log.info("BasicController.getCarRentalPackageByIdsForMap response is {}", JSON.toJSONString(resData));
            return Collections.emptyMap();
        }

        Map<Long, CarRentalPackagePO> carRentalPackageMap = resData.stream()
                .collect(Collectors.toMap(CarRentalPackagePO::getId, Function.identity(), (k1, k2) -> k1));
        return carRentalPackageMap;

    }

    /**
     * 根据租车套餐ID集获取套餐名称<br />
     * K：租车套餐ID<br />
     * V：租车套餐名称
     * @param carRentalPackageIds 租车套餐ID集
     * @return 租车套餐ID:租车套餐名称
     */
    protected Map<Long, String> getCarRentalPackageNameByIdsForMap(Set<Long> carRentalPackageIds) {

        List<CarRentalPackagePO> resData = carRentalPackageService.selectByIds(new ArrayList<>(carRentalPackageIds));
        if (CollectionUtils.isEmpty(resData)) {
            log.info("BasicController.getCarRentalPackageNameByIdsForMap response is {}", JSON.toJSONString(resData));
            return Collections.emptyMap();
        }

        Map<Long, String> carRentalPackageMap = resData.stream()
                .collect(Collectors.toMap(CarRentalPackagePO::getId, CarRentalPackagePO::getName, (k1, k2) -> k1));
        return carRentalPackageMap;

    }

    /**
     * 根据UID集获取C端用户信息<br />
     * K：UID<br />
     * V：C端用户实体
     * @param uids UID集
     * @return
     */
    protected Map<Long, UserInfo> getUserInfoByUidsForMap(Set<Long> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<UserInfo> userQueryWrapper = new LambdaQueryWrapper();
        userQueryWrapper.in(UserInfo::getUid, uids);
        List<UserInfo> userInfos = userInfoService.list(userQueryWrapper);
        if(userInfos.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUid, Function.identity(), (k1, k2) -> k1));

        return userInfoMap;
    }

    /**
     * 根据门店ID集获取门店名称<br />
     * K：门店ID<br />
     * V：门店名称
     * @param storeIds 门店ID集
     * @return
     */
    protected Map<Long, String> getStoreNameByIdsForMap(Set<Long> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }

        StoreQuery storeQuery = new StoreQuery();
        Triple<Boolean, String, Object> storeTriple = storeService.selectListByQuery(storeQuery);
        List<Store> stores = (List<Store>) storeTriple.getRight();
        if (CollectionUtils.isEmpty(stores)) {
            return Collections.emptyMap();
        }

        Map<Long, String> storeNameMap = stores.stream().collect(Collectors.toMap(Store::getId, Store::getName, (k1, k2) -> k1));

        return storeNameMap;
    }

    /**
     * 根据加盟商ID集获取加盟商名称<br />
     * K：加盟商ID<br />
     * V：加盟商名称
     * @param franchiseeIds 加盟商ID集
     * @return
     */
    protected Map<Long, String> getFranchiseeNameByIdsForMap(Set<Long> franchiseeIds) {
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return Collections.emptyMap();
        }

        FranchiseeQuery franchiseeQuery = new FranchiseeQuery();
        franchiseeQuery.setIds(new ArrayList<>(franchiseeIds));
        Triple<Boolean, String, Object> franchiseeTriple = franchiseeService.selectListByQuery(franchiseeQuery);
        List<Franchisee> franchiseeList = (List<Franchisee>) franchiseeTriple.getRight();
        if (franchiseeList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> franchiseeMap = franchiseeList.stream()
                .collect(Collectors.toMap(Franchisee::getId, Franchisee::getName, (k1, k2) -> k1));

        return franchiseeMap;
    }

    /**
     * 根据车辆型号ID集获取车辆型号名称<br />
     * K：车辆型号ID
     * V：车辆型号名称
     * @param carModelIds 车辆型号ID集
     * @return
     */
    protected Map<Integer, String> getCarModelNameByIdsForMap(Set<Integer> carModelIds) {
        if (CollectionUtils.isEmpty(carModelIds)) {
            return Collections.emptyMap();
        }

        ElectricityCarModelQuery electricityCarModelQuery = new ElectricityCarModelQuery();
        electricityCarModelQuery.setIds(carModelIds);
        List<ElectricityCarModel> carModelList = electricityCarModelService.selectByQuery(electricityCarModelQuery);
        if (CollectionUtils.isEmpty(carModelList)) {
            return Collections.emptyMap();
        }

        Map<Integer, String> carModelMap = carModelList.stream().collect(Collectors.toMap(ElectricityCarModel::getId, ElectricityCarModel::getName, (k1, k2) -> k1));

        return carModelMap;
    }

    /**
     * 检查权限
     * @return
     */
    protected void checkPermission() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
    }

}
