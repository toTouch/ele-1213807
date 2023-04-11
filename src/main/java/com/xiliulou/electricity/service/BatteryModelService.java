package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.vo.BatteryModelVO;
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
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryModel queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     */
    List<BatteryModel> queryByTenantIdFromCache(Integer tenantId);


    List<BatteryModel> queryByTenantIdFromDB(Integer tenantId);


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
    Boolean deleteById(Long id);

    List<BatteryModelVO> selectBatteryModels(Integer tenantId);

    List<BatteryModel> selectByPage(BatteryModelQuery query);

    Integer selectByPageCount(BatteryModelQuery query);

    Triple<Boolean, String, Object> save(BatteryModelQuery batteryModelQuery);

    Triple<Boolean, String, Object> modify(BatteryModelQuery batteryModelQuery);

    Triple<Boolean, String, Object> delete(Long id);

    Integer checkMidExist(Long mid);

    Integer batchInsertDefaultBatteryModel(List<BatteryModel> generateDefaultBatteryModel);
}
