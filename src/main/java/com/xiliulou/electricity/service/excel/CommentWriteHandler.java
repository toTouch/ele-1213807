package com.xiliulou.electricity.service.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.AbstractRowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class CommentWriteHandler extends AbstractRowWriteHandler {
    
    /**
     * sheet名称KEY
     */
    public static final String SHEETNAME_NAME = "sheetName";
    
    /**
     * 文档后缀名
     */
    private String extension;
    
    /**
     * 列索引key
     */
    public static final String COLINDEX_NAME = "8";
    
    /**
     * 行索引key
     */
    public static final String ROWINDEX_NAME = "1";
    
    /**
     * 批注内容key
     */
    public static final String COMMENTCONTENT_NAME = "commentContent";
    
    /**
     * sheet页名称列表
     */
    private List<String> sheetNameList;
    
    List<Map<String, String>> commentList = new ArrayList<>();
    
    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer integer, Integer integer1, Boolean aBoolean) {
    
    }
    
    @Override
    public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer integer, Boolean aBoolean) {
    
    }
    
    public CommentWriteHandler(List<Map<String, String>> commentList, String extension) {
        this.commentList = commentList != null && commentList.size() > 0 ? commentList.stream()
                .filter(x -> x.containsKey(SHEETNAME_NAME) && x.get(SHEETNAME_NAME) != null && StrUtil.isNotBlank(x.get(SHEETNAME_NAME)) && x.containsKey(COLINDEX_NAME)
                        && x.get(COLINDEX_NAME) != null && StrUtil.isNotBlank(x.get(COLINDEX_NAME)) && x.containsKey(ROWINDEX_NAME) && x.get(ROWINDEX_NAME) != null
                        && StrUtil.isNotBlank(x.get(ROWINDEX_NAME)) && x.containsKey(COMMENTCONTENT_NAME) && x.get(COMMENTCONTENT_NAME) != null && StrUtil.isNotBlank(
                        x.get(COMMENTCONTENT_NAME))).collect(Collectors.toList()) : new ArrayList<>();
        sheetNameList = this.commentList.stream().map(x -> x.get(SHEETNAME_NAME)).collect(Collectors.toList());
        this.extension = extension;
    }
    
    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {
        Sheet sheet = writeSheetHolder.getSheet();
        // 不需要添加批注，或者当前sheet页不需要添加批注
        if (commentList == null || commentList.size() <= 0 || !sheetNameList.contains(sheet.getSheetName())) {
            return;
        }
        
        // 获取当前行的批注信息
        List<Map<String, String>> rowCommentList = commentList.stream()
                .filter(x -> StrUtil.equals(x.get(SHEETNAME_NAME), sheet.getSheetName()) && relativeRowIndex == Integer.parseInt(x.get(ROWINDEX_NAME)))
                .collect(Collectors.toList());
        
        // 当前行没有批注信息
        if (rowCommentList.size() <= 0) {
            return;
        }
        List<String> colIndexList = rowCommentList.stream().map(x -> x.get(COLINDEX_NAME)).distinct().collect(Collectors.toList());
        for (String colIndex : colIndexList) {
            // 同一单元格的批注信息
            List<Map<String, String>> cellCommentList = rowCommentList.stream().filter(x -> StrUtil.equals(colIndex, x.get(COLINDEX_NAME))).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(cellCommentList)) {
                continue;
            }
            // 批注内容拼成一条
            String commentContent = cellCommentList.stream().map(x -> x.get(COMMENTCONTENT_NAME)).collect(Collectors.joining());
            Cell cell = row.getCell(Integer.parseInt(colIndex));
            if(Objects.equals(1, cell.getRowIndex())){
                addComment(cell, commentContent, extension);
            }
        }
        // 重新获取要添加的sheet页姓名
        sheetNameList = commentList.stream().map(x -> x.get(SHEETNAME_NAME)).collect(Collectors.toList());
    }
    
    /**
     * 生成批注信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param commentContent 批注内容
     */
    public static Map<String, String> createCommentMap(String sheetName, int rowIndex, int columnIndex, String commentContent) {
        Map<String, String> map = new HashMap<>();
        // sheet页名称
        map.put(SHEETNAME_NAME, sheetName);
        // 行号
        map.put(ROWINDEX_NAME, rowIndex + "");
        // 列号
        map.put(COLINDEX_NAME, columnIndex + "");
        // 批注内容
        map.put(COMMENTCONTENT_NAME, commentContent);
        return map;
    }
    
    /**
     * 给Cell添加批注
     *
     * @param cell      单元格
     * @param value     批注内容
     * @param extension 扩展名
     */
    public static void addComment(Cell cell, String value, String extension) {
        Sheet sheet = cell.getSheet();
        cell.removeCellComment();
        if ("xls".equals(extension)) {
            ClientAnchor anchor = new HSSFClientAnchor();
            // 关键修改
            anchor.setDx1(0);
            anchor.setDx2(0);
            anchor.setDy1(0);
            anchor.setDy2(0);
            anchor.setCol1(cell.getColumnIndex());
            anchor.setRow1(cell.getRowIndex());
            anchor.setCol2(cell.getColumnIndex() + 5);
            anchor.setRow2(cell.getRowIndex() + 6);
            // 结束
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            Comment comment = drawing.createCellComment(anchor);
            // 输入批注信息
            comment.setString(new HSSFRichTextString(value));
            // 将批注添加到单元格对象中
            cell.setCellComment(comment);
        } else if ("xlsx".equals(extension)) {
            ClientAnchor anchor = new XSSFClientAnchor();
            // 关键修改
            anchor.setDx1(0);
            anchor.setDx2(0);
            anchor.setDy1(0);
            anchor.setDy2(0);
            anchor.setCol1(cell.getColumnIndex());
            anchor.setRow1(cell.getRowIndex());
            anchor.setCol2(cell.getColumnIndex() + 5);
            anchor.setRow2(cell.getRowIndex() + 6);
            // 结束
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            Comment comment = drawing.createCellComment(anchor);
            // 输入批注信息
            comment.setString(new XSSFRichTextString(value));
            // 将批注添加到单元格对象中
            cell.setCellComment(comment);
        }
    }
}