package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class JsonUserFranchiseeController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ElectricityCabinetService electricityCabinetService;


    //列表查询
    @GetMapping(value = "/user/franchisee/queryByTenantId")
    public R queryByTenantId() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return franchiseeService.queryByTenantId(tenantId);

    }

    //根据三元组查询加盟商
    @GetMapping(value = "/user/franchisee/getFranchisee")
    public R getFranchisee(@RequestParam("productKey") String productKey, @RequestParam("deviceName") String deviceName) {
        return electricityCabinetService.getFranchisee(productKey, deviceName);
    }

    /**
     * 获取当前租户所有加盟商城市列表
     */
    @GetMapping(value = "/user/franchisee/cityList")
    public R franchiseeCityList() {
        return R.ok(franchiseeService.selectFranchiseeCityList());
    }
    /**
     * 获取当前城市所有加盟商区域列表
     */
    @GetMapping(value = "/user/franchisee/cityList/{cid}")
    public R franchiseeRegionList(@PathVariable("cid") Integer cid) {
        return R.ok(franchiseeService.selectFranchiseeRegionList(cid));
    }


    /**
     * 根据区县编码查加盟商
     */
    @GetMapping(value = "/user/franchisee/region")
    public R selectFranchiseeByArea(@RequestParam(value = "regionCode") String regionCode) {

        return returnTripleResult(franchiseeService.selectFranchiseeByArea(regionCode));
    }

    /**
     * 根据城市获取加盟商列表
     * @param cityCode
     * @return
     */
    @GetMapping(value = "/user/franchisee/city")
    public R selectFranchiseeByCity(@RequestParam(value = "cityCode") String cityCode) {

        return returnTripleResult(franchiseeService.selectFranchiseeByCity(cityCode));
    }


}
