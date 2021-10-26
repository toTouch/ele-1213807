package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 换电柜文件表(TElectricityCabinetFile)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
public interface ElectricityCabinetFileMapper extends BaseMapper<ElectricityCabinetFile>{


    void deleteByDeviceInfo(@Param("otherId") Long otherId, @Param("fileType")Integer fileType, @Param("isUseOSS")Integer isUseOSS);
}
