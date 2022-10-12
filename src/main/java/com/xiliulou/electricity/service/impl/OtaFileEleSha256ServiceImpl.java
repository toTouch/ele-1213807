package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.OtaFileConfig;
import com.xiliulou.electricity.entity.OtaFileEleSha256;
import com.xiliulou.electricity.mapper.OtaFileEleSha256Mapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.OtaFileConfigService;
import com.xiliulou.electricity.service.OtaFileEleSha256Service;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (OtaFileEleSha256)表服务实现类
 *
 * @author zgw
 * @since 2022-10-12 17:31:10
 */
@Service("otaFileEleSha256Service")
@Slf4j
public class OtaFileEleSha256ServiceImpl implements OtaFileEleSha256Service {
    
    @Resource
    private OtaFileEleSha256Mapper otaFileEleSha256Mapper;
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    private OtaFileConfigService otaFileConfigService;
    
    @Autowired
    private EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public OtaFileEleSha256 queryByIdFromDB(Long id) {
        return this.otaFileEleSha256Mapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public OtaFileEleSha256 queryByIdFromCache(Long id) {
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
    public List<OtaFileEleSha256> queryAllByLimit(int offset, int limit) {
        return this.otaFileEleSha256Mapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OtaFileEleSha256 insert(OtaFileEleSha256 otaFileEleSha256) {
        this.otaFileEleSha256Mapper.insertOne(otaFileEleSha256);
        return otaFileEleSha256;
    }
    
    /**
     * 修改数据
     *
     * @param otaFileEleSha256 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(OtaFileEleSha256 otaFileEleSha256) {
        return this.otaFileEleSha256Mapper.update(otaFileEleSha256);
        
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
        return this.otaFileEleSha256Mapper.deleteById(id) > 0;
    }
    
}
