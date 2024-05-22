package com.xiliulou.electricity.service.impl.merchant;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.EleChargeConfigCalcDetailDto;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetPowerMonthRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPowerDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPowerQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPowerRequest;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.excel.CommentWriteHandler;
import com.xiliulou.electricity.service.excel.HeadContentCellStyle;
import com.xiliulou.electricity.service.excel.MergeSameRowsStrategy;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthExcelVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author HeYafeng
 * @description 电费月度统计
 * @date 2024/2/20 12:36:09
 */
@Slf4j
@Service
public class MerchantCabinetPowerMonthRecordServiceImpl implements MerchantCabinetPowerMonthRecordService {
    
    @Resource
    MerchantCabinetPowerMonthRecordMapper merchantCabinetPowerMonthRecordMapper;
    
    @Resource
    private MerchantCabinetPowerMonthDetailService merchantCabinetPowerMonthDetailService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Slave
    @Override
    public List<MerchantCabinetPowerMonthRecordVO> listByPage(MerchantPowerRequest request) {
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isNotBlank(monthDate) && monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            // 数据库存的是yyyy-MM-01
            request.setMonthDate(monthDate + "-01");
        }
        
        MerchantPowerQueryModel queryModel = new MerchantPowerQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantCabinetPowerMonthRecordMapper.selectListByPage(queryModel);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPowerRequest request) {
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isNotBlank(monthDate) && monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            // 数据库存的是yyyy-MM-01
            request.setMonthDate(monthDate + "-01");
        }
        
        MerchantPowerQueryModel queryModel = new MerchantPowerQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantCabinetPowerMonthRecordMapper.countTotal(queryModel);
    }
    
    @Slave
    private List<MerchantCabinetPowerMonthExcelVO> getData(MerchantPowerRequest request) {
        List<MerchantCabinetPowerMonthExcelVO> excelVOList = new ArrayList<>();
        
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isBlank(monthDate) || !monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return excelVOList;
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        MerchantPowerDetailQueryModel queryModel = new MerchantPowerDetailQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(tenantId);
        
        // 数据库存的是yyyy-MM-01
        queryModel.setMonthDate(monthDate + "-01");
        
        List<MerchantCabinetPowerMonthDetailVO> detailList = merchantCabinetPowerMonthDetailService.listByTenantId(queryModel);
        if (CollectionUtils.isEmpty(detailList)) {
            return excelVOList;
        }
        
        // 获取场地名称
        Set<Long> placeIdSet = detailList.stream().filter(Objects::nonNull).map(MerchantCabinetPowerMonthDetailVO::getPlaceId).collect(Collectors.toSet());
        Map<Long, String> placeNameMap = listPlaceNames(placeIdSet, tenantId);
        
        // startPower或sumPower不为0的数据
        List<MerchantCabinetPowerMonthDetailVO> hasDataDetailList = detailList.stream()
                .filter(item -> !Objects.equals(item.getStartPower(), NumberConstant.ZERO_D) || !Objects.equals(item.getSumPower(), NumberConstant.ZERO_D))
                .collect(Collectors.toList());
        
        // 按场地进行分组
        Map<Long, List<MerchantCabinetPowerMonthDetailVO>> placeMap = hasDataDetailList.stream().collect(Collectors.groupingBy(MerchantCabinetPowerMonthDetailVO::getPlaceId));
        
        placeMap.forEach((placeId, placeDetailList) -> {
            if (CollectionUtils.isEmpty(placeDetailList)) {
                return;
            }
            
            // 排序
            placeDetailList.sort(Comparator.comparing(MerchantCabinetPowerMonthDetailVO::getEid));
            // 求和
            Double monthSumPower = placeDetailList.stream().mapToDouble(MerchantCabinetPowerMonthDetailVO::getSumPower).sum();
            Double monthSumCharge = placeDetailList.stream().mapToDouble(MerchantCabinetPowerMonthDetailVO::getSumCharge).sum();
            
            placeDetailList.forEach(item -> {
                String beginDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getBeginTime());
                String endDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getEndTime());
                String elePrice;
                
                List<EleChargeConfigCalcDetailDto> chargeConfigList = JsonUtil.fromJsonArray(item.getJsonRule(), EleChargeConfigCalcDetailDto.class);
                if (CollectionUtils.isEmpty(chargeConfigList)) {
                    return;
                }
                
                String elPeekPrice = StringUtils.EMPTY;
                String elOrdinaryPrice = StringUtils.EMPTY;
                String elValleyPrice = StringUtils.EMPTY;
                
                if (Objects.equals(chargeConfigList.size(), NumberConstant.ONE)) {
                    EleChargeConfigCalcDetailDto detail = chargeConfigList.get(NumberConstant.ZERO);
                    elePrice = String.valueOf(detail.getPrice());
                    
                } else {
                    for (EleChargeConfigCalcDetailDto detail : chargeConfigList) {
                        switch (detail.getType()) {
                            case ElePower.ORDINARY_TYPE:
                                elOrdinaryPrice = detail.getPrice() + "(平)";
                                break;
                            case ElePower.PEEK_TYPE:
                                elPeekPrice = detail.getPrice() + "(峰)";
                                break;
                            case ElePower.VALLEY_TYPE:
                                elValleyPrice = detail.getPrice() + "(谷)";
                                break;
                            default:
                                break;
                        }
                    }
                    
                    elePrice = elPeekPrice + elOrdinaryPrice + elValleyPrice;
                }
                
                // 查询场地
                MerchantCabinetPowerMonthExcelVO excelVO = MerchantCabinetPowerMonthExcelVO.builder().monthDate(monthDate).monthSumPower(monthSumPower)
                        .monthSumCharge(monthSumCharge).endPower(item.getEndPower()).sumCharge(item.getSumCharge()).endTime(endDate).beginTime(beginDate)
                        .startPower(item.getStartPower()).sumPower(item.getSumPower()).jsonRule(elePrice).sn(item.getSn()).build();
                
                if (ObjectUtils.isNotEmpty(placeNameMap.get(placeId))) {
                    excelVO.setPlaceName(placeNameMap.get(placeId));
                }
                
                excelVOList.add(excelVO);
            });
        });
        
        // 移除有数据的集合，再处理空数据
        detailList.removeAll(hasDataDetailList);
        if (CollectionUtils.isEmpty(detailList)) {
            return excelVOList;
        }
        
        // 按placeId进行升序
        detailList.sort(Comparator.comparing(MerchantCabinetPowerMonthDetailVO::getPlaceId));
        
        detailList.forEach(item -> {
            Long placeId = item.getPlaceId();
            String beginDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getBeginTime());
            String endDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getEndTime());
            
            MerchantCabinetPowerMonthExcelVO excelVO = MerchantCabinetPowerMonthExcelVO.builder().monthDate(monthDate).endTime(endDate).beginTime(beginDate).build();
            
            if (ObjectUtils.isNotEmpty(placeNameMap.get(placeId))) {
                excelVO.setPlaceName(placeNameMap.get(placeId));
            }
            
            excelVOList.add(excelVO);
        });
        
        return excelVOList;
    }
    
    private Map<Long, String> listPlaceNames(Set<Long> placeIdSet, Integer tenantId) {
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isEmpty(placeIdSet)) {
            return map;
        }
        
        int batchSize = 50;
        ArrayList<Long> placeIdList = new ArrayList<>(placeIdSet);
        ListUtils.partition(placeIdList, batchSize).forEach(ids -> {
            List<MerchantPlace> placeList = merchantPlaceService.queryByIdList(ids, tenantId);
            if (CollectionUtils.isEmpty(placeList)) {
                return;
            }
            
            Map<Long, String> nameMap = placeList.stream().collect(toMap(MerchantPlace::getId, MerchantPlace::getName, (key, key1) -> key1));
            map.putAll(nameMap);
            
        });
        
        return map;
    }
    
    @Override
    public void exportExcel(MerchantPowerRequest request, HttpServletResponse response) {
        String fileName = "场地电费出账记录.xlsx";
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, MerchantCabinetPowerMonthExcelVO.class).head(getHeader())
                    // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                    // 第二个参数是指定哪些列要进行合并
                    .registerWriteHandler(new MergeSameRowsStrategy(2, new int[] {0, 1, 2, 3})).registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                    .registerWriteHandler(new CommentWriteHandler(getComments(), "xlsx")).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                    .sheet("场地电费出账记录").doWrite(getData(request));
        } catch (Exception e) {
            log.error("Merchant power exportExcel error!", e);
        }
    }
    
    /**
     * 创建表头
     */
    private static List<List<String>> getHeader() {
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("出账年月", "出账年月"));
        headers.add(Arrays.asList("场地汇总", "场地名称"));
        headers.add(Arrays.asList("场地汇总", "月用电量(度)"));
        headers.add(Arrays.asList("场地汇总", "月电费(元)"));
        headers.add(Arrays.asList("柜机明细", "柜机编号"));
        headers.add(Arrays.asList("柜机明细", "开始度数"));
        headers.add(Arrays.asList("柜机明细", "结束度数"));
        headers.add(Arrays.asList("柜机明细", "用电量(度)"));
        headers.add(Arrays.asList("柜机明细", "单价(元)"));
        headers.add(Arrays.asList("柜机明细", "月电费(元)"));
        headers.add(Arrays.asList("柜机明细", "开始时间"));
        headers.add(Arrays.asList("柜机明细", "结束时间"));
        return headers;
    }
    
    private static List<Map<String, String>> getComments() {
        List<Map<String, String>> commentList = new ArrayList<>();
        commentList.add(CommentWriteHandler.createCommentMap("场地电费出账记录", 1, 8, "当前显示的单价为本月最新数值，若本月电价有调整，系统会自动分段计算月电费，故该列仅供参考。"));
        return commentList;
    }
}
    
