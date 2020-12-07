package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Store;
import java.util.List;

import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.vo.StoreVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 门店表(TStore)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
public interface StoreMapper extends BaseMapper<Store>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Store queryById(Integer id);

    /**
     * 查询指定行数据
     *
     */
    List<StoreVO> queryList(@Param("query") StoreQuery storeQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param store 实例对象
     * @return 对象列表
     */
    List<Store> queryAll(Store store);

    /**
     * 新增数据
     *
     * @param store 实例对象
     * @return 影响行数
     */
    int insertOne(Store store);

    /**
     * 修改数据
     *
     * @param store 实例对象
     * @return 影响行数
     */
    int update(Store store);

}