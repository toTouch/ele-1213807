package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivityRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 发起邀请活动记录(ShareActivityRecord)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
public interface ShareActivityRecordMapper  extends BaseMapper<ShareActivityRecord>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ShareActivityRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param shareActivityRecord 实例对象
     * @return 对象列表
     */
    List<ShareActivityRecord> queryAll(ShareActivityRecord shareActivityRecord);

    /**
     * 新增数据
     *
     * @param shareActivityRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ShareActivityRecord shareActivityRecord);

    /**
     * 修改数据
     *
     * @param shareActivityRecord 实例对象
     * @return 影响行数
     */
    int update(ShareActivityRecord shareActivityRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}