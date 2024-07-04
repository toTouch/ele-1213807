package com.xiliulou.electricity.service.impl.merchant;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.api.client.util.Lists;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.merchant.MerchantPlaceFeeMonthRecordDTO;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.service.excel.CommentWriteHandler;
import com.xiliulou.electricity.service.excel.HeadContentCellStyle;
import com.xiliulou.electricity.service.excel.MergeSameRowsStrategy;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthSummaryRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeSettlementService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordExcelVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthRecordExportVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthSummaryRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @ClassName : MerchantPlaceFeeSettlementServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
@Slf4j
@Service
public class MerchantPlaceFeeSettlementServiceImpl implements MerchantPlaceFeeSettlementService {
    
    @Resource
    private MerchantPlaceFeeMonthRecordService merchantPlaceFeeMonthRecordService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantPlaceFeeMonthSummaryRecordService merchantPlaceFeeMonthSummaryRecordService;
    
    private List<MerchantPlaceFeeMonthRecordExportVO> getData(String monthDate) {
        
        List<MerchantPlaceFeeMonthRecordExportVO> resultVOs = new ArrayList<>();
        List<MerchantPlaceFeeMonthRecord> merchantPlaceFeeMonthRecords = merchantPlaceFeeMonthRecordService.selectByMonthDate(monthDate, TenantContextHolder.getTenantId());
        if (CollectionUtils.isEmpty(merchantPlaceFeeMonthRecords)) {
            return resultVOs;
        }
        
        // 根据场地id分组 并monthPlaceFee求和
        Map<Long, List<MerchantPlaceFeeMonthRecord>> placeIdListMap = merchantPlaceFeeMonthRecords.stream().filter(item -> Objects.nonNull(item.getPlaceId()))
                .collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getPlaceId));
    
        Map<Long, String> placeNameMap = new HashMap<>();
        
        if (ObjectUtils.isNotEmpty(placeIdListMap)) {
            List<Long> placeIdList = new ArrayList<>(placeIdListMap.keySet());
            List<MerchantPlace> placeList = merchantPlaceService.queryByIdList(placeIdList, TenantContextHolder.getTenantId());
            
            if (ObjectUtils.isNotEmpty(placeList)) {
                placeNameMap = placeList.stream().collect(toMap(MerchantPlace::getId, MerchantPlace::getName, (key, key1) -> key1));
            }
        }
        
        List<MerchantPlaceFeeMonthRecordDTO> recordDTOList = Lists.newArrayList();
        
        placeIdListMap.forEach((placeId, merchantPlaceFeeMonthRecordList) -> {
            
            // 租赁天数求和
            Integer rentDays = merchantPlaceFeeMonthRecords.stream()
                    .filter(item -> Objects.nonNull(item.getRentDays()) && Objects.nonNull(item.getPlaceId()) && Objects.equals(item.getPlaceId(), placeId))
                    .map(MerchantPlaceFeeMonthRecord::getRentDays).reduce(0, Integer::sum);
            
            //月场地费求和
            BigDecimal monthPlaceFee = merchantPlaceFeeMonthRecords.stream()
                    .filter(item -> Objects.nonNull(item.getMonthPlaceFee()) && Objects.nonNull(item.getPlaceId()) && Objects.equals(item.getPlaceId(), placeId))
                    .map(MerchantPlaceFeeMonthRecord::getMonthPlaceFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            MerchantPlaceFeeMonthRecordDTO dto = new MerchantPlaceFeeMonthRecordDTO();
            
            dto.setMonthDate(merchantPlaceFeeMonthRecordList.get(0).getMonthDate());
            dto.setPlaceId(placeId);
            dto.setTenantId(merchantPlaceFeeMonthRecordList.get(0).getTenantId());
            dto.setMonthPlaceFee(monthPlaceFee);
            dto.setMonthRentDays(rentDays);
            recordDTOList.add(dto);
        });
        
        // recordDTOList转map
        Map<Long, MerchantPlaceFeeMonthRecordDTO> recordMap = recordDTOList.stream().collect(toMap(MerchantPlaceFeeMonthRecordDTO::getPlaceId, item -> item));
    
        Map<Long, String> finalPlaceNameMap = placeNameMap;
        
        resultVOs = merchantPlaceFeeMonthRecords.parallelStream().map(merchantPlaceFeeMonthRecord -> {
            MerchantPlaceFeeMonthRecordExportVO exportVO = new MerchantPlaceFeeMonthRecordExportVO();
            BeanUtils.copyProperties(merchantPlaceFeeMonthRecord, exportVO);
            exportVO.setRentStartTime(
                    Objects.nonNull(merchantPlaceFeeMonthRecord.getRentStartTime()) ? DateUtils.getYearAndMonthAndDayByTimeStamps(merchantPlaceFeeMonthRecord.getRentStartTime())
                            : null);
            exportVO.setRentEndTime(
                    Objects.nonNull(merchantPlaceFeeMonthRecord.getRentEndTime()) ? DateUtils.getYearAndMonthAndDayByTimeStamps(merchantPlaceFeeMonthRecord.getRentEndTime())
                            : null);
            Long recordPlaceId = merchantPlaceFeeMonthRecord.getPlaceId();
            
            if (ObjectUtils.isNotEmpty(finalPlaceNameMap.get(recordPlaceId))) {
                exportVO.setPlaceName(finalPlaceNameMap.get(recordPlaceId));
            }
            
            if (Objects.nonNull(merchantPlaceFeeMonthRecord.getPlaceId())) {
                MerchantPlaceFeeMonthRecordDTO merchantPlaceFeeMonthRecordDTO = recordMap.get(merchantPlaceFeeMonthRecord.getPlaceId());
                if (Objects.nonNull(merchantPlaceFeeMonthRecordDTO) && Objects.equals(merchantPlaceFeeMonthRecord.getPlaceId(), merchantPlaceFeeMonthRecordDTO.getPlaceId())) {
                    exportVO.setMonthTotalPlaceFee(merchantPlaceFeeMonthRecordDTO.getMonthPlaceFee());
                    exportVO.setMonthRentDays(merchantPlaceFeeMonthRecordDTO.getMonthRentDays());
                }
            }
            return exportVO;
        }).collect(Collectors.toList());
        
        return resultVOs;
    }
    
    @Override
    public void export(String monthDate, HttpServletResponse response) {
        
        String fileName = "场地费出账记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application /vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(outputStream, MerchantPlaceFeeMonthRecordExportVO.class).head(getHeader())
                    // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                    // 第二个参数是指定哪些列要进行合并
                    .registerWriteHandler(new MergeSameRowsStrategy(2, new int[] {0, 1, 2, 3})).registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                    .registerWriteHandler(new CommentWriteHandler(getComments(), "xlsx")).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                    .sheet("场地费出账记录").doWrite(getData(monthDate));
        } catch (Exception e) {
            log.error("导出报表失败！", e);
        }
    }
    
   /* public static void main(String[] args) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\excel\\test.xls"));
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("企业名称", "企业名称"));
        headers.add(Arrays.asList("支出/收入", "支出/收入"));
        headers.add(Arrays.asList("消费类型", "消费类型"));
        headers.add(Arrays.asList("云豆数", "云豆数"));
        headers.add(Arrays.asList("消费时间", "消费时间"));
        List<CloudBeanUseRecordExcelVO> list = new ArrayList<>();
        CloudBeanUseRecordExcelVO vo = new CloudBeanUseRecordExcelVO();
        vo.setEnterpriseName("111");
        vo.setIncomeAndExpend("收入");
        list.add(vo);
    
        List<CloudBeanUseRecordExcelVO> list1 = new ArrayList<>();
        CloudBeanUseRecordExcelVO vo1 = new CloudBeanUseRecordExcelVO();
        vo1.setEnterpriseName("112");
        vo1.setIncomeAndExpend("收入");
        list.add(vo1);
    
        *//*EasyExcel.write(outputStream, CloudBeanUseRecordExcelVO.class).head(headers)
                // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                // 第二个参数是指定哪些列要进行合并
                .registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                .registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                .withTemplate(new File("D:\\excel\\企业账单模版.xls"))
                // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                .sheet("场地费出账记录").doWrite(list);
    
        EasyExcel.write(outputStream, CloudBeanUseRecordExcelVO.class).head(headers)
                // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                // 第二个参数是指定哪些列要进行合并
                .registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                .registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                                .withTemplate(new File("D:\\excel\\企业账单模版.xls"))
                // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                .sheet("场地费出账记录").doWrite(list1);*//*
    
            // 打开 Excel 文件
       *//* String fileName = "D:\\excel\\企业账单模版 - 副本.xls";
        ExcelWriter excelWriter = EasyExcel.write(fileName).build();
    
        // 引用形式的描述信息：设置Sheet名
        WriteSheet sheet = EasyExcel.writerSheet(0, "Sheet1").build();
        sheet.setSheetName("My Sheet");
    
        // 引用形式的描述信息：导出Excel文件
        excelWriter.write(list, sheet);
        excelWriter.finish();*//*
        *//*ExcelReader excelReader = EasyExcel.read("D:\\excel\\企业账单模版 - 副本.xls").build();
        Sheet sheet = excelReader.getSheets().get(0);
        sheet.setSheetName("场地费出账记录");
        ExcelWriter excelWriter = EasyExcel.write(outputStream)
                .build();
        excelWriter.fill(list, sheet);*//*
//        excelWriter.fill(list, EasyExcel.writerSheet(0, "场地费出账记录").head(CloudBeanUseRecordExcelVO.class).build());
//        excelWriter.write(list1, EasyExcel.writerSheet(1, "场地费出账记录1").head(CloudBeanUseRecordExcelVO.class).build());
    
//        excelWriter.finish();
    
//        EasyExcel.write(outputStream).withTemplate("D:\\excel\\企业账单模版 - 副本.xls").sheet().doFill(list1);
        ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate("D:\\excel\\企业账单模版 - 副本.xls")
                .build();
        excelWriter.fill(list, EasyExcel.writerSheet(0, "场地费出账记录").build());
        excelWriter.write(list, EasyExcel.writerSheet(1, "场地费出账记录").build());
        excelWriter.finish();
    }*/
   public static void main(String[] args) throws IOException, InvalidFormatException {
       List<CloudBeanUseRecordExcelVO> list = new ArrayList<>();
       CloudBeanUseRecordExcelVO vo = new CloudBeanUseRecordExcelVO();
       vo.setEnterpriseName("111");
       vo.setIncomeAndExpend("收入");
       list.add(vo);
    
       List<CloudBeanUseRecordExcelVO> list1 = new ArrayList<>();
       CloudBeanUseRecordExcelVO vo1 = new CloudBeanUseRecordExcelVO();
       vo1.setEnterpriseName("112");
       vo1.setIncomeAndExpend("收入");
       list1.add(vo1);
       Map<String, List<CloudBeanUseRecordExcelVO>> map = new HashMap<>();
       map.put("crystal", list);
       map.put("crystal1", list1);
       FileOutputStream outputStream = new FileOutputStream(new File("D:\\excel\\test.xls"));
//       String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
      // InputStream stream = MerchantPlaceFeeSettlementServiceImpl.class.getClassLoader().getResourceAsStream("exceltemplate" + File.separator + "企业账单模版.xlsx");
       ClassPathResource classPathResource = new ClassPathResource("excelTemplate" + File.separator + "company.xlsx");
//       InputStream stream = MerchantPlaceFeeSettlementServiceImpl.class.getClassLoader()
//               .getResourceAsStream("exceltemplate" + File.separator + "企业账单模板.xlsx");
       InputStream stream = classPathResource.getInputStream();
       List<String> sheetNameList = new ArrayList<>();
       sheetNameList.add("crystal");
       sheetNameList.add("crystal1");
       
//       InputStream stream = new FileInputStream("D:\\excel\\企业账单模板.xlsx");
       // 把excel流给这个对象，后续可以操作
//       XSSFWorkbook workbook = WorkbookFactory.create(stream);
       XSSFWorkbook workbook = new XSSFWorkbook(stream);
       // 设置模板的第一个sheet的名称，名称我们使用合同号
       workbook.setSheetName(0, sheetNameList.get(0));
       
       
       for (int i = 1; i < sheetNameList.size(); i++) {
           // 剩余的全部复制模板sheet0即可
           workbook.cloneSheet(0, sheetNameList.get(i));
       }
       // 把workbook写到流里
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       workbook.write(baos);
       byte[] bytes = baos.toByteArray();
       stream = new ByteArrayInputStream(bytes);
       ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(stream).build();
    
       for (int i = 0; i < sheetNameList.size(); i++) {
           final String s = sheetNameList.get(i);
           WriteSheet writeSheet = EasyExcel.writerSheet(sheetNameList.get(i)).build();
           // list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
//           FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).direction(WriteDirectionEnum.VERTICAL).build();
           List<CloudBeanUseRecordExcelVO> cloudBeanUseRecordExcelVOS = map.get(sheetNameList.get(i));
           excelWriter.fill(map.get(sheetNameList.get(i)), writeSheet);
           //           excelWriter.fill(new FillWrapper("item", testExcel.getItemList()), fillConfig, writeSheet);
       }
       
       excelWriter.finish();
       baos.close();
       stream.close();
   }
    @Override
    public R page(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel) {
        List<MerchantPlaceFeeMonthSummaryRecord> merchantPlaceFeeMonthSummaryRecords = merchantPlaceFeeMonthSummaryRecordService.selectByCondition(queryModel);
        if (DataUtil.collectionIsUsable(merchantPlaceFeeMonthSummaryRecords)) {
            return R.ok(merchantPlaceFeeMonthSummaryRecords.parallelStream().map(item -> {
                MerchantPlaceFeeMonthSummaryRecordVO vo = new MerchantPlaceFeeMonthSummaryRecordVO();
                BeanUtils.copyProperties(item, vo);
                return vo;
            }).collect(Collectors.toList()));
        } else {
            return R.ok();
        }
    }
    
    @Override
    public R pageCount(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel) {
        return R.ok(merchantPlaceFeeMonthSummaryRecordService.pageCountByCondition(queryModel));
    }
    
    
    /**
     * 创建表头
     */
    private static List<List<String>> getHeader() {
        List<List<String>> headers = new ArrayList<>();
        headers.add(Arrays.asList("出账年月", "出账年月"));
        headers.add(Arrays.asList("场地汇总", "场地名称"));
        headers.add(Arrays.asList("场地汇总", "月租赁天数"));
        headers.add(Arrays.asList("场地汇总", "月场地费(月)"));
        headers.add(Arrays.asList("柜机明细", "柜机编号"));
        headers.add(Arrays.asList("柜机明细", "月租赁天数"));
        headers.add(Arrays.asList("柜机明细", "开始时间"));
        headers.add(Arrays.asList("柜机明细", "结束时间"));
        headers.add(Arrays.asList("柜机明细", "场地费（元/天）"));
        headers.add(Arrays.asList("柜机明细", "月场地费"));
        return headers;
    }
    
    
    private static List<Map<String, String>> getComments() {
        List<Map<String, String>> commentList = new ArrayList<>();
        commentList.add(
                CommentWriteHandler.createCommentMap("场地费出账记录", 1, 8, "当前显示的场地费为本月最新数值，若本月场地费有调整，系统会自动分段计算月场地费，故该列仅供参考。"));
        return commentList;
    }
    
    /**
     * 创建数据
     */
    private static List<List<Object>> getData1() {
        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("2024-01", "XXX", "20.00", "30", "SN10086", "1", "2022-01-25 11:08", "2022-01-27 11:08", "30", "80"));
        data.add(Arrays.asList("2024-01", "XXX", "20.00", "30", "SN10086", "1", "2022-01-25 11:08", "2022-01-27 11:08", "30", "80"));
        data.add(Arrays.asList("2024-01", "XXX", "20.00", "30", "SN10086", "1", "2022-01-25 11:08", "2022-01-27 11:08", "30", "80"));
        data.add(Arrays.asList("2024-01", "SS", "20.00", "30", "SN10087", "1", "2022-01-25 11:08", "2022-01-27 11:08", "30", "80"));
        data.add(Arrays.asList("2024-01", "SS", "20.00", "30", "SN10086", "1", "2022-01-25 11:08", "2022-01-27 11:08", "30", "80"));
        return data;
    }
    
}
