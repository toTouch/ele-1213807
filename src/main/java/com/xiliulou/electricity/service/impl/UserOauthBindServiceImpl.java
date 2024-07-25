package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.dto.WXMinProAuth2SessionResult;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.UserOauthBindMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.web.query.OauthBindQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (UserOauthBind)表服务实现类
 *
 * @author makejava
 * @since 2020-12-03 09:17:39
 */
@Service("userOauthBindService")
@Slf4j
public class UserOauthBindServiceImpl implements UserOauthBindService {
    
    @Resource
    ElectricityPayParamsService electricityPayParamsService;
    
    @Qualifier("restTemplateServiceImpl")
    @Resource
    RestTemplateService restTemplateService;
    
    @Resource
    private UserOauthBindMapper userOauthBindMapper;
    
    @Autowired
    private UserService userService;
    
    @Resource
    private UserInfoService userInfoService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserOauthBind queryByIdFromDB(Long id) {
        return this.userOauthBindMapper.selectById(id);
    }
    
    
    /**
     * 新增数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOauthBind insert(UserOauthBind userOauthBind) {
        this.userOauthBindMapper.insert(userOauthBind);
        return userOauthBind;
    }
    
    /**
     * 修改数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserOauthBind userOauthBind) {
        return this.userOauthBindMapper.updateById(userOauthBind);
        
    }
    
    
    @Override
    public UserOauthBind queryOauthByOpenIdAndSource(String openid, int source, Integer tenantId) {
        return this.userOauthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOauthBind>().eq(UserOauthBind::getThirdId, openid).eq(UserOauthBind::getSource, source).eq(UserOauthBind::getTenantId, tenantId));
    }
    
    @Override
    public List<UserOauthBind> selectListOauthByOpenIdAndSource(String openid, int source, Integer tenantId) {
        return userOauthBindMapper.selectListOauthByOpenIdAndSource(openid, source, tenantId);
    }
    
    @Override
    public UserOauthBind queryOneByOpenIdAndSource(String openid, Integer source, Integer tenantId) {
        return userOauthBindMapper.selectOneByOpenIdAndSource(openid, source, tenantId);
    }
    
    @Slave
    @Override
    public List<UserOauthBind> queryListByUidAndTenantId(Long uid, Integer tenantId) {
        return userOauthBindMapper.selectListByUidAndTenantId(uid,tenantId);
    }
    
    @Slave
    @Override
    public UserOauthBind queryByUserPhone(Long uid, String phone, int source, Integer tenantId) {
        return this.userOauthBindMapper.selectOne(new LambdaQueryWrapper<UserOauthBind>().eq(UserOauthBind::getUid, uid).eq(UserOauthBind::getPhone, phone).eq(UserOauthBind::getSource, source)
                .eq(UserOauthBind::getStatus, UserOauthBind.STATUS_BIND).eq(UserOauthBind::getTenantId, tenantId));
    }
    
    @Slave
    @Override
    public UserOauthBind queryByUserPhone(String phone, int source, Integer tenantId) {
        return this.userOauthBindMapper.selectUserByPhone(phone, source, tenantId);
    }
    
    @Override
    @Slave
    public Pair<Boolean, Object> queryListByCondition(Integer size, Integer offset, Long uid, String thirdId, String phone, Integer tenantId) {
        List<UserOauthBind> list = this.userOauthBindMapper.queryListByCondition(size, offset, uid, thirdId, phone, tenantId);
        return Pair.of(true, list);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> updateOauthBind(OauthBindQuery oauthBindQuery) {
        UserOauthBind userOauthBind = queryByIdFromDB(oauthBindQuery.getId());
        if (Objects.isNull(userOauthBind)) {
            return Pair.of(false, "查询不到第三方账号！");
        }
        
        UserOauthBind build = UserOauthBind.builder().status(oauthBindQuery.getStatus()).phone(oauthBindQuery.getPhone()).thirdId(oauthBindQuery.getThirdId())
                .id(oauthBindQuery.getId()).build();
        
        if (Objects.nonNull(oauthBindQuery.getStatus())) {
            User user = userService.queryByUidFromCache(userOauthBind.getUid());
            User updateUser = User.builder().updateTime(System.currentTimeMillis()).lockFlag(oauthBindQuery.getStatus() - 1).uid(user.getUid()).build();
            userService.updateUser(updateUser, user);
        }
        
        return update(build) == 1 ? Pair.of(true, null) : Pair.of(false, "修改失败 ");
    }
    
    /**
     * @param uid
     * @return
     */
    @Override
    public UserOauthBind queryUserOauthBySysId(Long uid, Integer tenantId) {
        
        return userOauthBindMapper.queryUserOauthBySysId(uid, tenantId);
    }
    
