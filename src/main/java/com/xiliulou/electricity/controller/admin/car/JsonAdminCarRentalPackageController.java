package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐表 Controller
 * TODO 权限后补
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/car/carRentalPackage")
public class JsonAdminCarRentalPackageController extends BasicController {

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
     * 检测唯一：租户ID+套餐名称
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
     * @param id 主键ID
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
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 调用服务
        return R.ok(carRentalPackageService.updateStatusById(id, status, user.getUid()));
    }

    /**
     * 条件查询列表
     * @param qryReq 请求参数类
     * @return 车辆套餐信息集
     */
    @PostMapping("/page")
    public R<List<CarRentalPackageVO>> page(@RequestBody CarRentalPackageQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new CarRentalPackageQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);

        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        List<CarRentalPackagePO> carRentalPackageEntityList = carRentalPackageService.page(qryModel);
        if (ObjectUtils.isEmpty(carRentalPackageEntityList)) {
            return R.ok(Collections.emptyList());
        }

        // 获取辅助业务信息（加盟商、车辆型号）
        Set<Long> franchiseeIds = new HashSet<>();
        Set<Integer> carModelIds = new HashSet<>();
        carRentalPackageEntityList.forEach(carRentalPackageEntity -> {
            franchiseeIds.add(Long.valueOf(carRentalPackageEntity.getFranchiseeId()));
            carModelIds.add(carRentalPackageEntity.getCarModelId());
        });

        // 获取辅助业务信息（加盟商、车辆型号）
        // 加盟商信息
        Map<Long, String> franchiseeMap = getFranchiseeNameByIdsForMap(franchiseeIds);

        // 车辆型号信息
        Map<Integer, String> carModelMap = getCarModelNameByIdsForMap(carModelIds);

        // 模型转换，封装返回
        List<CarRentalPackageVO> carRentalPackageVOList = carRentalPackageEntityList.stream().map(carRentalPackageEntity -> {

            CarRentalPackageVO carRentalPackageVo = new CarRentalPackageVO();
            BeanUtils.copyProperties(carRentalPackageEntity, carRentalPackageVo);

            if (!franchiseeMap.isEmpty()) {
                carRentalPackageVo.setFranchiseeName(franchiseeMap.getOrDefault(Long.valueOf(carRentalPackageEntity.getFranchiseeId()), ""));
            }

            if (!carModelMap.isEmpty()) {
                carRentalPackageVo.setCarModelName(carModelMap.getOrDefault(carRentalPackageEntity.getCarModelId(), ""));
            }

            return carRentalPackageVo;
        }).collect(Collectors.toList());

        return R.ok(carRentalPackageVOList);
    }

    /**
     * 条件查询总数
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

        // 转换请求体
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);

        // 调用服务
        return R.ok(carRentalPackageService.count(qryModel));
    }

    /**
     * 根据ID查询套餐详情
     * @param id 主键ID
     * @return 车辆套餐信息
     */
    @GetMapping("/queryById")
    public R<CarRentalPackageVO> queryById(Long id) {
        if (id <= 0L) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 调用服务
        CarRentalPackagePO carRentalPackageEntity = carRentalPackageService.selectById(id);
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
        CarRentalPackageVO carRentalPackageVo = new CarRentalPackageVO();
        BeanUtils.copyProperties(carRentalPackageEntity, carRentalPackageVo);

        // 赋值辅助业务数据
        carRentalPackageVo.setFranchiseeName(ObjectUtils.isNotEmpty(franchisee) ? franchisee.getName() : null);
        carRentalPackageVo.setStoreName(ObjectUtils.isNotEmpty(store) ? store.getName() : null);
        carRentalPackageVo.setCarModelName(ObjectUtils.isNotEmpty(carModel) ? carModel.getName() : null);

        // 查询电池型号
        if (carRentalPackageEntity.getType().equals(CarRentalPackageTypeEnum.CAR_BATTERY.getCode())) {
            List<CarRentalPackageCarBatteryRelPO> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(carRentalPackageEntity.getId());
            List<String> batteryModelTypes = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPO::getBatteryModelType).distinct().collect(Collectors.toList());
            carRentalPackageVo.setBatteryModelTypes(batteryModelTypes);
        }

        return R.ok(carRentalPackageVo);
    }

    /**
     * 根据ID删除套餐
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
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(carRentalPackageBizService.delPackageById(id, user.getUid()));
    }

    /**
     * 根据ID修改租车套餐
     * @param optModel 操作模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/modifyById")
    public R<Boolean> modifyById(@RequestBody @Valid CarRentalPackageOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getId(), optModel.getName())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        optModel.setTenantId(tenantId);
        optModel.setUpdateUid(user.getUid());

        CarRentalPackagePO entity = new CarRentalPackagePO();
        BeanUtils.copyProperties(optModel, entity);

        return R.ok(carRentalPackageService.updateById(entity));
    }

    /**
     * 新增租车套餐设置
     * @param optModel 操作模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody @Valid CarRentalPackageOptModel optModel) {

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        optModel.setTenantId(tenantId);
        optModel.setCreateUid(user.getUid());

        return R.ok(carRentalPackageBizService.insertPackage(optModel));
    }

}
