package com.xiliulou.electricity.controller.admin.car;

import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.MemberCardAndCarRentalPackageSortParamQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageNameReq;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ValidList;
import com.xiliulou.electricity.vo.car.CarRentalPackageSearchVO;
import com.xiliulou.electricity.vo.car.CarRentalPackageVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel.COUPON_MAX_LIMIT;
import static com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel.USER_GROUP_MAX_LIMIT;

/**
 * 租车套餐表 Controller
 *
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackage")
public class JsonAdminCarRentalPackageController extends BasicController {
    
    @Resource
    private BatteryModelService batteryModelService;
    
    @Resource
    private CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;
    
    @Resource
    private ElectricityCarModelService electricityCarModelService;
    
    @Resource
    private StoreService storeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private CarRentalPackageBizService carRentalPackageBizService;
    
    @Resource
    private CarRentalPackageService carRentalPackageService;
    
    
    /**
     * 根据名称查询列表
     *
     * @param qryReq 请求参数类
     * @return 车辆套餐信息集
     */
    @PostMapping("/pageByName")
    public R<List<CarRentalPackageVo>> pageByName(@RequestBody CarRentalPackageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageQryReq();
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);
        
        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        
        // 调用服务
        List<CarRentalPackagePo> carRentalPackageEntityList = carRentalPackageService.page(qryModel);
        if (ObjectUtils.isEmpty(carRentalPackageEntityList)) {
            return R.ok(Collections.emptyList());
        }
        
        // 模型转换，封装返回
        List<CarRentalPackageVo> carRentalPackageVOList = carRentalPackageEntityList.stream().map(carRentalPackageEntity -> {
            
            CarRentalPackageVo carRentalPackageVo = new CarRentalPackageVo();
            carRentalPackageVo.setId(carRentalPackageEntity.getId());
            carRentalPackageVo.setName(carRentalPackageEntity.getName());
            
            return carRentalPackageVo;
        }).collect(Collectors.toList());
        
        return R.ok(carRentalPackageVOList);
    }
    
    /**
     * 检测唯一：租户ID+套餐名称
     *
     * @param name 套餐名称
     * @return true(存在)、false(不存在)
     */
    @GetMapping("/uqByTenantIdAndName")
    public R<Boolean> uqByTenantIdAndName(String name) {
        if (StringUtils.isEmpty(name)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 获取租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 调用服务
        return R.ok(carRentalPackageService.uqByTenantIdAndName(tenantId, name));
    }
    
    /**
     * 根据ID修改上下架状态
     *
     * @param id     主键ID
     * @param status 上下架状态
     * @return true(成功)、false(失败)
     */
    @GetMapping("/modifyStatusById")
    public R<Boolean> modifyStatusById(Long id, Integer status) {
        if (!ObjectUtils.allNotNull(id, status)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 获取用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        // 调用服务
        return R.ok(carRentalPackageService.updateStatusById(id, status, user.getUid()));
    }
    
    /**
     * 条件查询列表
     *
     * @param qryReq 请求参数类
     * @return 车辆套餐信息集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageVo>> page(@RequestBody CarRentalPackageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageQryReq();
        }
        
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //   return R.ok(Collections.EMPTY_LIST);
        //}
        // 权限设置
        Triple<List<Integer>, List<Integer>, Boolean> triple = checkPermissionInteger();
        if (!triple.getRight()) {
            return R.ok(Collections.emptyList());
        }
        
        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(triple.getLeft());
        qryModel.setStoreIdList(triple.getMiddle());
        
        if (Objects.nonNull(qryReq.getUserGroupId()) && Objects.nonNull(qryReq.getApplicableType())) {
            return R.ok(Collections.emptyList());
        }
        
        if (StringUtils.isNotBlank(qryReq.getUserGroupId())) {
            qryModel.setIsUserGroup(YesNoEnum.NO.getCode());
        }
        
        if (!Objects.isNull(qryReq.getApplicableType())) {
            qryModel.setIsUserGroup(YesNoEnum.YES.getCode());
        }
        
        // 调用服务
        List<CarRentalPackagePo> carRentalPackageEntityList = carRentalPackageService.page(qryModel);
        if (ObjectUtils.isEmpty(carRentalPackageEntityList)) {
            return R.ok(Collections.emptyList());
        }
        
        // 获取辅助业务信息（加盟商、车辆型号、优惠券信息、关联信息）
        Set<Long> franchiseeIds = new HashSet<>();
        Set<Integer> carModelIds = new HashSet<>();
        //        List<Long> couponIds = new ArrayList<>();
        List<Long> packageIds = new ArrayList<>();
        carRentalPackageEntityList.forEach(carRentalPackageEntity -> {
            franchiseeIds.add(Long.valueOf(carRentalPackageEntity.getFranchiseeId()));
            carModelIds.add(carRentalPackageEntity.getCarModelId());
            //            Long couponId = carRentalPackageEntity.getCouponId();
            //            if (ObjectUtils.isNotEmpty(couponId) && !couponIds.contains(couponId)) {
            //                couponIds.add(couponId);
            //            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackageEntity.getType())) {
                packageIds.add(carRentalPackageEntity.getId());
            }
        });
        
        // 获取辅助业务信息（加盟商、车辆型号）
        // 加盟商信息
        Map<Long, String> franchiseeMap = getFranchiseeNameByIdsForMap(franchiseeIds);
        
        // 车辆型号信息
        Map<Integer, String> carModelMap = getCarModelNameByIdsForMap(carModelIds);
        
        // 优惠券信息
        //        Map<Long, Coupon> couponMap = getCouponForMapByIds(couponIds);
        
        // 模型转换，封装返回
        List<CarRentalPackageVo> carRentalPackageVOList = carRentalPackageEntityList.stream().map(carRentalPackageEntity -> {
            
            CarRentalPackageVo carRentalPackageVo = new CarRentalPackageVo();
            BeanUtils.copyProperties(carRentalPackageEntity, carRentalPackageVo, "couponIds", "userGroupIds");
            carRentalPackageVo.setCouponIds(carRentalPackageEntity.getCouponIds());
            if (!franchiseeMap.isEmpty()) {
                carRentalPackageVo.setFranchiseeName(franchiseeMap.getOrDefault(Long.valueOf(carRentalPackageEntity.getFranchiseeId()), ""));
            }
            
            if (!carModelMap.isEmpty()) {
                carRentalPackageVo.setCarModelName(carModelMap.getOrDefault(carRentalPackageEntity.getCarModelId(), ""));
            }
            
            //设置优惠劵信息
            List<Long> couponIds = carRentalPackageEntity.getCouponIds();
            carRentalPackageVo = carRentalPackageBizService.buildCouponsToCarRentalVo(carRentalPackageVo, couponIds);
            
            //设置用户分组信息
            List<Long> userGroupIds = carRentalPackageEntity.getUserGroupId();
            carRentalPackageVo = carRentalPackageBizService.buildUserGroupToCarRentalVo(carRentalPackageVo, userGroupIds);
            //            if (!couponMap.isEmpty()) {
            //                carRentalPackageVo.setCouponName(couponMap.getOrDefault(carRentalPackageEntity.getCouponId(), new Coupon()).getName());
            //            }
            
            // TODO 临时解决，添加字段，后续优化
            // 查询电池型号
            if (carRentalPackageEntity.getType().equals(RentalPackageTypeEnum.CAR_BATTERY.getCode())) {
                List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(carRentalPackageEntity.getId());
                List<String> batteryModelTypes = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(batteryModelTypes)) {
                    List<BatteryModel> batteryModels = batteryModelService.selectByBatteryTypes(tenantId, batteryModelTypes);
                    carRentalPackageVo.setBatteryModelTypeShorts(batteryModels.stream().map(BatteryModel::getBatteryVShort).distinct().collect(Collectors.toList()));
                }
            }
            return carRentalPackageVo;
        }).collect(Collectors.toList());
        
        return R.ok(carRentalPackageVOList);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageQryReq();
        }
        
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //    return R.ok(NumberConstant.ZERO);
        //}
        // 权限设置
        Triple<List<Integer>, List<Integer>, Boolean> triple = checkPermissionInteger();
        if (!triple.getRight()) {
            return R.ok(NumberConstant.ZERO);
        }
        
        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        qryModel.setFranchiseeIdList(triple.getLeft());
        qryModel.setStoreIdList(triple.getMiddle());
        
        if (Objects.nonNull(qryReq.getUserGroupId()) && Objects.nonNull(qryReq.getApplicableType())) {
            return R.ok(0);
        }
        
        if (StringUtils.isNotBlank(qryReq.getUserGroupId())) {
            qryModel.setIsUserGroup(YesNoEnum.NO.getCode());
        }
        
        if (!Objects.isNull(qryReq.getApplicableType())) {
            qryModel.setIsUserGroup(YesNoEnum.YES.getCode());
        }
        
        // 调用服务
        return R.ok(carRentalPackageService.count(qryModel));
    }
    
    /**
     * 根据ID查询套餐详情
     *
     * @param id 主键ID
     * @return 车辆套餐信息
     */
    @GetMapping("/queryById")
    public R<CarRentalPackageVo> queryById(Long id) {
        if (id <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 调用服务
        CarRentalPackagePo carRentalPackageEntity = carRentalPackageService.selectById(id);
        if (ObjectUtils.isEmpty(carRentalPackageEntity)) {
            return R.ok();
        }
        
        // 查询加盟商
        Long franchiseeId = Long.valueOf(carRentalPackageEntity.getFranchiseeId());
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        
        // 查询门店
        Long storeId = Long.valueOf(carRentalPackageEntity.getStoreId());
        Store store = storeService.queryByIdFromCache(storeId);
        
        // 查询车辆型号
        Integer carModelId = carRentalPackageEntity.getCarModelId();
        ElectricityCarModel carModel = electricityCarModelService.queryByIdFromCache(carModelId);
        
        // 转换模型，组装返回值
        CarRentalPackageVo carRentalPackageVo = new CarRentalPackageVo();
        BeanUtils.copyProperties(carRentalPackageEntity, carRentalPackageVo, "couponIds", "userGroupIds");
        
        // 赋值辅助业务数据
        carRentalPackageVo.setFranchiseeName(ObjectUtils.isNotEmpty(franchisee) ? franchisee.getName() : null);
        carRentalPackageVo.setStoreName(ObjectUtils.isNotEmpty(store) ? store.getName() : null);
        carRentalPackageVo.setCarModelName(ObjectUtils.isNotEmpty(carModel) ? carModel.getName() : null);
        
        //设置优惠劵名称
        List<Long> couponIds = carRentalPackageEntity.getCouponIds();
        carRentalPackageVo = carRentalPackageBizService.buildCouponsToCarRentalVo(carRentalPackageVo, couponIds);
        
        //设置用户分组信息
        List<Long> userGroupIds = carRentalPackageEntity.getUserGroupId();
        carRentalPackageVo = carRentalPackageBizService.buildUserGroupToCarRentalVo(carRentalPackageVo, userGroupIds);
        
        // 查询电池型号
        if (carRentalPackageEntity.getType().equals(RentalPackageTypeEnum.CAR_BATTERY.getCode())) {
            List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(carRentalPackageEntity.getId());
            List<String> batteryModelTypes = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).distinct().collect(Collectors.toList());
            carRentalPackageVo.setBatteryModelTypes(batteryModelTypes);
        }
        
        return R.ok(carRentalPackageVo);
    }
    
    /**
     * 根据ID删除套餐
     *
     * @param id 主键ID
     * @return true(成功)、false(失败)
     */
    @GetMapping("/delById")
    public R<Boolean> delById(Long id) {
        if (id <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        return R.ok(carRentalPackageBizService.delPackageById(id, user.getUid()));
    }
    
    /**
     * 根据ID修改租车套餐
     *
     * @param optModel 操作模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/modifyById")
    public R<Boolean> modifyById(@RequestBody @Valid CarRentalPackageOptModel optModel) {
    
        if (!ObjectUtils.allNotNull(optModel, optModel.getId(), optModel.getName())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        
        // 套餐名称长度最大为14
        if (optModel.getName().length() > 14) {
            throw new BizException("100377", "参数校验错误");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(optModel.getGiveCoupon(), YesNoEnum.YES.getCode()) && (CollectionUtil.isEmpty(optModel.getCouponIds())
                || optModel.getCouponIds().size() > COUPON_MAX_LIMIT)) {
            throw new BizException("300833", "优惠劵最多支持发10张");
        }
        
        if (Objects.equals(optModel.getIsUserGroup(), YesNoEnum.NO.getCode()) && (CollectionUtil.isEmpty(optModel.getUserGroupIds())
                || optModel.getUserGroupIds().size() > USER_GROUP_MAX_LIMIT)) {
            throw new BizException("300834", "用户分组最多支持选10个");
        }
        
        optModel.setTenantId(tenantId);
        optModel.setUpdateUid(user.getUid());
        
        CarRentalPackagePo entity = new CarRentalPackagePo();
        BeanUtils.copyProperties(optModel, entity, "couponId", "couponIds", "userGroupIds");
        if (!Objects.isNull(optModel.getCouponId())) {
            entity.setCouponId(optModel.getCouponId());
        }
        List<Long> couponIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(optModel.getCouponIds())) {
            couponIds.addAll(optModel.getCouponIds());
        }
        entity.setCouponIds(couponIds);
        entity.setUserGroupId(optModel.getUserGroupIds());
        return R.ok(carRentalPackageService.updateById(entity));
    }
    
    /**
     * 新增租车套餐设置
     *
     * @param optModel 操作模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody @Valid CarRentalPackageOptModel optModel) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        optModel.setTenantId(tenantId);
        optModel.setCreateUid(user.getUid());
        
        return R.ok(carRentalPackageBizService.insertPackage(optModel));
    }
    
    /**
     * 批量修改套餐排序参数，排序参数为用户端排序使用
     *
     * @param sortParamQueries
     * @return
     */
    @PostMapping("/batchUpdateSortParam")
    public R batchUpdateSortParam(@RequestBody @Validated ValidList<MemberCardAndCarRentalPackageSortParamQuery> sortParamQueries) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 仅超级管理员和运营商可修改排序参数
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        if (sortParamQueries.isEmpty()) {
            return R.ok();
        }
        
        return R.ok(carRentalPackageService.batchUpdateSortParam(sortParamQueries));
    }
    
    /**
     * 查询租车套餐以供排序
     *
     * @return
     */
    @GetMapping("/listCarRentalPackageForSort")
    public R listCarRentalPackageForSort() {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 查询数据较多，限制仅超级管理员和运营商可使用
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(carRentalPackageService.listCarRentalPackageForSort());
    }
    
    /**
     * <p>
     * Description: queryCarPackageName 14.4 套餐购买记录（2条优化项）
     * </p>
     *
     * @param packageName packageName 套餐名称
     * @return com.xiliulou.core.web.R<java.util.List < com.xiliulou.electricity.vo.car.CarRentalPackageSearchVo>>
     * <p>Project: JsonAdminCarRentalPackageController</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/3/14
     */
    @GetMapping("/search/name")
    public R<List<CarRentalPackageSearchVO>> queryCarPackageName(@RequestParam(value = "packageName", required = false) String packageName,
            @RequestParam(value = "offset", required = false) Long offset, @RequestParam(value = "size", required = false) Long size) {
        if (offset == null || offset < 0) {
            offset = 0L;
        }
        if (size == null || size < 0 || size > 50) {
            size = 50L;
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        CarRentalPackageNameReq rentalPackageNameReq = CarRentalPackageNameReq.builder().packageName(packageName).offset(offset).size(size).tenantId(tenantId.longValue()).build();
        return R.ok(carRentalPackageService.queryToSearchByName(rentalPackageNameReq));
    }
    
}
