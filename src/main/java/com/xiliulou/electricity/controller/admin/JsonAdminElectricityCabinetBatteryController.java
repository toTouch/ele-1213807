package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
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
                                       @RequestParam(value = "status", required = false) Integer status,
                                       @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
                                       @RequestParam(value = "sn", required = false) String sn) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //组装参数
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setTenantId(tenantId);
        electricityBatteryQuery.setChargeStatus(chargeStatus);
        return electricityBatteryService.queryList(electricityBatteryQuery, offset, size);
    }

    /**
     * 电池分页数量
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/queryCount")
    public R queryCount(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "chargeStatus", required = false) Integer chargeStatus,
            @RequestParam(value = "sn", required = false) String sn) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //组装参数
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setTenantId(tenantId);
        electricityBatteryQuery.setChargeStatus(chargeStatus);
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
            @RequestParam(value = "status", required = false) Integer status,
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
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(Objects.isNull(franchisee)){
            return R.ok(new ArrayList<>());
        }

        //加盟商电池
        List<FranchiseeBindElectricityBattery> franchiseeBindBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(franchisee.getId());

        if(ObjectUtil.isEmpty(franchiseeBindBindElectricityBatteryList)){
            return R.ok(new ArrayList<>());
        }
        List<Long> electricityBatteryIdList=new ArrayList<>();
        for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindBindElectricityBatteryList) {
            electricityBatteryIdList.add(franchiseeBindElectricityBattery.getElectricityBatteryId());
        }
        if(ObjectUtil.isEmpty(electricityBatteryIdList)){
            return R.ok(new ArrayList<>());
        }

        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setElectricityBatteryIdList(electricityBatteryIdList);
        electricityBatteryQuery.setTenantId(tenantId);
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
    public R queryCountByFranchisee(@RequestParam(value = "status", required = false) Integer status,
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
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(Objects.isNull(franchisee)){
            return R.ok(0);
        }

        //加盟商电池
        List<FranchiseeBindElectricityBattery> franchiseeBindBindElectricityBatteryList = franchiseeBindElectricityBatteryService.queryByFranchiseeId(franchisee.getId());

        if(ObjectUtil.isEmpty(franchiseeBindBindElectricityBatteryList)){
            return R.ok(0);
        }
        List<Long> electricityBatteryIdList=new ArrayList<>();
        for (FranchiseeBindElectricityBattery franchiseeBindElectricityBattery : franchiseeBindBindElectricityBatteryList) {
            electricityBatteryIdList.add(franchiseeBindElectricityBattery.getElectricityBatteryId());
        }
        if(ObjectUtil.isEmpty(electricityBatteryIdList)){
            return R.ok(0);
        }

        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setElectricityBatteryIdList(electricityBatteryIdList);
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
}
