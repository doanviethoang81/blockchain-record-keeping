package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.response.ActionChangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionChangeService implements IActionChangeService{

    private final ActionChangeRepository actionChangeRepository;

    private static final Map<String, String> FIELD_NAME_MAPPING = Map.ofEntries(
            Map.entry("issueDate", "Ngày cấp"),
            Map.entry("name", "Tên"),
            Map.entry("studentCode", "Mã số sinh viên"),
            Map.entry("email", "Email"),
            Map.entry("birthDate", "Ngày sinh"),
            Map.entry("studentClass", "Lớp"),
            Map.entry("imageUrl", "Url ảnh"),
            Map.entry("department.name", "Tên khoa"),
            Map.entry("diplomaNumber", "Số hiệu"),
            Map.entry("lotteryNumber", "Số vào sổ"),
            Map.entry("signer", "Người ký"),
            Map.entry("trainingLocation", "Nơi đào tạo"),
            Map.entry("degreeTitle", "Tên văn bằng"),
            Map.entry("graduationYear", "Năm tốt nghiệp"),
            Map.entry("educationMode", "Hình thức đào tạo")
    );

    @Override
    public List<ActionChangeResponse> getActionChange(Long id) {
        List<ActionChange> list = actionChangeRepository.findByLogId(id);
        List<ActionChangeResponse> changeResponseList = list.stream()
                .map(a -> new ActionChangeResponse(
                        a.getId(),
                        FIELD_NAME_MAPPING.getOrDefault(a.getFieldName(), a.getFieldName()),
                        a.getOldValue(),
                        a.getNewValue()
                ))
                .collect(Collectors.toList());
        return changeResponseList;
    }
}
