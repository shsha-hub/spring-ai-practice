package com.study.day04multimodal.service;

import org.springframework.util.MimeTypeUtils;

import java.util.Set;

/**
 * 엔드포인트가 기대하는 파일 카테고리.
 * validateFile()을 하나로 합치면서 사라졌던 "엔드포인트별 타입 검증"을
 * 다시 명시적으로 복원하기 위한 것 — 예: /image-analysis에 PDF가 들어오면 막는다.
 */
public enum MediaCategory {

    IMAGE(Set.of(MimeTypeUtils.IMAGE_JPEG_VALUE, MimeTypeUtils.IMAGE_PNG_VALUE), "JPEG나 PNG 이미지"),
    PDF(Set.of("application/pdf"), "PDF 파일"),
    AUDIO(Set.of("audio/wav", "audio/mpeg"), "WAV나 MP3 오디오");

    private final Set<String> allowedTypes;
    private final String label;

    MediaCategory(Set<String> allowedTypes, String label) {
        this.allowedTypes = allowedTypes;
        this.label = label;
    }

    public boolean accepts(String contentType) {
        return contentType != null && allowedTypes.contains(contentType);
    }

    public String label() {
        return label;
    }

}
