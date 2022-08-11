package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetHysicsOperRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.ElectricityCabinetHysicsOperRecordMapper;
import com.xiliulou.electricity.service.ElectricityCabinetHysicsOperRecordService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetPhysicsOperRecordVo;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * (ElectricityCabinetHysicsOperRecord)表服务实现类
 *
 * @author Hardy
 * @since 2022-08-08 14:42:07
 */
@Service("electricityCabinetHysicsOperRecordService")
@Slf4j
public class ElectricityCabinetHysicsOperRecordServiceImpl implements ElectricityCabinetHysicsOperRecordService {
    @Resource
    private ElectricityCabinetHysicsOperRecordMapper electricityCabinetHysicsOperRecordMapper;
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    @Autowired
    UserTypeFactory userTypeFactory;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetHysicsOperRecord queryByIdFromDB(Long id) {
        return this.electricityCabinetHysicsOperRecordMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ElectricityCabinetHysicsOperRecord queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetHysicsOperRecord> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetHysicsOperRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetHysicsOperRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetHysicsOperRecord insert(ElectricityCabinetHysicsOperRecord electricityCabinetHysicsOperRecord) {
        this.electricityCabinetHysicsOperRecordMapper.insertOne(electricityCabinetHysicsOperRecord);
        return electricityCabinetHysicsOperRecord;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetHysicsOperRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetHysicsOperRecord electricityCabinetHysicsOperRecord) {
       return this.electricityCabinetHysicsOperRecordMapper.update(electricityCabinetHysicsOperRecord);
         
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
        return this.electricityCabinetHysicsOperRecordMapper.deleteById(id) > 0;
    }

    @Override
    public R electricityOperRecordList(Integer size, Integer offset, Integer cupboardId, Integer type, Long beginTime, Long endTime, Integer cellNo) {
        if (size < 0 || size > 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //如果是查全部则直接跳过
        UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
        if (Objects.isNull(userTypeService)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        List<Integer> eleIdList = userTypeService.getEleIdListByUserType(user);
        if (ObjectUtil.isEmpty(eleIdList)) {
            return R.ok();
        }

        List<ElectricityCabinetHysicsOperRecord> cupboardPhysicsOperRecordList = this.electricityCabinetHysicsOperRecordMapper.cupboardOperRecordList(size, offset, eleIdList, type, beginTime, endTime, cellNo);
        if (CollectionUtils.isEmpty(cupboardPhysicsOperRecordList)) {
            return R.ok();
        }

        List<ElectricityCabinetPhysicsOperRecordVo> data = new ArrayList<>();
        cupboardPhysicsOperRecordList.parallelStream().forEachOrdered(item -> {
            ElectricityCabinetPhysicsOperRecordVo vo = new ElectricityCabinetPhysicsOperRecordVo();
            BeanUtils.copyProperties(item, vo);

            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(Objects.isNull(item.getElectricityId()) ? null : item.getElectricityId().intValue());
            if(Objects.nonNull(electricityCabinet)) {
                vo.setName(electricityCabinet.getName());
            }
            data.add(vo);
        });

        return R.ok(data);
    }
}
