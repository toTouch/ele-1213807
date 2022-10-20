package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.query.UpdateBoxesQuery;
import com.xiliulou.electricity.query.UpdateBoxesStatusQuery;
import com.xiliulou.electricity.query.UpdateUsableStatusQuery;
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
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam("electricityCabinetId") Integer electricityCabinetId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
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
	
	
	//列表查询
	@GetMapping(value = "/admin/electricityCabinetBox/list/super")
	public R queryListSuper(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam("electricityCabinetId") Integer electricityCabinetId) {
		if (size < 0 || size > 50) {
			size = 10L;
		}
		
		if (offset < 0) {
			offset = 0L;
		}
		
		
		
		ElectricityCabinetBoxQuery electricityCabinetBoxQuery = ElectricityCabinetBoxQuery.builder()
				.offset(offset)
				.size(size)
				.electricityCabinetId(electricityCabinetId)
				.tenantId(null).build();
		
		return electricityCabinetBoxService.queryList(electricityCabinetBoxQuery);
	}

    //更改可用状态
    @PostMapping(value = "/admin/electricityCabinetBox/updateUsableStatus")
    @Log(title = "更新格挡状态")
    public R updateUsableStatus(@RequestBody UpdateUsableStatusQuery updateUsableStatusQuery) {
        if (Objects.isNull(updateUsableStatusQuery.getId())
                || Objects.isNull(updateUsableStatusQuery.getUsableStatus())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByIdFromDB(updateUsableStatusQuery.getId());
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
        if (Objects.equals(updateUsableStatusQuery.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            dataMap.put("isForbidden", true);
        }
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);

        oldElectricityCabinetBox.setUsableStatus(updateUsableStatusQuery.getUsableStatus());
        oldElectricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        return electricityCabinetBoxService.modify(oldElectricityCabinetBox);
    }

    //更改多个仓门可用状态
    @PostMapping(value = "/admin/electricityCabinetBox/updateBoxesStatus")
    @Log(title = "更新多个仓门状态")
    public R updateBoxesStatus(@RequestBody UpdateBoxesStatusQuery updateBoxesStatusQuery) {

        if (Objects.isNull(updateBoxesStatusQuery.getElectricityCabinetId())
                || Objects.isNull(updateBoxesStatusQuery.getUsableStatus())
                || ObjectUtil.isEmpty(updateBoxesStatusQuery.getUpdateBoxesQueryList())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(updateBoxesStatusQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        List<String> cellList = new ArrayList<>();
        for (UpdateBoxesQuery updateBoxesQuery : updateBoxesStatusQuery.getUpdateBoxesQueryList()) {

            ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByIdFromDB(updateBoxesQuery.getId());
            if (Objects.isNull(oldElectricityCabinetBox)) {
                return R.fail("ELECTRICITY.0006", "未找到此仓门");
            }

            cellList.add(oldElectricityCabinetBox.getCellNo());

            oldElectricityCabinetBox.setUsableStatus(updateBoxesStatusQuery.getUsableStatus());
            oldElectricityCabinetBox.setUpdateTime(System.currentTimeMillis());
            electricityCabinetBoxService.modify(oldElectricityCabinetBox);
        }

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cell_list", cellList);
        dataMap.put("isForbidden", false);
        if (Objects.equals(updateBoxesStatusQuery.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            dataMap.put("isForbidden", true);
        }
        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.ELE_COMMAND_CELL_UPDATE)
                .build();

        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        return R.ok();
    }

    //查询柜机仓门总数
    @GetMapping(value = "/admin/electricityCabinetBox/queryBoxCount")
    public R queryBoxCount(@RequestParam("electricityCabinetId") Integer electricityCabinetId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        return electricityCabinetBoxService.queryBoxCount(electricityCabinetId,tenantId);
    }

	//查询柜机仓门总数
	@GetMapping(value = "/admin/electricityCabinetBox/superAdminQueryBoxCount")
	public R superAdminQueryBoxCount(@RequestParam("electricityCabinetId") Integer electricityCabinetId) {
		return electricityCabinetBoxService.queryBoxCount(electricityCabinetId,null);
	}

}
