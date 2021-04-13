package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import java.util.List;
import java.util.Map;

import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.SelectKey;

/**
 * 换电柜电量表(ElectricityCabinetPower)表数据库访问层
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
public interface ElectricityCabinetPowerMapper  extends BaseMapper<ElectricityCabinetPower>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPower queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetPower> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetPower 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetPower> queryAll(ElectricityCabinetPower electricityCabinetPower);

    /**
     * 新增数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetPower electricityCabinetPower);

    /**
     * 修改数据
     *
     * @param electricityCabinetPower 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetPower electricityCabinetPower);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    @Insert("insert into t_electricity_cabinet_power(eid,same_day_power,sum_power,date,create_time,update_time) " +
            " values(#{eid},#{sameDayPower},#{sumPower},#{date},#{createTime},#{updateTime}) " +
            " on duplicate key update same_day_power=#{sameDayPower},sum_power=#{sumPower},create_time=#{createTime},update_time=#{updateTime}")
    @SelectKey(keyProperty = "id", statement = "select LAST_INSERT_ID()", before = false, resultType = Long.class)
    int insertOrUpdate(ElectricityCabinetPower electricityCabinetPower);

    List<Map> queryList(@Param("query") ElectricityCabinetPowerQuery electricityCabinetPowerQuery);
}