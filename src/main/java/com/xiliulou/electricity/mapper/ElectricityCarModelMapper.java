package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜型号表(TElectricityCarModel)表数据库访问层
 *
 * @author makejava
 * @since 2022-06-06 11:01:04
 */
public interface ElectricityCarModelMapper extends BaseMapper<ElectricityCarModel> {


    /**
     * @return 对象列表
     */
    List<ElectricityCarModelVO> queryList(@Param("query") ElectricityCarModelQuery electricityCarModelQuery);

	Integer queryCount(@Param("query") ElectricityCarModelQuery electricityCarModelQuery);
}
