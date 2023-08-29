package com.xiliulou.electricity.controller.admin;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityBatteryDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JsonAdminElectricityBatteryDataController {

    @Autowired
    private ElectricityBatteryDataService electricityBatteryDataService;

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


        return electricityBatteryDataService.queryStockBatteryPageData(offset, size, sn, franchiseeId, electricityCabinetId);
    }
    /**
     * 获取库存电池的数据总数
     */
    @GetMapping(value = "/admin/batteryData/stockBattery/count")
    public R getStockBatteryDataCount(  @RequestParam(value = "sn", required =  false) String sn,
                                          @RequestParam(value = "franchiseeId", required =  false) Long franchiseeId,
                                          @RequestParam(value = "electricityCabinetId", required =  false) Integer electricityCabinetId) {
        return electricityBatteryDataService.queryStockBatteryPageDataCount(sn, franchiseeId, electricityCabinetId);
    }


}
