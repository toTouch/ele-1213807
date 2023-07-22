package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.mapper.ElePowerMapper;
import com.xiliulou.electricity.query.ElePowerListQuery;
import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import com.xiliulou.electricity.service.ElePowerMonthRecordService;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElePowerDayVo;
import com.xiliulou.electricity.vo.ElePowerExcelVo;
import com.xiliulou.electricity.vo.ElePowerVo;
import com.xiliulou.electricity.vo.ElectricityBatteryExcelVO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (ElePower)表服务实现类
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
@Service("elePowerService")
@Slf4j
public class ElePowerServiceImpl implements ElePowerService {
    @Resource
    private ElePowerMapper elePowerMapper;

    @Autowired
    ElePowerMonthRecordService monthRecordService;

    @Autowired
    RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePower queryByIdFromDB(Long id) {
        return this.elePowerMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePower queryByIdFromCache(Long id) {
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
    public List<ElePower> queryAllByLimit(int offset, int limit) {
        return this.elePowerMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElePower insert(ElePower elePower) {
        this.elePowerMapper.insertOne(elePower);
        return elePower;
    }

    /**
     * 修改数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElePower elePower) {
        return this.elePowerMapper.update(elePower);

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
        return this.elePowerMapper.deleteById(id) > 0;
    }

    @Override
    public int insertOrUpdate(ElePower power) {
        return this.elePowerMapper.insertOrUpdate(power);
    }

    @Override
    public Pair<Boolean, Object> queryList(ElePowerListQuery query) {
        List<ElePower> powerList = this.elePowerMapper.queryPartAttList(query);
        if (!DataUtil.collectionIsUsable(powerList)) {
            return Pair.of(true, Collections.EMPTY_LIST);
        }

        List<ElePowerVo> list = powerList.parallelStream().map(e -> {
            ElePowerVo elePowerVo = new ElePowerVo();
            BeanUtil.copyProperties(e, elePowerVo);
            return elePowerVo;

        }).collect(Collectors.toList());
        return Pair.of(true, list);
    }

    @Override
    public Pair<Boolean, Object> queryDayList(Long eid, Long startTime, Long endTime, Integer tenantId) {
        return Pair.of(true, this.elePowerMapper.queryDayList(eid, startTime, endTime, tenantId));
    }

    @Override
    public Pair<Boolean, Object> queryMonthList(Long eid, Long startTime, Long endTime, Integer tenantId) {
        return Pair.of(true, this.elePowerMapper.queryMonthList(eid, startTime, endTime, tenantId));
    }

    @Override
    public Pair<Boolean, Object> queryDayDetail(Long eid, Long startTime, Long endTime, Integer tenantId) {
        return Pair.of(true, this.elePowerMapper.queryDayDetail(eid, startTime, endTime, tenantId));
    }

    @Override
    public Pair<Boolean, Object> queryMonthDetail(Long eid, Long startTime, Long endTime, Integer tenantId) {
        return Pair.of(true, this.elePowerMapper.queryMonthDetail(eid, startTime, endTime, tenantId));
    }

    @Override
    public void exportList(ElePowerListQuery query, HttpServletResponse response) {
        List<ElePower> elePowers = elePowerMapper.queryPartAttList(query);
        if (!DataUtil.collectionIsUsable(elePowers)) {
            throw new CustomBusinessException("柜机电量为空");
        }

        List<ElePowerExcelVo> vos = elePowers.parallelStream().map(e -> {
            ElePowerExcelVo vo = new ElePowerExcelVo();
            vo.setHourPower(e.getHourPower());
            vo.setSumPower(e.getSumPower());
            vo.setElectricCharge(e.getElectricCharge());
            vo.setEName(e.getEName());
            vo.setReportTime(DateUtils.parseTimeToStringDate(e.getReportTime()));
            return vo;
        }).collect(Collectors.toList());

        String fileName = "耗电量记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, ElePowerExcelVo.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(vos);
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }


}
