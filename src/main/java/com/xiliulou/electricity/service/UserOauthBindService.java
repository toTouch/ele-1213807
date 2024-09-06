package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.web.query.OauthBindQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (UserOauthBind)表服务接口
 *
 * @author makejava
 * @since 2020-12-03 09:17:39
 */
public interface UserOauthBindService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UserOauthBind queryByIdFromDB(Long id);
    
    
    /**
     * 新增数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    UserOauthBind insert(UserOauthBind userOauthBind);
    
    /**
     * 修改数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    Integer update(UserOauthBind userOauthBind);
    
    
    
    UserOauthBind queryOauthByOpenIdAndSource(String openid, int source, Integer tenantId);
    
    List<UserOauthBind> selectListOauthByOpenIdAndSource(String openid, int source, Integer tenantId);
    
    UserOauthBind queryByUserPhone(Long uid, String phone, int source, Integer tenantId);
    
    Pair<Boolean, Object> queryListByCondition(Integer size, Integer offset, Long uid, String thirdId, String phone, Integer tenantId);
    
    Pair<Boolean, Object> updateOauthBind(OauthBindQuery oauthBindQuery);
    
    
    UserOauthBind queryUserOauthBySysId(Long uid, Integer tenantId);
    
    List<UserOauthBind> queryListByUid(Long uid);
    
    Boolean deleteById(Long id);
    
    Boolean checkOpenIdByJsCode(String jsCode);
    
    /**
     * 根据用户id+电话+租户id查询
     *
     * @param phone
     * @param uid
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/8 11:29
     */
    List<UserOauthBind> selectListByUidAndPhone(String phone, Long uid, Integer tenantId);
    UserOauthBind selectByUidAndPhone(String phone, Long uid, Integer tenantId);
    
    /**
     * 根据手机号、类型、租户查询用户
     *
     * @param phone    手机号
     * @param source   类型
     * @param tenantId 租户ID
     * @return 绑定集
     */
    List<UserOauthBind> listUserByPhone(String phone, Integer source, Integer tenantId);
    
    /**
     * @param phone
     * @param source
     * @param tenantId
     * @return
     * @see UserOauthBindService#listUserByPhone(String, Integer, Integer)
     */
    @Deprecated
    UserOauthBind selectUserByPhone(String phone, Integer source, Integer tenantId);
    
    
    Integer updateOpenIdByUid(String openId, Integer status, Long uid, Integer source, Integer tenantId);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    /**
     * 根据uid 删除用户绑定信息
     *
     * @param uid
     * @return
     */
    Integer deleteByUid(Long uid, Integer tenantId);
    
    UserOauthBind queryOauthByOpenIdAndUid(Long id, String openId, Integer tenantId);
    
    List<UserOauthBind> queryOpenIdListByUidsAndTenantId(List<Long> longs, Integer tenantId);
    
    UserOauthBind queryOneByOpenIdAndSource(String openid, Integer source, Integer tenantId);
    
    
    /**
     * 根据用户id+租户id查询授权的第三方信息集合
     *
     * @param uid
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/7/25 11:13
     */
    List<UserOauthBind> queryListByUidAndTenantId(Long uid, Integer tenantId);
    
    /**
     * 根据用户id+租户id+用户渠道来源查询
     *
     * @param uid      用户
     * @param tenantId 租户
     * @param channel  ${@link com.xiliulou.core.base.enums.ChannelEnum}
     * @author caobotao.cbt
     * @date 2024/8/5 18:51
     */
    UserOauthBind queryByUidAndTenantAndChannel(Long uid, Integer tenantId, String channel);
    
    /**
     * 根据用户id+租户id+source查询
     *
     * @param uid      用户
     * @param tenantId 租户
     * @param source
     * @author caobotao.cbt
     * @date 2024/8/5 18:51
     */
    UserOauthBind queryByUidAndTenantAndSource(Long uid, Integer tenantId, Integer source);
    
    /**
     * 根据用户id+source查询
     *
     * @param uid
     * @param source
     * @author caobotao.cbt
     * @date 2024/8/7 19:14
     */
    List<UserOauthBind> queryListByUidAndSource(Long uid, Integer source);
    
    /**
     * 根据参数获取数量
     *
     * @param openId
     * @param sourceWxPro
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/9/5 20:39
     */
    Integer countByThirdIdAndSourceAndTenantId(String openId, Integer sourceWxPro, Integer tenantId);
    
}
