package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
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
public class JsonUserElectricityMemberCardOrderController {

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;


    @PostMapping("user/memberCard/payMemberCard")
    public R payMemberCard(@RequestBody @Validated(value = CreateGroup.class) ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request) {
        return electricityMemberCardOrderService.createOrder(electricityMemberCardOrderQuery, request);
    }

    @GetMapping("user/memberCardOrder/list")
    public R queryUserList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                           @RequestParam(value = "queryStartTime", required = false) Long queryStartTime, @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        return electricityMemberCardOrderService.queryUserList(offset, size, queryStartTime, queryEndTime);
    }

    @GetMapping("user/memberCardOrder/count")
    public R getMemberCardOrderCount(@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                     @RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户!");
        }
        return electricityMemberCardOrderService.getMemberCardOrderCount(uid, queryStartTime, queryEndTime);
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
    public R disableMemberCardForLimitTime(@RequestParam("disableCardDays") Integer disableCardDays, @RequestParam(value = "disableDeadline",required = false) Long disableDeadline) {
        return electricityMemberCardOrderService.disableMemberCardForLimitTime(disableCardDays, disableDeadline);
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
     * 购买租车套餐(旧小程序租车套餐)
     * @param
     * @return
     */
    @PostMapping("user/memberCard/payRentCarMemberCard")
    @Deprecated
    public R payRentCarMemberCard(@RequestBody @Validated CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request) {
        //旧版小程序不允许操作
        if(Boolean.TRUE){
            return R.fail("100257","该版本暂不支持租车,请升级小程序");
        }
        return electricityMemberCardOrderService.payRentCarMemberCard(carMemberCardOrderQuery, request);
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


}
