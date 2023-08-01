package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.reqparam.qry.userinfo.UserInfoQryReq;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.userinfo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息Controller
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/admin/userInfo/v2")
public class JsonAdminUserInfoV2Controller {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 根据关键字查询用户集
     * @param userInfoQryReq 查询模型
     * @return 用户集
     */
    @PostMapping("/queryByKeywords")
    public R<List<UserInfoVO>> queryByKeywords(@RequestBody UserInfoQryReq userInfoQryReq) {
        if (ObjectUtils.isEmpty(userInfoQryReq)) {
            userInfoQryReq = new UserInfoQryReq();
        }

        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .tenantId(tenantId)
                .keywords(userInfoQryReq.getKeywords())
                .offset(Long.valueOf(userInfoQryReq.getOffset()))
                .size(Long.valueOf(userInfoQryReq.getSize()))
                .build();

        List<UserInfo> userInfos = userInfoService.page(userInfoQuery);
        if (CollectionUtils.isEmpty(userInfos)) {
            return R.ok();
        }

        List<UserInfoVO> userInfoVoList = userInfos.stream().map(userInfo -> {
            // 拼装返回字段
            UserInfoVO userInfoVo = new UserInfoVO();
            userInfoVo.setId(userInfo.getId());
            userInfoVo.setUid(userInfo.getUid());
            userInfoVo.setName(userInfo.getName());
            userInfoVo.setPhone(userInfo.getPhone());

            // 赋值复合字段
            StringBuilder builderNameAndPhone = new StringBuilder();
            if (StringUtils.isNotBlank(userInfo.getName())) {
                builderNameAndPhone.append(userInfo.getName());
            }
            if (StringUtils.isNotBlank(builderNameAndPhone.toString())) {
                builderNameAndPhone.append("/");
            }
            if (StringUtils.isNotBlank(userInfo.getPhone())) {
                builderNameAndPhone.append(userInfo.getPhone());
            }
            userInfoVo.setNameAndPhone(builderNameAndPhone.toString());

            return userInfoVo;
        }).collect(Collectors.toList());

        return R.ok(userInfoVoList);
    }
}
