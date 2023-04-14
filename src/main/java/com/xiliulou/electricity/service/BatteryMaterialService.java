package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMaterial;
import com.xiliulou.electricity.query.BatteryMaterialQuery;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 电池材质(BatteryMaterial)表服务接口
 *
 * @author zzlong
 * @since 2023-04-11 10:56:47
 */
public interface BatteryMaterialService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMaterial queryByIdFromDB(Long id);

    List<BatteryMaterial> selectAllFromCache();

    List<BatteryMaterial> selectAllFromDB();

    /**
     * 新增数据
     *
     * @param batteryMaterial 实例对象
     * @return 实例对象
     */
    BatteryMaterial insert(BatteryMaterial batteryMaterial);

    /**
     * 修改数据
     *
     * @param batteryMaterial 实例对象
     * @return 实例对象
     */
    Integer update(BatteryMaterial batteryMaterial);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteById(Long id);

    List<BatteryMaterial> selectByPage(BatteryMaterialQuery query);

    Integer selectByPageCount(BatteryMaterialQuery query);

    Triple<Boolean, String, Object> save(BatteryMaterialQuery batteryMaterialQuery);

    Integer checkExistByName(String name);

    Triple<Boolean, String, Object> delete(Long id);

    List<SearchVo> selectBySearch(BatteryMaterialQuery query);
}
