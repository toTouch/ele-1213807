package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleEsignConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * EsignConfig 数据库访问层
 * @author: Kenneth
 * @Date: 2023/7/7 23:06
 * @Description:
 */
public interface ElectricityEsignConfigMapper {

    EleEsignConfig selectEsignConfigById(Integer id);

    EleEsignConfig selectLatestByTenantId(@Param("tenantId") Integer tenantId);

    List<EleEsignConfig> selectEsignConfigByPage(@Param("offset") int offset, @Param("limit") int limit);

    List<EleEsignConfig> selectEsignConfigByQuery(EleEsignConfig eleEsignConfig);

    int insertEsignConfig(EleEsignConfig eleEsignConfig);

    int updateEsignConfig(EleEsignConfig eleEsignConfig);

    int deleteEsignConfigById(Integer id);

}
