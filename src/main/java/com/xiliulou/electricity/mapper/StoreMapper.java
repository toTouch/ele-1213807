package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.vo.StoreVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门店表(TStore)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
public interface StoreMapper extends BaseMapper<Store> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Store queryById(Integer id);

    /**
     * 查询指定行数据
     */
    IPage queryList(Page page, @Param("query") StoreQuery storeQuery);

    IPage listByFranchisee(Page page, @Param("query") StoreQuery storeQuery);


    /**
     * 修改数据
     *
     * @param store 实例对象
     * @return 影响行数
     */
    int update(Store store);

    List<StoreVO> showInfoByDistance(@Param("query") StoreQuery storeQuery);

    Integer homeTwoTotal(@Param("storeIdList") List<Integer> storeIdList);
}