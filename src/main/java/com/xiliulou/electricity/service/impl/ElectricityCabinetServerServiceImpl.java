package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.cabinet.ElectricityCabinetServerBO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCabinetServerMapper;
import com.xiliulou.electricity.request.cabinet.ElectricityCabinetServerUpdateRequest;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetServerTimeAddResultVO;
import com.xiliulou.electricity.vo.ElectricityCabinetServerVo;
import com.xiliulou.electricity.vo.PageDataAndCountVo;

import java.util.*;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (ElectricityCabinetServer)表服务实现类
 *
 * @author zgw
 * @since 2022-09-26 11:40:35
 */
@Service("electricityCabinetServerService")
@Slf4j
public class ElectricityCabinetServerServiceImpl
        implements ElectricityCabinetServerService, DisposableBean {
    @Resource
    private ElectricityCabinetServerMapper electricityCabinetServerMapper;

    @Autowired
    private ElectricityCabinetService electricityCabinetService;

    @Autowired
    private UserService userService;

    @Autowired
    private ElectricityCabinetServerOperRecordService electricityCabinetServerOperRecordService;

    @Autowired
    private TenantService tenantService;

    @Resource
    private RedisService redisService;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    private final TtlXllThreadPoolExecutorServiceWrapper threadPool = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("CABINET-SERVER-THREAD-POOL", 10, "cabinetServerThread:"));

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetServer queryByIdFromDB(Long id) {
        return this.electricityCabinetServerMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetServer queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetServer> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetServerMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetServer 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetServer insert(
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(
            ElectricityCabinetServer electricityCabinetServer) {
        return this.electricityCabinetServerMapper.update(electricityCabinetServer);

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
        return this.electricityCabinetServerMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetServer queryByProductKeyAndDeviceName(String productKey, String deviceName) {
        return this.electricityCabinetServerMapper.queryByProductKeyAndDeviceName(productKey, deviceName);
    }

    @Slave
    @Override
    public R queryList(String eleName, String deviceName, String tenantName, Long serverTimeStart, Long serverTimeEnd,
                       Long offset, Long size) {
        if (!Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("ELECTRICITY.006", "用户权限不足");
        }

        List<ElectricityCabinetServerVo> result = new ArrayList<>();

        List<ElectricityCabinetServer> data = electricityCabinetServerMapper
                .queryList(eleName, deviceName, tenantName, serverTimeStart, serverTimeEnd, offset, size);
        if (DataUtil.collectionIsUsable(data)) {
            data.forEach(item -> {
                ElectricityCabinetServerVo vo = new ElectricityCabinetServerVo();
                BeanUtils.copyProperties(item, vo);

                ElectricityCabinet electricityCabinet =
                        electricityCabinetService.queryByIdFromCache(item.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    vo.setEleName(electricityCabinet.getName());
                }

                Tenant tenant = tenantService.queryByIdFromCache(item.getTenantId());
                if (Objects.nonNull(tenant)) {
                    vo.setTenantName(tenant.getName());
                }

                result.add(vo);
            });
        }

        Long count =
                electricityCabinetServerMapper.queryCount(eleName, deviceName, tenantName, serverTimeStart, serverTimeEnd);
        return R.ok(new PageDataAndCountVo<>(result, count));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteOne(Long id) {
        if (!Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateOne(Long id, Long serverTimeStart, Long serverTimeEnd) {
        if (!Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
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
            insert.setCreateTime(System.currentTimeMillis());
            electricityCabinetServerOperRecordService.insert(insert);

            return null;
        });
        return R.ok();
    }

    @Override
    public void insertOrUpdateByElectricityCabinet(ElectricityCabinet electricityCabinet,
                                                   ElectricityCabinet oldElectricityCabinet) {
        ElectricityCabinetServer electricityCabinetServer =
                queryByProductKeyAndDeviceName(oldElectricityCabinet.getProductKey(),
                        oldElectricityCabinet.getDeviceName());
        if (Objects.nonNull(electricityCabinetServer)) {
            electricityCabinetServer.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetServer.setProductKey(electricityCabinet.getProductKey());
            electricityCabinetServer.setDeviceName(electricityCabinet.getDeviceName());
            electricityCabinetServer.setTenantId(electricityCabinet.getTenantId());
            electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_NORMAL);
            electricityCabinetServer.setUpdateTime(System.currentTimeMillis());
            this.update(electricityCabinetServer);
            return;
        }

        electricityCabinetServer = queryByProductKeyAndDeviceName(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (Objects.nonNull(electricityCabinetServer)) {
            electricityCabinetServer.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetServer.setTenantId(electricityCabinet.getTenantId());
            electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_NORMAL);
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
        electricityCabinetServer.setServerEndTime(Objects.isNull(electricityCabinet.getCreateTime()) ? 0
                : electricityCabinet.getCreateTime() + 24L * 3600000 * 395);  //395 ==> 365 + 30  一年零一月
        electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_NORMAL);
        electricityCabinetServer.setCreateTime(System.currentTimeMillis());
        electricityCabinetServer.setUpdateTime(System.currentTimeMillis());

        this.insert(electricityCabinetServer);
    }

    @Slave
    @Override
    public ElectricityCabinetServer selectByEid(Integer id) {
        return this.electricityCabinetServerMapper.selectByEid(id);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetServer> listCabinetServerByEids(List<Integer> electricityCabinetIdList) {
        return this.electricityCabinetServerMapper.selectListByEids(electricityCabinetIdList);
    }

    @Override
    public R addServerEndTime(ElectricityCabinetServerUpdateRequest request) {
        log.info("add cabinet server end time info! request:{}", request);

        if (Objects.isNull(request.getTenantId()) && ObjectUtils.isEmpty(request.getCabinetSnList())) {
            return R.fail("请求参数错误!");
        }

        // 判断租户是否存在
        if (Objects.nonNull(request.getTenantId())) {
            Tenant tenant = tenantService.queryByIdFromCache(request.getTenantId());
            if (Objects.isNull(tenant)) {
                return R.fail("ELECTRICITY.00101", "找不到租户!");
            }
        }

        TtlTraceIdSupport.set();

        try {
            // 根据柜机sn处理服务时间
            if (ObjectUtils.isNotEmpty(request.getCabinetSnList())) {
                return dealWithCabinetSnList(request);
            }

            // 根据租户id处理柜机服务时间
            dealWithTenantId(request);
        } catch (Exception e) {
            log.error("add cabinet server end time error!", e);
        } finally {
            TtlTraceIdSupport.clear();
        }

        return R.ok();
    }

    private void dealWithTenantId(ElectricityCabinetServerUpdateRequest request) {
        Long maxId = 0L;
        Integer size = 500;
        long updateTime = System.currentTimeMillis();

        while (true) {
            // 查询柜机的服务时间
            List<ElectricityCabinetServerBO> electricityCabinetServerList = electricityCabinetServerMapper.listByTenantId(request.getTenantId(), null, maxId, size);
            if (ObjectUtils.isEmpty(electricityCabinetServerList)) {
                break;
            }

            maxId = electricityCabinetServerList.get(electricityCabinetServerList.size() - 1).getCabinetId();

            electricityCabinetServerList.stream().forEach(item -> {
                threadPool.execute(() -> {
                    if (Objects.isNull(item.getServerEndTime())) {
                        return;
                    }

                    long serverEndTime = DateUtils.getAfterYear(item.getServerEndTime(), request.getYearNum());
                    // 修改柜机的服务时间
                    electricityCabinetServerMapper.updateServerEndTime(item.getCabinetServerId(), serverEndTime, updateTime);
                });
            });
        }

        log.info("add cabinet server end time success! tenantId:{}", request.getTenantId());
    }

    private R dealWithCabinetSnList(ElectricityCabinetServerUpdateRequest request) {
        List<String> cabinetSnList = new ArrayList<>(request.getCabinetSnList());
        List<String> repeatSnList = new ArrayList<>();
        List<String> notFindSnList = new ArrayList<>();
        Long updateTime = System.currentTimeMillis();
        List<ElectricityCabinetServer> cabinetServerList = new ArrayList<>();

        // 初始化柜机信息
        List<ElectricityCabinetServerBO> existsCabinetList = checkBatterySnList(cabinetSnList, repeatSnList, cabinetServerList);
        if (ObjectUtils.isEmpty(existsCabinetList)) {
            ElectricityCabinetServerTimeAddResultVO resultVO = ElectricityCabinetServerTimeAddResultVO.builder().successNum(0).failNum(request.getCabinetSnList().size())
                    .notFoundSnList(cabinetSnList).build();
            return R.ok(resultVO);
        }

        Map<String, ElectricityCabinetServerBO> existsMap = existsCabinetList.stream().collect(Collectors.toMap(ElectricityCabinetServerBO::getSn, Function.identity(), (v1, v2) -> v1));
        AtomicReference<Integer> successNum = new AtomicReference<>(0);

        cabinetSnList.stream().forEach(sn -> {
            // 过滤掉不存在的
            if (!existsMap.containsKey(sn)) {
                notFindSnList.add(sn);
                return;
            }

            // 过滤掉重复的
            if (repeatSnList.contains(sn)) {
                return;
            }

            // 过滤服务时间为空的
            ElectricityCabinetServerBO electricityCabinetServerBO = existsMap.get(sn);
            if (Objects.isNull(electricityCabinetServerBO.getServerEndTime())) {
                return;
            }

            successNum.set(successNum.get() + 1);

            threadPool.execute(() -> {
                // 修改柜机的服务时间
                long serverEndTime = DateUtils.getAfterYear(electricityCabinetServerBO.getServerEndTime(), request.getYearNum());
                electricityCabinetServerMapper.updateServerEndTime(electricityCabinetServerBO.getCabinetServerId(), serverEndTime, updateTime);
            });

        });

        ElectricityCabinetServerTimeAddResultVO resultVO = ElectricityCabinetServerTimeAddResultVO.builder().successNum(successNum.get()).failNum(repeatSnList.size() + notFindSnList.size())
                .notFoundSnList(notFindSnList).repeatSnList(repeatSnList).build();

        log.info("add cabinet server end time success! request:{}", request);

        return R.ok(resultVO);
    }

    private List<ElectricityCabinetServerBO> checkBatterySnList(List<String> batterySnList, List<String> repeatSnList, List<ElectricityCabinetServer> cabinetServerList) {
        if (batterySnList.size() > 500) {
            List<List<String>> partition = ListUtils.partition(batterySnList, 500);
            List<ElectricityCabinetServerBO> existsCabinetSnList = Collections.synchronizedList(new ArrayList<>());
            List<String> repeatCabinetSnList = Collections.synchronizedList(new ArrayList<>());

            List<CompletableFuture<List<ElectricityCabinetServerBO>>> collect = partition.stream().map(item -> {
                CompletableFuture<List<ElectricityCabinetServerBO>> exceptionally = CompletableFuture.supplyAsync(() -> {
                    List<ElectricityCabinetServerBO> electricityCabinetServerList = electricityCabinetServerMapper.listByTenantId(null, item, 0L, 500);
                    return electricityCabinetServerList;
                }, threadPool).whenComplete((result, throwable) -> {
                    if (result != null && ObjectUtils.isNotEmpty(result)) {
                        existsCabinetSnList.addAll(result);

                        Map<String, List<ElectricityCabinetServerBO>> cabinetMap = result.stream().collect(Collectors.groupingBy(ElectricityCabinetServerBO::getSn));
                        cabinetMap.keySet().forEach(sn -> {
                            if (ObjectUtils.isNotEmpty(cabinetMap.get(sn)) && cabinetMap.get(sn).size() > 1) {
                                repeatCabinetSnList.add(sn);
                            }
                        });
                    }

                    if (throwable != null) {
                        log.error("add cabinet server time check  error", throwable);
                    }

                });
                return exceptionally;
            }).collect(Collectors.toList());

            CompletableFuture<Void> resultFuture = CompletableFuture.allOf(collect.toArray(new CompletableFuture[collect.size()]));

            try {
                resultFuture.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Data summary browsing error for add cabinet server time check", e);
            }

            if (ObjectUtils.isNotEmpty(repeatCabinetSnList)) {
                repeatSnList.addAll(repeatCabinetSnList);
            }

            return existsCabinetSnList;
        }

        List<ElectricityCabinetServerBO> electricityCabinetServerList = electricityCabinetServerMapper.listByTenantId(null, batterySnList, 0L, 500);
        if (ObjectUtils.isEmpty(electricityCabinetServerList)) {
            return Collections.emptyList();
        }

        Map<String, List<ElectricityCabinetServerBO>> cabinetMap = electricityCabinetServerList.stream().collect(Collectors.groupingBy(ElectricityCabinetServerBO::getSn));
        cabinetMap.forEach((sn, value) -> {
            if (ObjectUtils.isNotEmpty(value) && value.size() > 1) {
                repeatSnList.add(sn);
            }
        });

        return electricityCabinetServerList;
    }

    @Override
    public Integer deleteByEid(Integer eid) {
        return this.electricityCabinetServerMapper.deleteByEid(eid);
    }

    @Override
    public Integer logicalDeleteByEid(Integer id) {
        ElectricityCabinetServer electricityCabinetServer = new ElectricityCabinetServer();
        electricityCabinetServer.setElectricityCabinetId(id);
        electricityCabinetServer.setDelFlag(ElectricityCabinetServer.DEL_DEL);
        electricityCabinetServer.setUpdateTime(System.currentTimeMillis());
        return this.electricityCabinetServerMapper.updateByEid(electricityCabinetServer);
    }

    @Override
    public void destroy() throws Exception {
        threadPool.shutdown();
    }
}
