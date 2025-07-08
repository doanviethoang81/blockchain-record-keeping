package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateExcelDTO;

import java.util.List;

public class ExportCertificateExample {
    public static void export(List<CertificateExcelDTO> dataList, String filePath) {
        EasyExcel.write(filePath, CertificateExcelDTO.class)
                .sheet("Danh sách chứng chỉ")
                .doWrite(dataList);
    }
}
