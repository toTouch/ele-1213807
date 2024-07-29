package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.dto.BatteryModelDTO;
import com.xiliulou.electricity.entity.BatteryModel;

import java.util.List;

import com.xiliulou.electricity.query.BatteryModelQuery;
import com.xiliulou.electricity.query.asset.BatteryModelQueryModel;
import com.xiliulou.electricity.vo.BatteryModelPageVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 电池型号(BatteryModel)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-11 10:59:51
 */
public interface BatteryModelMapper extends BaseMapper<BatteryModel> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryModel queryById(Long id);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryModel 实例对象
     * @return 对象列表
     */
    List<BatteryModel> queryAll(BatteryModel batteryModel);

    /**
     * 新增数据
     *
     * @param batteryModel 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryModel batteryModel);

    /**
     * 修改数据
     *
     * @param batteryModel 实例对象
     * @return 影响行数
     */
    int update(BatteryModel batteryModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<BatteryModelPageVO> selectByPage(BatteryModelQuery query);

    Integer selectByPageCount(BatteryModelQuery query);

    Integer checkMidExist(@Param("mid") Long mid);

    Integer batchInsertDefaultBatteryModel(List<BatteryModel> generateDefaultBatteryModel);

    List<String> selectShortBatteryType(@Param("batteryTypes") List<String> batteryTypes, @Param("tenantId") Integer tenantId);
    
    List<BatteryModel> selectListBrandAndModel(BatteryModelQueryModel batteryModelQueryModel);
    
    List<BatteryModel> selectListBatteryModelByBatteryTypeList(@Param("batteryTypeList") List<String> batteryTypeList, @Param("tenantId") Integer tenantId);
    
    List<BatteryModelDTO> selectListShortBatteryTypeByMemberIds(@Param("memberCardIds") List<Long> memberCardIds, @Param("tenantId") Integer tenantId);
}
