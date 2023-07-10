package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.MemberCardBatteryType;

import java.util.List;

/**
 * (MemberCardBatteryType)表服务接口
 *
 * @author zzlong
 * @since 2023-07-07 14:07:42
 */
public interface MemberCardBatteryTypeService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    MemberCardBatteryType queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    MemberCardBatteryType queryByIdFromCache(Long id);

    /**
     * 修改数据
     *
     * @param memberCardBatteryType 实例对象
     * @return 实例对象
     */
    Integer update(MemberCardBatteryType memberCardBatteryType);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Integer batchInsert(List<MemberCardBatteryType> buildMemberCardBatteryTypeList);
}
