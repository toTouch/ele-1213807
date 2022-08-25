package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetTraffic;
import com.xiliulou.electricity.mapper.ElectricityCabinetTrafficMapper;
import com.xiliulou.electricity.service.ElectricityCabinetTrafficService;
import com.xiliulou.electricity.vo.EleDepositOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityCabinetPowerExcelVo;
import com.xiliulou.electricity.vo.ElectricityCabinetPowerVo;
import com.xiliulou.electricity.vo.ElectricityCabinetTrafficExcelVo;
import com.xiliulou.electricity.vo.ElectricityCabinetTrafficVo;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Objects;

/**
 * zgw
 */
@Service
@Slf4j
public class ElectricityCabinetTrafficServiceImpl implements ElectricityCabinetTrafficService {

    @Resource
    ElectricityCabinetTrafficMapper electricityCabinetTrafficMapper;

    @Override
    public int insertOrUpdate(ElectricityCabinetTraffic electricityCabinetTraffic) {
        ElectricityCabinetTraffic queryById = queryById(electricityCabinetTraffic.getId());
        if (Objects.isNull(queryById)) {
            return insertOne(electricityCabinetTraffic);
        }
        return updateById(electricityCabinetTraffic);
    }

    @Override
    public ElectricityCabinetTraffic queryById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return electricityCabinetTrafficMapper.queryById(id);
    }

    @Override
    public int updateById(ElectricityCabinetTraffic electricityCabinetTraffic) {
        return electricityCabinetTrafficMapper.updateOneById(electricityCabinetTraffic);
    }

    @Override
    public int insertOne(ElectricityCabinetTraffic electricityCabinetTraffic) {
        return electricityCabinetTrafficMapper.insertOne(electricityCabinetTraffic);
    }

    @Override
    public R queryList(Long size, Long offset, Integer electricityCabinetId, String electricityCabinetName, LocalDate date, Long beginTime, Long endTime) {
        return R.ok(electricityCabinetTrafficMapper.queryList(size, offset, electricityCabinetId, electricityCabinetName, beginTime, endTime, date));
    }

    @Override
    public void exportExcel( Integer electricityCabinetId,
        String electricityCabinetName, LocalDate date, Long beginTime, Long endTime,
        HttpServletResponse response) {
        Long offset=0L;
        Long size=2000L;
        List<ElectricityCabinetTrafficVo> electricityCabinetTrafficVos = electricityCabinetTrafficMapper
            .queryList(size, offset, electricityCabinetId, electricityCabinetName, beginTime, endTime, date);
        if (ObjectUtil.isEmpty(electricityCabinetTrafficVos)) {
            throw new CustomBusinessException("查不到柜机电量");
        }
        List<ElectricityCabinetTrafficExcelVo> electricityCabinetTrafficExcelVos = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        int index = 0;
        for (ElectricityCabinetTrafficVo electricityCabinetTrafficVo : electricityCabinetTrafficVos) {
            index++;
            ElectricityCabinetTrafficExcelVo excelVo = new ElectricityCabinetTrafficExcelVo();
            excelVo.setId(index);
            excelVo.setDate(electricityCabinetTrafficVo.getDate());
            excelVo
                .setElectricityCabinetName(electricityCabinetTrafficVo.getElectricityCabinetName());
            excelVo.setSameDayTraffic(electricityCabinetTrafficVo.getSameDayTraffic());
            excelVo.setSumTraffic(electricityCabinetTrafficVo.getSumTraffic());
            excelVo.setDate(electricityCabinetTrafficVo.getDate());

            if (Objects.nonNull(electricityCabinetTrafficVo.getCreateTime())) {
                excelVo.setCreateTime(
                    simpleDateFormat.format(new Date(electricityCabinetTrafficVo.getCreateTime())));
            }

            if (Objects.nonNull(electricityCabinetTrafficVo.getUpdateTime())) {
                excelVo.setUpdateTime(
                    simpleDateFormat.format(new Date(electricityCabinetTrafficVo.getUpdateTime())));
            }

            electricityCabinetTrafficExcelVos.add(excelVo);

        }
        String fileName = "换电柜流量报表.xlsx";

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder
                .encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, EleDepositOrderExcelVO.class).sheet("sheet")
                .doWrite(electricityCabinetTrafficExcelVos);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }

    }

    @Override
    public void expiredDel() {
        Long time = System.currentTimeMillis() - (1000L * 3600 * 24 * 365);
        electricityCabinetTrafficMapper.removeLessThanTime(time);
    }
}
