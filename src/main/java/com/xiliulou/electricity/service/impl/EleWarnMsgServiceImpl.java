package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.AppMalfunctionConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ReportManagement;
import com.xiliulou.electricity.mapper.EleWarnMsgMapper;
import com.xiliulou.electricity.query.EleWarnMsgExcelQuery;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ReportManagementService;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表服务实现类
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@Service("eleWarnMsgService")
@Slf4j
public class EleWarnMsgServiceImpl implements EleWarnMsgService {
    
    /**
     * excel导出每次查询条数
     */
    private static final Integer EXPORT_LIMIT = 2000;
    
    private static final String EXCEL_TYPE="xlsx";
    private static final String BEGIN_TIME="beginTime";
    private static final String END_TIME="endTime";

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ExecutorService exportExecutorService = XllThreadPoolExecutors.newFixedThreadPool("eleWarnMsgExportExecutor", 1, "ele_warnMsg_export_executor");

    @Resource
    private EleWarnMsgMapper eleWarnMsgMapper;
    @Resource(name="aliyunOssService")
    private StorageService storageService;
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ReportManagementService reportManagementService;
    @Autowired
    private ClickHouseService clickHouseService;
    @Autowired
    private StorageConfig storageConfig;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleWarnMsg queryByIdFromDB(Long id) {
        return this.eleWarnMsgMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleWarnMsg insert(EleWarnMsg eleWarnMsg) {
        this.eleWarnMsgMapper.insert(eleWarnMsg);
        return eleWarnMsg;
    }

    /**
     * 修改数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleWarnMsg eleWarnMsg) {
        return this.eleWarnMsgMapper.updateById(eleWarnMsg);

    }

    @Override
    public R queryList(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenant(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryAllTenantList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenantCount() {
        return R.ok(eleWarnMsgMapper.queryAllTenantCount());
    }

    @Override
    public R queryCount(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryCount(eleWarnMsgQuery));
    }

    @Override
    public void delete(Long id) {
        eleWarnMsgMapper.deleteById(id);
    }

    @Override
    public R queryStatisticsEleWarmMsg(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticsEleWarmMsg(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgByElectricityCabinet(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticEleWarnMsgByElectricityCabinet(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgRanking(EleWarnMsgQuery eleWarnMsgQuery) {
        List<EleWarnMsgVo> eleWarnMsgRankingVos = null;
        if (Objects.nonNull(eleWarnMsgQuery.getElectricityCabinetId())) {
            eleWarnMsgRankingVos=eleWarnMsgMapper.queryStatisticEleWarnMsgRankingByElectricityCabinetId(eleWarnMsgQuery);
            return R.ok(eleWarnMsgRankingVos);
        }else {
            eleWarnMsgRankingVos = eleWarnMsgMapper.queryStatisticEleWarnMsgRanking(eleWarnMsgQuery);
        }
        if (Objects.nonNull(eleWarnMsgRankingVos)) {
            for (EleWarnMsgVo eleWarnMsgVo : eleWarnMsgRankingVos) {
                EleWarnMsgVo eleWarnMsgVoForTenant = eleWarnMsgMapper.queryStatisticEleWarnMsgForTenant(eleWarnMsgVo.getElectricityCabinetId());
                eleWarnMsgVo.setElectricityCabinetName(eleWarnMsgVoForTenant.getElectricityCabinetName());
                eleWarnMsgVo.setTenantName(eleWarnMsgVoForTenant.getTenantName());
            }
        }
        return R.ok(eleWarnMsgRankingVos);
    }

    @Override
    public R queryStatisticEleWarnMsgRankingCount() {
        return R.ok(eleWarnMsgMapper.queryStatisticEleWarnMsgRankingCount());
    }

    @Override
    public void queryElectricityName(List<Object> list) {

        if (Objects.isNull(list)){
            return;
        }

        for (Object object:list){
            if (object instanceof EleBatteryWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleBatteryWarnMsgVo) object).getElectricityCabinetId()));
                ((EleBatteryWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            } else if (object instanceof EleBusinessWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleBusinessWarnMsgVo) object).getElectricityCabinetId()));
                ((EleBusinessWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }else if (object instanceof EleCabinetWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleCabinetWarnMsgVo) object).getElectricityCabinetId()));
                ((EleCabinetWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }else if (object instanceof EleCellWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleCellWarnMsgVo) object).getElectricityCabinetId()));
                ((EleCellWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }
        }

    }

    /**
     * 异常告警导出
     *
     * @param warnMsgQuery
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> submitExportTask(EleWarnMsgExcelQuery warnMsgQuery) {

        //限频
        if (!redisService.setNx(CacheConstant.WARN_MESSAGE_EXPORT_CACHE + SecurityUtils.getUid(), "1", 120 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0001", "操作频繁！");
        }

        //保存导出任务
        ReportManagement reportManagement = buildReportManagement();
        if (Objects.isNull(reportManagementService.insert(reportManagement))) {
            log.error("ELE ERROR! export excel fail");
            return Triple.of(false, "000001", "导出失败！");
        }

        exportExecutorService.submit(() -> {
            switchExportType(warnMsgQuery, reportManagement);
        });

        return Triple.of(true, "", "任务创建成功！");
    }

    /**
     * 根据业务类型导出
     *
     * @param warnMsgQuery
     * @param reportManagement
     */
    private void switchExportType(EleWarnMsgExcelQuery warnMsgQuery, ReportManagement reportManagement) {
        //更新任务状态
        reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_EXPORTING, null));

