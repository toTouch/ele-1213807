package com.xiliulou.electricity.service.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.xiliulou.electricity.constant.NumberConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author HeYafeng
 * @description 商户导出excel单元格合策略
 * @date 2024/5/28 09:51:04
 */
@Slf4j
public class MerchantMergeStrategy extends AbstractMergeStrategy {
    
    private final Integer startRow;
    
    private final Integer maxRow;
    
    private final Set<Integer> needMergeCols = Set.of(1, 2, 3);
    
    private Boolean col0Merged = false;
    
    private final Map<Integer, MergeRange> mergeRangeMap = new HashMap<>();
    
    
    /**
     * @param startRow 开始行
     * @param maxRow   数据集大小，用于区别结束行位置
     **/
    public MerchantMergeStrategy(Integer startRow, Integer maxRow) {
        this.startRow = startRow;
        this.maxRow = maxRow;
    }
    
    /**
     * 每行每列都会进入，绝对不要在这写循环
     */
    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        try {
            //当前列
            int currentCellIndex = cell.getColumnIndex();
            
            // 合并列0
            if (Objects.equals(0, currentCellIndex) && !col0Merged) {
                executeMergeRange(sheet, new MergeRange(startRow, maxRow + 1, 0, 0, null));
                col0Merged = true;
                return;
            }
            
            // 不是1或2或3列，直接返回
            if (!needMergeCols.contains(currentCellIndex)) {
                return;
            }
            
            // 由于列1和列2和列3的合并策略相同，并且单元格的读取顺序是：从左到右、从上到下，所以当列3合并时，列1和列2必然可以合并。因此这里存列3的合并范围即可：key-3，value-合并范围
            if (Objects.equals(currentCellIndex, NumberConstant.THREE)) {
                
                int currentRowIndex = cell.getRowIndex();
                String currentCol1Value = getCurrentCol1Value(cell);
                
                if (!mergeRangeMap.containsKey(currentCellIndex) && Objects.nonNull(currentCol1Value)) {
                    mergeRangeMap.put(currentCellIndex, new MergeRange(currentRowIndex, currentRowIndex, currentCellIndex, currentCellIndex, currentCol1Value));
                    return;
                }
                
                MergeRange mergeRange = mergeRangeMap.get(currentCellIndex);
                // 列1的值与Map中key的值不同时，执行合并策略
                if (Objects.nonNull(currentCol1Value) && !Objects.equals(currentCol1Value, mergeRange.getPreCol1Value())) {
                    handleMerge(sheet, mergeRange, currentCol1Value);
                    // 更新起始行位置
                    mergeRangeMap.put(currentCellIndex, new MergeRange(currentRowIndex, currentRowIndex, currentCellIndex, currentCellIndex, currentCol1Value));
                }
                
                // 合并后的行数加1
                mergeRange.endRow += 1;
                
                // 触发最后一次没完成的合并
                if (relativeRowIndex.equals(maxRow + 1)) {
                    MergeRange lastMergeRange = mergeRangeMap.get(currentCellIndex);
                    // 合并列1列2列3
                    handleMerge(sheet, lastMergeRange, currentCol1Value);
                }
            }
        } catch (Exception e) {
            log.error("Merge cells error!", e);
        }
    }
    
    
    private void handleMerge(Sheet sheet, MergeRange mergeRange, String currentCol1Value) {
        // 合并列1
        executeMergeRange(sheet, new MergeRange(mergeRange.startRow, mergeRange.endRow, mergeRange.startCol - 2, mergeRange.endCol - 2, currentCol1Value));
        // 合并列2
        executeMergeRange(sheet, new MergeRange(mergeRange.startRow, mergeRange.endRow, mergeRange.startCol - 1, mergeRange.endCol - 1, currentCol1Value));
        // 合并列3
        executeMergeRange(sheet, new MergeRange(mergeRange.startRow, mergeRange.endRow, mergeRange.startCol, mergeRange.endCol, currentCol1Value));
    }
    
    private void executeMergeRange(Sheet sheet, MergeRange mergeRange) {
        // 同行同列不能合并，会抛异常
        if (!Objects.equals(mergeRange.startRow, mergeRange.endRow) || !Objects.equals(mergeRange.startCol, mergeRange.endCol)) {
            sheet.addMergedRegion(new CellRangeAddress(mergeRange.startRow, mergeRange.endRow, mergeRange.startCol, mergeRange.endCol));
        }
    }
    
    private String getCurrentCol1Value(Cell cell) {
        if (cell.getColumnIndex() >= 1) {
            Cell preCell = cell.getSheet().getRow(cell.getRowIndex()).getCell(1);
            convertCellType(preCell);
            return preCell.getStringCellValue();
        }
        return null;
    }
    
    private void convertCellType(Cell cell) {
        if (Objects.equals(CellType.NUMERIC, cell.getCellTypeEnum())) {
            cell.setCellType(CellType.STRING);
        }
    }
    
}

@Data
class MergeRange {
    
    int startRow;
    
    int endRow;
    
    int startCol;
    
    int endCol;
    
    String preCol1Value;
    
    public MergeRange(int startRow, int endRow, int startCol, int endCol, String preCol1Value) {
        this.startRow = startRow;
        this.endRow = endRow;
        this.startCol = startCol;
        this.endCol = endCol;
        this.preCol1Value = preCol1Value;
    }
    
}
