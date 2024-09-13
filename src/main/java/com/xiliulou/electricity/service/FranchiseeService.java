package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.FranchiseeSearchVO;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (Franchisee)表服务接口
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
public interface FranchiseeService {
    
    R save(FranchiseeAddAndUpdate franchiseeAddAndUpdate);
    
    R edit(FranchiseeAddAndUpdate franchiseeAddAndUpdate);
    
    R delete(Long id);
    
    Franchisee queryByIdFromDB(Long id);
    
    Franchisee queryByIdFromCache(Long id);
    
    /**
     * 根据id批量查询
     *
     * @param ids
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/22 17:58
     */
    List<Franchisee> queryByIds(List<Long> ids, Integer tenantId);
    
    
    R queryList(FranchiseeQuery franchiseeQuery);
    
    @Deprecated
    R bindElectricityBattery(BindElectricityBatteryQuery bindElectricityBatteryQuery);
    
    R getElectricityBatteryList(Long id);
    
    Franchisee queryByUid(Long uid);
    
    R queryCount(FranchiseeQuery franchiseeQuery);
    
    void deleteByUid(Long uid);
    
    Integer queryByFanchisee(Long uid);
    
    R setSplit(List<FranchiseeSetSplitQuery> franchiseeSetSplitQueryList);
    
    Franchisee queryByElectricityBatteryId(Long id);
    
    R queryByTenantId(Integer tenantId);
    
    R queryByCabinetId(Integer id, Integer cabinetId);
    
    
    Franchisee queryByUserId(Long uid);
    
    Franchisee queryByIdAndTenantId(Long id, Integer tenantId);
    
    Triple<Boolean, String, Object> selectListByQuery(FranchiseeQuery franchiseeQuery);
    
    List<City> selectFranchiseeCityList();
    
    List<Region> selectFranchiseeRegionList(Integer cid);
    
    Triple<Boolean, String, Object> selectFranchiseeByArea(String regionCode);
    
    Triple<Boolean, String, Object> selectFranchiseeByCity(String cityCode);
    
    int update(Franchisee franchisee);
    
    Triple<Boolean, String, Object> moveFranchisee();
    
    List<FranchiseeSearchVO> search(FranchiseeQuery franchiseeQuery);
    
    Integer checkBatteryModelIsUse(Integer batteryModel, Integer tenantId);
    
    Triple<Boolean, String, Object> selectById(Long id);
}
