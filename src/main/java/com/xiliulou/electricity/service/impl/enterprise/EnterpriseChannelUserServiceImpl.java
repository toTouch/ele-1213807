package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.bean.BeanUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.enums.enterprise.InvitationWayEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
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
        log.info("add new user by enterprise start, channel user = {}", JsonUtil.toJson(query));
        //检查当前用户是否可用
        Triple<Boolean, String, Object> result = verifyUserInfo(query);
        if(Boolean.FALSE.equals(result.getLeft())){
            return result;
        }

        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        BeanUtil.copyProperties(query, enterpriseChannelUser);
        enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);

        log.info("add new user by enterprise end, enterprise channel user = {}",enterpriseChannelUser.getId());

        return Triple.of(true, "", null);
    }

    @Override
    public Triple<Boolean, String, Object> queryUser(EnterpriseChannelUserQuery enterpriseChannelUserQuery) {
        Integer tenantId = TenantContextHolder.getTenantId();
        UserInfo userInfo = userInfoService.queryUserByPhoneAndFranchisee(enterpriseChannelUserQuery.getPhone(), enterpriseChannelUserQuery.getFranchiseeId().intValue(), tenantId);

        //返回查询到的用户信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = new EnterpriseChannelUserVO();
        if(Objects.isNull(userInfo)){
            log.error("query enterprise user by phone failed. Not found user. phone is {}, franchisee id = {} ", enterpriseChannelUserQuery.getPhone(), enterpriseChannelUserQuery.getFranchiseeId());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        enterpriseChannelUserVO.setUid(userInfo.getUid());
        enterpriseChannelUserVO.setName(userInfo.getName());
        enterpriseChannelUserVO.setPhone(userInfo.getPhone());
        enterpriseChannelUserVO.setAuthStatus(userInfo.getAuthStatus());

        return Triple.of(true, "", enterpriseChannelUserVO);
    }

    @Override
    public Triple<Boolean, String, Object> generateChannelUser(EnterpriseChannelUserQuery query) {
        if (!ObjectUtils.allNotNull(query.getEnterpriseId(), query.getFranchiseeId())) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }
        log.info("generate channel user start, enterprise channel info = {}", query.getEnterpriseId());

        Integer tenantId = TenantContextHolder.getTenantId();
        //先查找库中是否存在已经创建好的基础用户数据，如果存在，则使用已存在的数据，若不存在，则创建新的。
        EnterpriseChannelUser existEnterpriseChannelRecord = enterpriseChannelUserMapper.selectUnusedChannelUser(query.getEnterpriseId(), query.getFranchiseeId(), tenantId.longValue());
        if(Objects.nonNull(existEnterpriseChannelRecord)){
            log.info("exist channel user record end, channel user info = {}", JsonUtil.toJson(existEnterpriseChannelRecord));
            return Triple.of(true, "", existEnterpriseChannelRecord);
        }

        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setEnterpriseId(query.getEnterpriseId());
        enterpriseChannelUser.setInvitationWay(InvitationWayEnum.INVITATION_WAY_FACE_TO_FACE.getCode());
        enterpriseChannelUser.setFranchiseeId(query.getFranchiseeId());
        enterpriseChannelUser.setTenantId(TenantContextHolder.getTenantId().longValue());
        enterpriseChannelUser.setCreateTime(System.currentTimeMillis());
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());

        enterpriseChannelUserMapper.insertOne(enterpriseChannelUser);
        log.info("generate channel user end, channel user info = {}", JsonUtil.toJson(enterpriseChannelUser));

        return Triple.of(true, "", enterpriseChannelUser);
    }

    @Override
    public Triple<Boolean, String, Object> updateUserAfterQRScan(EnterpriseChannelUserQuery query) {
        if (!ObjectUtils.allNotNull(query.getUid(), query.getId(), query.getRenewalStatus())) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }

        Triple<Boolean, String, Object> result = verifyUserInfo(query);
        if(Boolean.FALSE.equals(result.getLeft())){
            return result;
        }

        EnterpriseChannelUser enterpriseChannelUser = new EnterpriseChannelUser();
        enterpriseChannelUser.setId(query.getId());
        enterpriseChannelUser.setUid(query.getUid());
        enterpriseChannelUser.setRenewalStatus(query.getRenewalStatus());
        enterpriseChannelUser.setUpdateTime(System.currentTimeMillis());

        enterpriseChannelUserMapper.update(enterpriseChannelUser);

        return Triple.of(true, "", enterpriseChannelUser);
    }

    @Override
    public Triple<Boolean, String, Object> checkUserExist(Long id, Long uid) {
        if (!ObjectUtils.allNotNull(id, uid)) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }

        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectChannelUserByIdAndUid(id, uid);
        if(Objects.isNull(enterpriseChannelUser)){
            return Triple.of(false, "300062", "被邀请的用户不存在");
        }

        return Triple.of(true, "", enterpriseChannelUser);
    }

    public Triple<Boolean, String, Object> verifyUserInfo(EnterpriseChannelUserQuery query){

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

        //根据当前企业ID查询企业信息
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(query.getEnterpriseId());

        // 4. 检查当前用户是否隶属于企业渠道所属加盟商
        if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userInfo.getFranchiseeId().equals(enterpriseInfo.getFranchiseeId())) {
            log.error("add user to enterprise failed. add new user's franchiseeId is {}. enterprise franchiseeId is {}", userInfo.getFranchiseeId(), enterpriseInfo.getFranchiseeId());
            return Triple.of(false, "300036", "所属机构不匹配");
        }

        // 5. 检查用户是否购买过线上套餐(包含换电, 租车, 车电一体套餐)
        if(userInfo.getPayCount() > 0){
            log.info("Exist package pay count for current user, uid = {}", userInfo.getUid());
            return Triple.of(false, "300060", "当前用户已购买过线上套餐");
        }

        // 6. 当前用户不能属于其他企业
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserMapper.selectUsedChannelUser(uid, null, null);
        if(Objects.nonNull(enterpriseChannelUser)){
            log.info("The user already belongs to another enterprise, enterprise id = {}", enterpriseChannelUser.getEnterpriseId());
            return Triple.of(false, "300061", "当前用户已加入其他企业, 无法重复添加");
        }

        return Triple.of(true, "", null);
    }

}
