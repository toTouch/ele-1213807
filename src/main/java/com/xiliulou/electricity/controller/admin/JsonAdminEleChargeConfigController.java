package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.query.ChargeConfigQuery;
import com.xiliulou.electricity.service.EleChargeConfigService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2023/7/18 10:22
 */
@RestController
public class JsonAdminEleChargeConfigController extends BaseController {
    @Autowired
    EleChargeConfigService eleChargeConfigService;
    @Autowired
    UserDataScopeService userDataScopeService;

    @GetMapping("/admin/charge/config/list")
    public R getList(ChargeConfigListQuery chargeConfigListQuery) {
        if (Objects.isNull(chargeConfigListQuery.getSize()) || chargeConfigListQuery.getSize() >= 50 || chargeConfigListQuery.getSize() < 0) {
            chargeConfigListQuery.setSize(10);
        }

        if (Objects.isNull(chargeConfigListQuery.getOffset()) || chargeConfigListQuery.getOffset() < 0) {
            chargeConfigListQuery.setOffset(0);
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        chargeConfigListQuery.setStoreIds(storeIds);
        chargeConfigListQuery.setFranchiseeIds(franchiseeIds);
        chargeConfigListQuery.setTenantId(TenantContextHolder.getTenantId());
        return returnPairResult(eleChargeConfigService.queryList(chargeConfigListQuery));
    }

    @GetMapping("/admin/charge/config/list/count")
    public R getListCount(ChargeConfigListQuery chargeConfigListQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(NumberConstant.ZERO);
            }
        }

        chargeConfigListQuery.setStoreIds(storeIds);
        chargeConfigListQuery.setFranchiseeIds(franchiseeIds);
        chargeConfigListQuery.setTenantId(TenantContextHolder.getTenantId());
        return returnPairResult(eleChargeConfigService.queryListCount(chargeConfigListQuery));
    }

    @PostMapping("/admin/charge/config/save")
    @Log(title = "增加电费规则")
    public R saveConfig(@RequestBody @Validated ChargeConfigQuery chargeConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(eleChargeConfigService.saveConfig(chargeConfigQuery));
    }

    @PostMapping("/admin/charge/config/modify")
    @Log(title = "修改电费规则")
    public R modifyConfig(@RequestBody @Validated(UpdateGroup.class) ChargeConfigQuery chargeConfigQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(eleChargeConfigService.modifyConfig(chargeConfigQuery));
    }

    @PostMapping("/admin/charge/config/del/{id}")
    @Log(title = "删除电费规则")
    public R delConfig(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }

        return returnPairResult(eleChargeConfigService.delConfig(id));
    }


}
