package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;
import com.xiliulou.electricity.mapper.EleOtaUpgradeHistoryMapper;
import com.xiliulou.electricity.service.EleOtaUpgradeHistoryService;
import com.xiliulou.electricity.vo.EleOtaUpgradeHistoryVo;
import com.xiliulou.electricity.vo.PageDataAndCountVo;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleOtaUpgradeHistory)表服务实现类
 *
 * @author Hardy
 * @since 2022-10-14 14:35:40
 */
@Service("eleOtaUpgradeHistoryService")
@Slf4j
public class EleOtaUpgradeHistoryServiceImpl implements EleOtaUpgradeHistoryService {
    
    @Resource
    private EleOtaUpgradeHistoryMapper eleOtaUpgradeHistoryMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgradeHistory queryByIdFromDB(Long id) {
        return this.eleOtaUpgradeHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgradeHistory queryByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<EleOtaUpgradeHistory> queryAllByLimit(int offset, int limit) {
        return this.eleOtaUpgradeHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleOtaUpgradeHistory insert(EleOtaUpgradeHistory eleOtaUpgradeHistory) {
        this.eleOtaUpgradeHistoryMapper.insertOne(eleOtaUpgradeHistory);
        return eleOtaUpgradeHistory;
    }
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleOtaUpgradeHistory eleOtaUpgradeHistory) {
        return this.eleOtaUpgradeHistoryMapper.update(eleOtaUpgradeHistory);
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.eleOtaUpgradeHistoryMapper.deleteById(id) > 0;
    }

    
    @Override
    public R queryList(Integer eid, Integer cellNo, Integer type, String upgradeVersion, String historyVersion,
            String status, Long startTime, Long endTime, Long offset, Long size) {
        List<EleOtaUpgradeHistoryVo> vo = eleOtaUpgradeHistoryMapper
                .queryList(eid, cellNo, type, upgradeVersion, historyVersion, status, startTime, endTime, offset, size);
        Long count = eleOtaUpgradeHistoryMapper
                .queryCount(eid, cellNo, type, upgradeVersion, historyVersion, status, startTime, endTime);
        
        PageDataAndCountVo<List<EleOtaUpgradeHistoryVo>> pageDataAndCountVo = new PageDataAndCountVo<>();
        pageDataAndCountVo.setData(vo);
        pageDataAndCountVo.setCount(count);
        return R.ok(pageDataAndCountVo);
    }

    @Override
    public EleOtaUpgradeHistory queryByCellNoAndSessionId(Integer id, Integer cellNo, String sessionId, Integer type) {
        return eleOtaUpgradeHistoryMapper.queryByCellNoAndSessionId(id, cellNo, sessionId, type);
    }
}
