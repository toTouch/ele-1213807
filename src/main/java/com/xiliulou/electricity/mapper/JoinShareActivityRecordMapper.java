package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
public interface JoinShareActivityRecordMapper  extends BaseMapper<JoinShareActivityRecord>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    JoinShareActivityRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<JoinShareActivityRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param joinShareActivityRecord 实例对象
     * @return 对象列表
     */
    List<JoinShareActivityRecord> queryAll(JoinShareActivityRecord joinShareActivityRecord);

    /**
     * 新增数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 影响行数
     */
    int insertOne(JoinShareActivityRecord joinShareActivityRecord);

    /**
     * 修改数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 影响行数
     */
    int update(JoinShareActivityRecord joinShareActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}