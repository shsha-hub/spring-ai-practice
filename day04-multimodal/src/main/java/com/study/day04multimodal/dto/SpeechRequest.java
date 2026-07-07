package com.study.day04multimodal.dto;

/** POST /api/speech-generation 요청 바디: {"text": "...", "voiceName": "Kore"} */
public record SpeechRequest(String text,
                            String voiceName) {
}
