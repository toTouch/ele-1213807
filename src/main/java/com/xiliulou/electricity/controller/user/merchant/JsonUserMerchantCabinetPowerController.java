package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 小程序-电费
 * @date 2024/2/20 14:10:49
 */

@RestController
@Slf4j
public class JsonUserMerchantCabinetPowerController extends BaseController {
    
    @Resource
    MerchantCabinetPowerService merchantCabinetPowerService;
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    @GetMapping("/user/merchant/cabinetPower/isShowPowerPage")
    public R isShowPowerPage(@RequestParam Long merchantId) {
        return R.ok(merchantCabinetPowerService.isShowPowerPage(merchantId));
    }
    
    /**
     * 筛选条件：场地列表/柜机列表
     */
    @GetMapping("/user/merchant/cabinetPower/placeAndCabinetList")
    public R placeAndCabinetList(@RequestParam Long merchantId) {
        return R.ok(merchantCabinetPowerService.listPlaceAndCabinetByMerchantId(merchantId));
    }
    
    /**
     * 筛选条件：根据场地id查询柜机列表
     */
    @GetMapping("/user/merchant/cabinetPower/cabinetListByPlace")
    public R cabinetListByPlace(@RequestParam Long merchantId, @RequestParam Long placeId) {
        return R.ok(merchantCabinetPowerService.listCabinetByPlaceId(merchantId, placeId));
    }
    
    /**
     * 统计：今日/昨日/本月/上月/累计-电量/电费
     * 柜机列表：今日/本月/本年-电量/电费
     */
    @GetMapping("/user/merchant/cabinetPower/PowerData")
    public R powerData(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.powerData(request));
    }
    
    /**
     * 统计分析-折线图
     */
    public R lineData(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId, Long startTime, Long endTime) {
        
        return R.ok();
    }
    
    /**
     * 柜机电费详情
     */
    public R cabinetPowerDetal(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        return R.ok();
    }
    
}
