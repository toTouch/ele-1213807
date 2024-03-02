package com.xiliulou.electricity.service.impl.merchant;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.EleChargeConfigCalcDetailDto;
import com.xiliulou.electricity.entity.ElePower;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
        if (StringUtils.isBlank(monthDate) || !monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return Collections.emptyList();
        }
        
        // 数据库存的是yyyy-MM-01
        request.setMonthDate(monthDate + "-01");
        
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
        if (StringUtils.isBlank(monthDate) || !monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return NumberConstant.ZERO;
        }
        
        // 数据库存的是yyyy-MM-01
        request.setMonthDate(monthDate + "-01");
        
        MerchantPowerQueryModel queryModel = new MerchantPowerQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        return merchantCabinetPowerMonthRecordMapper.countTotal(queryModel);
    }
    
    @Slave
    @Override
    public void exportExcel(MerchantPowerRequest request, HttpServletResponse response) {
        String monthDate = request.getMonthDate();
        //年月格式校验，判断date是否yyyy-MM格式
        if (StringUtils.isBlank(monthDate) || !monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
            return;
        }
        
        MerchantPowerDetailQueryModel queryModel = new MerchantPowerDetailQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        queryModel.setTenantId(TenantContextHolder.getTenantId());
        
        // 数据库存的是yyyy-MM-01
        queryModel.setMonthDate(monthDate + "-01");
        
        List<MerchantCabinetPowerMonthDetailVO> detailList = merchantCabinetPowerMonthDetailService.listByTenantId(queryModel);
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        
        List<MerchantCabinetPowerMonthExcelVO> excelVOList = new ArrayList<>();
        
        // 按场地进行分组
        Map<Long, List<MerchantCabinetPowerMonthDetailVO>> placeMap = detailList.stream().collect(Collectors.groupingBy(MerchantCabinetPowerMonthDetailVO::getPlaceId));
        
        placeMap.forEach((placeId, placeDetailList) -> {
            
            // 求和
            Double monthSumPower = placeDetailList.stream().mapToDouble(MerchantCabinetPowerMonthDetailVO::getSumPower).sum();
            Double monthSumCharge = placeDetailList.stream().mapToDouble(MerchantCabinetPowerMonthDetailVO::getSumCharge).sum();
            
            if (CollectionUtils.isNotEmpty(placeDetailList)) {
                
                placeDetailList.forEach(item -> {
                    
                    String beginDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getBeginTime());
                    String endDate = DateUtils.getYearAndMonthAndDayByTimeStamps(item.getEndTime());
                    AtomicReference<String> elePrice = new AtomicReference<>("");
                    
                    List<EleChargeConfigCalcDetailDto> chargeConfigList = JsonUtil.fromJsonArray(item.getJsonRule(), EleChargeConfigCalcDetailDto.class);
                    if (CollectionUtils.isNotEmpty(chargeConfigList)) {
                        if (Objects.equals(chargeConfigList.size(), NumberConstant.ONE)) {
                            EleChargeConfigCalcDetailDto detail = chargeConfigList.get(NumberConstant.ZERO);
                            elePrice.set(String.valueOf(detail.getPrice()));
                        } else {
                            chargeConfigList.forEach(detail -> {
                                switch (detail.getType()) {
                                    case ElePower.ORDINARY_TYPE:
                                        elePrice.set(detail.getPrice() + "(平)");
                                        break;
                                    case ElePower.PEEK_TYPE:
                                        elePrice.set(detail.getPrice() + "(峰)");
                                        break;
                                    case ElePower.VALLEY_TYPE:
                                        elePrice.set(detail.getPrice() + "(谷)");
                                        break;
                                    default:
                                        break;
                                }
                            });
                        }
                    }
                    
                    MerchantCabinetPowerMonthExcelVO excelVO = MerchantCabinetPowerMonthExcelVO.builder().monthDate(monthDate)
                            .placeName(Optional.ofNullable(merchantPlaceService.queryByIdFromCache(item.getPlaceId()).getName()).orElse("")).monthSumPower(monthSumPower)
                            .monthSumCharge(monthSumCharge).endPower(item.getEndPower()).sumCharge(item.getSumCharge()).endTime(endDate).beginTime(beginDate)
                            .startPower(item.getStartPower()).sumPower(item.getSumPower()).jsonRule(String.valueOf(elePrice)).sn(item.getSn()).build();
                    
                    excelVOList.add(excelVO);
                });
            }
        });
        
        String fileName = "场地电费出账记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
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
                    .sheet("场地电费出账记录").doWrite(excelVOList);
        } catch (Exception e) {
            log.error("导出报表失败！", e);
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
    
