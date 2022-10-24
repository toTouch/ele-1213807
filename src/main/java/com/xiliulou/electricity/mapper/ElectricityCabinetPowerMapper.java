package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.vo.ElectricityCabinetPowerVo;
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



    @Insert("insert into t_electricity_cabinet_power(eid,same_day_power,sum_power,date,create_time,update_time) " +
            " values(#{eid},#{sameDayPower},#{sumPower},#{date},#{createTime},#{updateTime}) " +
            " on duplicate key update same_day_power=#{sameDayPower},sum_power=#{sumPower},create_time=#{createTime},update_time=#{updateTime}")
    @SelectKey(keyProperty = "id", statement = "select LAST_INSERT_ID()", before = false, resultType = Long.class)
    int insertOrUpdate(ElectricityCabinetPower electricityCabinetPower);

    List<ElectricityCabinetPowerVo> queryList(@Param("query") ElectricityCabinetPowerQuery electricityCabinetPowerQuery);

    ElectricityCabinetPowerVo queryLatestPower(@Param("query") ElectricityCabinetPowerQuery electricityCabinetPowerQuery);
}