        switch (warnMsgQuery.getType()) {
            case EleWarnMsgExcelQuery.TYPE_BUSINESS_WARN:
                exportBusinessWarnToExcel(warnMsgQuery, reportManagement);
                break;
            case EleWarnMsgExcelQuery.TYPE_CABINET_WARN:
                exportEleCabinetWarnToExcel(warnMsgQuery, reportManagement);
                break;
            case EleWarnMsgExcelQuery.TYPE_CELL_WARN:
                exportCellWarnToExcel(warnMsgQuery, reportManagement);
                break;
            case EleWarnMsgExcelQuery.TYPE_BATTERY_WARN:
                exportBatteryWarnToExcel(warnMsgQuery, reportManagement);
                break;
            default:
                log.error("ELE ERROR! not found type={},jobId={}", warnMsgQuery.getType(), reportManagement.getJobId());
        }
    }

    /**
     * 导出业务异常到Excel
     *
     * @param warnMsgQuery
     */
    private void exportBusinessWarnToExcel(EleWarnMsgExcelQuery warnMsgQuery, ReportManagement reportManagement) {

        List<EleBusinessWarnMsgVo> dataList = getBusinessWarnDatas(warnMsgQuery);

        List<EleBusinessWarnExcelVO> excelVOList = Lists.newArrayList();
        for (int i = 0; i < dataList.size(); i++) {
            EleBusinessWarnExcelVO excelEntity = new EleBusinessWarnExcelVO();
            excelEntity.setId(i+1);
            excelEntity.setCabinetName(dataList.get(i).getCabinetName());
            excelEntity.setCellNo(dataList.get(i).getCellNo());
            excelEntity.setErrorMsg(AppMalfunctionConstant.acquireBusinessAbnormal(dataList.get(i).getErrorCode()));
            excelEntity.setReportTime(dataList.get(i).getReportTime());

            excelVOList.add(excelEntity);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            //生成Excel
            EasyExcel.write(bos, EleBusinessWarnExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOList);
            //上传到OSS
            Map<String, String> resultMap = uploadExcelToOSS(bos);
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_SUCCESS, resultMap.get(CommonConstant.FILE_NAME)));
            return;
        } catch (Exception e) {
            log.error("ELE ERROR! export excel fail,jobId={},ex={}", reportManagement.getJobId(), e.getMessage());
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_FAIL, null));
        }finally {
            redisService.delete(CacheConstant.WARN_MESSAGE_EXPORT_CACHE + SecurityUtils.getUid());
        }
    }

    /**
     * 导出柜机异常到Excel
     *
     * @param warnMsgQuery
     * @param reportManagement
     */
    private void exportEleCabinetWarnToExcel(EleWarnMsgExcelQuery warnMsgQuery, ReportManagement reportManagement) {
        List<EleCabinetWarnMsgVo> dataList = getEleCabinetWarnDatas(warnMsgQuery);

        List<EleCabinetWarnExcelVO> excelVOList = Lists.newArrayList();
        for (int i = 0; i < dataList.size(); i++) {
            EleCabinetWarnExcelVO excelEntity = new EleCabinetWarnExcelVO();
            excelEntity.setId(i+1);
            excelEntity.setCabinetName(dataList.get(i).getCabinetName());
            excelEntity.setErrorMsg(AppMalfunctionConstant.acquireEleHardwareAbnormal(dataList.get(i).getErrorCode()));
            excelEntity.setReportTime(dataList.get(i).getReportTime());

            excelVOList.add(excelEntity);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            //生成Excel
            EasyExcel.write(bos, EleCabinetWarnExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOList);
            //上传到OSS
            Map<String, String> resultMap = uploadExcelToOSS(bos);
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_SUCCESS, resultMap.get(CommonConstant.FILE_NAME)));
            return;
        } catch (Exception e) {
            log.error("ELE ERROR! export excel fail,jobId={},ex={}", reportManagement.getJobId(), e.getMessage());
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_FAIL, null));
        }finally {
            redisService.delete(CacheConstant.WARN_MESSAGE_EXPORT_CACHE + SecurityUtils.getUid());
        }
    }

    /**
     * 导出格挡异常
     *
     * @param warnMsgQuery
     * @param reportManagement
     */
    private void exportCellWarnToExcel(EleWarnMsgExcelQuery warnMsgQuery, ReportManagement reportManagement) {
        List<EleCellWarnMsgVo> dataList = getCellWarnDatas(warnMsgQuery);

        List<EleCellWarnExcelVO> excelVOList = Lists.newArrayList();
        for (int i = 0; i < dataList.size(); i++) {
            EleCellWarnExcelVO excelEntity = new EleCellWarnExcelVO();
            excelEntity.setId(i+1);
            excelEntity.setCabinetName(dataList.get(i).getCabinetName());
            excelEntity.setCellNo(dataList.get(i).getCellNo());
            excelEntity.setReportTime(dataList.get(i).getReportTime());
            excelEntity.setErrorMsg(dataList.get(i).getErrorMsg());
            excelEntity.setErrorType(AppMalfunctionConstant.acquireCellHardwareAbnormal(dataList.get(i).getErrorCode()));
            excelEntity.setOperateType(AppMalfunctionConstant.acquireOperateType(dataList.get(i).getOperateType()));

            excelVOList.add(excelEntity);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            //生成Excel
            EasyExcel.write(bos, EleCellWarnExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOList);
            //上传到OSS
            Map<String, String> resultMap = uploadExcelToOSS(bos);
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_SUCCESS, resultMap.get(CommonConstant.FILE_NAME)));
            return;
        } catch (Exception e) {
            log.error("ELE ERROR! export excel fail,jobId={},ex={}", reportManagement.getJobId(), e.getMessage());
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_FAIL, null));
        }finally {
            redisService.delete(CacheConstant.WARN_MESSAGE_EXPORT_CACHE + SecurityUtils.getUid());
        }
    }


    /**
     * 导出电池异常
     *
     * @param warnMsgQuery
     * @param reportManagement
     */
    private void exportBatteryWarnToExcel(EleWarnMsgExcelQuery warnMsgQuery, ReportManagement reportManagement) {
        List<EleBatteryWarnMsgVo> dataList = getBatteryWarnDatas(warnMsgQuery);

        List<EleBatteryWarnExcelVO> excelVOList = Lists.newArrayList();
        for (int i = 0; i < dataList.size(); i++) {
            EleBatteryWarnExcelVO excelEntity = new EleBatteryWarnExcelVO();
            excelEntity.setId(i+1);
            excelEntity.setCabinetName(dataList.get(i).getCabinetName());
            excelEntity.setBatteryName(dataList.get(i).getBatteryName());
            excelEntity.setErrorMsg(dataList.get(i).getErrorMsg());
            excelEntity.setReportTime(dataList.get(i).getReportTime());

            excelVOList.add(excelEntity);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            //生成Excel
            EasyExcel.write(bos, EleBatteryWarnExcelVO.class).sheet("sheet").registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).doWrite(excelVOList);
            //上传到OSS
            Map<String, String> resultMap = uploadExcelToOSS(bos);
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_SUCCESS, resultMap.get(CommonConstant.FILE_NAME)));
            return;
        } catch (Exception e) {
            log.error("ELE ERROR! export excel fail,jobId={},ex={}", reportManagement.getJobId(), e.getMessage());
            reportManagementService.update(buildReportManagementUpdate(reportManagement.getId(), ReportManagement.STATUS_FAIL, null));
        }finally {
            redisService.delete(CacheConstant.WARN_MESSAGE_EXPORT_CACHE + SecurityUtils.getUid());
        }
    }

    /**
     * 业务异常数据
     *
     * @param warnMsgQuery
     * @return
     */
    private List<EleBusinessWarnMsgVo> getBusinessWarnDatas(EleWarnMsgExcelQuery warnMsgQuery) {
        List<EleBusinessWarnMsgVo> list = Lists.newArrayList();

        Map<String, String> dateMap = formatQueryDate(warnMsgQuery.getBeginTime(), warnMsgQuery.getEndTime());
    
        int index = 0;
        while (true) {
            List<EleBusinessWarnMsgVo> tempList = Lists.newArrayList();;
            
            if (Objects.isNull(warnMsgQuery.getElectricityCabinetId())) {
                String sql = "select electricityCabinetId ,cellNo ,errorMsg ,errorCode ,reportTime from t_warn_msg_business where reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            } else {
                String sql = "select electricityCabinetId ,cellNo ,errorMsg ,errorCode ,reportTime from t_warn_msg_business where  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }
            index += EXPORT_LIMIT;
        
            if (CollectionUtils.isEmpty(tempList)) {
                break;
            }
    
            tempList.stream().forEach(item -> {
                if (StringUtils.isBlank(item.getElectricityCabinetId())) {
                    return;
                }
        
                ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(Integer.parseInt(item.getElectricityCabinetId()));
                if (Objects.nonNull(cabinet)) {
                    item.setCabinetName(cabinet.getName());
                }
            });
            
            list.addAll(tempList);
        }
        
        return list;
    }

    /**
     * 柜机异常数据
     *
     * @param warnMsgQuery
     * @return
     */
    private List<EleCabinetWarnMsgVo> getEleCabinetWarnDatas(EleWarnMsgExcelQuery warnMsgQuery) {
        List<EleCabinetWarnMsgVo> list = Lists.newArrayList();

        Map<String, String> dateMap = formatQueryDate(warnMsgQuery.getBeginTime(), warnMsgQuery.getEndTime());
    
        int index = 0;
        while (true) {
            List<EleCabinetWarnMsgVo> tempList = Lists.newArrayList();
            
            if (Objects.isNull(warnMsgQuery.getElectricityCabinetId())) {
                String sql = "select electricityCabinetId,operateType,errorMsg,errorCode ,reportTime from t_warn_msg_cabinet where  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            } else {
                String sql = "select electricityCabinetId,operateType,errorMsg,errorCode ,reportTime from t_warn_msg_cabinet where electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }
            
            index += EXPORT_LIMIT;
            
            if (CollectionUtils.isEmpty(tempList)) {
                break;
            }
    
            tempList.stream().forEach(item -> {
                if (StringUtils.isBlank(item.getElectricityCabinetId())) {
                    return;
                }
        
                ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(Integer.parseInt(item.getElectricityCabinetId()));
                if (Objects.nonNull(cabinet)) {
                    item.setCabinetName(cabinet.getName());
                }
            });
            
            list.addAll(tempList);
        }

        return list;
    }

    /**
     * 格挡异常数据
     *
     * @param warnMsgQuery
     * @return
     */
    private List<EleCellWarnMsgVo> getCellWarnDatas(EleWarnMsgExcelQuery warnMsgQuery) {

        List<EleCellWarnMsgVo> list = Lists.newArrayList();

        Map<String, String> dateMap = formatQueryDate(warnMsgQuery.getBeginTime(), warnMsgQuery.getEndTime());
    
        int index = 0;
        while (true) {
            List<EleCellWarnMsgVo> tempList = Lists.newArrayList();
            
            if (Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && Objects.nonNull(warnMsgQuery.getCellNo()) && Objects.nonNull(warnMsgQuery.getOperateType())) {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  electricityCabinetId=? and cellNo=? and operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), warnMsgQuery.getCellNo(), warnMsgQuery.getOperateType(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else if (Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && Objects.nonNull(warnMsgQuery.getCellNo()) && Objects.isNull(warnMsgQuery.getOperateType())) {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where electricityCabinetId=? and cellNo=?  and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), warnMsgQuery.getCellNo(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else if (Objects.isNull(warnMsgQuery.getElectricityCabinetId()) && Objects.isNull(warnMsgQuery.getCellNo()) && Objects.nonNull(warnMsgQuery.getOperateType())) {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, warnMsgQuery.getOperateType(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index,  EXPORT_LIMIT);
            }else if (Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && Objects.nonNull(warnMsgQuery.getOperateType())) {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  electricityCabinetId=? and  operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), warnMsgQuery.getOperateType(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else if (Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && Objects.isNull(warnMsgQuery.getOperateType())) {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index,  EXPORT_LIMIT);
            }else if(Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && Objects.nonNull(warnMsgQuery.getCellNo())){
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  electricityCabinetId=? and cellNo=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), warnMsgQuery.getCellNo(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index,  EXPORT_LIMIT);
            } else {
                String sql = "select electricityCabinetId,cellNo,errorMsg,errorCode,operateType,reportTime from t_warn_msg_cell where  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index,  EXPORT_LIMIT);
            }
            index += EXPORT_LIMIT;
            
            if (CollectionUtils.isEmpty(tempList)) {
                break;
            }
    
            tempList.stream().forEach(item -> {
                if (StringUtils.isBlank(item.getElectricityCabinetId())) {
                    return;
                }
        
                ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(Integer.parseInt(item.getElectricityCabinetId()));
                if (Objects.nonNull(cabinet)) {
                    item.setCabinetName(cabinet.getName());
                }
            });
            
            list.addAll(tempList);
        }

        return list;
    }

    /**
     * 电池异常
     *
     * @param warnMsgQuery
     * @return
     */
    private List<EleBatteryWarnMsgVo> getBatteryWarnDatas(EleWarnMsgExcelQuery warnMsgQuery) {
    
        List<EleBatteryWarnMsgVo> list = Lists.newArrayList();
    
        Map<String, String> dateMap = formatQueryDate(warnMsgQuery.getBeginTime(), warnMsgQuery.getEndTime());
    
        int index = 0;
        while (true) {
            List<EleBatteryWarnMsgVo> tempList = Lists.newArrayList();
            
            if (StrUtil.isNotEmpty(warnMsgQuery.getBatteryName()) && Objects.isNull(warnMsgQuery.getElectricityCabinetId())) {
                String sql = "select electricityCabinetId,batteryName,errorMsg,errorCode ,reportTime from t_warn_msg_battery where batteryName=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, warnMsgQuery.getBatteryName(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else if (Objects.nonNull(warnMsgQuery.getElectricityCabinetId()) && StrUtil.isEmpty(warnMsgQuery.getBatteryName())) {
                String sql = "select electricityCabinetId,batteryName,errorMsg,errorCode ,reportTime from t_warn_msg_battery where electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else if (StrUtil.isNotEmpty(warnMsgQuery.getBatteryName()) && Objects.nonNull(warnMsgQuery.getElectricityCabinetId())) {
                String sql = "select electricityCabinetId,batteryName,errorMsg,errorCode ,reportTime from t_warn_msg_battery where and  electricityCabinetId=? and  batteryName=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, String.valueOf(warnMsgQuery.getElectricityCabinetId()), warnMsgQuery.getBatteryName(), dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }else {
                String sql = "select electricityCabinetId,batteryName,errorMsg,errorCode ,reportTime from t_warn_msg_battery where reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
                tempList = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, dateMap.get(BEGIN_TIME), dateMap.get(END_TIME), index, EXPORT_LIMIT);
            }
            index += EXPORT_LIMIT;
            
            if (CollectionUtils.isEmpty(tempList)) {
                break;
            }
    
            tempList.stream().forEach(item -> {
                if (StringUtils.isBlank(item.getElectricityCabinetId())) {
                    return;
                }
        
                ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(Integer.parseInt(item.getElectricityCabinetId()));
                if (Objects.nonNull(cabinet)) {
                    item.setCabinetName(cabinet.getName());
                }
            });
            
            list.addAll(tempList);
        }
    
        return list;
    }

    /**
     * 上传到OSS
     *
     * @return
     */
    private Map<String, String> uploadExcelToOSS(ByteArrayOutputStream bos) throws Exception {
        
        String basePath="saas/report/";

        String fileName = basePath+IdUtil.simpleUUID() + StrUtil.DOT + EXCEL_TYPE;
        String bucketName = storageConfig.getBucketName();
        Map<String, String> resultMap = new HashMap<>(2);
        resultMap.put(CommonConstant.BUCKET_NAME, bucketName);
        resultMap.put(CommonConstant.FILE_NAME, fileName);

        storageService.uploadFile(bucketName, fileName, new ByteArrayInputStream(bos.toByteArray()));
        return resultMap;
    }

    private ReportManagement buildReportManagement() {

        ReportManagement reportManagement = new ReportManagement();
        reportManagement.setJobId(IdUtil.simpleUUID());
        reportManagement.setType(ReportManagement.TYPE_WARN_MESSAGE);
        reportManagement.setStatus(ReportManagement.STATUS_INIT);
        reportManagement.setTenantId(TenantContextHolder.getTenantId());
        reportManagement.setDelFlag(ReportManagement.DEL_NORMAL);
        reportManagement.setCreateTime(System.currentTimeMillis());
        return reportManagement;
    }

    private ReportManagement buildReportManagementUpdate(Long id, Integer status, String url) {
        ReportManagement reportManagement = new ReportManagement();
        reportManagement.setId(id);
        reportManagement.setStatus(status);
        reportManagement.setUrl(url);
        reportManagement.setUpdateTime(System.currentTimeMillis());
        return reportManagement;
    }

    private Map<String, String> formatQueryDate(Long startTime, Long endTime) {
        Map<String, String> result = new HashMap<>(2);
        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(startTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);

        result.put(BEGIN_TIME, begin);
        result.put(END_TIME, end);

        return result;
    }

}
