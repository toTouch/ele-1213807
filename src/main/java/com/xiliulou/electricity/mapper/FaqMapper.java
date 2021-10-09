package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.Faq;
import com.xiliulou.electricity.service.FaqService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Faq)表数据库访问层
 *
 *
 * @author makejava
 * @since 2021-09-26 14:06:23
 */
public interface FaqMapper extends BaseMapper<Faq> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Faq queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Faq> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param faq 实例对象
     * @return 对象列表
     */
    List<Faq> queryAll(Faq faq);

    /**
     * 新增数据
     *
     * @param faq 实例对象
     * @return 影响行数
     */
    int insertOne(Faq faq);

    /**
     * 修改数据
     *
     * @param faq 实例对象
     * @return 影响行数
     */
    int update(Faq faq);

    /**
     * 通过主键删除数据
     *
     * @return 影响行数
     */

    List<FaqService> queryList(@Param("size") Integer size, @Param("offset") Integer offset);

    int deleteById(@Param("id") Integer id);

    Integer queryCount();
}
