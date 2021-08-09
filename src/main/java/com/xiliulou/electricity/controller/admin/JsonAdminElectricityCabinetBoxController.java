package com.xiliulou.electricity.controller.admin;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetBoxController {
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
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam("electricityCabinetId") Integer electricityCabinetId) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityCabinetBoxQuery electricityCabinetBoxQuery = ElectricityCabinetBoxQuery.builder()
                .offset(offset)
                .size(size)
                .electricityCabinetId(electricityCabinetId)
                .tenantId(tenantId).build();

        return electricityCabinetBoxService.queryList(electricityCabinetBoxQuery);
    }

    //更改可用状态
    @PostMapping(value = "/admin/electricityCabinetBox/updateUsableStatus")
    public R updateUsableStatus(@RequestBody ElectricityCabinetBox electricityCabinetBox) {
        if (Objects.isNull(electricityCabinetBox.getId()) && Objects.isNull(electricityCabinetBox.getUsableStatus())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }



        ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByIdFromDB(electricityCabinetBox.getId());
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }


        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(oldElectricityCabinetBox.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        List<String> cellList = new ArrayList<>();
        cellList.add(oldElectricityCabinetBox.getCellNo());
        dataMap.put("cell_list", cellList);
        dataMap.put("isForbidden", false);
        if (Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            dataMap.put("isForbidden", true);
        }
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_UPDATE)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);

        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }



    //更改多个仓门可用状态
    @PostMapping(value = "/admin/electricityCabinetBox/updateBoxesStatus")
    public R updateBoxesStatus(@RequestBody ElectricityCabinetBox electricityCabinetBox) {
        if (Objects.isNull(electricityCabinetBox.getId()) && Objects.isNull(electricityCabinetBox.getUsableStatus())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }



        ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByIdFromDB(electricityCabinetBox.getId());
        if (Objects.isNull(oldElectricityCabinetBox)) {
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }


        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(oldElectricityCabinetBox.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        List<String> cellList = new ArrayList<>();
        cellList.add(oldElectricityCabinetBox.getCellNo());
        dataMap.put("cell_list", cellList);
        dataMap.put("isForbidden", false);
        if (Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            dataMap.put("isForbidden", true);
        }
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.ELE_COMMAND_CELL_UPDATE)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);

        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        return electricityCabinetBoxService.modify(electricityCabinetBox);
    }



}
