package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.request.userinfo.EleUserAuthRequest;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ValidList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 实名认证信息(TEleUserAuth)表控制层
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
@RestController
@Slf4j
public class JsonUserEleUserAuthController {
    
    /**
     * 服务对象
     */
    @Autowired
    EleUserAuthService eleUserAuthService;
    
    @Autowired
    EleAuthEntryService eleAuthEntryService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ActivityService activityService;
    
    @Resource
    private UserDelRecordService userDelRecordService;
    
    //实名认证
    @PostMapping("/user/auth")
    public R webAuth(@RequestBody @Validated ValidList<EleUserAuthRequest> eleUserAuthList) {
        if (!DataUtil.collectionIsUsable(eleUserAuthList)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //限频
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        R result = eleUserAuthService.webAuth(eleUserAuthList);
        
        //实名认证审核通过后，触发活动处理流程
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setUid(uid);
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        log.info("handle activity after auto review success: {}", JsonUtil.toJson(activityProcessDTO));
        
        activityService.asyncProcessActivity(activityProcessDTO);
    
        // 老用户实名认证后,恢复用户历史分组及流失用户标记
        userDelRecordService.asyncRecoverUserInfoGroup(uid);
        
        return result;
        
    }
    
    
    /**
     * 获取需要实名认证资料项
     */
    @GetMapping(value = "/user/authEntry/list")
    public R getEleAuthEntriesList() {
        Integer tenantId = TenantContextHolder.getTenantId();
        return R.ok(eleAuthEntryService.getUseEleAuthEntriesList(tenantId));
    }
    
    /**
     * 获取当前用户的具体审核状态
     *
     * @param
     * @return
     */
    @GetMapping(value = "/user/authStatus")
    public R getEleUserAuthSpecificStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return eleUserAuthService.getEleUserAuthSpecificStatus(uid);
    }
    
    /**
     * 获取当前用户的具体审核状态及审核原因
     *
     * @param
     * @return
     */
    @GetMapping(value = "/user/authStatusInfo")
    public R selectUserAuthStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return eleUserAuthService.selectUserAuthStatus(uid);
    }
    
    /**
     * 获取当前的用户资料项
     *
     * @param
     * @return
     */
    @GetMapping(value = "/user/current/authEntry/list")
    public R getCurrentEleAuthEntriesList() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return eleUserAuthService.selectCurrentEleAuthEntriesList(uid);
    }
    
    /**
     * 获取上传身份证照片所需的签名
     */
    @GetMapping(value = "/user/acquire/upload/idcard/file/sign")
    public R getUploadIdcardFileSign() {
        return eleUserAuthService.acquireIdcardFileSign();
    }
    
    /**
     * 获取上传自拍照片所需的签名
     */
    @GetMapping(value = "/user/acquire/upload/selfie/file/sign")
    public R getUploadselfieFileSign() {
        return eleUserAuthService.acquireselfieFileSign();
    }
    
}
