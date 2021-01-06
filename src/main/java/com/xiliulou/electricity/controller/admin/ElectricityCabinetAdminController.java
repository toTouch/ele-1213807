package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
public class ElectricityCabinetAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.save(electricityCabinetAddAndUpdate);
    }

    //修改换电柜
    @PutMapping(value = "/admin/electricityCabinet")
    public R update(@RequestBody @Validated(value = UpdateGroup.class)  ElectricityCabinetAddAndUpdate electricityCabinetAddAndUpdate) {
        return electricityCabinetService.edit(electricityCabinetAddAndUpdate);
    }

    //删除换电柜
    @DeleteMapping(value = "/admin/electricityCabinet/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        return electricityCabinetService.delete(id);
    }


   //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "areaId", required = false) Integer areaId,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                       @RequestParam(value = "powerStatus", required = false) Integer powerStatus,
                       @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size)
                .sn(sn)
                .name(name)
                .areaId(areaId)
                .address(address)
                .usableStatus(usableStatus)
                .powerStatus(powerStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }

    //禁用换电柜
    @PostMapping(value = "/admin/electricityCabinet/disable/{id}")
    public R disable(@PathVariable("id") Integer id) {
        return electricityCabinetService.disable(id);
    }


    //重启换电柜
    @PostMapping(value = "/admin/electricityCabinet/reboot/{id}")
    public R reboot(@PathVariable("id") Integer id) {
        return electricityCabinetService.reboot(id);
    }


    //首页一
    @GetMapping(value = "/admin/electricityCabinet/homeOne/{type}")
    public R homeOne(@PathVariable("type") Integer type) {
        return electricityCabinetService.homeOne(type);
    }

    //首页二
    @GetMapping(value = "/admin/electricityCabinet/homeTwo")
    public R homeTwo( @RequestParam(value = "areaId", required = false) Integer areaId) {
        return electricityCabinetService.homeTwo(areaId);
    }

    //首页三
    @GetMapping(value = "/admin/electricityCabinet/homeThree/{day}")
    public R homeThree(@PathVariable("day") Integer day) {
        return electricityCabinetService.homeThree(day);
    }

    //发送命令
    @PostMapping(value = "/admin/electricityCabinet/command")
    public R sendCommandToEleForOuterV2(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return  electricityCabinetService.sendCommandToEleForOuter(eleOuterCommandQuery);

    }

    //查看开门命令
    @GetMapping("/admin/electricityCabinet/open/check")
    public R checkOpenSession(@RequestParam("sessionId") String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        return electricityCabinetService.checkOpenSessionId(sessionId);
    }




}