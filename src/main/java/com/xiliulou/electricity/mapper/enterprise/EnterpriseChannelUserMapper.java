package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;

import java.util.List;

import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 企业渠道邀请用户表(EnterpriseChannelUser)表数据库访问层
 *
 * @author baoyu
 * @since 2023-09-14 10:18:18
 */
public interface EnterpriseChannelUserMapper extends BaseMapper<EnterpriseChannelUser> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseChannelUser queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EnterpriseChannelUser> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param enterpriseChannelUser 实例对象
     * @return 对象列表
     */
    List<EnterpriseChannelUser> queryAll(EnterpriseChannelUser enterpriseChannelUser);

    /**
     * 新增数据
     *
     * @param enterpriseChannelUser 实例对象
     * @return 影响行数
     */
    int insertOne(EnterpriseChannelUser enterpriseChannelUser);

    /**
     * 修改数据
     *
     * @param enterpriseChannelUser 实例对象
     * @return 影响行数
     */
    int update(EnterpriseChannelUser enterpriseChannelUser);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 查询未关联到企业渠道用户的记录(已生成邀请二维码，但是还没有用户扫码加入)
     * @param enterpriseId
     * @param franchiseeId
     * @param tenantId
     * @return
     */
    EnterpriseChannelUser selectUnusedChannelUser(@Param("enterpriseId") Long enterpriseId, @Param("tenantId") Long tenantId);

    /**
     * 查询已关联到企业的用户
     * @param uid
     * @param tenantId
     * @return
     */
    EnterpriseChannelUser selectUsedChannelUser(@Param("uid") Long uid,  @Param("tenantId") Long tenantId);

    /**
     * 根据ID和UID查询已加入企业的用户
     * @param id
     * @param uid
     * @return
     */
    EnterpriseChannelUser selectChannelUserByIdAndUid(@Param("id")Long id, @Param("uid") Long uid);


    EnterpriseChannelUser selectChannelUserByEnterpriseIdAndUid(@Param("enterpriseId")Long enterpriseId, @Param("uid") Long uid);
    
    EnterpriseChannelUser selectByUid(Long uid);
    
    EnterpriseChannelUserVO selectUserRelatedEnterprise(Long uid);
    
    Integer updateChannelUser(EnterpriseChannelUserQuery enterpriseChannelUserQuery);
}
