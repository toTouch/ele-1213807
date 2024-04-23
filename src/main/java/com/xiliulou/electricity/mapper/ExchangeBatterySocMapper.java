package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ExchangeBatterySoc;
import org.apache.ibatis.annotations.Param;

/**
 * 换电电池soc(ExchangeBatterySoc)表数据库访问层
 *
 * @author makejava
 * @since 2024-04-10 10:06:36
 */
public interface ExchangeBatterySocMapper {
    
    /**
     * 通过ID查询单条数据
     *
     * @return 实例对象
     */
    ExchangeBatterySoc selectOneByUidAndSn(@Param("sn") String sn);
    
    /**
     * 新增数据
     *
     * @param exchangeBatterySoc 实例对象
     * @return 影响行数
     */
    int insertOne(ExchangeBatterySoc exchangeBatterySoc);
    
    /**
     * 修改数据
     *
     * @param exchangeBatterySoc 实例对象
     * @return 影响行数
     */
    int update(ExchangeBatterySoc exchangeBatterySoc);
    
    /**
     * 通过主键逻辑删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int removeById(Long id);
    
    
}
