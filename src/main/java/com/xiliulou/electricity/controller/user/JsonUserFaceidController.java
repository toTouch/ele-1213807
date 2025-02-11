package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.query.AlipayUserCertifyInfoQuery;
import com.xiliulou.electricity.query.FaceidResultQuery;
import com.xiliulou.electricity.query.UserCertifyInfoQuery;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.FaceidService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 人脸核身
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-01-15:58
 */
@RestController
@Slf4j
public class JsonUserFaceidController extends BaseController {
    
    @Autowired
    private FaceidService faceidService;
    
    @Autowired
    ActivityService activityService;
    
    @Resource
    private UserDelRecordService userDelRecordService;
    
    /**
     * 获取人脸核身token
     */
    @GetMapping(value = "/user/faceid/getToken")
    public R getToken() {
        return returnTripleResult(faceidService.getEidToken());
    }
    
    /**
     * 人脸核身结果
     */
    @PostMapping(value = "/user/faceid/verifyEidResult")
    public R verifyEidResult(@RequestBody @Validated FaceidResultQuery faceidResultQuery) {
        Triple<Boolean, String, Object> result = faceidService.verifyEidResult(faceidResultQuery);
        
        //人脸核身成功后，异步触发活动处理流程
        Long uid = SecurityUtils.getUid();
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setUid(uid);
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        log.info("handle activity after face id auth success: {}", JsonUtil.toJson(activityProcessDTO));
        activityService.asyncProcessActivity(activityProcessDTO);
    
        // 老用户实名认证后,恢复用户历史分组
        userDelRecordService.asyncRecoverUserInfoGroup(uid);
        
        return returnTripleResult(result);
    }
    
    /**
     * 获取支付宝人脸核身URL和CertifyId
     */
    @PostMapping(value = "/user/alipay/queryCertifyId")
    public R queryAliPayCertifyId(@RequestBody @Validated(CreateGroup.class) AlipayUserCertifyInfoQuery query) {
        return returnTripleResult(faceidService.queryAliPayCertifyInfo(query));
    }
    
    /**
     * 查询人脸核身结果
     */
    @PostMapping(value = "/user/alipay/queryUserCertifyResult")
    public R queryAliPayUserCertifyUrl(@RequestBody @Validated(UpdateGroup.class) AlipayUserCertifyInfoQuery query) {
        return returnTripleResult(faceidService.queryAliPayUserCertifyResult(query));
    }
    
}
