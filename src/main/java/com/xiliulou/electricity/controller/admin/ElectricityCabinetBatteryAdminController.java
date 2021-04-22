package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityBatteryBind;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.service.ElectricityBatteryBindService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
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
public class ElectricityCabinetBatteryAdminController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityBatteryBindService electricityBatteryBindService;
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
                                       @RequestParam(value = "sn", required = false) String sn) {
        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        return electricityBatteryService.getElectricityBatteryPage(electricityBatteryQuery, offset, size);
    }

    /**
     * 加盟商电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/pageByFranchisee")
    public R pageByFranchisee(@RequestParam(value = "offset") Long offset,
                                       @RequestParam(value = "size") Long size,
                                       @RequestParam(value = "status", required = false) Integer status,
                                       @RequestParam(value = "sn", required = false) String sn) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Franchisee> franchiseeList=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchiseeList)){
            return R.ok();
        }
        List<ElectricityBatteryBind> electricityBatteryBindBinds=new ArrayList<>();
        for (Franchisee franchisee:franchiseeList) {
            List<ElectricityBatteryBind> electricityBatteryBindBindList=electricityBatteryBindService.queryByFranchiseeId(franchisee.getId());
            electricityBatteryBindBinds.addAll(electricityBatteryBindBindList);
        }
        if(ObjectUtil.isEmpty(electricityBatteryBindBinds)){
            return R.ok();
        }
        List<Long> electricityBatteryIdList=new ArrayList<>();
        for (ElectricityBatteryBind electricityBatteryBind:electricityBatteryBindBinds) {
            electricityBatteryIdList.add(electricityBatteryBind.getElectricityBatteryId());
        }
        if(ObjectUtil.isEmpty(electricityBatteryIdList)){
            return R.ok();
        }

        ElectricityBatteryQuery electricityBatteryQuery = new ElectricityBatteryQuery();
        electricityBatteryQuery.setStatus(status);
        electricityBatteryQuery.setSn(sn);
        electricityBatteryQuery.setElectricityBatteryIdList(electricityBatteryIdList);
        return electricityBatteryService.pageByFranchisee(electricityBatteryQuery, offset, size);
    }


    /**
     * 获取单个电池
     *
     * @param
     * @return
     */
    @GetMapping(value = "/admin/battery/{id}")
    public R queryById(@PathVariable("id") Long id) {
        return R.ok(electricityBatteryService.queryById(id));
    }
}
