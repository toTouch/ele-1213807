package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
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
     * @return
     */
    @GetMapping("/user/enterpriseInfo/detail")
    public R queryEnterpriseInfo(){
        return R.ok(enterpriseInfoService.selectByUid(SecurityUtils.getUid()));
    }

    /**
     * 云豆充值
     */
    @PutMapping("/user/enterpriseInfo/recharge")
    public R recharge(@RequestBody @Validated UserCloudBeanRechargeQuery userCloudBeanRechargeQuery, HttpServletRequest request) {
        return returnTripleResult(enterpriseInfoService.rechargeForUser(userCloudBeanRechargeQuery, request));
    }
    
    /**
     * 云豆概览
     */
    @GetMapping("/user/enterpriseInfo/cloudBean/generalView")
    public R cloudBeanGeneralView(){
        return returnTripleResult(enterpriseInfoService.cloudBeanGeneralView());
    }
    
    /**
     * 云豆回收
     */
    @PutMapping("/user/enterpriseInfo/recycleCloudBean/{uid}")
    public R recycleCloudBean(@PathVariable("uid") Long uid) {
        return returnTripleResult(enterpriseInfoService.recycleCloudBean(uid));
    }
    

}
