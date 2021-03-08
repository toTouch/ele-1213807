package com.xiliulou.electricity.controller.user;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@RestController
@Slf4j
public class EleDepositOrderUserController {
    /**
     * 服务对象
     */
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;

    //缴纳押金
    @PostMapping("/user/payDeposit")
    public R payDeposit(HttpServletRequest request) {
        return eleDepositOrderService.payDeposit(request);
    }

    //退还押金
    @PostMapping("/user/returnDeposit")
    public R returnDeposit(HttpServletRequest request) {
        return eleDepositOrderService.returnDeposit(request); }


    //查询缴纳押金状态
    @PostMapping("/user/eleDepositOrder/queryStatus")
    public R queryStatus(@RequestParam("orderId") String orderId) {
        EleDepositOrder eleDepositOrder=eleDepositOrderService.queryByOrderId(orderId);
        if(Objects.isNull(eleDepositOrder)){
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        return R.ok(eleDepositOrder.getStatus());
    }

    //用户查询押金
    @GetMapping(value = "/user/queryDeposit")
    public R queryDeposit(){
        Map<String,String> map=new HashMap<>();
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        User user=userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}",uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}",uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if ((Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT)||Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY))
                &&Objects.nonNull(userInfo.getBatteryDeposit())&&Objects.nonNull(userInfo.getOrderId())) {
            //是否退款 TODO
            map.put("deposit",userInfo.getBatteryDeposit().toString());
            //最后一次缴纳押金时间
            map.put("time", eleDepositOrderService.queryByOrderId(userInfo.getOrderId()).getUpdateTime().toString());
            return R.ok(map);
        }

        Franchisee franchisee=franchiseeService.queryByCid(user.getCid());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ",user.getCid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        map.put("deposit",franchisee.getBatteryDeposit().toString());
        map.put("time",null);
        return R.ok(map);
    }

}