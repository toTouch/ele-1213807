package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserOauthBind;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * (UserOauthBind)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-03 09:17:39
 */
public interface UserOauthBindMapper extends BaseMapper<UserOauthBind> {


    List<UserOauthBind> queryListByCondition(@Param("size") Integer size, @Param("offset") Integer offset, @Param("uid") Long uid, @Param("thirdId") String thirdId, @Param("phone") String phone, @Param("tenantId") Integer tenantId);


    UserOauthBind queryUserOauthBySysId(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    UserOauthBind selectByUidAndPhone(@Param("phone") String phone, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据手机号、类型、租户查询用户
     *
     * @param phone    手机号
     * @param source   类型
     * @param tenantId 租户ID
     * @return 绑定集
     */
    List<UserOauthBind> selectListUserByPhone(@Param("phone") String phone, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    /**
     *
     * @param phone
     * @param source
     * @param tenantId
     * @return
     *
     * @see UserOauthBindMapper#selectListUserByPhone(String, Integer, Integer)
     */
    UserOauthBind selectUserByPhone(@Param("phone") String phone, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    Integer updateOpenIdByUid(@Param("openId") String openId, @Param("status") Integer status, @Param("uid") Long uid, @Param("tenantId") Integer tenantId,
            @Param("updateTime") Long updateTime);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone,@Param("updateTime") Long updateTime);
    
    List<UserOauthBind> selectListOauthByOpenIdAndSource(@Param("openId") String openId, @Param("source") Integer source,@Param("tenantId") Integer tenantId);
    
    Integer deleteByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    UserOauthBind queryOauthByOpenIdAndUid(@Param("id") Long id, @Param("openId") String openId, @Param("tenantId") Integer tenantId);
    
    List<UserOauthBind> selectOpenIdListByUidsAndTenantId(@Param("uids") List<Long> longs,@Param("tenantId") Integer tenantId);
    
    UserOauthBind selectOneByOpenIdAndSource(@Param("openId") String openId, @Param("source") Integer source,@Param("tenantId") Integer tenantId);
}
