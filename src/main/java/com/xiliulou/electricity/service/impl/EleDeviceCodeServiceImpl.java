package com.xiliulou.electricity.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.entity.EleDeviceCode;
import com.xiliulou.electricity.mapper.EleDeviceCodeMapper;
import com.xiliulou.electricity.query.EleDeviceCodeInsertQuery;
import com.xiliulou.electricity.query.EleDeviceCodeOuterQuery;
import com.xiliulou.electricity.query.EleDeviceCodeQuery;
import com.xiliulou.electricity.query.EleDeviceCodeRegisterQuery;
import com.xiliulou.electricity.service.EleDeviceCodeService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.EleDeviceCodeVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (EleDeviceCode)
 *
 * @author zhaozhilong
 * @since 2024-10-11 09:27:08
 */
@Service("eleDeviceCodeService")
public class EleDeviceCodeServiceImpl implements EleDeviceCodeService {
    
    @Resource
    private EleDeviceCodeMapper eleDeviceCodeMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private EleCommonConfig eleCommonConfig;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    
    @Slave
    @Override
    public EleDeviceCode queryByIdFromDB(Long id) {
        return this.eleDeviceCodeMapper.selectById(id);
    }
    
    @Slave
    @Override
    public EleDeviceCode queryBySnFromDB(String productKey, String deviceName) {
        return this.eleDeviceCodeMapper.selectBySn(productKey, deviceName);
    }
    
    @Override
    public EleDeviceCode queryBySnFromCache(String productKey, String deviceName) {
        EleDeviceCode cacheEleDeviceCode = redisService.getWithHash(CacheConstant.CACHE_DEVICE_CODE_SN + productKey + ":" + deviceName, EleDeviceCode.class);
        if (Objects.nonNull(cacheEleDeviceCode)) {
            return cacheEleDeviceCode;
        }
        
        EleDeviceCode eleDeviceCode = this.queryBySnFromDB(productKey, deviceName);
        if (Objects.isNull(eleDeviceCode)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_DEVICE_CODE_SN + eleDeviceCode.getProductKey() + ":" + eleDeviceCode.getDeviceName(), eleDeviceCode);
        return eleDeviceCode;
    }
    
    @Override
    public EleDeviceCode queryByIdFromCache(Long id) {
        EleDeviceCode cacheEleDeviceCode = redisService.getWithHash(CacheConstant.CACHE_DEVICE_CODE + id, EleDeviceCode.class);
        if (Objects.nonNull(cacheEleDeviceCode)) {
            return cacheEleDeviceCode;
        }
        
        EleDeviceCode eleDeviceCode = this.queryByIdFromDB(id);
        if (Objects.isNull(eleDeviceCode)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_DEVICE_CODE + id, eleDeviceCode);
        return eleDeviceCode;
    }
    
    @Override
    public int insert(EleDeviceCode eleDeviceCode) {
        return this.eleDeviceCodeMapper.insert(eleDeviceCode);
    }
    
