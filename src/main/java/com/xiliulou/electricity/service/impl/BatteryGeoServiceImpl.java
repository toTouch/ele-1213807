package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BatteryGeo;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.BatteryGeoMapper;
import com.xiliulou.electricity.service.BatteryGeoService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.GeoHashUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (BatteryGeo)表服务实现类
 *
 * @author makejava
 * @since 2023-03-03 08:54:46
 */
@Service("batteryGeoService")
@Slf4j
public class BatteryGeoServiceImpl implements BatteryGeoService {
    
    @Resource
    private BatteryGeoMapper batteryGeoMapper;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryGeo queryByIdFromDB(Long id) {
        return this.batteryGeoMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryGeo queryByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<BatteryGeo> queryAllByLimit(int offset, int limit) {
        return this.batteryGeoMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param batteryGeo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryGeo insertOrUpdate(BatteryGeo batteryGeo) {
        this.batteryGeoMapper.insertOrUpdate(batteryGeo);
        return batteryGeo;
    }
    
    /**
     * 修改数据
     *
     * @param batteryGeo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryGeo batteryGeo) {
        return this.batteryGeoMapper.update(batteryGeo);
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.batteryGeoMapper.deleteById(id) > 0;
    }
    
    @Override
    public Triple<Boolean, String, Object> queryBatteryMap(Double lat, Double lon, Long size, Integer length) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return Triple.of(true, null, Collections.emptyList());
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return Triple.of(true, null, Collections.emptyList());
        }
        
        return Triple.of(true, null, batteryGeoMapper.queryAllList(franchiseeIds,GeoHashUtil.getGeoHashBase32For9(lat, lon, length),
                TenantContextHolder.getTenantId(), lat, lon, size));
    }
    
    @Override
    public int deleteBySn(String sn) {
        return batteryGeoMapper.deleteBySn(sn);
    }
}
