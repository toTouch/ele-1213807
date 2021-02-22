package com.xiliulou.electricity.controller.user;
import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class EleUserAuthUserController {
    /**
     * 服务对象
     */
    @Resource
    EleUserAuthService eleUserAuthService;

    @Autowired
    EleAuthEntryService eleAuthEntryService;

    @Autowired
    RedisService redisService;

    //实名认证
    @PostMapping("/user/auth")
    public R commitInfoAuth(@RequestBody List<EleUserAuth> EleUserAuthList) {
        if (!DataUtil.collectionIsUsable(EleUserAuthList)) {
            return R.fail("不合法的参数");
        }
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("未获取到当前用户信息!");
        }

        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 5*1000L, false);
        if (!getLockSuccess) {
            return R.fail("操作频繁,请稍后再试!");
        }

        eleUserAuthService.insertEleUserAuthList(EleUserAuthList);
        redisService.deleteKeys(ElectricityCabinetConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid);
        return R.ok();

    }

    //修改实名认证(实名认证审核中，实名认证未通过允许修改)
    @PutMapping("/user/auth")
    public R updateInfoAuth(@RequestBody List<EleUserAuth> eleUserAuthList) {
        if (!DataUtil.collectionIsUsable(eleUserAuthList)) {
            return R.fail("不合法的参数");
        }
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("未获取到当前用户信息!");
        }

        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 5*1000L, false);
        if (!getLockSuccess) {
            return R.fail("操作频繁,请稍后再试!");
        }

        eleUserAuthService.updateEleUserAuthList(eleUserAuthList);
        redisService.deleteKeys(ElectricityCabinetConstant.ELE_CACHE_USER_AUTH_LOCK_KEY + uid);
        return R.ok();
    }

    /**
     * 获取资料项
     *
     */
    @GetMapping(value = "/user/authEntry/list")
    public R getEleAuthEntriesList() {
        return R.ok(eleAuthEntryService.getEleAuthEntriesList());
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
            return R.fail("未获取到当前用户信息!");
        }

        return R.ok(eleUserAuthService.getEleUserAuthSpecificStatus(uid));
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
            return R.fail("未获取到当前用户信息!");
        }

        return eleUserAuthService.selectCurrentEleAuthEntriesList();
    }

}