package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryMaterial;

import java.util.List;

import com.xiliulou.electricity.query.BatteryMaterialQuery;
import com.xiliulou.electricity.vo.BatteryMaterialSearchVO;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 电池材质(BatteryMaterial)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-11 10:56:47
 */
public interface BatteryMaterialMapper extends BaseMapper<BatteryMaterial> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMaterial queryById(Long id);


    List<BatteryMaterial> selectAllFromDB();

    /**
     * 新增数据
     *
     * @param batteryMaterial 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryMaterial batteryMaterial);

    /**
     * 修改数据
     *
     * @param batteryMaterial 实例对象
     * @return 影响行数
     */
    int update(BatteryMaterial batteryMaterial);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<BatteryMaterial> selectByPage(BatteryMaterialQuery query);

    Integer selectByPageCount(BatteryMaterialQuery query);

    Integer checkExistByType(@Param("type") String type);

    Integer checkExistByKind(@Param("kind") Integer kind);

    List<BatteryMaterialSearchVO> selectBySearch(BatteryMaterialQuery query);
}
