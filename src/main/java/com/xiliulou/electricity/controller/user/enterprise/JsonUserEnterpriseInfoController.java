package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-16:02
 */
@RestController
@Slf4j
public class JsonUserEnterpriseInfoController extends BaseController {

    @Resource
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 获取用户是否属于企业渠道
     */
    @GetMapping("/user/enterpriseInfo/check")
    public R enterpriseInfoCheck() {
        return R.ok(enterpriseInfoService.checkUserType());
    }

    /**
     * 获取用户云豆详情
     */
    @GetMapping("/user/cloudBean/detail")
    public R cloudBeanDetail() {
        return R.ok(enterpriseInfoService.cloudBeanDetail());
    }


    /**
     * 根据UID查询企业详情
     * @param uid
     * @return
     */
    @GetMapping("/user/enterpriseInfo/detail")
    public R queryEnterpriseInfo(@RequestParam(value = "uid", required = true) Long uid){

        return R.ok(enterpriseInfoService.selectByUid(uid));
    }

    /**
     * 云豆充值
     */
    @PutMapping("/user/enterpriseInfo/recharge")
    public R recharge(@RequestBody @Validated UserCloudBeanRechargeQuery userCloudBeanRechargeQuery, HttpServletRequest request) {
        return returnTripleResult(enterpriseInfoService.rechargeForUser(userCloudBeanRechargeQuery, request));
    }

}
