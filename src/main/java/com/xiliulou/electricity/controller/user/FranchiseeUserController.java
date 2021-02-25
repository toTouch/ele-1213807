package com.xiliulou.electricity.controller.user;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Objects;


/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class FranchiseeUserController {
    /**
     * 服务对象
     */
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;


    //用户查询押金
    @GetMapping(value = "/user/queryDeposit")
    public R queryDeposit(){
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

        Franchisee franchisee=franchiseeService.queryByCid(user.getCid());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ",user.getCid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        return R.ok(franchisee.getBatteryDeposit());
    }





}