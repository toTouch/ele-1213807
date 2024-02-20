package com.xiliulou.electricity.service.impl.merchant;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthRecordMapper;
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
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeMonthRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    
    private List<MerchantPlaceFeeMonthRecordVO> getData(String monthDate) {
        
        List<MerchantPlaceFeeMonthRecordVO> merchantPlaceFeeMonthRecords = merchantPlaceFeeMonthRecordService.selectByMonthDate(date, TenantContextHolder.getTenantId());
        merchantPlaceFeeMonthRecords.parallelStream().forEach(merchantPlaceFeeMonthRecord -> {
            Long placeId = merchantPlaceFeeMonthRecord.getPlaceId();
            MerchantPlace merchantPlace = merchantPlaceService.queryFromCacheById(placeId);
            if (Objects.nonNull(merchantPlace)) {
                merchantPlaceFeeMonthRecord.setPlaceName(merchantPlace.getName());
            }
        });
        return merchantPlaceFeeMonthRecords;
    }
    
    @Override
    public void export(String monthDate, HttpServletResponse response) {
        
        String fileName = "场地费出账记录.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            EasyExcel.write(fileName).head(getHeader())
                    // 合并策略：合并相同数据的行。第一个参数表示从哪一行开始进行合并，由于表头占了两行，因此从第2行开始（索引从0开始）
                    // 第二个参数是指定哪些列要进行合并
                    .registerWriteHandler(new MergeSameRowsStrategy(2, new int[] {0, 1, 2, 3})).registerWriteHandler(HeadContentCellStyle.myHorizontalCellStyleStrategy())
                    .registerWriteHandler(new CommentWriteHandler(getComments(), "xlsx")).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy())
                    // 注意：需要先调用registerWriteHandler()再调用sheet()方法才能使合并策略生效！！！
                    .sheet("场地费出账记录").doWrite(getData(monthDate));
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
    
    @Override
    public R page(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel) {
        List<MerchantPlaceFeeMonthSummaryRecord> merchantPlaceFeeMonthSummaryRecords = merchantPlaceFeeMonthSummaryRecordService.selectByCondition(queryModel);
        if (DataUtil.collectionIsUsable(merchantPlaceFeeMonthSummaryRecords)) {
            return R.ok(merchantPlaceFeeMonthSummaryRecords);
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
