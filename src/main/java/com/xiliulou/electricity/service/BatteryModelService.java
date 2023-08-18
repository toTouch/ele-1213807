package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.vo.BatteryModelAndMaterialVO;
import com.xiliulou.electricity.vo.BatteryModelPageVO;
import com.xiliulou.electricity.vo.BatteryTypeVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 电池型号(BatteryModel)表服务接口
 *
 * @author zzlong
 * @since 2023-04-11 10:59:51
 */
public interface BatteryModelService {

    /**
     * 根据电池型号集查询数据
     * @param tenantId 租户ID
     * @param idList 电池型号ID集
     * @return 电池型号集
     */
    List<BatteryModel> selectByIds(Integer tenantId, List<Long> idList);

    /**
     * 根据电池型号集查询数据
     * @param tenantId 租户ID
     * @param batteryTypes 电池型号集
     * @return 电池型号集
     */
    List<BatteryModel> selectByBatteryTypes(Integer tenantId, List<String> batteryTypes);

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryModel queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     */
    List<BatteryModel> queryByTenantIdFromCache(Integer tenantId);

    List<BatteryModel> queryByTenantIdFromDB(Integer tenantId);

    List<BatteryModel> selectByTenantIdFromDB(Integer tenantId);
    /**
     * 新增数据
     *
     * @param batteryModel 实例对象
     * @return 实例对象
     */
    BatteryModel insert(BatteryModel batteryModel);

    /**
     * 修改数据
     *
     * @param batteryModel 实例对象
     * @return 实例对象
     */
    Integer update(BatteryModel batteryModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteById(Long id);

    BatteryModelAndMaterialVO selectBatteryModels(Integer tenantId);

    List<BatteryModelPageVO> selectByPage(BatteryModelQuery query);

    Integer selectByPageCount(BatteryModelQuery query);

    Triple<Boolean, String, Object> save(BatteryModelQuery batteryModelQuery);

    Triple<Boolean, String, Object> modify(BatteryModelQuery batteryModelQuery);

    Triple<Boolean, String, Object> delete(Long id);

    Integer checkMidExist(Long mid);

    Integer batchInsertDefaultBatteryModel(List<BatteryModel> generateDefaultBatteryModel);

    String acquireBatteryShort(Integer batteryModel, Integer tenantId);

    Integer acquireBatteryModel(String type, Integer tenantId);

    String acquireBatteryShortType(String batteryType, Integer tenantId);

    String analysisBatteryTypeByBatteryName(String batteryName);

    List<BatteryModel> selectCustomizeBatteryType(BatteryModelQuery query);

    List<BatteryTypeVO> selectBatteryTypeAll();
    List<BatteryTypeVO> selectBatteryTypeAll(Integer tenantId);

    List<String> selectBatteryVAll();

    List<String> transformShortBatteryType(List<BatteryTypeVO> batteryModels, List<String> batteryTypes);

    List<String> selectShortBatteryType(List<String> batteryTypes, Integer tenantId);
}
