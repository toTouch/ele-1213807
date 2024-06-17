package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.FreeDepositCarMemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-22-13:27
 */
@Slf4j
@RestController
public class JsonUserRentCarMemberCardOrderController extends BaseController {
    
    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;
    
    @Autowired
    private ElectricityCarModelService electricityCarModelService;
    
    @GetMapping("/user/rentCar/memberCard/list")
    public R getElectricityMemberCardPage(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        RentCarMemberCardOrderQuery memberCardOrderQuery = RentCarMemberCardOrderQuery.builder().offset(offset).size(size).uid(user.getUid()).orderId(orderId)
                .beginTime(queryStartTime).endTime(queryEndTime).tenantId(TenantContextHolder.getTenantId()).status(status).build();
        
        return R.ok(carMemberCardOrderService.selectByPage(memberCardOrderQuery));
    }
    
    /**
     * 获取当前用户所属车辆型号租赁方式
     */
    @GetMapping("/user/rentCar/userCarModel/rentType")
    public R userCarModel() {
        return returnTripleResult(electricityCarModelService.acquireUserCarModelInfo());
    }
    
    
    /**
     * 查询用户套餐详情
     */
    @GetMapping("/user/rentCar/memberCard/info")
    public R userCarMemberCardInfo() {
        return returnTripleResult(carMemberCardOrderService.userCarMemberCardInfo());
    }
}
