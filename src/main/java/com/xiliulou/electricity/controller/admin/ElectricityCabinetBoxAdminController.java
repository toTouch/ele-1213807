package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@RestController
public class ElectricityCabinetBoxAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetBox/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetBoxQuery electricityCabinetBoxQuery = ElectricityCabinetBoxQuery.builder()
                .offset(offset)
                .size(size).build();

        return electricityCabinetBoxService.queryList(electricityCabinetBoxQuery);
    }

    //更改可用状态
    @PutMapping(value = "/admin/electricityCabinetBox/updateUsableStatus")
    public R updateUsableStatus(@RequestBody ElectricityCabinetBox electricityCabinetBox) {
        //TODO 判断参数
        ElectricityCabinetBox oldElectricityCabinetBox=electricityCabinetBoxService.queryByIdFromCache(electricityCabinetBox.getId());
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("SYSTEM.0006","未找到此仓门");
        }
        //TODO 发送命令
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }

    //后台一键开门
    @PostMapping(value = "/admin/electricityCabinetBox/releaseBox")
    public R releaseBox(@RequestParam("electricityCabinetId") Integer electricityCabinetId) {
        ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("SYSTEM.0005","未找到换电柜");
        }
        //TODO 判断订单
        //TODO 发送命令
        return electricityCabinetBoxService.modifyByElectricityCabinetId(electricityCabinetId);
    }

    //后台一键全开
    @PostMapping(value = "/admin/electricityCabinetBox/openAllDoor")
    public R openAllDoor(@RequestBody ElectricityCabinetBox electricityCabinetBox) {
        //TODO 判断参数
        ElectricityCabinetBox oldElectricityCabinetBox=electricityCabinetBoxService.queryByIdFromCache(electricityCabinetBox.getId());
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("SYSTEM.0006","未找到此仓门");
        }
        //TODO 判断订单
        //TODO 发送命令
        electricityCabinetBox.setBoxStatus(0);
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }



}