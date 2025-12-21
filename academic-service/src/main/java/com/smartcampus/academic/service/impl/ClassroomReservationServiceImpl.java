package com.smartcampus.academic.service.impl;

import com.smartcampus.academic.dto.request.CreateReservationRequest;
import com.smartcampus.academic.dto.response.ReservationResponse;
import com.smartcampus.academic.entity.Classroom;
import com.smartcampus.academic.entity.ClassroomReservation;
import com.smartcampus.academic.repository.ClassroomRepository;
import com.smartcampus.academic.repository.ClassroomReservationRepository;
import com.smartcampus.academic.service.ClassroomReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomReservationServiceImpl implements ClassroomReservationService {

    private final ClassroomReservationRepository reservationRepository;
    private final ClassroomRepository classroomRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, CreateReservationRequest request) {
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Derslik bulunamadı"));

        // Çakışma kontrolü
        List<ClassroomReservation> conflicts = reservationRepository.findConflictingReservations(
                request.getClassroomId(), request.getReservationDate(),
                request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Bu derslik ve saatte çakışma var!");
        }

        ClassroomReservation reservation = ClassroomReservation.builder()
                .classroom(classroom)
                .userId(userId)
                .reservationDate(request.getReservationDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .purpose(request.getPurpose())
                .notes(request.getNotes())
                .status(ClassroomReservation.ReservationStatus.PENDING)
                .build();

        ClassroomReservation saved = reservationRepository.save(reservation);
        log.info("Rezervasyon oluşturuldu: userId={}, classroom={}, date={}",
                userId, classroom.getRoomNumber(), request.getReservationDate());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        ClassroomReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservationsPaged(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByClassroom(Long classroomId, LocalDate date) {
        return reservationRepository.findByClassroomIdAndReservationDateOrderByStartTimeAsc(classroomId, date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getPendingReservations(Pageable pageable) {
        return reservationRepository.findByStatusOrderByCreatedAtAsc(
                ClassroomReservation.ReservationStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ReservationResponse approveReservation(Long reservationId, Long adminId) {
        ClassroomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!reservation.isPending()) {
            throw new RuntimeException("Bu rezervasyon zaten işlenmiş");
        }

        reservation.approve(adminId);
        ClassroomReservation saved = reservationRepository.save(reservation);
        log.info("Rezervasyon onaylandı: id={}, adminId={}", reservationId, adminId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ReservationResponse rejectReservation(Long reservationId, Long adminId, String reason) {
        ClassroomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!reservation.isPending()) {
            throw new RuntimeException("Bu rezervasyon zaten işlenmiş");
        }

        reservation.reject(adminId, reason);
        ClassroomReservation saved = reservationRepository.save(reservation);
        log.info("Rezervasyon reddedildi: id={}, reason={}", reservationId, reason);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        ClassroomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("Bu rezervasyon size ait değil");
        }

        if (!reservation.isCancellable()) {
            throw new RuntimeException("Bu rezervasyon iptal edilemez");
        }

        reservation.setStatus(ClassroomReservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        log.info("Rezervasyon iptal edildi: id={}", reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAvailableSlots(Long classroomId, LocalDate date) {
        // Bu metod, ilgili dersliğin o gün için mevcut rezervasyonlarını döner
        // Müsait slotları frontend hesaplayabilir
        return reservationRepository.findByClassroomIdAndReservationDateOrderByStartTimeAsc(classroomId, date)
                .stream()
                .filter(r -> r.getStatus() == ClassroomReservation.ReservationStatus.APPROVED ||
                        r.getStatus() == ClassroomReservation.ReservationStatus.PENDING)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponse(ClassroomReservation reservation) {
        Classroom classroom = reservation.getClassroom();

        return ReservationResponse.builder()
                .id(reservation.getId())
                .classroomId(classroom.getId())
                .classroomName(classroom.getBuilding() + " " + classroom.getRoomNumber())
                .userId(reservation.getUserId())
                .reservationDate(reservation.getReservationDate())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .purpose(reservation.getPurpose())
                .status(reservation.getStatus())
                .approvedBy(reservation.getApprovedBy())
                .approvedAt(reservation.getApprovedAt())
                .rejectionReason(reservation.getRejectionReason())
                .notes(reservation.getNotes())
                .isCancellable(reservation.isCancellable())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
