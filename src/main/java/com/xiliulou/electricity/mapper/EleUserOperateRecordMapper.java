package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.vo.BorrowExpireBatteryVo;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(TEleUserOperateRecord)表数据库访问层
 *
 * @author makejava
 * @since 2022-07-12 14:44:12
 */
public interface EleUserOperateRecordMapper extends BaseMapper<EleUserOperateRecord> {


    List<EleUserOperateRecord> queryList(@Param("uid") Long uid, @Param("size") Long size, @Param("offset") Long offset, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime, @Param("operateModel") Integer operateModel);

    Integer queryCount(@Param("uid") Long uid, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime, @Param("operateModel") Integer operateModel);


}
