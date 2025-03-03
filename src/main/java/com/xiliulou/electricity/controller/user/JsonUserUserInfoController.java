package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.request.userinfo.emergencyContact.EmergencyContactRequest;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.emergencyContact.EmergencyContactService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ValidList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    
    @Resource
    private EmergencyContactService emergencyContactService;
    
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
     * 小程序首页 用户状态(新),适配新的免押代扣
     */
    @GetMapping(value = "/user/userInfoStatus/v2")
    public R selectUserInfoStatusV2() {
        return returnTripleResult(userInfoService.selectUserInfoStatusV2());
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
    
    /**
     * 小程序首页-账号管理
     */
    @GetMapping("/user/accountInfo")
    public R userAccountInfo() {
        return R.ok(userInfoService.selectAccountInfo());
    }
    
    /**
     * 查询紧急联系人
     */
    @GetMapping(value = "/user/emergencyContact/list")
    public R emergencyContactList() {
        return R.ok(emergencyContactService.listByUidFromCache(SecurityUtils.getUid()));
    }
    
    /**
     * 新增或编辑紧急联系人
     */
    @PostMapping(value = "/user/emergencyContact/insertOrUpdate")
    public R emergencyContactInsertOrUpdate(@RequestBody @Validated ValidList<EmergencyContactRequest> emergencyContactList) {
        return emergencyContactService.insertOrUpdate(emergencyContactList);
    }
    
    /**
     * 用户押金状态
     */
    @GetMapping(value = "/user/depositStatus")
    public R queryDepositStatus() {
        return R.ok(userInfoService.queryDepositStatus());
    }
    /**
     * 注销账号前置校验
     */
    @GetMapping("/user/account/del/preCheck")
    public R deleteAccountPreCheck() {
        return userInfoService.deleteAccountPreCheck();
    }
    
    /**
     * 注销账号
     */
    @PostMapping("/user/account/del")
    public R deleteAccount() {
        return userInfoService.deleteAccount();
    }
}