    @Override
    public int updateById(EleDeviceCode eleDeviceCode, String productKey, String deviceName) {
        int update = this.eleDeviceCodeMapper.updateById(eleDeviceCode);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_DEVICE_CODE + eleDeviceCode.getId());
            redisService.delete(CacheConstant.CACHE_DEVICE_CODE_SN + productKey + ":" + deviceName);
        });
        
        return update;
    }
    
    @Override
    public int deleteById(Long id, String productKey, String deviceName) {
        int delete = this.eleDeviceCodeMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_DEVICE_CODE + id);
            redisService.delete(CacheConstant.CACHE_DEVICE_CODE_SN + productKey + ":" + deviceName);
        });
        
        return delete;
    }
    
    @Slave
    @Override
    public Integer existsDeviceName(String deviceName) {
        return this.eleDeviceCodeMapper.existsDeviceName(deviceName);
    }
    
    //    @Slave
    @Override
    public List<EleDeviceCodeVO> listByPage(EleDeviceCodeQuery eleDeviceCode) {
        List<EleDeviceCode> eleDeviceCodes = this.eleDeviceCodeMapper.selectByPage(eleDeviceCode);
        if (CollectionUtils.isEmpty(eleDeviceCodes)) {
            return Collections.EMPTY_LIST;
        }
        
        return eleDeviceCodes.stream().map(item -> {
            EleDeviceCodeVO eleDeviceCodeVO = new EleDeviceCodeVO();
            BeanUtils.copyProperties(item, eleDeviceCodeVO);
            
            return eleDeviceCodeVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public int countByPage(EleDeviceCodeQuery query) {
        return this.eleDeviceCodeMapper.countByPage(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> save(EleDeviceCodeQuery query) {
        for (EleDeviceCodeInsertQuery entity : query.getDeviceNames()) {
            if (Objects.nonNull(applicationContext.getBean(EleDeviceCodeService.class).existsDeviceName(entity.getDeviceName()))) {
                return Triple.of(false, "100487", "设备编号已存在:" + entity.getDeviceName());
            }
        }
        
        eleDeviceCodeMapper.batchInsert(buildEleDeviceCode(query));
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(EleDeviceCodeQuery query) {
        EleDeviceCode deviceCode = queryByIdFromCache(query.getId());
        if (Objects.isNull(deviceCode)) {
            return Triple.of(false, "100488", "设备不存在");
        }
        
        EleDeviceCode deviceCodeUpdate = new EleDeviceCode();
        deviceCodeUpdate.setId(deviceCode.getId());
        deviceCodeUpdate.setUpdateTime(System.currentTimeMillis());
        deviceCodeUpdate.setRemark(query.getRemark());
        
        updateById(deviceCodeUpdate, deviceCode.getProductKey(), deviceCode.getDeviceName());
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        EleDeviceCode deviceCode = queryByIdFromCache(id);
        if (Objects.isNull(deviceCode)) {
            return Triple.of(false, "100488", "设备不存在");
        }
        
        EleDeviceCode deviceCodeUpdate = new EleDeviceCode();
        deviceCodeUpdate.setId(deviceCode.getId());
        deviceCodeUpdate.setDelFlag(CommonConstant.DEL_Y);
        deviceCodeUpdate.setUpdateTime(System.currentTimeMillis());
        
        updateById(deviceCodeUpdate, deviceCode.getProductKey(), deviceCode.getDeviceName());
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> deviceRegister(EleDeviceCodeRegisterQuery query) {
        for (EleDeviceCodeOuterQuery entity : query.getDeviceNames()) {
            if (Objects.nonNull(applicationContext.getBean(EleDeviceCodeService.class).existsDeviceName(entity.getDeviceName()))) {
                return Triple.of(false, "100487", "设备编号已存在:" + entity.getDeviceName());
            }
        }
        
        eleDeviceCodeMapper.batchInsert(buildEleDeviceCodeList(query));
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> deviceInfo(EleDeviceCodeOuterQuery query) {
        EleDeviceCode deviceCode = this.queryBySnFromCache(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(deviceCode)) {
            return Triple.of(false, "100488", "设备不存在");
        }
        
        return Triple.of(true, null, deviceCode);
    }
    
    @Slave
    @Override
    public List<EleDeviceCode> queryListDeviceInfo(EleDeviceCodeRegisterQuery query) {
        return eleDeviceCodeMapper.selectListDeviceInfo(query.getDeviceNameSet(), query.getProductKey());
    }
    
    private List<EleDeviceCode> buildEleDeviceCode(EleDeviceCodeQuery query) {
        return query.getDeviceNames().stream().map(item -> {
            long time = System.currentTimeMillis();
            EleDeviceCode deviceCode = new EleDeviceCode();
            deviceCode.setProductKey(eleCommonConfig.getProductKey());
            deviceCode.setDeviceName(item.getDeviceName());
            deviceCode.setSecret(SecureUtil.hmacMd5(eleCommonConfig.getProductKey() + item).digestHex(String.valueOf(time)));
            deviceCode.setOnlineStatus(EleCabinetConstant.STATUS_OFFLINE);
            deviceCode.setRemark(item.getRemark());
            deviceCode.setDelFlag(CommonConstant.DEL_N);
            deviceCode.setCreateTime(time);
            deviceCode.setUpdateTime(time);
            return deviceCode;
        }).collect(Collectors.toList());
    }
    
    private List<EleDeviceCode> buildEleDeviceCodeList(EleDeviceCodeRegisterQuery query) {
        return query.getDeviceNames().stream().map(item -> {
            long time = System.currentTimeMillis();
            EleDeviceCode deviceCode = new EleDeviceCode();
            deviceCode.setProductKey(eleCommonConfig.getProductKey());
            deviceCode.setDeviceName(item.getDeviceName());
            deviceCode.setSecret(SecureUtil.hmacMd5(eleCommonConfig.getProductKey() + item).digestHex(String.valueOf(time)));
            deviceCode.setOnlineStatus(EleCabinetConstant.STATUS_OFFLINE);
            deviceCode.setRemark("");
            deviceCode.setDelFlag(CommonConstant.DEL_N);
            deviceCode.setCreateTime(time);
            deviceCode.setUpdateTime(time);
            return deviceCode;
        }).collect(Collectors.toList());
    }
}
