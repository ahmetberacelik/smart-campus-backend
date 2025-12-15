package com.smartcampus.academic.service;

import com.smartcampus.academic.dto.response.TranscriptResponse;

public interface TranscriptPdfService {
    byte[] generateTranscriptPdf(TranscriptResponse transcript);
}
