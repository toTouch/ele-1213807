package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.EleCabinetDataAnalyseService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-18-9:10
 */
@RestController
@Slf4j
public class JsonAdminEleCabinetDataAnalyseController extends BaseController {

    @Autowired
    private EleCabinetDataAnalyseService eleCabinetDataAnalyseService;

    /**
     * 全部柜机列表
     */
    @GetMapping("/admin/eleCabinet/all/page")
    public R allPage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                     @RequestParam(value = "name", required = false) String name,
                     @RequestParam(value = "sn", required = false) String sn,
                     @RequestParam(value = "address", required = false) String address,
                     @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                     @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset)
                .sn(sn).address(address).franchiseeId(franchiseeId).storeId(storeId).name(name)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflineByPage(cabinetQuery));
    }

    /**
     * 全部柜机列表总数
     */
    @GetMapping(value = "/admin/eleCabinet/all/count")
    public R allPageCount(@RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "sn", required = false) String sn,
                          @RequestParam(value = "address", required = false) String address,
                          @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                          @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().sn(sn).address(address)
                .franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflinePageCount(cabinetQuery));
    }

    /**
     * 离线列表
     */
    @GetMapping("/admin/eleCabinet/offline/page")
    public R offlinePage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                         @RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "sn", required = false) String sn,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                         @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset)
                .onlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS).sn(sn).address(address)
                .franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflineByPage(cabinetQuery));
    }

    @GetMapping(value = "/admin/eleCabinet/offline/count")
    public R offlinePageCount(@RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "sn", required = false) String sn,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                              @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().onlineStatus(ElectricityCabinet.ELECTRICITY_CABINET_OFFLINE_STATUS)
                .sn(sn).address(address).franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflinePageCount(cabinetQuery));
    }

    /**
     * 禁用列表
     */
    @GetMapping(value = "/admin/eleCabinet/disable/page")
    public R disablePage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                         @RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "sn", required = false) String sn,
                         @RequestParam(value = "address", required = false) String address,
                         @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                         @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset)
                .usableStatus(ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS).sn(sn).address(address)
                .name(name).tenantId(TenantContextHolder.getTenantId()).storeId(storeId).franchiseeId(franchiseeId).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflineByPage(cabinetQuery));
    }

    @GetMapping(value = "/admin/eleCabinet/disable/count")
    public R disablePageCount(@RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "sn", required = false) String sn,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                              @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().sn(sn).address(address).name(name)
                .usableStatus(ElectricityCabinet.ELECTRICITY_CABINET_UN_USABLE_STATUS)
                .tenantId(TenantContextHolder.getTenantId()).storeId(storeId).franchiseeId(franchiseeId).build();

        return R.ok(eleCabinetDataAnalyseService.selectOfflinePageCount(cabinetQuery));
    }

    /**
     * 锁仓列表
     */
    @GetMapping(value = "/admin/eleCabinet/lock/page")
    public R lockPage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                      @RequestParam(value = "name", required = false) String name,
                      @RequestParam(value = "sn", required = false) String sn,
                      @RequestParam(value = "address", required = false) String address,
                      @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                      @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).sn(sn)
                .storeId(storeId).usableStatusCell(ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE).address(address)
                .franchiseeId(franchiseeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectLockPage(cabinetQuery));
    }


    @GetMapping(value = "/admin/eleCabinet/lock/count")
    public R lockPageCount(@RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "sn", required = false) String sn,
                           @RequestParam(value = "address", required = false) String address,
                           @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                           @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().sn(sn).storeId(storeId)
                .usableStatusCell(ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE).address(address)
                .franchiseeId(franchiseeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectLockPageCount(cabinetQuery));
    }

    /**
     * 少电列表
     */
    @GetMapping(value = "/admin/eleCabinet/lowPower/page")
    public R lowPowerPage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "sn", required = false) String sn,
                          @RequestParam(value = "address", required = false) String address,
                          @RequestParam(value = "lowChargeRate") Double lowChargeRate,
                          @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                          @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).sn(sn)
                .address(address).lowChargeRate(lowChargeRate)
                .franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectPowerPage(cabinetQuery));
    }

    @GetMapping(value = "/admin/eleCabinet/lowPower/count")
    public R lowPowerPageCount(@RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "sn", required = false) String sn,
                               @RequestParam(value = "address", required = false) String address,
                               @RequestParam(value = "lowChargeRate") Double lowChargeRate,
                               @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                               @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().sn(sn).address(address).lowChargeRate(lowChargeRate)
                .franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectPowerPageCount(cabinetQuery));
    }

    /**
     * 多电列表
     */
    @GetMapping(value = "/admin/eleCabinet/fullPower/page")
    public R fullPowerPage(@RequestParam("size") long size, @RequestParam("offset") long offset,
                           @RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "sn", required = false) String sn,
                           @RequestParam(value = "address", required = false) String address,
                           @RequestParam(value = "fullChargeRate") Double fullChargeRate,
                           @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                           @RequestParam(value = "storeId", required = false) Long storeId) {

        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(Collections.emptyList());
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().size(size).offset(offset).sn(sn).address(address)
                .fullChargeRate(fullChargeRate).franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectPowerPage(cabinetQuery));
    }

    @GetMapping(value = "/admin/eleCabinet/fullPower/count")
    public R fullPowerPageCount(@RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "sn", required = false) String sn,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam(value = "fullChargeRate") Double fullChargeRate,
                                @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                @RequestParam(value = "storeId", required = false) Long storeId) {

        Triple<Boolean, String, Object> verifyUserPermissionResult = verifyUserPermission();
        if (Boolean.FALSE.equals(verifyUserPermissionResult.getLeft())) {
            return R.ok(0);
        }

        ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().sn(sn).address(address)
                .fullChargeRate(fullChargeRate).franchiseeId(franchiseeId).storeId(storeId).name(name).tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(eleCabinetDataAnalyseService.selectPowerPageCount(cabinetQuery));
    }

    /**
     * 日均换电次数
     *
     * @return
     */
    @GetMapping(value = "/admin/eleCabinet/average/count")
    public R averageStatistics(@RequestParam(value = "eid") Integer eid) {
        return R.ok(eleCabinetDataAnalyseService.averageStatistics(eid));
    }

    /**
     * 今日换电次数
     *
     * @return
     */
    @GetMapping(value = "/admin/eleCabinet/today/statistics")
    public R todayStatistics(@RequestParam(value = "eid") Integer eid) {
        return R.ok(eleCabinetDataAnalyseService.todayStatistics(eid));
    }


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
}
