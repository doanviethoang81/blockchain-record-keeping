package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import jakarta.transaction.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CertificateExcelListener extends AnalysisEventListener<CertificateExcelRowDTO> {

    private final List<CertificateExcelRowDTO> dataList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;

    public CertificateExcelListener(StudentRepository studentRepository, CertificateRepository certificateRepository) {
        this.studentRepository = studentRepository;
        this.certificateRepository = certificateRepository;
    }

    @Override
    public void invoke(CertificateExcelRowDTO row, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();

        try {
            Field[] fields = CertificateExcelRowDTO.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    Object value = field.get(row);
                    ExcelProperty property = field.getAnnotation(ExcelProperty.class);
                    String fieldName = property != null ? property.value()[0] : field.getName();

                    if (value == null || (value instanceof String && ((String) value).isBlank())) {
                        errorMessages.add("Dòng "+ rowIndex + ": Đang thiếu dữ liệu!");
                        return;
                    }
                    // Kiểm tra định dạng email
                    if (fieldName.equalsIgnoreCase("Email") && value instanceof String emailValue) {
                        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
                        if (!emailValue.matches(regex)) {
                            errorMessages.add("Dòng "+ rowIndex + ": Email sai định dạng. Giá trị: " + emailValue);
                        }
                    }
                } catch (IllegalAccessException e) {
                    errorMessages.add("Không thể đọc trường: " + field.getName());
                }
            }
            // Nếu không có lỗi thì thêm vào danh sách hợp lệ
            dataList.add(row);
        } catch (Exception e) {
            errorMessages.add("Dòng " + rowIndex + ": Lỗi không xác định - " + e.getMessage());
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();

        if (exception instanceof ExcelDataConvertException convertEx) {
            int colIndex = convertEx.getColumnIndex();
            String cellValue = String.valueOf(convertEx.getCellData().getStringValue());
            errorMessages.add("Dòng " + rowIndex + ": Sai định dạng ngày \"" + cellValue + "\". Định dạng yêu cầu: dd/MM/yyyy");
        } else {
            errorMessages.add("Dòng " + rowIndex + ": " + exception.getMessage());
        }
    }


    @Override
    @Transactional
    public void doAfterAllAnalysed(AnalysisContext context) {
        for (CertificateExcelRowDTO dto : dataList) {
            // 1. Tìm hoặc tạo student
            Student student = studentRepository.findByStudentCode(dto.getStudentCode())
                    .orElseGet(() -> {
                        //fake khoa nhớ đổi lại khoa reald
                        Department department = new Department();
                        department.setId(1L);

                        Student newStudent = new Student();
                        newStudent.setDepartment(department);//này đổi
                        newStudent.setName(dto.getName());
                        newStudent.setStudentCode(dto.getStudentCode());
                        newStudent.setEmail(dto.getEmail());
                        newStudent.setClassName(dto.getClassName());
                        newStudent.setBirthDate(dto.getDateOfBirth());
                        newStudent.setCourse(dto.getCourse());
                        return studentRepository.save(newStudent); // lưu nếu chưa có
                    });

            //fake loại chứng chỉ
            UniversityCertificateType fake = new UniversityCertificateType();
            fake.setId(2L);
            // 2. Tạo certificate
            Certificate certificate = new Certificate();
            certificate.setStudent(student);
            certificate.setUniversityCertificateType(fake);// đổi lại nhá
            certificate.setDegreeTitle(dto.getDegreeTitle());
            certificate.setGraduationYear(dto.getGraduationYear());
            certificate.setRating(dto.getRating());
            certificate.setIssueDate(dto.getIssueDate());
            certificate.setEducationMode(dto.getEducationMode());
            certificate.setTrainingLocation(dto.getTrainingLocation());
            certificate.setSigner(dto.getSigner());
            certificate.setDiplomaNumber(dto.getDiplomaNumber());
            certificate.setLotteryNumber(dto.getLotteryNumber());
            certificate.setBlockchainTxHash("76123"); // sua lai sau
            certificate.setStatus("1");// sua lai

            certificateRepository.save(certificate);
        }

        System.out.println("Đã lưu xong " + dataList.size() + " dòng");

    }

    public List<CertificateExcelRowDTO> getDataList() {
        return dataList;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
