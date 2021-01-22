package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Province;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * (Province)表数据库访问层
 *
 * @author makejava
 * @since 2021-01-21 18:05:46
 */
public interface ProvinceMapper  extends BaseMapper<Province>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Province queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Province> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param province 实例对象
     * @return 对象列表
     */
    List<Province> queryAll(Province province);

    /**
     * 新增数据
     *
     * @param province 实例对象
     * @return 影响行数
     */
    int insertOne(Province province);

    /**
     * 修改数据
     *
     * @param province 实例对象
     * @return 影响行数
     */
    int update(Province province);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    @Select(" select id, code, name from electricity.t_province")
	List<Province> queryAllCity();

}