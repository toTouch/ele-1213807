package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
public class JsonUserUserInfoController extends BaseController {
    
    /**
     * 服务对象
     */
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserService userService;
    
    @Resource
    UserOauthBindService userOauthBindService;
    
    /**
     * 小程序首页获取用户详情
     */
    @GetMapping(value = "/user/userInfoDetail")
    public R userInfoDetail() {
        return R.ok();
    }
    
    /**
     * 小程序首页 用户状态(新)
     */
    @GetMapping(value = "/user/userInfoStatus")
    public R selectUserInfoStatus() {
        return returnTripleResult(userInfoService.selectUserInfoStatus());
    }
    
    /**
     * 登录成功回调
     */
    @PutMapping(value = "/user/userSource/callback")
    public R loginCallBack(@RequestBody UserSourceQuery query) {
        userService.loginCallBack(query);
        return R.ok();
    }
    
    /**
     * 用户切换不同微信应用时open id不可用，导致微信支付失败 如果openID 未变化，则返回true，若变化，则返回false，需要用户重新登录
     *
     * @param jsCode
     * @return
     */
    @GetMapping("/user/checkUserThirdId")
    public R checkUserThirdId(@RequestParam(value = "jsCode", required = true) String jsCode) {
        return R.ok(userOauthBindService.checkOpenIdByJsCode(jsCode));
    }
    
}
