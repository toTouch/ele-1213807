package com.xiliulou.electricity.controller.user.info;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.userinfo.UserInfoVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/info/v2")
public class JsonUserInfoV2Controller extends BasicController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取实名认证信息
     * @return 用户基本信息
     */
    @GetMapping("/queryAuthentication")
    public R<UserInfoVO> queryAuthentication() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            UserInfoVO info = new UserInfoVO();
            info.setName(userInfo.getName());
            info.setPhone(userInfo.getPhone());
            info.setIdNumber(userInfo.getIdNumber());
            return R.ok(info);
        }

        return R.ok();
    }
}
