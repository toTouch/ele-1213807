package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageQueryReq;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageVO;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐控制层
 *
 * @author xiaohui.song
 **/
@RestController
@RequestMapping("/admin/car/carRentalPackage")
public class JsonAdminCarRentalPackageController extends JsonAdminBasicController {

    @Resource
    private ElectricityCarModelService electricityCarModelService;

    @Resource
    private StoreService storeService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    /**
     * 检测唯一：租户ID+套餐名称
     * @param name 套餐名称
     * @return
     */
    @GetMapping("/uqByTenantIdAndName")
    public R<Boolean> uqByTenantIdAndName(String name) {
        if (StringUtils.isEmpty(name)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // 获取租户
        Integer tenantId = TenantContextHolder.getTenantId();
        // 调用服务
        return carRentalPackageService.uqByTenantIdAndName(tenantId, name);
    }

    /**
     * 根据ID修改上下架状态
     * @param id 主键ID
     * @param status 上下架状态
     * @return
     */
    @PostMapping("/modifyStatusById")
    public R<Boolean> modifyStatusById(Long id, Integer status) {
        if (ObjectUtils.allNotNull(id, status)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // 获取用户
        TokenUser user = SecurityUtils.getUserInfo();
        // 调用服务
        return carRentalPackageService.updateStatusById(id, status, user.getUid());
    }

    /**
     * 条件查询列表
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageVO>> page(@RequestBody CarRentalPackageQueryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageQueryReq();
        }
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);
        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        // 调用服务
        R<List<CarRentalPackagePO>> listRes = carRentalPackageService.page(qryModel);
        if (!listRes.isSuccess()) {
            return R.fail(listRes.getErrCode(), listRes.getErrMsg());
        }
        // 获取辅助业务信息（加盟商、车辆型号）
        Set<Long> franchiseeIds = new HashSet<>();
        Set<Integer> carModelIds = new HashSet<>();
        List<CarRentalPackagePO> carRentalPackagePOList = listRes.getData();
        carRentalPackagePOList.forEach(carRentalPackage -> {
            franchiseeIds.add(Long.valueOf(carRentalPackage.getFranchiseeId()));
            carModelIds.add(carRentalPackage.getCarModelId());
        });
        // 获取辅助业务信息（加盟商、车辆型号）
        // 加盟商信息
        FranchiseeQuery franchiseeQuery = new FranchiseeQuery();
        franchiseeQuery.setIds(new ArrayList<>(franchiseeIds));
        Triple<Boolean, String, Object> franchiseeTriple = franchiseeService.selectListByQuery(franchiseeQuery);
        List<Franchisee> franchiseeList = (List<Franchisee>) franchiseeTriple.getRight();
        Map<Long, String> franchiseeMap = null;
        if (!franchiseeList.isEmpty()) {
            franchiseeMap = franchiseeList.stream().collect(Collectors.toMap(Franchisee::getId, Franchisee::getName, (k1, k2) -> k1));
        }
        // 车辆型号信息
        ElectricityCarModelQuery electricityCarModelQuery = new ElectricityCarModelQuery();
        electricityCarModelQuery.setIds(carModelIds);
        List<ElectricityCarModel> carModelList = electricityCarModelService.selectByQuery(electricityCarModelQuery);
        Map<Integer, String> carModelMap = null;
        if (!carModelList.isEmpty()) {
            carModelMap = carModelList.stream().collect(Collectors.toMap(ElectricityCarModel::getId, ElectricityCarModel::getName, (k1, k2) -> k1));
        }
        // 模型转换，封装返回
        Map<Long, String> finalFranchiseeMap = franchiseeMap;
        Map<Integer, String> finalCarModelMap = carModelMap;
        List<CarRentalPackageVO> carRentalPackageVOList = carRentalPackagePOList.stream().map(carRentalPackage -> {
            CarRentalPackageVO carRentalPackageVO = new CarRentalPackageVO();
            BeanUtils.copyProperties(carRentalPackage, carRentalPackageVO);
            if (!finalFranchiseeMap.isEmpty()) {
                carRentalPackageVO.setFranchiseeName(finalFranchiseeMap.getOrDefault(Long.valueOf(carRentalPackage.getFranchiseeId()), ""));
            }
            if (!finalCarModelMap.isEmpty()) {
                carRentalPackageVO.setCarModelName(finalCarModelMap.getOrDefault(carRentalPackage.getCarModelId(), ""));
            }
            return carRentalPackageVO;
        }).collect(Collectors.toList());
        return R.ok(carRentalPackageVOList);
    }

    /**
     * 条件查询总数
     * @param queryReq 请求参数类
     * @return
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody CarRentalPackageQueryReq queryReq) {
        if (null == queryReq) {
            queryReq = new CarRentalPackageQueryReq();
        }
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        queryReq.setTenantId(tenantId);
        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(queryReq, qryModel);
        // 调用服务
        return carRentalPackageService.count(qryModel);
    }

    /**
     * 根据ID查询套餐详情
     * @param id 主键ID
     * @return
     */
    @GetMapping("/queryById")
    public R<CarRentalPackageVO> queryById(Long id) {
        if (id <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // 调用服务
        R<CarRentalPackagePO> carRentalPackagePORes = carRentalPackageService.selectById(id);
        if (!carRentalPackagePORes.isSuccess()) {
            return R.fail(carRentalPackagePORes.getErrCode(), carRentalPackagePORes.getErrMsg());
        }
        CarRentalPackagePO carRentalPackagePO = carRentalPackagePORes.getData();
        // 查询加盟商
        Long franchiseeId = Long.valueOf(carRentalPackagePO.getFranchiseeId());
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        // 查询门店
        Long storeId = Long.valueOf(carRentalPackagePO.getStoreId());
        Store store = storeService.queryByIdFromCache(storeId);
        // 查询型号
        Integer carModelId = carRentalPackagePO.getCarModelId();
        ElectricityCarModel carModel = electricityCarModelService.queryByIdFromCache(carModelId);
        // 转换模型，组装返回值
        CarRentalPackageVO carRentalPackageVO = new CarRentalPackageVO();
        BeanUtils.copyProperties(carRentalPackagePO, carRentalPackageVO);
        // 赋值辅助业务数据
        carRentalPackageVO.setFranchiseeName(franchisee.getName());
        carRentalPackageVO.setStoreName(store.getName());
        carRentalPackageVO.setCarModelName(carModel.getName());
        // 返回
        return R.ok(carRentalPackageVO);
    }

    /**
     * 根据ID删除套餐
     * @param id 主键ID
     * @return
     */
    @GetMapping("/delById")
    public R<Boolean> delById(Long id) {
        if (id <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // 登录态获取用户信息
        TokenUser user = SecurityUtils.getUserInfo();
        // 调用服务
        return carRentalPackageService.delById(id, user.getUid());
    }

    /**
     * 根据ID修改租车套餐
     * @param optModel 操作模型
     * @return
     */
    @PostMapping("/modifyById")
    public R<Boolean> modifyById(CarRentalPackageOptModel optModel) {
        if (optModel == null || optModel.getId() <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        // 登录态获取用户信息
        TokenUser user = SecurityUtils.getUserInfo();
        // 赋值操作人
        optModel.setUpdateUid(user.getUid());
        // 调用服务
        return carRentalPackageService.updateById(optModel);
    }

    /**
     * 新增租车套餐
     * @param optModel 操作模型
     * @return
     */
    @PostMapping("/insert")
    public R<Long> insert(CarRentalPackageOptModel optModel) {
        // 赋值租户及操作人
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        optModel.setTenantId(tenantId);
        optModel.setCreateUid(user.getUid());
        // 调用服务
        return carRentalPackageService.insert(optModel);
    }

}
