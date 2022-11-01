package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ReportManagement;
import com.xiliulou.electricity.mapper.ReportManagementMapper;
import com.xiliulou.electricity.service.ReportManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 报表管理(ReportManagement)表服务实现类
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */
@Service("reportManagementService")
@Slf4j
public class ReportManagementServiceImpl implements ReportManagementService {

    @Autowired
    private ReportManagementMapper reportManagementMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ReportManagement selectByIdFromDB(Long id) {
        return this.reportManagementMapper.selectById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ReportManagement selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<ReportManagement> selectByPage(int offset, int limit) {
        return this.reportManagementMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param reportManagement 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportManagement insert(ReportManagement reportManagement) {
        this.reportManagementMapper.insertOne(reportManagement);
        return reportManagement;
    }

    /**
     * 修改数据
     *
     * @param reportManagement 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ReportManagement reportManagement) {
       return this.reportManagementMapper.update(reportManagement);
         
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
        return this.reportManagementMapper.deleteById(id) > 0;
    }
}
