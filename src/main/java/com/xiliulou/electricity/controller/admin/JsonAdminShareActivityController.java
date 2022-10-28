package com.xiliulou.electricity.controller.admin;

import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@RestController
@Slf4j
public class JsonAdminShareActivityController {
    /**
     * 服务对象
     */
    @Autowired
    private ShareActivityService shareActivityService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserDataScopeService userDataScopeService;

    //新增
    @PostMapping(value = "/admin/shareActivity")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        return shareActivityService.insert(shareActivityAddAndUpdateQuery);
    }

    //修改--暂时无此功能
    @PutMapping(value = "/admin/shareActivity")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
        return shareActivityService.update(shareActivityAddAndUpdateQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivity/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeIds", required = false) List<Long> franchiseeIds,
                       @RequestParam(value = "type", required = false) String type,
                       @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.TYPE_USER_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[])
                    JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            shareActivityQuery.setTypeList(typeList);
        }
        return shareActivityService.queryList(shareActivityQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/shareActivity/count")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "franchiseeIds", required = false) List<Long> franchiseeIds,
                        @RequestParam(value = "type", required = false) String type,
                        @RequestParam(value = "status", required = false) Integer status) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getDataType(), User.TYPE_USER_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        ShareActivityQuery shareActivityQuery = ShareActivityQuery.builder()
                .name(name)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .status(status).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[]) JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            shareActivityQuery.setTypeList(typeList);
        }
        return shareActivityService.queryCount(shareActivityQuery);
    }

    //根据id查询活动详情
    @GetMapping(value = "/admin/shareActivity/queryInfo/{id}")
    public R queryInfo(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return shareActivityService.queryInfo(id);
    }
}
