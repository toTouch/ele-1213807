package com.xiliulou.electricity.service.excel;

import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * excel自适应宽度
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-08-19:30
 */
public class AutoHeadColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {
    
    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<CellData> list, Cell cell, Head head,
            Integer integer, Boolean isHead) {
        
        if (isHead) {
            int length = cell.getStringCellValue().getBytes().length;
            writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), length * 300);
        }
        
    }
}
