package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.dtos.request.DegreePrintData;
import com.example.blockchain.record.keeping.enums.*;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.NotificationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DegreeExcelListener extends AnalysisEventListener<DegreeExcelRowRequest> {

    private final RatingService ratingService;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final DegreeService degreeService;
    private final StudentService studentService;
    private final User user;
    private final GraphicsTextWriter graphicsTextWriter;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final UserService userService;
    private final NotificateService notificateService;
    private final NotificationReceiverService notificationReceiverService;
    private final NotificationWebSocketSender notificationWebSocketSender;

    public DegreeExcelListener(RatingService ratingService,
                               EducationModelSevice educationModelSevice,
                               DegreeTitleSevice degreeTitleSevice,
                               DegreeService degreeService,
                               StudentService studentService,
                               GraphicsTextWriter graphicsTextWriter,
                               User user,
                               AuditLogService auditLogService,
                               HttpServletRequest httpServletRequest,
                               LogRepository logRepository,
                               UserService userService,
                               NotificateService notificateService,
                               NotificationReceiverService notificationReceiverService, NotificationWebSocketSender notificationWebSocketSender
    ) {
        this.ratingService = ratingService;
        this.educationModelSevice = educationModelSevice;
        this.degreeTitleSevice = degreeTitleSevice;
        this.degreeService = degreeService;
        this.studentService = studentService;
        this.graphicsTextWriter = graphicsTextWriter;
        this.user =user;
        this.auditLogService = auditLogService;
        this.httpServletRequest = httpServletRequest;
        this.logRepository = logRepository;
        this.userService = userService;
        this.notificateService = notificateService;
        this.notificationReceiverService = notificationReceiverService;
        this.notificationWebSocketSender = notificationWebSocketSender;
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    private final List<DegreeExcelRowRequest> rows = new ArrayList<>();

    @Override
    public void invoke(DegreeExcelRowRequest data, AnalysisContext analysisContext) {
        rows.add(data);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    @Transactional
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<String> errors = new ArrayList<>();
        Set<String> duplicateStudentCodes = new HashSet<>();
        Set<String> duplicateDiplomaNumber = new HashSet<>();
        Set<String> duplicateLotteryNumber = new HashSet<>();

        if (rows.isEmpty()) {
            throw new ListBadRequestException("File Excel không chứa dữ liệu", List.of("Không có dòng nào trong file"));
        }

        if (rows.size() > 1000) {
            throw new BadRequestException("Chỉ cho phép tối đa cấp 1000 chứng chỉ/lần import");
        }

        //thu thập tất cả mã sinh viên
        Set<String> allStudentCodes = rows.stream()
                .map(DegreeExcelRowRequest::getStudentCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //tìm tất cả sinh viên 1 lần
        Map<String, Student> studentMap = studentService
                .findByStudentCodesOfDepartment(user.getDepartment().getId(), allStudentCodes)
                .stream()
                .collect(Collectors.toMap(Student::getStudentCode, s -> s));

        Map<String, Boolean> grantedDegrees = degreeService.checkStudentsGrantedDegree(allStudentCodes);

        Set<String> allDiplomaNumbers = rows.stream()
                .map(DegreeExcelRowRequest::getDiplomaNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> allLotteryNumbers = rows.stream()
                .map(DegreeExcelRowRequest::getLotteryNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> existingDiplomaNumbers = degreeService.findAllDiplomaNumbers(allDiplomaNumbers);
        Set<String> existingLotteryNumbers = degreeService.findAllLotteryNumbers(allLotteryNumbers);

        List<Degree> degreeList = new ArrayList<>();
        List<DegreePrintData> printDataList = new ArrayList<>();
        List<Notifications> notificationsList = new ArrayList<>();
        List<NotificationReceivers> notificationReceiversList = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            DegreeExcelRowRequest row = rows.get(i);
            int rowIndex = i + 1;

            if (!duplicateStudentCodes.add(row.getStudentCode())) {
                errors.add("Dòng " + rowIndex + ": Trùng mã sinh viên trong file");
                continue;
            }

            if (!duplicateDiplomaNumber.add(row.getDiplomaNumber())) {
                errors.add("Dòng " + rowIndex + ": Trùng số hiệu bằng trong file");
                continue;
            }

            if (!duplicateLotteryNumber.add(row.getLotteryNumber())) {
                errors.add("Dòng " + rowIndex + ": Trùng số vào sổ trong file");
                continue;
            }

            if (row.getStudentCode() == null || row.getStudentCode().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Mã số sinh viên không được để trống");
                continue;
            }

            if (row.getRatingName() == null || row.getRatingName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Xếp loại không được để trống");
                continue;
            }

            if (row.getDegreeTitleName() == null || row.getDegreeTitleName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Danh hiệu không được để trống");
                continue;
            }

            if (row.getEducationModeName() == null || row.getEducationModeName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Hình thức đào tạo không được để trống");
                continue;
            }

            if (row.getGraduationYear() == null || row.getGraduationYear().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Năm tốt nghiệp không được để trống");
                continue;
            }

            if (row.getTrainingLocation() == null || row.getTrainingLocation().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Địa điểm đào tạo không được để trống");
                continue;
            }

            if (row.getSigner() == null || row.getSigner().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Người ký không được để trống");
                continue;
            }
            if (row.getDiplomaNumber() == null || row.getDiplomaNumber().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Số hiệu bằng không được để trống");
                continue;
            }
            if (row.getLotteryNumber() == null || row.getLotteryNumber().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Số vào xổ không được để trống");
                continue;
            }

            if (row.getIssueDate() == null) {
                errors.add("Dòng " + rowIndex + ": Ngày cấp không được để trống");
                continue;
            }

            ZonedDateTime issueDate;
            try {
                LocalDate localDate = LocalDate.parse(row.getIssueDate(), formatter);
                issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime oneYearAgo = now.minusYears(1);
                ZonedDateTime oneYearLater = now.plusYears(1);

                if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                    errors.add("Dòng " + rowIndex + ": Ngày cấp văn bằng phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
                    continue;
                }
            } catch (DateTimeParseException e) {
                errors.add("Dòng " + rowIndex + ": Ngày cấp chứng chỉ không đúng định dạng dd/MM/yyyy");
                continue;
            }

            Student student = studentMap.get(row.getStudentCode());
            if (student == null) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên không tồn tại trong khoa");
                continue;
            }
            if (Boolean.TRUE.equals(grantedDegrees.get(row.getStudentCode()))) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên này đã được cấp văn bằng");
                continue;
            }

            if (existingDiplomaNumbers.contains(row.getDiplomaNumber())) {
                errors.add("Dòng " + rowIndex + ": Số hiệu bằng đã tồn tại!");
                continue;
            }
            if (existingLotteryNumbers.contains(row.getLotteryNumber())) {
                errors.add("Dòng " + rowIndex + ": Số vào sổ đã tồn tại!");
                continue;
            }

            Rating rating =ratingService.findByName(row.getRatingName());
            DegreeTitle degreeTitle = degreeTitleSevice.findByName(row.getDegreeTitleName());
            EducationMode educationMode = educationModelSevice.findByName(row.getEducationModeName());

            Degree degree = new Degree();
            degree.setStudent(student);
            degree.setRating(rating);
            degree.setDegreeTitle(degreeTitle);
            degree.setEducationMode(educationMode);
            degree.setGraduationYear(row.getGraduationYear());
            degree.setIssueDate(issueDate.toLocalDate());
            degree.setTrainingLocation(row.getTrainingLocation());
            degree.setSigner(row.getSigner());
            degree.setDiplomaNumber(row.getDiplomaNumber());
            degree.setLotteryNumber(row.getLotteryNumber());
            degree.setBlockchainTxHash(null);
            degree.setIpfsUrl(null);
            degree.setQrCode(null);
            degree.setStatus(Status.PENDING);
            degree.setCreatedAt(now.toLocalDateTime());
            degree.setUpdatedAt(now.toLocalDateTime());

            degreeList.add(degree);

            // tạo ảnh
            DegreePrintData degreePrintData = new DegreePrintData();
            degreePrintData.setUniversityName(student.getStudentClass().getDepartment().getUniversity().getName());
            degreePrintData.setDegreeTitle("Bằng " + degreeTitle.getName());
            degreePrintData.setDepartmentName(student.getStudentClass().getDepartment().getName());
            degreePrintData.setName(student.getName());
            degreePrintData.setBirthDate(student.getBirthDate().format(formatter));
            degreePrintData.setGraduationYear(row.getGraduationYear());
            degreePrintData.setRating(rating.getName());
            degreePrintData.setEducationMode(educationMode.getName());
            degreePrintData.setDay(String.valueOf(issueDate.getDayOfMonth()));
            degreePrintData.setMonth(String.valueOf(issueDate.getMonthValue()));
            degreePrintData.setYear(String.valueOf(issueDate.getYear()));
            degreePrintData.setTrainingLocation(row.getTrainingLocation());
            degreePrintData.setSigner(row.getSigner());
            degreePrintData.setDiplomaNumber(row.getDiplomaNumber());
            degreePrintData.setLotteryNumber(row.getLotteryNumber());

            String image_url = graphicsTextWriter.drawDegreeText(degreePrintData);

            degree.setImageUrl(image_url);

            printDataList.add(degreePrintData);
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        //tạo ảnh song song
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> imageFutures = printDataList.stream()
                .map(printData -> executor.submit(() -> graphicsTextWriter.drawDegreeText(printData)))
                .collect(Collectors.toList());

        for (int i = 0; i < degreeList.size(); i++) {
            try {
                degreeList.get(i).setImageUrl(imageFutures.get(i).get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Lỗi tạo ảnh cho văn bằng", e);
            }
        }

        executor.shutdown();
        degreeService.saveAll(degreeList);

        for (Degree degree : degreeList) {
            Student student = degree.getStudent();
            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.DEGREE_CREATED.getName());
            notifications.setContent("Khoa "+ student.getStudentClass().getDepartment().getName().toLowerCase() +" đã tạo văn bằng có số hiệu: "+ degree.getDiplomaNumber());
            notifications.setType(NotificationType.DEGREE_CREATED);
            notifications.setDocumentType("DEGREE");
            notifications.setDocumentId(degree.getId());
            notificationsList.add(notifications);
        }
        notificateService.saveAll(notificationsList);

        User userUniversity = userService.findByUser(user.getDepartment().getUniversity().getEmail());

        for (Notifications notification : notificationsList) {
            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notification);
            notificationReceivers.setReceiverId(userUniversity.getId());
            notificationReceivers.setCreatedAt(now.toLocalDateTime());
            notificationReceiversList.add(notificationReceivers);
        }
        notificationReceiverService.saveAll(notificationReceiversList);

        //gửi WebSocket
        for (int i = 0; i < notificationsList.size(); i++) {
            Notifications noti = notificationsList.get(i);
            NotificationReceivers receiver = notificationReceiversList.get(i);

            NotificationResponse response = new NotificationResponse(
                    receiver.getId(),
                    noti.getTitle(),
                    noti.getContent(),
                    noti.getType(),
                    false,
                    noti.getDocumentType(),
                    noti.getDocumentId(),
                    receiver.getCreatedAt()
            );
            notificationWebSocketSender.sendNotification(receiver.getReceiverId(), response);
        }

        //log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.CREATED);
        log.setEntityName(Entity.degrees);
        log.setEntityId(null);
        log.setDescription(LogTemplate.IMPORT_DEGREES.format(rows.size()));
        log.setIpAddress(ipAdress);
        log.setCreatedAt(now.toLocalDateTime());
        logRepository.save(log);
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
