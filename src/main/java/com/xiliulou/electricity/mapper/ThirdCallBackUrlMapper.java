package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ThirdCallBackUrl)表数据库访问层
 *
 * @author makejava
 * @since 2021-11-10 15:25:19
 */
public interface ThirdCallBackUrlMapper  extends BaseMapper<ThirdCallBackUrl>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdCallBackUrl queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ThirdCallBackUrl> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param thirdCallBackUrl 实例对象
     * @return 对象列表
     */
    List<ThirdCallBackUrl> queryAll(ThirdCallBackUrl thirdCallBackUrl);

    /**
     * 新增数据
     *
     * @param thirdCallBackUrl 实例对象
     * @return 影响行数
     */
    int insertOne(ThirdCallBackUrl thirdCallBackUrl);

    /**
     * 修改数据
     *
     * @param thirdCallBackUrl 实例对象
     * @return 影响行数
     */
    int update(ThirdCallBackUrl thirdCallBackUrl);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    ThirdCallBackUrl queryByTenantId(@Param("tenantId") Integer id);
}
