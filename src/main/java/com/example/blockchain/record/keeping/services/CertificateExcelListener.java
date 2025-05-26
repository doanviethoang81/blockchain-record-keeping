package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CertificateExcelListener extends AnalysisEventListener<CertificateExcelRowDTO> {

    private final List<CertificateExcelRowDTO> dataList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final UniversityService universityService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final UserService userService;
    private final CertificateTypeService certificateTypeService;
    private final String nameCertificateType;
    private final MultipartFile imageFile;
    private final StudentClassService studentClassService;


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
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));


        System.out.println("Loại chứng chỉ được chọn: " + nameCertificateType);
        System.out.println("Loại chứng chỉ được chọn: " + imageFile);

//        // ✅ Lưu file ảnh (imageFile) nếu cần
//        if (imageFile != null && !imageFile.isEmpty()) {
//            try {
//                String uploadDir = "uploads/images/"; // bạn có thể đổi đường dẫn này
//                String filePath = uploadDir + nameCertificateType + "_" + imageFile.getOriginalFilename();
//                File dest = new File(filePath);
//                dest.getParentFile().mkdirs(); // tạo thư mục nếu chưa có
//                imageFile.transferTo(dest);    // lưu file ảnh
//                System.out.println("Đã lưu ảnh tại: " + filePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        for (CertificateExcelRowDTO dto : dataList) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);
//            UniversityCertificateType universityCertificateType = universityCertificateTypeService.findByUniversityId(university.getId());

            User user= userService.findByUser(username);
            Department department=user.getDepartment();
            // 1. Tìm hoặc tạo student
            Student student = studentRepository.findByStudentCode(dto.getStudentCode())
                    .orElseGet(() -> {
                        Student newStudent = new Student();
                        StudentClass studentClass = studentClassService.findByName(dto.getStudenClass());
                        newStudent.setStudentClass(studentClass);//này đổi
                        newStudent.setName(dto.getName());
                        newStudent.setStudentCode(dto.getStudentCode());
                        newStudent.setEmail(dto.getEmail());
                        newStudent.setBirthDate(dto.getDateOfBirth());
                        newStudent.setCourse(dto.getCourse());
                        newStudent.setCreatedAt(vietnamTime.toLocalDateTime());
                        newStudent.setUpdatedAt(vietnamTime.toLocalDateTime());
                        return studentRepository.save(newStudent);
                    });

            //loại chứng chỉ
            Long id = Long.valueOf(nameCertificateType);
            CertificateType certificateType= certificateTypeService.findById(id);
            UniversityCertificateType universityCertificateType = universityCertificateTypeService.findByCartificateType(certificateType);

            //Tạo certificate
            Certificate certificate = new Certificate();
            certificate.setStudent(student);
            certificate.setUniversityCertificateType(universityCertificateType);
            certificate.setIssueDate(dto.getIssueDate());
            certificate.setDiplomaNumber(dto.getDiplomaNumber());
            certificate.setBlockchainTxHash("76123"); // sua lai sau
            certificate.setImageUrl("1231");//sua
            certificate.setQrCodeUrl("1313");//sua
            certificate.setStatus("1");// sua lai
            certificate.setCreatedAt(vietnamTime.toLocalDateTime());

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
