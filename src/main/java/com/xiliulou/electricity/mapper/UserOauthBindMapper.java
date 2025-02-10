package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.query.UserOauthBindListQuery;
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
    
    
    List<UserOauthBind> queryListByCondition(@Param("size") Integer size, @Param("offset") Integer offset, @Param("uid") Long uid, @Param("thirdId") String thirdId,
            @Param("phone") String phone, @Param("tenantId") Integer tenantId);
    
    
    List<UserOauthBind> selectListByUidAndPhone(@Param("phone") String phone, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据手机号、类型、租户查询用户
     *
     * @param phone    手机号
     * @param source   类型
     * @param tenantId 租户ID
     * @return 绑定集
     */
    List<UserOauthBind> selectListUserByPhone(@Param("phone") String phone, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    
    Integer updateOpenIdByUid(@Param("openId") String openId, @Param("status") Integer status, @Param("uid") Long uid, @Param("tenantId") Integer tenantId,
            @Param("source") Integer source, @Param("updateTime") Long updateTime);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone, @Param("updateTime") Long updateTime);
    
    List<UserOauthBind> selectListOauthByOpenIdAndSource(@Param("openId") String openId, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    Integer deleteByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    UserOauthBind queryOauthByOpenIdAndUid(@Param("id") Long id, @Param("openId") String openId, @Param("tenantId") Integer tenantId);
    
    List<UserOauthBind> selectOpenIdListByUidsAndTenantId(@Param("uids") List<Long> longs, @Param("tenantId") Integer tenantId);
    
    UserOauthBind selectOneByOpenIdAndSource(@Param("openId") String openId, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据uid+租户id查询所有授权
     *
     * @param uid
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/7/25 11:15
     */
    List<UserOauthBind> selectListByUidAndTenantId(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据用户id+租户id+source查询
     *
     * @param uid
     * @param tenantId
     * @param source
     * @author caobotao.cbt
     * @date 2024/8/5 18:56
     */
    UserOauthBind selectByUidAndTenantIdAndSource(@Param("uid") Long uid, @Param("tenantId") Integer tenantId, @Param("source") Integer source);
    
    /**
     * 根据uid+source查询
     *
     * @param uid
     * @param source
     * @author caobotao.cbt
     * @date 2024/8/7 19:21
     */
    List<UserOauthBind> selectByUidAndSource(@Param("uid") Long uid, @Param("source") Integer source);
    
    
    Integer countByThirdIdAndSourceAndTenantId(@Param("thirdId") String thirdId, @Param("source") Integer source, @Param("tenantId") Integer tenantId);
    
    List<UserOauthBind> selectListByUidAndPhoneList(@Param("queryList") List<UserOauthBindListQuery> queryList, @Param("tenantId") Integer tenantId);
}
