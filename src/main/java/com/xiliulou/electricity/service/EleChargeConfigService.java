package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.EleChargeConfigCalcDetailDto;
import com.xiliulou.electricity.entity.EleChargeConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.query.ChargeConfigQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (EleChargeConfig)表服务接口
 *
 * @author makejava
 * @since 2023-07-18 10:21:40
 */
public interface EleChargeConfigService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleChargeConfig queryByIdFromDb(Long id);

    EleChargeConfig queryFromDb(Long franchiseeId, Long storeId, Long eid, int typeAllCabinet);

    EleChargeConfig queryConfigByStoreIdFromCache(Long storeId, Long id);

    EleChargeConfig queryConfigByFranchiseeIdFromCache(Long franchiseeId);

    EleChargeConfig queryConfigByTenantIdFromCache(Integer tenantId);
     EleChargeConfig queryByTenantIdFromDb(Integer tenantId);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleChargeConfig queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleChargeConfig> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param eleChargeConfig 实例对象
     * @return 实例对象
     */
    EleChargeConfig insert(EleChargeConfig eleChargeConfig);

    /**
     * 修改数据
     *
     * @param eleChargeConfig 实例对象
     * @param config
     * @return 实例对象
     */
    Integer update(EleChargeConfig eleChargeConfig, EleChargeConfig config);

    /**
     * 通过主键删除数据
     *
     * @param id     主键
     * @param config
     * @return 是否成功
     */
    Boolean deleteById(Long id, EleChargeConfig config);

    Pair<Boolean, Object> queryList(ChargeConfigListQuery chargeConfigListQuery);

    Pair<Boolean, Object> saveConfig(ChargeConfigQuery chargeConfigQuery);

    Pair<Boolean, Object> modifyConfig(ChargeConfigQuery chargeConfigQuery);

    Pair<Boolean, Object> delConfig(Long id);

    EleChargeConfig queryConfigByCabinetWithLayer(ElectricityCabinet electricityCabinet, Long id);


    EleChargeConfigCalcDetailDto acquireConfigTypeAndUnitPriceAccrodingTime(EleChargeConfig eleChargeConfig, Long createTime);
}
