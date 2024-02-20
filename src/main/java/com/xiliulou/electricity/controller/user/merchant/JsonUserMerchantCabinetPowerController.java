package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
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
    private MerchantPlaceBindService merchantPlaceBindService;
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    @GetMapping("/user/merchant/cabinetPower/isShowPowerPage")
    public R isShowPowerPage(@RequestParam Long merchantId) {
        return R.ok(merchantPlaceBindService.isShowPowerPage(merchantId));
    }
    
    /**
     * 筛选条件：场地列表/柜机列表
     */
    @GetMapping("/user/merchant/cabinetPower/placeAndCabinetList")
    public R placeAndCabinetList(@RequestParam Long merchantId) {
        return R.ok(merchantPlaceBindService.listPlaceAndCabinetByMerchantId(merchantId));
    }
    
    /**
     * 筛选条件：根据场地id查询柜机列表
     */
    @GetMapping("/user/merchant/cabinetPower/cabinetListByPlace")
    public R cabinetListByPlace(@RequestParam Long merchantId, @RequestParam Long placeId) {
        return R.ok(merchantPlaceBindService.listCabinetByPlaceId(merchantId, placeId));
    }
}
