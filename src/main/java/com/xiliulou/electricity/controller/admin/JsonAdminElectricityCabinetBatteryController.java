package com.xiliulou.electricity.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.BatteryExcelQuery;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.BatteryExcelListener;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description: 电池 controller
 * @author: Mr.YG
 * @create: 2020-11-27 14:08
 **/
@RestController
@Slf4j
public class JsonAdminElectricityCabinetBatteryController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
    @Autowired
    FranchiseeService franchiseeService;

    /**
     * 新增电池
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/battery")
    public R save(@RequestBody @Validated ElectricityBattery electricityBattery) {

        return electricityBatteryService.saveElectricityBattery(electricityBattery);
    }

    /**
     * 修改电池
     *
     * @param
     * @return
     */
    @PutMapping(value = "/admin/battery")
    public R update(@RequestBody @Validated ElectricityBattery electricityBattery) {
        if (Objects.isNull(electricityBattery.getId())) {
            return R.fail("请求参数错误!");
        }

        return electricityBatteryService.update(electricityBattery);
    }

    /**
     * 删除电池
     *
     * @param
     * @return
     */
    @DeleteMapping(value = "/admin/battery/{id}")
    public R delete(@PathVariable("id") Long id) {
        return electricityBatteryService.deleteElectricityBattery(id);
    }

    /**
     * 电池分页
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/page")
    public R getElectricityBatteryPage(@RequestParam(value = "offset") Long offset,
                                       @RequestParam(value = "size") Long size,
                                       @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
                                       @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
                                       @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                                       @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
                                       @RequestParam(value = "sn", required = false) String sn,
                                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                       @RequestParam(value = "franchiseeName", required = false) String franchiseeName) {


        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setBusinessStatus(businessStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        electricityBatteryQuery.setFranchiseeId(franchiseeId);
        electricityBatteryQuery.setElectricityCabinetName(electricityCabinetName);
        electricityBatteryQuery.setFranchiseeName(franchiseeName);
        return electricityBatteryService.queryList(electricityBatteryQuery, offset, size);
    }

    /**
     * 获取当前加盟商的电池+未绑定加盟商的电池
     * @param offset
     * @param size
     * @param franchiseeId
     * @return
     */
    @GetMapping(value = "/admin/battery/bind/page")
    public R batteryBindPage(@RequestParam(value = "offset") Long offset,
                             @RequestParam(value = "size") Long size,
                             @RequestParam(value = "franchiseeId") Long franchiseeId) {
        return electricityBatteryService.queryBindListByPage(offset, size, franchiseeId);
    }

    /**
     * 分配电池
     */

    /**
     * 电池分页数量
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/queryCount")
    public R queryCount(@RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
                        @RequestParam(value = "businessStatus", required = false) Integer businessStatus,
                        @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
                        @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                        @RequestParam(value = "sn", required = false) String sn,
                        @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                        @RequestParam(value = "franchiseeName", required = false) String franchiseeName) {


        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setBusinessStatus(businessStatus);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        electricityBatteryQuery.setElectricityCabinetName(electricityCabinetName);
        electricityBatteryQuery.setFranchiseeId(franchiseeId);
        electricityBatteryQuery.setFranchiseeName(franchiseeName);
        return electricityBatteryService.queryCount(electricityBatteryQuery);
    }


    /**
     * 加盟商电池数量
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/pageByFranchisee")
    public R pageByFranchisee(@RequestParam(value = "offset") Long offset,
                              @RequestParam(value = "size") Long size,
                              @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
                              @RequestParam(value = "sn", required = false) String sn,
                              @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
        if (Objects.isNull(franchisee)) {
            return R.ok(CollectionUtils.EMPTY_COLLECTION);
        }

        //加盟商电池
//        List<FranchiseeBindElectricityBattery> franchiseeBindBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(franchisee.getId());
//
//        if (ObjectUtil.isEmpty(franchiseeBindBindElectricityBatteryList)) {
//            return R.ok(CollectionUtils.EMPTY_COLLECTION);
//        }
//        List<Long> electricityBatteryIdList = new ArrayList<>();
//        for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindBindElectricityBatteryList) {
//            electricityBatteryIdList.add(franchiseeBindElectricityBattery.getElectricityBatteryId());
//        }
//        if (ObjectUtil.isEmpty(electricityBatteryIdList)) {
//            return R.ok(CollectionUtils.EMPTY_COLLECTION);
//        }

        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setSn(sn);
//        electricityBatteryQuery.setElectricityBatteryIdList(electricityBatteryIdList);
        electricityBatteryQuery.setFranchiseeId(franchisee.getId());
        electricityBatteryQuery.setTenantId(TenantContextHolder.getTenantId());
        electricityBatteryQuery.setChargeStatus(chargeStatus);

        return electricityBatteryService.queryList(electricityBatteryQuery, offset, size);
    }


    /**
     * 加盟商电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/queryCountByFranchisee")
    public R queryCountByFranchisee(@RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
                                    @RequestParam(value = "sn", required = false) String sn,
                                    @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //加盟商
        Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
        if (Objects.isNull(franchisee)) {
            return R.ok(0);
        }

//        //加盟商电池
//        List<FranchiseeBindElectricityBattery> franchiseeBindBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(franchisee.getId());
//
//        if (ObjectUtil.isEmpty(franchiseeBindBindElectricityBatteryList)) {
//            return R.ok(0);
//        }
//        List<Long> electricityBatteryIdList = new ArrayList<>();
//        for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindBindElectricityBatteryList) {
//            electricityBatteryIdList.add(franchiseeBindElectricityBattery.getElectricityBatteryId());
//        }
//        if (ObjectUtil.isEmpty(electricityBatteryIdList)) {
//            return R.ok(0);
//        }

        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setPhysicsStatus(physicsStatus);
        electricityBatteryQuery.setSn(sn);
//        electricityBatteryQuery.setElectricityBatteryIdList(electricityBatteryIdList);
        electricityBatteryQuery.setFranchiseeId(franchisee.getId());
        electricityBatteryQuery.setTenantId(tenantId);
        electricityBatteryQuery.setChargeStatus(chargeStatus);

        return electricityBatteryService.queryCount(electricityBatteryQuery);
    }


    /**
     * 获取单个电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/{id}")
    public R queryById(@PathVariable("id") Long id) {
        return electricityBatteryService.queryById(id);
    }

    /**
     * 电池绑定/解绑加盟商
     */
    @PostMapping(value = "/admin/franchisee/bindElectricityBattery")
    public R bindElectricityBattery(@RequestBody @Validated(value = CreateGroup.class)
        BindElectricityBatteryQuery bindElectricityBatteryQuery) {
//        return franchiseeService.bindElectricityBattery(bindElectricityBatteryQuery);
        return electricityBatteryService.bindFranchisee(bindElectricityBatteryQuery);
    }






    /**
     * 文件上传
     * <p>
     * 1. 创建excel对应的实体对象
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器
     * <p>
     * 3. 直接读即可
     */
    @PostMapping("/admin/battery/excel")
    @Transactional(rollbackFor = Exception.class)
    public R upload(@RequestParam("file") MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), BatteryExcelQuery.class, new BatteryExcelListener(electricityBatteryService)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.ok();
    }

    /**
     * 电池总览
     * @param
     * @param sn
     * @return
     */
    @GetMapping("/admin/battery/queryBatteryOverview")
    public R queryBatteryOverview(@RequestParam(value = "businessStatus", required = false) Integer businessStatus,
                                  @RequestParam(value = "physicsStatus", required = false) Integer physicsStatus,
                                  @RequestParam(value = "sn", required = false) String sn) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityBatteryQuery electricityBatteryQuery=ElectricityBatteryQuery.builder()
                .physicsStatus(physicsStatus)
                .businessStatus(businessStatus)
                .sn(sn)
                .tenantId(tenantId).build();
        return electricityBatteryService.queryBatteryOverview(electricityBatteryQuery);
    }

    /**
     * 电池总览--电池统计
     * @return
     */
    @GetMapping("/admin/battery/batteryStatistical")
    public R batteryStatistical() {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        return electricityBatteryService.batteryStatistical(tenantId);
    }
}
