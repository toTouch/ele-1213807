package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaUpgrade;
import com.xiliulou.electricity.query.OtaUpgradeQuery;

import java.util.List;

/**
 * (EleOtaUpgrade)表服务接口
 *
 * @author Hardy
 * @since 2022-10-14 09:02:00
 */
public interface EleOtaUpgradeService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgrade queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgrade queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleOtaUpgrade> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    EleOtaUpgrade insert(EleOtaUpgrade eleOtaUpgrade);
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    Integer update(EleOtaUpgrade eleOtaUpgrade);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    R queryVoList(Long eid);

    void updateEleOtaUpgradeAndSaveHistory(List<Integer> cellNos, Integer eid,  String sessionId);
    
    EleOtaUpgrade queryByEidAndCellNo(Integer eid, Integer cellNo, Integer type);
    
}
