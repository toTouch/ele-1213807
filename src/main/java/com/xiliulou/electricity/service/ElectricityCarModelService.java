package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.FranchiseeMoveInfo;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.vo.ElectricityCarModelSearchVO;
import com.xiliulou.electricity.vo.ElectricityCarModelVO;
import org.apache.commons.lang3.tuple.Triple;

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
     * 根据主键ID进行更新
     * @param carModel
     * @return
     */
    boolean updateById(ElectricityCarModel carModel);

    /**
     * 根据门店ID集，获取指定数量的数据，已租数量降序
     * @param size 取值范围
     * @param storeIdList 门店ID集
     * @return 车辆型号集
     */
    List<ElectricityCarModelVO> selectByStoreIdListLimit(Integer size, List<Long> storeIdList);


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
      ElectricityCarModel queryByIdFromCache(Integer id);
    
    Integer insert(ElectricityCarModel electricityCarModel);

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

    Integer updateFranchiseeById(List<ElectricityCarModel> electricityCarModels, Long franchiseeId);

    void moveCarModel(FranchiseeMoveInfo franchiseeMoveInfo);
    
    R queryPull(Long size, Long offset, Long franchiseeId, String name);

    Triple<Boolean, String, Object> acquireUserCarModelInfo();
    
    ElectricityCarModel queryByNameAndStoreId(String name, Long storeId);

    List<Long> selectByStoreIds(List<Long> storeIds);

    List<ElectricityCarModel> selectListByFranchiseeId(Long franchiseeId);

    List<ElectricityCarModelSearchVO> search(ElectricityCarModelQuery electricityCarModelQuery);
}
