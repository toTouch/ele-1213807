package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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


    //新增
    @PostMapping(value = "/admin/shareActivity")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ActivityAddAndUpdateQuery activityAddAndUpdateQuery) {
        return shareActivityService.insert(activityAddAndUpdateQuery);
    }

    //修改--暂时无此功能
    @PutMapping(value = "/admin/shareActivity")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ActivityAddAndUpdateQuery activityAddAndUpdateQuery) {
        return shareActivityService.update(activityAddAndUpdateQuery);
    }



    //列表查询
    @GetMapping(value = "/admin/activity/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeId", required = false) Integer franchiseeId,
                       @RequestParam(value = "type", required = false) String type) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        List<Integer> franchiseeIdList=null;

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            List<Franchisee> franchiseeList = franchiseeService.queryByUid(user.getUid());
            if (ObjectUtil.isEmpty(franchiseeList)) {
                return R.ok();
            }

            franchiseeIdList = new ArrayList<>();
            for (Franchisee franchisee : franchiseeList) {
                franchiseeIdList.add(franchisee.getId());
            }
        }



        ActivityQuery activityQuery = ActivityQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIdList(franchiseeIdList).build();



        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[])
                    JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            activityQuery.setTypeList(typeList);
        }
        return activityService.queryList(activityQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/activity/count")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "franchiseeId", required = false) Integer franchiseeId,
                        @RequestParam(value = "type", required = false) String type) {


        List<Integer> franchiseeIdList=null;

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            List<Franchisee> franchiseeList = franchiseeService.queryByUid(user.getUid());
            if (ObjectUtil.isEmpty(franchiseeList)) {
                return R.ok();
            }

            franchiseeIdList = new ArrayList<>();
            for (Franchisee franchisee : franchiseeList) {
                franchiseeIdList.add(franchisee.getId());
            }
        }



        ActivityQuery activityQuery = ActivityQuery.builder()
                .name(name)
                .franchiseeId(franchiseeId)
                .franchiseeIdList(franchiseeIdList).build();

        if (StringUtils.isNotEmpty(type)) {
            Integer[] types = (Integer[])
                    JSONUtil.parseArray(type).toArray(Integer[].class);

            List<Integer> typeList = Arrays.asList(types);
            activityQuery.setTypeList(typeList);
        }
        return activityService.queryCount(activityQuery);
    }

    //根据id查询活动详情
    @GetMapping(value = "/admin/activity/queryInfo/{id}")
    public R queryInfo(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return activityService.queryInfo(id,false);
    }
}
