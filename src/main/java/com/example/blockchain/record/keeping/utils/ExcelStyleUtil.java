package com.example.blockchain.record.keeping.utils;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

public class ExcelStyleUtil {

    public static HorizontalCellStyleStrategy certificateStyleStrategy() {
        // Header style
        WriteCellStyle headStyle = new WriteCellStyle();
        WriteFont headFont = new WriteFont();
        headFont.setFontName("Times New Roman");
        headFont.setFontHeightInPoints((short) 13);
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Content style
        WriteCellStyle contentStyle = new WriteCellStyle();
        WriteFont contentFont = new WriteFont();
        contentFont.setFontName("Times New Roman");
        contentFont.setFontHeightInPoints((short) 12);
        contentStyle.setWriteFont(contentFont);
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    public static CellWriteHandler centerAlignFirstColumn() {
        return new CellWriteHandler() {
            @Override
            public void afterCellDispose(CellWriteHandlerContext context) {
                if (context.getColumnIndex() == 0 && context.getCell() != null) {
                    Cell cell = context.getCell();
                    CellStyle originalStyle = cell.getCellStyle();

                    // Tạo style mới để tránh ghi đè các style khác
                    Workbook workbook = context.getWriteSheetHolder().getSheet().getWorkbook();
                    CellStyle newStyle = workbook.createCellStyle();
                    newStyle.cloneStyleFrom(originalStyle); // Giữ lại các định dạng khác

                    newStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
                    newStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc nếu cần

                    cell.setCellStyle(newStyle);
                }
            }
        };
    }
}

