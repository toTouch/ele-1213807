package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;

import java.util.List;
import java.util.Map;

/**
 * 换电柜型号表(TElectricityCarModel)表服务接口
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
public interface ElectricityCarModelService {


      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      ElectricityCarModel queryByIdFromCache(Integer id);

    R save(ElectricityCarModelQuery electricityCarModelQuery);

    R edit(ElectricityCarModelQuery electricityCarModelQuery);

    R delete(Integer id);

    R queryList(ElectricityCarModelQuery electricityCarModelQuery);

	R queryCount(ElectricityCarModelQuery electricityCarModelQuery);
    
    R selectByStoreId(ElectricityCarModelQuery electricityCarModelQuery);

    List<ElectricityCarModel> selectByQuery(ElectricityCarModelQuery query);

    List<ElectricityCarModel> selectByPage(ElectricityCarModelQuery query);

    Map<String,Double> parseRentCarPriceRule(ElectricityCarModel electricityCarModel);

    List<ElectricityCarModelVO> selectList(ElectricityCarModelQuery query);

    ElectricityCarModelVO selectDetailById(Long id);
    
    R queryPull(Long size, Long offset, Long franchiseeId, String name);
}
