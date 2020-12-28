package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetBox/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset,
                       @RequestParam("electricityCabinetId") Integer electricityCabinetId) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetBoxQuery electricityCabinetBoxQuery = ElectricityCabinetBoxQuery.builder()
                .offset(offset)
                .size(size)
                .electricityCabinetId(electricityCabinetId).build();

        return electricityCabinetBoxService.queryList(electricityCabinetBoxQuery);
    }

    //更改可用状态
    @PostMapping(value = "/admin/electricityCabinetBox/updateUsableStatus")
    public R updateUsableStatus(@RequestBody ElectricityCabinetBox electricityCabinetBox) {
        if(Objects.isNull(electricityCabinetBox.getId())&&Objects.isNull(electricityCabinetBox.getUsableStatus())){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinetBox oldElectricityCabinetBox=electricityCabinetBoxService.queryByIdFromDB(electricityCabinetBox.getId());
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("ELECTRICITY.0006","未找到此仓门");
        }
        ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(oldElectricityCabinetBox.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cell_no", oldElectricityCabinetBox.getCellNo());
        dataMap.put("distribute", oldElectricityCabinetBox.getUsableStatus());

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_UPDATE)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }

    //后台一键开门
    @PostMapping(value = "/admin/electricityCabinetBox/openDoor/{id}")
    public R openDoor(@PathVariable("id") Long id) {
        if(Objects.isNull(id)){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinetBox oldElectricityCabinetBox=electricityCabinetBoxService.queryByIdFromDB(id);
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("ELECTRICITY.0006","未找到此仓门");
        }
        if(Objects.equals(oldElectricityCabinetBox.getStatus(),ElectricityCabinetBox.STATUS_ORDER_OCCUPY)){
            return R.fail("ELECTRICITY.0013","仓门有订单，不能开门");
        }
        ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(oldElectricityCabinetBox.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cell_no", oldElectricityCabinetBox.getCellNo());

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_OPEN_DOOR)
                .build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        ElectricityCabinetBox electricityCabinetBox=new ElectricityCabinetBox();
        electricityCabinetBox.setId(id);
        electricityCabinetBox.setBoxStatus(ElectricityCabinetBox.STATUS_OPEN_DOOR);
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }

    //后台一键全开
    @PostMapping(value = "/admin/electricityCabinetBox/openAllDoor/{electricityCabinetId}")
    public R openAllDoor(@PathVariable("electricityCabinetId") Integer electricityCabinetId) {
        if(Objects.isNull(electricityCabinetId)){
            return R.fail("ELECTRICITY.0007","不合法的参数");
        }
        ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005","未找到换电柜");
        }
        List<ElectricityCabinetBox> electricityCabinetBoxList=electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinetId);
        if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
            return R.fail("ELECTRICITY.0014","换电柜没有仓门，不能开门");
        }
        for (ElectricityCabinetBox electricityCabinetBox:electricityCabinetBoxList) {
            if(Objects.equals(electricityCabinetBox.getStatus(),ElectricityCabinetBox.STATUS_ORDER_OCCUPY)){
                return R.fail("ELECTRICITY.0013","仓门有订单，不能开门");
            }
        }
        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_ALL_OPEN_DOOR)
                .build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        ElectricityCabinetBox electricityCabinetBox=new ElectricityCabinetBox();
        electricityCabinetBox.setElectricityCabinetId(electricityCabinetId);
        electricityCabinetBox.setBoxStatus(ElectricityCabinetBox.STATUS_OPEN_DOOR);
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        return electricityCabinetBoxService.modifyByElectricityCabinetId(electricityCabinetBox);
    }




}