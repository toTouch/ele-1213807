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
import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序-电费
 * @date 2024/2/20 14:10:49
 */

@RestController
@Slf4j
public class JsonUserMerchantCabinetPowerController extends BaseController {
    
    @Resource
    private MerchantCabinetPowerService merchantCabinetPowerService;
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    @GetMapping("/user/merchant/power/isShowPowerPage")
    public R isShowPowerPage(@RequestParam Long merchantId) {
        return R.ok(merchantCabinetPowerService.isShowPowerPage(merchantId));
    }
    
    /**
     * 筛选条件：场地列表/柜机列表
     */
    @GetMapping("/user/merchant/power/placeAndCabinetList")
    public R placeAndCabinetList(@RequestParam Long merchantId) {
        return R.ok(merchantCabinetPowerService.listPlaceAndCabinetByMerchantId(merchantId));
    }
    
    /**
     * 筛选条件：根据场地id查询柜机列表
     */
    @GetMapping("/user/merchant/power/cabinetListByPlace")
    public R cabinetListByPlace(@RequestParam Long merchantId, @RequestParam Long placeId) {
        return R.ok(merchantCabinetPowerService.listCabinetByPlaceId(merchantId, placeId));
    }
    
    /**
     * 今日电量/电费
     */
    @GetMapping("/user/merchant/power/todayPower")
    public R todayPower(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.todayPower(request));
    }
    
    /**
     * 昨日电量/电费
     */
    @GetMapping("/user/merchant/power/yesterdayPower")
    public R yesterdayPower(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.yesterdayPower(request));
    }
    
    /**
     * 上月电量/电费
     */
    @GetMapping("/user/merchant/power/lastMonthPower")
    public R lastMonthPower(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.lastMonthPower(request));
    }
    
    /**
     * 累计电量/电费 （包含本月电量/电费）
     */
    @GetMapping("/user/merchant/power/totalPower")
    public R totalPower(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.totalPower(request));
    }
    
    /**
     * 统计分析-折线图 近N个自然月（不包含本月）
     */
    @GetMapping("/user/merchant/power/lineData")
    public R lineData(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId, @RequestParam(value = "monthList") List<String> monthList) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.lineData(request));
    }
    
    /**
     * 柜机电费列表
     */
    @GetMapping("/user/merchant/power/cabinetPowerList")
    public R cabinetPowerList(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.cabinetPowerList(request));
        
    }
    
    /**
     * 柜机电费详情
     */
    @GetMapping("/user/merchant/power/cabinetPowerDetail")
    public R cabinetPowerDetail(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        
        MerchantCabinetPowerRequest request = MerchantCabinetPowerRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantCabinetPowerService.cabinetPowerDetail(request));
    }
}
