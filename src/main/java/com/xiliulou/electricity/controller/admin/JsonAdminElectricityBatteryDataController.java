package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminElectricityBatteryDataController  extends BaseController {

    @Autowired
    private ElectricityBatteryDataService electricityBatteryDataService;
    private Triple<Boolean, String, Object> verifyUserPermission() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return Triple.of(false, null, Collections.emptyList());
        }

        return Triple.of(true, null, null);
    }
    /**
     * 获取全部电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/allBattery/page")
    public R getAllBatteryPageData( @RequestParam("offset") long offset,
                                    @RequestParam("size") long size,
                                    @RequestParam(value = "sn", required =  false) String sn,
                                    @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                    @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId,
                                    @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectAllBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取全部电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/allBattery/count")
    public R getAllBatteryDataCount( @RequestParam(value = "sn", required =  false) String sn,
                                     @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                     @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId,
                                     @RequestParam(value = "uid", required = false) Long uid) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectAllBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取在柜电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/inCabinetBattery/page")
    public R getInCabinetBatteryPageData( @RequestParam("offset") long offset,
                                          @RequestParam("size") long size,
                                          @RequestParam(value = "uid", required = false) Long uid,
                                          @RequestParam(value = "sn", required =  false) String sn,
                                          @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                          @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectInCabinetBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取在柜电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/inCabinetBattery/count")
    public R getInCabinetBatteryDataCount(  @RequestParam(value = "uid", required = false) Long uid,
                                            @RequestParam(value = "sn", required =  false) String sn,
                                            @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                            @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectInCabinetBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }


    /**
     * 获取待租电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/pendingRentalBattery/page")
    public R getPendingRentalBatteryPageData( @RequestParam("offset") long offset,
                                              @RequestParam("size") long size,
                                              @RequestParam(value = "uid", required = false) Long uid,
                                              @RequestParam(value = "sn", required =  false) String sn,
                                              @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                              @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectPendingRentalBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取待租电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/pendingRentalBattery/count")
    public R getPendingRentalBatteryDataCount(  @RequestParam(value = "uid", required = false) Long uid,
                                                @RequestParam(value = "sn", required =  false) String sn,
                                                @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                                @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectPendingRentalBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取已租电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/leasedBattery/page")
    public R getLeasedBatteryPageData( @RequestParam("offset") long offset,
                                       @RequestParam("size") long size,
                                       @RequestParam(value = "uid", required = false) Long uid,
                                       @RequestParam(value = "sn", required =  false) String sn,
                                       @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                       @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectLeasedBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取已租电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/leasedBattery/count")
    public R getLeasedBatteryDataCount(  @RequestParam(value = "uid", required = false) Long uid,
                                         @RequestParam(value = "sn", required =  false) String sn,
                                         @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                         @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectLeasedBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取游离电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/strayBattery/page")
    public R getStrayBatteryPageData( @RequestParam("offset") long offset,
                                      @RequestParam("size") long size,
                                      @RequestParam(value = "uid", required = false) Long uid,
                                      @RequestParam(value = "sn", required =  false) String sn,
                                      @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                      @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectStrayBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取游离电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/strayBattery/count")
    public R getStrayBatteryDataCount(  @RequestParam(value = "sn", required =  false) String sn,
                                        @RequestParam(value = "uid", required = false) Long uid,
                                        @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                        @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectStrayBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }


    /**
     * 获取逾期电池的分页数据
     */
    @GetMapping(value = "/admin/batteryData/overdueBattery/page")
    public R getOverdueBatteryPageData( @RequestParam("offset") long offset,
                                        @RequestParam("size") long size,
                                        @RequestParam(value = "uid", required = false) Long uid,
                                        @RequestParam(value = "sn", required =  false) String sn,
                                        @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                        @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectOverdueBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取逾期电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/overdueBattery/count")
    public R getOverdueBatteryDataCount(  @RequestParam(value = "sn", required =  false) String sn,
                                          @RequestParam(value = "uid", required = false) Long uid,
                                          @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                          @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectOverdueBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取逾期电池的分页数据(车电一体)
     */
    @GetMapping(value = "/admin/batteryData/overdueCarBattery/page")
    public R getOverdueCarBatteryPageData( @RequestParam("offset") long offset,
                                        @RequestParam("size") long size,
                                        @RequestParam(value = "uid", required = false) Long uid,
                                        @RequestParam(value = "sn", required =  false) String sn,
                                        @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                        @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectOverdueCarBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId, uid);
    }

    /**
     * 获取逾期电池的数据总数(车电一体)
     */
    @GetMapping(value = "/admin/batteryData/overdueCarBattery/count")
    public R getOverdueCarBatteryDataCount(  @RequestParam(value = "sn", required =  false) String sn,
                                          @RequestParam(value = "uid", required = false) Long uid,
                                          @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                          @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.selectOverdueCarBatteryDataCount(sn, franchiseeId, electricityCabinetId, uid);
    }
    /**
     * 获取库存电池的分页数据
     * @param offset 启示页
     * @param size 每页大小
     * @param sn 电池编码
     * @param franchiseeId
     * @param electricityCabinetId
     * @return
     */
    @GetMapping(value = "/admin/batteryData/stockBattery/page")
    public R getStockBatteryPageDate( @RequestParam("offset") long offset,
                                      @RequestParam("size") long size,
                                      @RequestParam(value = "sn", required =  false) String sn,
                                      @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                      @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId){

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.queryStockBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId);
    }
    /**
     * 获取库存电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/stockBattery/count")
    public R getStockBatteryDataCount(  @RequestParam(value = "sn", required =  false) String sn,
                                          @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                          @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return returnTripleResult(verifyUserPermissionResult);
        }
        return electricityBatteryDataService.queryStockBatteryPageDataCount(sn, franchiseeId, electricityCabinetId);
    }


}