    @Slave
    @Override
    public List<UserOauthBind> queryListByUid(Long uid) {
        return userOauthBindMapper.selectList(new LambdaQueryWrapper<UserOauthBind>().eq(UserOauthBind::getUid, uid));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userOauthBindMapper.deleteById(id) > 0;
    }
    
    @Override
    public Integer updateOpenIdByUid(String openId, Integer status, Long uid, Integer tenantId) {
        return userOauthBindMapper.updateOpenIdByUid(openId, status, uid, tenantId, System.currentTimeMillis());
    }
    
    @Override
    @Slave
    public UserOauthBind selectByUidAndPhone(String phone, Long uid, Integer tenantId) {
        return userOauthBindMapper.selectByUidAndPhone(phone, uid, tenantId);
    }
    
    /**
     * 根据手机号、类型、租户查询用户
     *
     * @param phone    手机号
     * @param source   类型
     * @param tenantId 租户ID
     * @return 绑定集
     */
    @Slave
    @Override
    public List<UserOauthBind> listUserByPhone(String phone, Integer source, Integer tenantId) {
        return userOauthBindMapper.selectListUserByPhone(phone, source, tenantId);
    }
    
    /**
     *
     * @param phone
     * @param source
     * @param tenantId
     * @return
     *
     * @see UserOauthBindServiceImpl#listUserByPhone(String, Integer, Integer)
     */
    @Deprecated
    @Override
    @Slave
    public UserOauthBind selectUserByPhone(String phone, Integer source, Integer tenantId) {
        return userOauthBindMapper.selectUserByPhone(phone, source, tenantId);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return userOauthBindMapper.updatePhoneByUid(tenantId, uid, newPhone, System.currentTimeMillis());
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer deleteByUid(Long uid, Integer tenantId) {
        return userOauthBindMapper.deleteByUid(uid, tenantId);
    }
    
    @Override
    public UserOauthBind queryOauthByOpenIdAndUid(Long id, String openId, Integer tenantId) {
        return userOauthBindMapper.queryOauthByOpenIdAndUid(id, openId, tenantId);
    }
    
    @Override
    public List<UserOauthBind> queryOpenIdListByUidsAndTenantId(List<Long> longs, Integer tenantId) {
        return userOauthBindMapper.selectOpenIdListByUidsAndTenantId(longs, tenantId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkOpenIdByJsCode(String jsCode) {
        Long uid = SecurityUtils.getUid();
        Long tenantId = TenantContextHolder.getTenantId().longValue();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("check open id failed, not found user,uid={}", uid);
            return Boolean.FALSE;
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("check open id failed, user is unUsable,uid={}", userInfo.getUid());
            return Boolean.FALSE;
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("check open id failed, user not auth,uid={}", userInfo.getUid());
            return Boolean.FALSE;
        }
        
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId.intValue(), MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        if (Objects.isNull(electricityPayParams) || StrUtil.isEmpty(electricityPayParams.getMerchantMinProAppId()) || StrUtil.isEmpty(
                electricityPayParams.getMerchantMinProAppSecert())) {
            log.warn("check open id failed, not found appId,appSecret! uid = {}, tenantId = {}", uid, tenantId);
            //throw new AuthenticationServiceException("未能查找到appId和appSecret！");
            return Boolean.FALSE;
        }
        
        String codeUrl = String.format(CacheConstant.WX_MIN_PRO_AUTHORIZATION_CODE_URL, electricityPayParams.getMerchantMinProAppId(),
                electricityPayParams.getMerchantMinProAppSecert(), jsCode);
        
        String bodyStr = restTemplateService.getForString(codeUrl, null);
        log.info("check open id from wx, call wx pro auth for query open id message={}", bodyStr);
        
        WXMinProAuth2SessionResult result = JsonUtil.fromJson(bodyStr, WXMinProAuth2SessionResult.class);
        if (Objects.isNull(result) || StrUtil.isEmpty(result.getOpenid()) || StrUtil.isEmpty(result.getSession_key())) {
            log.error("check open id failed, wxResult has error! bodyStr = {}, uid = {}, tenantId = {}", bodyStr, uid, tenantId);
            //throw new AuthenticationServiceException("微信返回异常！");
            return Boolean.FALSE;
        }
        
        String openId = result.getOpenid();
        List<UserOauthBind> userOauthBindList = this.selectListOauthByOpenIdAndSource(openId, UserOauthBind.SOURCE_WX_PRO, tenantId.intValue());
        
        if (CollectionUtils.isEmpty(userOauthBindList)) {
            return Boolean.FALSE;
        }
        
        if (userOauthBindList.get(0).getThirdId().equals(openId)) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
}
