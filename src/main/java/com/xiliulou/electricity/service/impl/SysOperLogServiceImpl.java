package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.SysOperLog;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.SysOperLogMapper;
import com.xiliulou.electricity.query.SysOperLogQuery;
import com.xiliulou.electricity.service.SysOperLogService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.SysOperLogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作日志记录(SysOperLog)表服务实现类
 *
 * @author zzlong
 * @since 2022-10-11 19:47:27
 */
@Service("sysOperLogService")
@Slf4j
public class SysOperLogServiceImpl implements SysOperLogService {
    
    @Autowired
    private SysOperLogMapper sysOperLogMapper;
    
    @Autowired
    private UserService userService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public SysOperLog selectByIdFromDB(Long id) {
        return this.sysOperLogMapper.selectById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public SysOperLog selectByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<SysOperLogVO> selectByPage(SysOperLogQuery sysOperLogQuery) {
        List<SysOperLog> sysOperLogs = this.sysOperLogMapper.selectByPage(sysOperLogQuery);
        if (CollectionUtils.isEmpty(sysOperLogs)) {
            return Collections.EMPTY_LIST;
        }
        
        List<SysOperLogVO> sysOperLogVOList = sysOperLogs.parallelStream().map(item -> {
            SysOperLogVO sysOperLogVO = new SysOperLogVO();
            BeanUtils.copyProperties(item, sysOperLogVO);

            User user = userService.queryByUidFromCache(item.getOperatorUid());
            if (Objects.nonNull(user)) {
                sysOperLogVO.setOperatorUserName(user.getName());
            }
            return sysOperLogVO;
        }).collect(Collectors.toList());
        
        return sysOperLogVOList;
    }
    
    @Override
    public int pageCount(SysOperLogQuery sysOperLogQuery) {
        return this.sysOperLogMapper.pageCount(sysOperLogQuery);
    }
    
    /**
     * 新增数据
     *
     * @param sysOperLog 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysOperLog insert(SysOperLog sysOperLog) {
        this.sysOperLogMapper.insertOne(sysOperLog);
        return sysOperLog;
    }
    
    /**
     * 修改数据
     *
     * @param sysOperLog 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(SysOperLog sysOperLog) {
        return this.sysOperLogMapper.update(sysOperLog);
        
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
        return this.sysOperLogMapper.deleteById(id) > 0;
    }
}
