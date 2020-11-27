/*
package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

*/
/**
 * 换电柜文件表(TElectricityCabinetFile)表服务接口
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 *//*

public interface ElectricityCabinetFileService {

    */
/**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     *//*

    ElectricityCabinetFile queryByIdFromDB(Long id);
    
      */
/**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     *//*

    ElectricityCabinetFile queryByIdFromCache(Long id);

    */
/**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     *//*

    List<ElectricityCabinetFile> queryAllByLimit(int offset, int limit);

    */
/**
     * 新增数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     *//*

    ElectricityCabinetFile insert(ElectricityCabinetFile electricityCabinetFile);

    */
/**
     * 修改数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     *//*

    Integer update(ElectricityCabinetFile electricityCabinetFile);

    */
/**
     * 通过主键删除数据
     *
     *//*

    R deleteById(Long id);

    List<ElectricityCabinetFile> queryByDeviceInfo(Integer electricityCabinetId, Integer fileType);

    void getMinioFile(String fileName, HttpServletResponse response);
}*/
