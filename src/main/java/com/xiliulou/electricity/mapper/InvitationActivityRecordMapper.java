package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.InvitationActivityRecord;

import java.util.List;

import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.vo.InvitationActivityRecordVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (InvitationActivityRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-05 20:17:53
 */
public interface InvitationActivityRecordMapper extends BaseMapper<InvitationActivityRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param invitationActivityRecord 实例对象
     * @return 对象列表
     */
    List<InvitationActivityRecord> queryAll(InvitationActivityRecord invitationActivityRecord);

    /**
     * 新增数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 影响行数
     */
    int insertOne(InvitationActivityRecord invitationActivityRecord);

    /**
     * 修改数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 影响行数
     */
    int update(InvitationActivityRecord invitationActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<InvitationActivityRecordVO> selectByPage(InvitationActivityRecordQuery query);

    Integer selectByPageCount(InvitationActivityRecordQuery query);
}
