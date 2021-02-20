package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleAuthEntry;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 实名认证资料项(TEleAuthEntry)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
public interface EleAuthEntryMapper extends BaseMapper<EleAuthEntry>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleAuthEntry queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    List<EleAuthEntry> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleAuthEntry 实例对象
     * @return 对象列表
     */
    List<EleAuthEntry> queryAll(EleAuthEntry eleAuthEntry);

    /**
     * 新增数据
     *
     * @param eleAuthEntry 实例对象
     * @return 影响行数
     */
    int insertOne(EleAuthEntry eleAuthEntry);

    /**
     * 修改数据
     *
     * @param eleAuthEntry 实例对象
     * @return 影响行数
     */
    int update(EleAuthEntry eleAuthEntry);

}