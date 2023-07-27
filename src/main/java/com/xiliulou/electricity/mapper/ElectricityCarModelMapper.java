package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.vo.CarModelPullVo;
import com.xiliulou.electricity.vo.ElectricityCarModelSearchVO;
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
     * 根据门店ID集，获取指定数量的数据，已租数量降序
     * @param size 取值数量
     * @param storeIdList 门店ID集
     * @return 车辆型号集
     */
    List<ElectricityCarModelVO> selectByStoreIdListLimit(@Param("size") Integer size, @Param("storeIdList") List<Long> storeIdList);


    /**
     * @return 对象列表
     */
    List<ElectricityCarModelVO> queryList(@Param("query") ElectricityCarModelQuery electricityCarModelQuery);

	Integer queryCount(@Param("query") ElectricityCarModelQuery electricityCarModelQuery);
    
    Integer update(ElectricityCarModel electricityCarModel);

    List<ElectricityCarModel> selectByPage(ElectricityCarModelQuery query);

    List<ElectricityCarModel> selectByQuery(ElectricityCarModelQuery query);
    
    List<CarModelPullVo> queryPull(@Param("size") Long size, @Param("offset") Long offset,
            @Param("franchiseeId") Long franchiseeId, @Param("name") String name,
            @Param("tenantId") Integer tenantId);
    
    ElectricityCarModel queryByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);

    List<Long> selectByStoreIds(@Param("storeIds") List<Long> storeIds);

    List<ElectricityCarModelSearchVO> search(ElectricityCarModelQuery electricityCarModelQuery);
}
