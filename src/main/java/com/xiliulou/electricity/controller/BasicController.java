package com.xiliulou.electricity.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderRentRefundQryModel;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
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
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

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
     * 根据套餐退租订单编码集获取对应退租订单信息<br />
     * K：套餐购买订单编码<br />
     * V：退租订单信息
     * @param orderNos 退租订单编码集
     * @return K：套餐退租订单编码，V：退租订单信息
     */
    protected Map<String, CarRentalPackageOrderRentRefundPo> queryCarRentalRentRefundOrderByOrderNos(Set<String> orderNos) {
        if (CollectionUtils.isEmpty(orderNos)) {
            return Collections.emptyMap();
        }

        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        qryModel.setOrderNoList(new ArrayList<>(orderNos));
        List<CarRentalPackageOrderRentRefundPo> rentRefundEntityList = carRentalPackageOrderRentRefundService.list(qryModel);

        if (CollectionUtils.isEmpty(rentRefundEntityList)) {
            return Collections.emptyMap();
        }

        return rentRefundEntityList.stream().collect(Collectors.toMap(CarRentalPackageOrderRentRefundPo::getOrderNo, Function.identity(), (k1, k2) -> k1));

    }

    /**
     * 根据套餐购买订单编码集获取对应退租订单信息<br />
     * K：套餐购买订单编码<br />
     * V：退租订单信息
     * @param rentalPackageOrderNos 套餐购买订单编码集
     * @return K：套餐购买订单编码，V：退租订单信息
     */
    protected Map<String, CarRentalPackageOrderRentRefundPo> queryCarRentalRentRefundOrderByRentalOrderNos(Set<String> rentalPackageOrderNos) {
        if (CollectionUtils.isEmpty(rentalPackageOrderNos)) {
            return Collections.emptyMap();
        }

        CarRentalPackageOrderRentRefundQryModel qryModel = new CarRentalPackageOrderRentRefundQryModel();
        qryModel.setRentalPackageOrderNoList(new ArrayList<>(rentalPackageOrderNos));
        List<CarRentalPackageOrderRentRefundPo> rentRefundEntityList = carRentalPackageOrderRentRefundService.list(qryModel);

        if (CollectionUtils.isEmpty(rentRefundEntityList)) {
            return Collections.emptyMap();
        }
        // 此处有一个及其不易注意的坑点，k1, k2 的取值，目前取值k2，注意点在于数据库的数据，要按照主键ID正序排列
        return rentRefundEntityList.stream().collect(Collectors.toMap(CarRentalPackageOrderRentRefundPo::getRentalPackageOrderNo, Function.identity(), (k1, k2) -> k2));

    }

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
    protected Map<Long, CarRentalPackagePo> getCarRentalPackageByIdsForMap(Set<Long> carRentalPackageIds) {

        List<CarRentalPackagePo> resData = carRentalPackageService.selectByIds(new ArrayList<>(carRentalPackageIds));
        if (CollectionUtils.isEmpty(resData)) {
            log.info("BasicController.getCarRentalPackageByIdsForMap response is {}", JSON.toJSONString(resData));
            return Collections.emptyMap();
        }

        Map<Long, CarRentalPackagePo> carRentalPackageMap = resData.stream()
                .collect(Collectors.toMap(CarRentalPackagePo::getId, Function.identity(), (k1, k2) -> k1));
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

        List<CarRentalPackagePo> resData = carRentalPackageService.selectByIds(new ArrayList<>(carRentalPackageIds));
        if (CollectionUtils.isEmpty(resData)) {
            log.info("BasicController.getCarRentalPackageNameByIdsForMap response is {}", JSON.toJSONString(resData));
            return Collections.emptyMap();
        }

        Map<Long, String> carRentalPackageMap = resData.stream()
                .collect(Collectors.toMap(CarRentalPackagePo::getId, CarRentalPackagePo::getName, (k1, k2) -> k1));
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
     * 检查权限<br />
     * <pre>
     *     加盟商ID集：Long
     *     门店ID集：Long
     * </pre>
     * @return L:加盟商ID集，M：门店ID集，R：校验是否成功
     */
    protected Triple<List<Long>, List<Long>, Boolean> checkPermission() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 加盟商数据权
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                log.info("BasicController.checkPermission. Franchisee data rights are empty.");
                return Triple.of(null, null, false);
            }
        }

        // 门店数据权
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                log.info("BasicController.checkPermission. Store data rights are empty.");
                return Triple.of(null, storeIds, false);
            }
        }

        return Triple.of(franchiseeIds, storeIds, true);
    }

    /**
     * 检查权限<br />
     * <pre>
     *     加盟商ID集：Integer
     *     门店ID集：Integer
     * </pre>
     * @return L:加盟商ID集，M：门店ID集，R：校验是否成功
     */
    protected Triple<List<Integer>, List<Integer>, Boolean> checkPermissionInteger() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 加盟商数据权
        List<Integer> franchiseeIdList = null;
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                log.info("BasicController.checkPermission. Franchisee data rights are empty.");
                return Triple.of(null, null, false);
            }
            franchiseeIdList = franchiseeIds.stream().map(franchiseeId -> franchiseeId.intValue()).collect(Collectors.toList());
        }

        // 门店数据权
        List<Integer> storeIdList = null;
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(storeIds)) {
                log.info("BasicController.checkPermission. Store data rights are empty.");
                return Triple.of(null, null, false);
            }
            storeIdList = storeIds.stream().map(storeId -> storeId.intValue()).collect(Collectors.toList());
        }

        return Triple.of(franchiseeIdList, storeIdList, true);
    }

}
