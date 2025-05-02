package com.example.blockchain.record.keeping.excels;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter implements Converter<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDate.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDate convertToJavaData(ReadConverterContext<?> context) {
        ReadCellData<?> cellData = context.getReadCellData();

        try {
            if (cellData.getType() == CellDataTypeEnum.NUMBER) {
                // Excel date stored as numeric value
                double excelDate = cellData.getNumberValue().doubleValue();
                // Convert from Excel numeric date to LocalDate
                return LocalDate.of(1899, 12, 30).plusDays((long) excelDate);
            } else {
                String value = cellData.getStringValue();
                return LocalDate.parse(value, FORMATTER);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi định dạng ngày: " + cellData.getStringValue() + ". Định dạng đúng là dd/MM/yyyy");
        }
    }


}
