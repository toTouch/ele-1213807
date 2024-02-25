package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRequest;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @description 小程序-场地费
 * @date 2024/2/20 14:10:49
 */

@RestController
@Slf4j
public class JsonUserMerchantCabinetPlaceController extends BaseController {
    
    @Resource
    MerchantCabinetPowerService merchantCabinetPowerService;
    
    @Resource
    private MerchantPlaceFeeService merchantPlaceFeeService;
    
    /**
     * 是否显示场地费页面：0-不显示，1-显示
     */
    @GetMapping("/user/merchant/place/isShowPlacePage")
    public R isShowPlacePage(@RequestParam Long merchantId) {
        return R.ok(merchantPlaceFeeService.isShowPlacePage(merchantId));
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
     * 统计上月，本月，累计场地费
     * 统计设备数量
     */
    @GetMapping("/user/merchant/place/getFeeData")
    public R getFeeData(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
    
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId).build();
        
        return R.ok(merchantPlaceFeeService.getFeeData(request));
    }
    
    /**
     * 统计分析-折线图
     */
    @GetMapping("/user/merchant/place/getLineData")
    public R lineData(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId, Long startTime, Long endTime) {
         MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchantId).placeId(placeId)
                .cabinetId(cabinetId).startTime(startTime).endTime(endTime).build();
        
        return R.ok(merchantPlaceFeeService.lineData(request));
    }
    
    /**
     * 根据柜机id获取场地费
     */
    @GetMapping("/user/merchant/place/getPlaceDetailByCabinetId")
    public R getPlaceDetailByCabinetId(@RequestParam Long merchantId,@RequestParam("month") String month,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchantId).month(month)
                .cabinetId(cabinetId).build();
        return R.ok(merchantPlaceFeeService.getPlaceDetailByCabinetId(request));
    }
    
    /**
     * 柜机电费详情
     */
    @GetMapping("/user/merchant/place/getCabinetPlaceDetail")
    public R getCabinetPlaceDetail(@RequestParam Long merchantId, @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "cabinetId", required = false) Long cabinetId) {
        MerchantPlaceFeeRequest request = MerchantPlaceFeeRequest.builder().merchantId(merchantId).placeId(placeId)
                .cabinetId(cabinetId).build();
        
        return R.ok(merchantPlaceFeeService.getCabinetPlaceDetail(request));
    }
    
}
