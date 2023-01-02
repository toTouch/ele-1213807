package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ApiReturnOrder;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.web.query.CarGpsQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author : eclair
 * @date : 2023/1/2 09:33
 */
public interface CarAttrMapper extends BaseMapper<CarAttr> {
    
    List<CarAttr> getGpsList(CarGpsQuery carGpsQuery);
}
