package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.dto.EleMonthPowerGroupDto;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.entity.ElePowerMonthRecord;
import com.xiliulou.electricity.mapper.ElePowerMonthRecordMapper;
import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import com.xiliulou.electricity.service.ElePowerMonthRecordService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.vo.ElePowerMonthRecordExcelVo;
import com.xiliulou.electricity.vo.ElePowerMonthRecordVo;
import com.xiliulou.electricity.vo.ElectricityBatteryExcelVO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (ElePowerMonthRecord)表服务实现类
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
@Service("elePowerMonthRecordService")
@Slf4j
public class ElePowerMonthRecordServiceImpl implements ElePowerMonthRecordService {
    @Resource
    private ElePowerMonthRecordMapper elePowerMonthRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePowerMonthRecord queryByIdFromDB(Long id) {
        return this.elePowerMonthRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePowerMonthRecord queryByIdFromCache(Long id) {
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
    public List<ElePowerMonthRecord> queryAllByLimit(int offset, int limit) {
        return this.elePowerMonthRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElePowerMonthRecord insert(ElePowerMonthRecord elePowerMonthRecord) {
        this.elePowerMonthRecordMapper.insertOne(elePowerMonthRecord);
        return elePowerMonthRecord;
    }

    /**
     * 修改数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElePowerMonthRecord elePowerMonthRecord) {
        return this.elePowerMonthRecordMapper.update(elePowerMonthRecord);

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
        return this.elePowerMonthRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryMonthStatistics(PowerMonthStatisticsQuery query) {
        List<ElePowerMonthRecord> list = this.elePowerMonthRecordMapper.queryPartAttrList(query);
        if (!DataUtil.collectionIsUsable(list)) {
            return Pair.of(true, Collections.EMPTY_LIST);
        }

        return Pair.of(true, list.parallelStream().map(e -> {
            ElePowerMonthRecord elePowerMonthRecord = new ElePowerMonthRecord();
            BeanUtil.copyProperties(e, elePowerMonthRecord);
            return elePowerMonthRecord;
        }).collect(Collectors.toList()));
    }

    @Override
    public Pair<Boolean, Object> queryMonthStatisticsCount(PowerMonthStatisticsQuery query) {
        return Pair.of(true, this.elePowerMonthRecordMapper.queryCount(query));
    }

    @Override
    public void exportMonthStatistics(PowerMonthStatisticsQuery query, HttpServletResponse response) {
        List<ElePowerMonthRecord> list = this.elePowerMonthRecordMapper.queryPartAttrList(query);
        if (!DataUtil.collectionIsUsable(list)) {
            throw new CustomBusinessException("没有耗电月记录，无法导出");
        }

        List<ElePowerMonthRecordExcelVo> vos = list.parallelStream().map(e -> {
            ElePowerMonthRecordExcelVo vo = new ElePowerMonthRecordExcelVo();
            vo.setEName(e.getEName());
            vo.setStoreName(e.getStoreName());
            vo.setFranchiseeName(e.getFranchiseeName());
            vo.setMonthStartPower(e.getMonthStartPower());
            vo.setMonthEndPower(e.getMonthEndPower());
            vo.setMonthSumPower(e.getMonthSumPower());
            vo.setMonthSumCharge(e.getMonthSumCharge());
            vo.setTypeDetail(generatePowerTypeDetail(e.getJsonCharge()));
            vo.setDate(e.getDate());
            return vo;
        }).collect(Collectors.toList());


        String fileName = "耗电量月记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, ElePowerMonthRecordExcelVo.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(vos);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }

    private String generatePowerTypeDetail(String jsonCharge) {
        List<EleMonthPowerGroupDto> eleMonthPowerGroupDtos = JsonUtil.fromJsonArray(jsonCharge, EleMonthPowerGroupDto.class);
        if (!DataUtil.collectionIsUsable(eleMonthPowerGroupDtos)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (EleMonthPowerGroupDto eleMonthPowerGroupDto : eleMonthPowerGroupDtos) {
            sb.append(queryPowerType(eleMonthPowerGroupDto.getType()))
                    .append("[").append("月耗电量:").append(eleMonthPowerGroupDto.getSumPower())
                    .append("月电费:").append(eleMonthPowerGroupDto.getSumCharge()).append("]");
        }
        return sb.toString();
    }

    public String queryPowerType(Integer type) {
        switch (type) {
            case ElePower.ORDINARY_TYPE:
                return "平用电";
            case ElePower.PEEK_TYPE:
                return "峰用电";
            case ElePower.VALLEY_TYPE:
                return "谷用电";
            default:
                return "";
        }
    }
}
