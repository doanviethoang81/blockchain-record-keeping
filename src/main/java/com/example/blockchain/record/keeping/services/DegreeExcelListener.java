package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DegreeExcelListener extends AnalysisEventListener<DegreeExcelRowRequest> {
    private final List<DegreeExcelRowRequest> dataList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final StudentRepository studentRepository;
    private final UniversityService universityService;
    private final UserService userService;
    private final MultipartFile imageFile;
    private final RatingService ratingService;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final DegreeService degreeService;
    private final StudentClassService studentClassService;



    public DegreeExcelListener(StudentRepository studentRepository, UniversityService universityService, UserService userService, MultipartFile imageFile, RatingService ratingService, EducationModelSevice educationModelSevice, DegreeTitleSevice degreeTitleSevice, DegreeService degreeService, StudentClassService studentClassService) {
        this.studentRepository = studentRepository;
        this.universityService = universityService;
        this.userService = userService;
        this.imageFile = imageFile;
        this.ratingService = ratingService;
        this.educationModelSevice = educationModelSevice;
        this.degreeTitleSevice = degreeTitleSevice;
        this.degreeService = degreeService;
        this.studentClassService = studentClassService;
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
    public void invoke(DegreeExcelRowRequest row, AnalysisContext context) {
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
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }


    @Override
    @Transactional
    public void doAfterAllAnalysed(AnalysisContext context) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));


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

        for (DegreeExcelRowRequest dto : dataList) {

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

            //Tạo certificate
            Degree degree = new Degree();
            degree.setStudent(student);
            DegreeTitle degreeTitle = degreeTitleSevice.findByName(dto.getDegreeTitle());
            degree.setDegreeTitle(degreeTitle);
            degree.setGraduationYear(dto.getGraduationYear());
            Rating rating = ratingService.findByName(dto.getDegreeTitle());
            degree.setRating(rating);
            degree.setIssueDate(dto.getIssueDate());
            EducationMode educationMode = educationModelSevice.findByName(dto.getDegreeTitle());
            degree.setEducationMode(educationMode);
            degree.setTrainingLocation(dto.getTrainingLocation());
            degree.setSigner(dto.getSigner());
            degree.setDiplomaNumber(dto.getDiplomaNumber());
            degree.setLotteryNumber(dto.getLotteryNumber());
            degree.setBlockchainTxHash("76123"); // sua lai sau
            degree.setStatus("1");// sua lai
            degree.setCreatedAt(vietnamTime.toLocalDateTime());

            degreeService.save(degree);
        }
        System.out.println("Đã lưu xong " + dataList.size() + " dòng");
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }

    public List<DegreeExcelRowRequest> getDataList() {
        return dataList;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
