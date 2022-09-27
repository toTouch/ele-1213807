package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetServer;
import com.xiliulou.electricity.entity.ElectricityCabinetServerOperRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.ElectricityCabinetServerMapper;
import com.xiliulou.electricity.service.ElectricityCabinetServerOperRecordService;
import com.xiliulou.electricity.service.ElectricityCabinetServerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * (ElectricityCabinetServer)表服务实现类
 *
 * @author zgw
 * @since 2022-09-26 11:40:35
 */
@Service("electricityCabinetServerService") @Slf4j public class ElectricityCabinetServerServiceImpl
    implements ElectricityCabinetServerService {
    @Resource private ElectricityCabinetServerMapper electricityCabinetServerMapper;

    @Autowired private ElectricityCabinetService electricityCabinetService;

    @Autowired private UserService userService;

    @Autowired private ElectricityCabinetServerOperRecordService electricityCabinetServerOperRecordService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override public ElectricityCabinetServer queryByIdFromDB(Long id) {
        return this.electricityCabinetServerMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override public ElectricityCabinetServer queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override public List<ElectricityCabinetServer> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetServerMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 实例对象
     */
    @Override @Transactional(rollbackFor = Exception.class) public ElectricityCabinetServer insert(
        ElectricityCabinetServer electricityCabinetServer) {
        this.electricityCabinetServerMapper.insertOne(electricityCabinetServer);
        return electricityCabinetServer;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 实例对象
     */
    @Override @Transactional(rollbackFor = Exception.class) public Integer update(
        ElectricityCabinetServer electricityCabinetServer) {
        return this.electricityCabinetServerMapper.update(electricityCabinetServer);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override @Transactional(rollbackFor = Exception.class) public Boolean deleteById(Long id) {
        return this.electricityCabinetServerMapper.deleteById(id) > 0;
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetServer queryByProductKeyAndDeviceName(String productKey, String deviceName) {
        return this.electricityCabinetServerMapper.queryByProductKeyAndDeviceName(productKey, deviceName);
    }

    @Override
    public R queryList(String eleName, String deviceName, String tenantName, Long serverTimeStart, Long serverTimeEnd,
        Long offset, Long size) {
        if (Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("ELECTRICITY.006", "用户权限不足");
        }

        List<ElectricityCabinetServer> data = electricityCabinetServerMapper
            .queryList(eleName, deviceName, tenantName, serverTimeStart, serverTimeEnd, offset, size);
        return R.ok(data);
    }

    @Override @Transactional(rollbackFor = Exception.class) public R deleteOne(Long id) {
        if (Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("ELECTRICITY.006", "用户权限不足");
        }

        ElectricityCabinetServer electricityCabinetServer = queryByIdFromDB(id);
        if (Objects.isNull(electricityCabinetServer)) {
            return R.fail("100228", "未找到电柜服务信息");
        }

        ElectricityCabinet electricityCabinet =
            electricityCabinetService.queryByIdFromCache(electricityCabinetServer.getElectricityCabinetId());
        if (Objects.nonNull(electricityCabinet) && Objects
            .equals(electricityCabinet.getDelFlag(), ElectricityCabinet.DEL_NORMAL)) {
            return R.fail("100229", "电柜服务信息还有绑定电柜，无法删除");
        }

        electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_DEL);
        this.update(electricityCabinetServer);
        return R.ok();
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public R updateOne(Long id, Long serverTimeStart, Long serverTimeEnd) {
        if (Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("ELECTRICITY.006", "用户权限不足");
        }

        User user = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityCabinetServer electricityCabinetServer = this.queryByIdFromDB(id);
        if (Objects.isNull(electricityCabinetServer)) {
            return R.fail("100228", "未找到电柜服务信息");
        }

        ElectricityCabinetServer update = new ElectricityCabinetServer();
        update.setId(electricityCabinetServer.getId());
        update.setServerBeginTime(serverTimeStart);
        update.setServerEndTime(serverTimeEnd);

        //记录修改日志
        DbUtils.dbOperateSuccessThen(update(update), () -> {
            ElectricityCabinetServerOperRecord insert = new ElectricityCabinetServerOperRecord();
            insert.setCreateUid(user.getUid());
            insert.setEleServerId(electricityCabinetServer.getId());
            insert.setOldServerBeginTime(electricityCabinetServer.getServerBeginTime());
            insert.setOldServerEndTime(electricityCabinetServer.getServerEndTime());
            insert.setNewServerBeginTime(serverTimeStart);
            insert.setNewServerEndTime(serverTimeEnd);
            electricityCabinetServerOperRecordService.insert(insert);

            return null;
        });
        return R.ok();
    }

    @Override public void insertOrUpdateByElectricityCabinet(ElectricityCabinet electricityCabinet,
        ElectricityCabinet oldElectricityCabinet) {
        ElectricityCabinetServer electricityCabinetServer =
            queryByProductKeyAndDeviceName(oldElectricityCabinet.getProductKey(),
                oldElectricityCabinet.getDeviceName());
        if (Objects.nonNull(electricityCabinetServer)) {
            electricityCabinetServer.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetServer.setProductKey(electricityCabinet.getProductKey());
            electricityCabinetServer.setDeviceName(electricityCabinet.getDeviceName());
            electricityCabinetServer.setTenantId(electricityCabinet.getTenantId());
            electricityCabinetServer.setUpdateTime(System.currentTimeMillis());
            this.update(electricityCabinetServer);
            return;
        }

        electricityCabinetServer = new ElectricityCabinetServer();
        electricityCabinetServer.setElectricityCabinetId(electricityCabinet.getId());
        electricityCabinetServer.setProductKey(electricityCabinet.getProductKey());
        electricityCabinetServer.setDeviceName(electricityCabinet.getDeviceName());
        electricityCabinetServer.setTenantId(electricityCabinet.getTenantId());
        electricityCabinetServer.setServerBeginTime(electricityCabinet.getCreateTime());
        electricityCabinetServer.setServerEndTime(electricityCabinet.getCreateTime() + 24L * 3600000 * 365);
        electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_NORMAL);
        electricityCabinetServer.setCreateTime(System.currentTimeMillis());
        electricityCabinetServer.setUpdateTime(System.currentTimeMillis());

        this.insert(electricityCabinetServer);
    }
}
