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


    UserOauthBind queryOauthByOpenIdAndSource(String openid, int source,Integer tenantId);

    UserOauthBind queryByUserPhone(String phone, int source,Integer tenantId);

    Pair<Boolean, Object> queryListByCondition(Integer size, Integer offset, Long uid, String thirdId, String phone,Integer tenantId);

    Pair<Boolean, Object> updateOauthBind(OauthBindQuery oauthBindQuery);

     UserOauthBind queryUserOauthBySysId(Long uid,Integer tenantId);

    List<UserOauthBind> queryListByUid(Long uid);

    Boolean deleteById(Long id);
    
    Boolean checkOpenIdByJsCode(String jsCode);
    
    UserOauthBind selectByUidAndPhone(String phone,Long uid,Integer tenantId);
    
    UserOauthBind selectUserByPhone(String phone,Integer source,Integer tenantId);
    
    Integer updateOpenIdByUid(String openId, String accessToken, String refreshToken, Integer status, Long uid, Integer tenantId);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
}
