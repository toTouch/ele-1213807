package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetPower;
import com.xiliulou.electricity.mapper.ElectricityCabinetPowerMapper;
import com.xiliulou.electricity.query.ElectricityCabinetPowerQuery;
import com.xiliulou.electricity.service.ElectricityCabinetPowerService;
import com.xiliulou.electricity.vo.ElectricityCabinetPowerExcelVo;
import com.xiliulou.electricity.vo.ElectricityCabinetPowerVo;
import com.xiliulou.electricity.vo.ElectricityCabinetSumPowerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 换电柜电量表(ElectricityCabinetPower)表服务实现类
 *
 * @author makejava
 * @since 2021-01-27 16:22:44
 */
@Service("electricityCabinetPowerService")
@Slf4j
public class ElectricityCabinetPowerServiceImpl implements ElectricityCabinetPowerService {

    @Resource
    private ElectricityCabinetPowerMapper electricityCabinetPowerMapper;

    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors
        .newFixedThreadPool("DATA-SCREEN-THREAD-POOL", 4, "dataScreenThread:");


    @Override
    public Integer insertOrUpdate(ElectricityCabinetPower electricityCabinetPower) {
        return this.electricityCabinetPowerMapper.insertOrUpdate(electricityCabinetPower);
    }

    @Slave
    @Override
    public ElectricityCabinetPower selectLatestByEid(Integer id) {
        return this.electricityCabinetPowerMapper.selectLatestByEid(id);
    }

    @Override
    public R queryList(ElectricityCabinetPowerQuery electricityCabinetPowerQuery) {

        ElectricityCabinetSumPowerVo electricityCabinetSumPowerVo=new ElectricityCabinetSumPowerVo();

        //电量列表统计
        CompletableFuture<Void> powerList = CompletableFuture.runAsync(() -> {
            List<ElectricityCabinetPowerVo> electricityCabinetPowerVoList=electricityCabinetPowerMapper.queryList(electricityCabinetPowerQuery);
            electricityCabinetSumPowerVo.setElectricityCabinetPowerVo(electricityCabinetPowerVoList);
        }, threadPool).exceptionally(e -> {
            log.error("query electricityCabinet power list ERROR!", e);
            return null;
        });

        //总电量统计
        CompletableFuture<Void> sumPower = CompletableFuture.runAsync(() -> {
            ElectricityCabinetPowerVo electricityCabinetPowerVo=electricityCabinetPowerMapper.queryLatestPower(electricityCabinetPowerQuery);
            if (Objects.nonNull(electricityCabinetPowerVo)) {
                electricityCabinetSumPowerVo.setSumPower(electricityCabinetPowerVo.getSumPower());
            }
        }, threadPool).exceptionally(e -> {
            log.error("query electricityCabinet sum power ERROR!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(powerList,sumPower);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ORDER STATISTICS ERROR!", e);
        }
        return R.ok(electricityCabinetSumPowerVo);
    }

    @Slave
    @Override
    public void exportExcel(ElectricityCabinetPowerQuery electricityCabinetPowerQuery,
        HttpServletResponse response) {

        electricityCabinetPowerQuery.setOffset(0L);
        electricityCabinetPowerQuery.setSize(2000L);
        List<ElectricityCabinetPowerVo> electricityCabinetPowerVos = electricityCabinetPowerMapper
            .queryList(electricityCabinetPowerQuery);
        if (ObjectUtil.isEmpty(electricityCabinetPowerVos)) {
            throw new CustomBusinessException("查不到柜机电量");
        }

        List<ElectricityCabinetPowerExcelVo> electricityCabinetPowerExcelVos = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        int index = 0;
        for (ElectricityCabinetPowerVo electricityCabinetPowerVo : electricityCabinetPowerVos) {
            index++;
            ElectricityCabinetPowerExcelVo excelVo = new ElectricityCabinetPowerExcelVo();
            excelVo.setId(index);
            excelVo.setDate(electricityCabinetPowerVo.getDate().toString());
            excelVo
                .setElectricityCabinetName(electricityCabinetPowerVo.getElectricityCabinetName());
            excelVo.setSameDayPower(electricityCabinetPowerVo.getSameDayPower());
            excelVo.setSumPower(electricityCabinetPowerVo.getSumPower());

            if (Objects.nonNull(electricityCabinetPowerVo.getCreateTime())) {
                excelVo.setCreateTime(
                    simpleDateFormat.format(new Date(electricityCabinetPowerVo.getCreateTime())));
            }

            if (Objects.nonNull(electricityCabinetPowerVo.getUpdateTime())) {
                excelVo.setUpdateTime(
                    simpleDateFormat.format(new Date(electricityCabinetPowerVo.getUpdateTime())));
            }

            electricityCabinetPowerExcelVos.add(excelVo);

        }

        String fileName = "换电柜电量报表.xlsx";

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder
                .encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityCabinetPowerExcelVo.class).sheet("sheet")
                .doWrite(electricityCabinetPowerExcelVos);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
}
