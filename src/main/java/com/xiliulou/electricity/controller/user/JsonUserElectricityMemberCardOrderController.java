package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.BatteryMemberCardAndInsuranceQuery;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:35
 **/

@Slf4j
@RestController
public class JsonUserElectricityMemberCardOrderController extends BaseController {

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;


    @PostMapping("user/memberCard/payMemberCard")
    public R payMemberCard(@RequestBody @Validated(value = CreateGroup.class) ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request) {
//        return electricityMemberCardOrderService.createOrder(electricityMemberCardOrderQuery, request);
//        return returnTripleResult(electricityMemberCardOrderService.buyBatteryMemberCard(electricityMemberCardOrderQuery, request));
        return R.fail("000001","小程序版本过低，请升级小程序");
    }

    @GetMapping("user/battery/membercard/info")
    public R userBatteryDepositAndMembercardInfo(){
        return returnTripleResult(electricityMemberCardOrderService.userBatteryDepositAndMembercardInfo());
    }
    
    @GetMapping("user/memberCardOrder/list")
    public R queryUserList(@RequestParam("offset") long offset, @RequestParam("size") long size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        
        ElectricityMemberCardOrderQuery orderQuery = new ElectricityMemberCardOrderQuery();
        orderQuery.setSize(size);
        orderQuery.setOffset(offset);
        orderQuery.setUid(uid);
        orderQuery.setStatus(status);
        orderQuery.setTenantId(TenantContextHolder.getTenantId());
        orderQuery.setQueryStartTime(queryStartTime);
        orderQuery.setQueryEndTime(queryEndTime);
        
        return R.ok(electricityMemberCardOrderService.selectUserMemberCardOrderList(orderQuery));
    }

    @GetMapping("/user/memberCardOrder/listV3")
    public R selectElectricityMemberCardOrderList(@RequestParam("offset") long offset, @RequestParam("size") long size,
                           @RequestParam(value = "status", required = false) Integer status,
                           @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                           @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }

        ElectricityMemberCardOrderQuery orderQuery = new ElectricityMemberCardOrderQuery();
        orderQuery.setSize(size);
        orderQuery.setOffset(offset);
        orderQuery.setUid(uid);
        orderQuery.setStatus(status);
        orderQuery.setTenantId(TenantContextHolder.getTenantId());
        orderQuery.setQueryStartTime(queryStartTime);
        orderQuery.setQueryEndTime(queryEndTime);

        return R.ok(electricityMemberCardOrderService.selectElectricityMemberCardOrderList(orderQuery));
    }
    
    @GetMapping("user/memberCardOrder/count")
    public R getMemberCardOrderCount(@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        
        ElectricityMemberCardOrderQuery orderQuery = new ElectricityMemberCardOrderQuery();
        orderQuery.setUid(uid);
        orderQuery.setStatus(status);
        orderQuery.setTenantId(TenantContextHolder.getTenantId());
        orderQuery.setQueryStartTime(queryStartTime);
        orderQuery.setQueryEndTime(queryEndTime);
        
        return R.ok(electricityMemberCardOrderService.selectUserMemberCardOrderCount(orderQuery));
    }
    
    /**
     * 不限制时间停启卡
     */
    @PutMapping("user/memberCard/openOrDisableMemberCard")
    public R openOrDisableMemberCard(@RequestParam("usableStatus") Integer usableStatus) {
        return electricityMemberCardOrderService.openOrDisableMemberCard(usableStatus);
    }

    /**
     * 限制时间停卡
     *
     * @param disableCardDays
     * @return
     */
    @PutMapping("user/memberCard/disableMemberCardForLimitTime")
    public R disableMemberCardForLimitTime(@RequestParam("disableCardDays") Integer disableCardDays,
                                           @RequestParam(value = "disableDeadline",required = false) Long disableDeadline,
                                           @RequestParam(value = "applyReason",required = false) String applyReason) {
        return electricityMemberCardOrderService.disableMemberCardForLimitTime(disableCardDays, disableDeadline,applyReason);
    }

    /**
     * 限制时间启用卡
     *
     * @return
     */
    @PutMapping("user/memberCard/enableMemberCardForLimitTime")
    public R enableMemberCardForLimitTime() {
        return electricityMemberCardOrderService.enableMemberCardForLimitTime();
    }
    
    /***
     * 停卡撤销
     */
    @PutMapping("user/memberCard/disableMemberCardForRollback")
    public R disableMemberCardForRollback() {
        return electricityMemberCardOrderService.disableMemberCardForRollback();
    }

    /**
     * 用户获取停卡是否限制时间
     *
     * @return
     */
    @GetMapping("user/memberCard/enableOrDisableMemberCardIsLimitTime")
    public R enableOrDisableMemberCardIsLimitTime() {
        return electricityMemberCardOrderService.enableOrDisableMemberCardIsLimitTime();
    }

    @GetMapping("user/memberCard/getDisableMemberCardList")
    public R getDisableMemberCardList(@RequestParam("offset") Long offset, @RequestParam("size") Long size) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery = ElectricityMemberCardRecordQuery.builder()
                .size(size)
                .offset(offset)
                .uid(user.getUid()).build();

        return electricityMemberCardOrderService.getDisableMemberCardList(electricityMemberCardRecordQuery);
    }

    /**
     * 查询用户是否存在换电和租车套餐
     * @return
     */
    @GetMapping("user/memberCard/queryUserExistMemberCard")
    public R queryUserExistMemberCard(){
        return electricityMemberCardOrderService.queryUserExistMemberCard();
    }

    /**
     * 取消购买套餐
     * @return
     */
    @PutMapping("user/memberCard/cancelPayMemberCard")
    public R cancelPayMemberCard(){
        return electricityMemberCardOrderService.cancelPayMemberCard();
    }
    
    /**
     * 结束订单
     */
    @PutMapping("user/memberCard/endOrder/{orderNo}")
    public R endOrder(@PathVariable("orderNo") String orderNo) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
    
        return returnTripleResult(electricityMemberCardOrderService.endOrder(orderNo, uid));
    }
}
