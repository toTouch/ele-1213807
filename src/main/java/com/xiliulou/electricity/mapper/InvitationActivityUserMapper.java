package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.InvitationActivityUser;

import java.util.List;

import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.vo.InvitationActivityUserVO;
import com.xiliulou.electricity.vo.UserInfoVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (InvitationActivityUser)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-05 16:11:08
 */
public interface InvitationActivityUserMapper extends BaseMapper<InvitationActivityUser> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityUser queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityUser> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivityUser 实例对象
     * @return 对象列表
     */
    List<InvitationActivityUser> queryAll(InvitationActivityUser invitationActivityUser);

    /**
     * 新增数据
     *
     * @param invitationActivityUser 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivityUser invitationActivityUser);

    /**
     * 修改数据
     *
     * @param invitationActivityUser 实例对象
     * @return 影响行数
     */
    int update(InvitationActivityUser invitationActivityUser);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<InvitationActivityUserVO> selectByPage(InvitationActivityUserQuery query);

    Integer selectByPageCount(InvitationActivityUserQuery query);
}
