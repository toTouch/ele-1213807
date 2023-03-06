package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.FreeDepositRechargeRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.FreeDepositRechargeRecordMapper;
import com.xiliulou.electricity.query.FreeDepositRechargeRecordQuery;
import com.xiliulou.electricity.service.FreeDepositRechargeRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.FreeDepositRechargeRecordVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (FreeDepositRechargeRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-20 15:46:57
 */
@Service("freeDepositRechargeRecordService")
@Slf4j
public class FreeDepositRechargeRecordServiceImpl implements FreeDepositRechargeRecordService {
    @Autowired
    private FreeDepositRechargeRecordMapper freeDepositRechargeRecordMapper;

    @Autowired
    private UserService userService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositRechargeRecord selectByIdFromDB(Long id) {
        return this.freeDepositRechargeRecordMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositRechargeRecord selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<FreeDepositRechargeRecordVO> selectByPage(FreeDepositRechargeRecordQuery query) {
        List<FreeDepositRechargeRecord> freeDepositRechargeRecords = this.freeDepositRechargeRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(freeDepositRechargeRecords)) {
            return Collections.EMPTY_LIST;
        }

        return freeDepositRechargeRecords.stream().map(item -> {
            FreeDepositRechargeRecordVO freeDepositRechargeRecordVO = new FreeDepositRechargeRecordVO();
            BeanUtils.copyProperties(item, freeDepositRechargeRecordVO);
            if (Objects.nonNull(item.getOperator())) {
                User user = userService.queryByUidFromCache(item.getOperator());
                freeDepositRechargeRecordVO.setOperatorName(user.getName());
            }

            return freeDepositRechargeRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(FreeDepositRechargeRecordQuery query) {
        return this.freeDepositRechargeRecordMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreeDepositRechargeRecord insert(FreeDepositRechargeRecord freeDepositRechargeRecord) {
        this.freeDepositRechargeRecordMapper.insertOne(freeDepositRechargeRecord);
        return freeDepositRechargeRecord;
    }

    /**
     * 修改数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FreeDepositRechargeRecord freeDepositRechargeRecord) {
        return this.freeDepositRechargeRecordMapper.update(freeDepositRechargeRecord);

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
        return this.freeDepositRechargeRecordMapper.deleteById(id) > 0;
    }
}
