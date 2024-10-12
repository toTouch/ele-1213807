package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleDeviceCode;
import com.xiliulou.electricity.query.EleDeviceCodeOuterQuery;
import com.xiliulou.electricity.query.EleDeviceCodeQuery;
import com.xiliulou.electricity.query.EleDeviceCodeRegisterQuery;
import com.xiliulou.electricity.vo.EleDeviceCodeVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (EleDeviceCode)
 *
 * @author zhaozhilong
 * @since 2024-10-11 09:27:08
 */
public interface EleDeviceCodeService {
    
    EleDeviceCode queryByIdFromDB(Long id);
    
    EleDeviceCode queryBySnFromDB(String productKey, String deviceName);
    
    EleDeviceCode queryByIdFromCache(Long id);
    
    EleDeviceCode queryBySnFromCache(String productKey, String deviceName);
    
    List<EleDeviceCodeVO> listByPage(EleDeviceCodeQuery eleDeviceCode);
    
    int insert(EleDeviceCode eleDeviceCode);
    
    int updateById(EleDeviceCode eleDeviceCode, String productKey, String deviceName);
    
    int deleteById(Long id, String productKey, String deviceName);
    
    Integer existsDeviceName(String deviceName);
    
    int countByPage(EleDeviceCodeQuery query);
    
    Triple<Boolean, String, Object> save(EleDeviceCodeQuery query);
    
    Triple<Boolean, String, Object> modify(EleDeviceCodeQuery query);
    
    Triple<Boolean, String, Object> delete(Long id);
    
    Triple<Boolean, String, Object> deviceRegister(EleDeviceCodeRegisterQuery query);
    
    Triple<Boolean, String, Object> deviceInfo(EleDeviceCodeOuterQuery query);
    
   List<EleDeviceCode> queryListDeviceInfo(EleDeviceCodeRegisterQuery query);
}
