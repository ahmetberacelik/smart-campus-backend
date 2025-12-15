package com.smartcampus.attendance.service;

import com.smartcampus.attendance.dto.request.ExcuseRequestDto;
import com.smartcampus.attendance.dto.request.ReviewExcuseRequest;
import com.smartcampus.attendance.dto.response.ExcuseRequestResponse;
import com.smartcampus.attendance.dto.response.PageResponse;
import com.smartcampus.attendance.entity.ExcuseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ExcuseRequestService {

    ExcuseRequestResponse createExcuseRequest(Long studentId, ExcuseRequestDto request, MultipartFile document);

    PageResponse<ExcuseRequestResponse> getExcuseRequestsForFaculty(Long instructorId, Long sectionId,
                                                                     ExcuseStatus status, Pageable pageable);

    PageResponse<ExcuseRequestResponse> getMyExcuseRequests(Long studentId, Pageable pageable);

    ExcuseRequestResponse approveExcuseRequest(Long instructorId, Long requestId, ReviewExcuseRequest request);

    ExcuseRequestResponse rejectExcuseRequest(Long instructorId, Long requestId, ReviewExcuseRequest request);
}
