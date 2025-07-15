package com.example.blockchain.record.keeping.services;

import com.STUcoin.contract.STUcoin_sol_STUcoin;
import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentService implements IStudentService{
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;
    private final WalletService walletService;

    // danh sách sv của các lớp của 1 khoa
    public List<Student> studentOfClassOfDepartmentList(Long idDepartment){
        List<Student> studentList = studentRepository.getAllStudentOfDepartment(idDepartment);
         return  studentList;
    }

    // tìm mssv của 1 class
    @Override
    public Student findByStudentCodeOfUniversity(String studentCode, Long universityId) {
        return studentRepository.findByStudentCodeOfUniversity(studentCode,universityId).orElse(null);
    }

    @Override
    public List<Student> getAllStudentOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName, int limit, int offset) {
        return studentRepository.searchStudentsByUniversity(universityId,departmentName,className,studentCode,studentName, limit, offset);
    }

    @Override
    public List<Student> searchStudents(Long departmentId, String className, String studentCode, String name, int limit, int offset) {
        return studentRepository.searchStudents(departmentId,className,studentCode, name, limit, offset);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.students)
    public Student createStudent(StudentRequest studentRequest) throws Exception {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        StudentClass studentClass = studentClassRepository.findById(studentRequest.getClassId())
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp với id "+ studentRequest.getClassId()));
        Student student = new Student();
        student.setStudentClass(studentClass);
        student.setName(studentRequest.getName());
        student.setStudentCode(studentRequest.getStudentCode());
        student.setEmail(studentRequest.getEmail());
        student.setBirthDate(studentRequest.getBirthDate());
        student.setCourse(studentRequest.getCourse());
        student.setStatus(Status.ACTIVE);
        student.setPassword(passwordEncoder.encode(studentRequest.getStudentCode()));
        student.setCreatedAt(vietnamTime.toLocalDateTime());
        student.setUpdatedAt(vietnamTime.toLocalDateTime());

        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        String privateKey = ecKeyPair.getPrivateKey().toString(16);
        String publicKey = ecKeyPair.getPublicKey().toString(16);

        String walletAddress = "0x" + Keys.getAddress(ecKeyPair.getPublicKey());

        AuditingContext.setDescription("Tạo sinh viên có mã số sinh viên: " + student.getStudentCode());
        studentRepository.save(student);

        Wallet wallet = new Wallet();
        wallet.setStudent(student);
        wallet.setWalletAddress(walletAddress);
        wallet.setPrivateKey(privateKey);
        wallet.setPublicKey(publicKey);

        walletService.create(wallet);

        String METAMASK_PRIVATE_KEY = EnvUtil.get("METAMASK_PRIVATE_KEY");

        Credentials systemCredentials = Credentials.create(METAMASK_PRIVATE_KEY);

        String ALCHEMY_URL = EnvUtil.get("ALCHEMY_URL");
        String toContract = EnvUtil.get("SMART_CONTRACT_STUCOIN_ADDRESS");

        // tạo Web3j instance
        Web3j web3j = Web3j.build(new HttpService(ALCHEMY_URL));

        // load contract STUcoin
        STUcoin_sol_STUcoin contract = STUcoin_sol_STUcoin.load(
                toContract,
                web3j,
                new RawTransactionManager(web3j, systemCredentials),
                new DefaultGasProvider()
        );

        //gửi token
        TransactionReceipt receipt = contract.transfer(walletAddress, new BigInteger("1000000000000000000")) // 5 STUcoin (18 decimals)
                .send();

        return student;
    }

    @Override
    public Student findByEmailStudentCodeOfDepartment(String email, Long universityId) {
        return studentRepository.findByEmailStudentCodeOfDepartment(email,universityId).orElse(null);
    }

    @Override
    public Student update(Long id , UpdateStudentRequest updateStudentRequest) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Student student = studentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy sinh viên với id "+ id));
        Student studentOld = auditLogService.cloneStudent(student);
        StudentClass studentClass = studentClassRepository.findById(updateStudentRequest.getClassId())
                .orElseThrow(()-> new RuntimeException("Không tìm lớp với id "+ updateStudentRequest.getClassId()));
        student.setStudentClass(studentClass);
        student.setName(updateStudentRequest.getName());
        student.setStudentCode(updateStudentRequest.getStudentCode());
        student.setEmail(updateStudentRequest.getEmail());
        student.setBirthDate(updateStudentRequest.getBirthDate());
        student.setCourse(updateStudentRequest.getCourse());
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        studentRepository.save(student);


        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = auditLogService.compareObjects(null, studentOld, student);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.students);
            log.setEntityId(id);
            log.setDescription(LogTemplate.UPDATE_STUDENT.format(student.getStudentCode()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }

        return student;
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.students)
    public Student delete(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Student student = studentRepository.findByIdAndStatus(id, Status.ACTIVE )
                .orElseThrow(()-> new RuntimeException("Không tìm thấy sinh viên với id "+ id));
        student.setStatus(Status.DELETED);
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        AuditingContext.setDescription("Xóa sinh viên có mã số sinh viên: " + student.getStudentCode());
        return studentRepository.save(student);
    }

    @Override
    public Optional<StudentClass> findByClassNameAndDepartmentId(Long departmentId, String className) {
        return studentClassRepository.findByClassNameAndDepartmentId(departmentId,className);
    }

    @Override
    public List<Student> findByStudentOfDepartment(Long departmentId, String studentCode) {
        return studentRepository.findByStudentOfDepartment(departmentId,studentCode);
    }

    @Override
    public Optional<Student> findByOneStudentOfDepartment(Long departmentId, String studentCode) {
        return studentRepository.findByOneStudentOfDepartment(departmentId,studentCode);
    }

    @Override
    public List<Student> findByStudentCodesOfDepartment(Long departmentId, Set<String> allStudentCodes) {
        return studentRepository.findByStudentCodesOfDepartment(departmentId,allStudentCodes);
    }

    @Override
    public Student findByEmail(String email) {
        return studentRepository.findByEmailAndStatus(email, Status.ACTIVE)
                .orElse(null);
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        Student student= studentRepository.findByEmailAndStatus(email,Status.ACTIVE)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy tài khoản này!"));
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        student.setPassword(passwordEncoder.encode(newPassword));
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        studentRepository.save(student);
        return true;
    }

    @Override
    public long countStudentsByUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName) {
        return studentRepository.countStudentsByUniversity(universityId, departmentName, className, studentCode, studentName);
    }

    @Override
    public long countStudentOdDepartment(Long departmentId, String className, String studentCode, String studentName) {
        return studentRepository.countStudentOdDepartment(departmentId, className, studentCode, studentName);
    }

    @Override
    public long countCertificateOfStudent(Long id) {
        return studentRepository.countCertificateOfStudent(id);
    }

    @Override
    public long countDegreeOfStudent(Long id) {
        return studentRepository.countDegreeOfStudent(id);
    }

    public Student findById(Long id){
        return studentRepository.findByIdAndStatus(id, Status.ACTIVE).orElse(null);
    }

    public boolean changePasswordOfStudent(String email, ChangePasswordRequest changePasswordRequest) {
        Optional<Student> studentOptional = studentRepository.findByEmailAndStatus(email, Status.ACTIVE);
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();

            if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), student.getPassword())) {

                if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                    student.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                    student.setUpdatedAt(vietnamTime.toLocalDateTime());
                    studentRepository.save(student);
                    return true;
                }
            }
        }
        return false;
    }
}
