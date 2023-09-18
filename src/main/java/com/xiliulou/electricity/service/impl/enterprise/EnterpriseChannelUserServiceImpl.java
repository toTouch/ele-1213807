package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 14:14
 */

@Slf4j
public class EnterpriseChannelUserServiceImpl implements EnterpriseChannelUserService {

    @Resource
    private EnterpriseChannelUserMapper enterpriseChannelUserMapper;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private EnterpriseInfoService enterpriseInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseChannelUserQuery query) {
        //检查当前用户是否可用
        // 1. 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("add user to enterprise failed. Not found user. uid is {} ", query.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        // 2. 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("add user to enterprise failed. User is unUsable. uid is {} ", query.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        // 3. 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("add user to enterprise failed. User not auth. uid is {}", query.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "用户尚未实名认证");
        }

        //根据当前企业用户的UID查询企业信息
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.selectByUid(uid);

        //4. 检查当前用户是否隶属于企业渠道所属加盟商
        if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userInfo.getFranchiseeId().equals(enterpriseInfo.getFranchiseeId())) {
            log.error("add user to enterprise failed. add new user's franchiseeId is {}. enterprise franchiseeId is {}", userInfo.getFranchiseeId(), enterpriseInfo.getFranchiseeId());
            return Triple.of(false, "300036", "所属机构不匹配");
        }

        //5. 检查用户是否购买过线上套餐(包含换电, 租车, 车电一体套餐)
        if(userInfo.getPayCount() > 0){
            log.info("Exist package pay count for current user, uid = {}", userInfo.getUid());
            return Triple.of(false, "300060", "当前用户已购买过线上套餐");
        }

        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        BeanUtil.copyProperties(query, enterpriseChannelUser);
        enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);

        return Triple.of(true, "", null);
    }
}
