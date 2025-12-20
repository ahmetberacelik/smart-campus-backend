package com.smartcampus.meal.service.impl;

import com.smartcampus.meal.dto.request.ReservationRequest;
import com.smartcampus.meal.dto.response.ReservationResponse;
import com.smartcampus.meal.entity.Cafeteria;
import com.smartcampus.meal.entity.MealMenu;
import com.smartcampus.meal.entity.MealReservation;
import com.smartcampus.meal.repository.CafeteriaRepository;
import com.smartcampus.meal.repository.MealMenuRepository;
import com.smartcampus.meal.repository.MealReservationRepository;
import com.smartcampus.meal.service.QRCodeService;
import com.smartcampus.meal.service.ReservationService;
import com.smartcampus.meal.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final MealReservationRepository reservationRepository;
    private final MealMenuRepository menuRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final WalletService walletService;
    private final QRCodeService qrCodeService;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        // Menü kontrolü
        MealMenu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new RuntimeException("Menü bulunamadı"));

        if (!menu.getIsPublished()) {
            throw new RuntimeException("Bu menü henüz yayınlanmamış");
        }

        // Yemekhane kontrolü
        Cafeteria cafeteria = cafeteriaRepository.findById(request.getCafeteriaId())
                .orElseThrow(() -> new RuntimeException("Yemekhane bulunamadı"));

        // Aynı gün, aynı öğün için tekrar rezervasyon kontrolü
        if (reservationRepository.existsByUserIdAndReservationDateAndMealTypeAndStatusNot(
                userId, request.getReservationDate(), request.getMealType(), MealReservation.ReservationStatus.CANCELLED)) {
            throw new RuntimeException("Bu tarih ve öğün için zaten rezervasyonunuz var");
        }

        BigDecimal amount = BigDecimal.ZERO;
        boolean useScholarship = false;

        // Burs veya ücretli kontrol
        if (Boolean.TRUE.equals(request.getUseScholarship()) && walletService.canUseScholarship(userId)) {
            useScholarship = true;
            walletService.useScholarship(userId);
            log.info("Burs kullanıldı: userId={}", userId);
        } else {
            // Ücretli yemek
            amount = menu.getPrice();
            if (!walletService.hasEnoughBalance(userId, amount)) {
                throw new RuntimeException("Yetersiz bakiye. Gerekli tutar: " + amount + " TL");
            }
            walletService.debit(userId, amount, "MEAL", null, "Yemek rezervasyonu: " + menu.getMealType());
        }

        // QR kod oluştur
        String qrCode = qrCodeService.generateUniqueCode();

        // Rezervasyon kaydet
        MealReservation reservation = MealReservation.builder()
                .userId(userId)
                .menu(menu)
                .cafeteria(cafeteria)
                .reservationDate(request.getReservationDate())
                .mealType(request.getMealType())
                .amount(amount)
                .qrCode(qrCode)
                .isScholarshipUsed(useScholarship)
                .status(MealReservation.ReservationStatus.RESERVED)
                .build();

        MealReservation saved = reservationRepository.save(reservation);
        log.info("Rezervasyon oluşturuldu: userId={}, qrCode={}", userId, qrCode);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long reservationId) {
        MealReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationByQrCode(String qrCode) {
        MealReservation reservation = reservationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUpcomingReservations(Long userId) {
        return reservationRepository.findUpcomingReservations(userId, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservations(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        MealReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("Bu rezervasyon size ait değil");
        }

        if (!reservation.isCancellable()) {
            throw new RuntimeException("Bu rezervasyon iptal edilemez");
        }

        reservation.setStatus(MealReservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        // Ücret iadesi (burs kullanılmadıysa)
        if (!Boolean.TRUE.equals(reservation.getIsScholarshipUsed()) && reservation.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.refund(userId, reservation.getAmount(), reservationId, "Yemek rezervasyonu iptali");
        }

        log.info("Rezervasyon iptal edildi: reservationId={}", reservationId);
    }

    @Override
    @Transactional
    public ReservationResponse useReservation(String qrCode) {
        MealReservation reservation = reservationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (reservation.getStatus() != MealReservation.ReservationStatus.RESERVED) {
            throw new RuntimeException("Bu rezervasyon kullanılamaz. Durum: " + reservation.getStatus());
        }

        if (!reservation.getReservationDate().equals(LocalDate.now())) {
            throw new RuntimeException("Bu rezervasyon bugün için geçerli değil");
        }

        reservation.setStatus(MealReservation.ReservationStatus.USED);
        reservation.setUsedAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        log.info("Rezervasyon kullanıldı: qrCode={}", qrCode);
        return mapToResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getDailyReservations(Long cafeteriaId, LocalDate date, MealMenu.MealType mealType) {
        return reservationRepository.findByCafeteriaIdAndReservationDateAndMealType(cafeteriaId, date, mealType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countTodayReservations(Long cafeteriaId) {
        return reservationRepository.findByCafeteriaIdAndReservationDateAndMealType(
                cafeteriaId, LocalDate.now(), MealMenu.MealType.LUNCH).size();
    }

    @Override
    public long countUsedReservationsToday(Long cafeteriaId) {
        return reservationRepository.findByCafeteriaIdAndReservationDateAndMealType(
                        cafeteriaId, LocalDate.now(), MealMenu.MealType.LUNCH)
                .stream()
                .filter(r -> r.getStatus() == MealReservation.ReservationStatus.USED)
                .count();
    }

    private ReservationResponse mapToResponse(MealReservation reservation) {
        String qrImage = qrCodeService.generateQRCodeImage(reservation.getQrCode(), 200, 200);
        
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .menuId(reservation.getMenu().getId())
                .cafeteriaId(reservation.getCafeteria().getId())
                .cafeteriaName(reservation.getCafeteria().getName())
                .reservationDate(reservation.getReservationDate())
                .mealType(reservation.getMealType())
                .amount(reservation.getAmount())
                .qrCode(reservation.getQrCode())
                .qrCodeImage(qrImage)
                .isScholarshipUsed(reservation.getIsScholarshipUsed())
                .status(reservation.getStatus())
                .usedAt(reservation.getUsedAt())
                .createdAt(reservation.getCreatedAt())
                .isCancellable(reservation.isCancellable())
                .menuItemsJson(reservation.getMenu().getItemsJson())
                .build();
    }
}
