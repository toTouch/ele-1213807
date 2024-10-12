package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleDeviceCode;
import com.xiliulou.electricity.query.EleDeviceCodeQuery;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * (EleDeviceCode)
 *
 * @author zhaozhilong
 * @since 2024-10-11 09:27:08
 */
public interface EleDeviceCodeMapper {
    
    EleDeviceCode selectById(Long id);
    
    List<EleDeviceCode> selectByPage(EleDeviceCodeQuery eleDeviceCode);
    
    int insert(EleDeviceCode eleDeviceCode);
    
    int updateById(EleDeviceCode eleDeviceCode);
    
    int deleteById(Long id);
    
    int countByPage(EleDeviceCodeQuery query);
    
    Integer existsDeviceName(String deviceName);
    
    int batchInsert(List<EleDeviceCode> eleDeviceCodes);
    
    EleDeviceCode selectBySn(@Param("productKey") String productKey, @Param("deviceName") String deviceName);
    
    Triple<Boolean, String, Object> selectListDeviceInfo(@Param("deviceNames") Set<String> deviceNames, @Param("productKey") String productKey);
}

