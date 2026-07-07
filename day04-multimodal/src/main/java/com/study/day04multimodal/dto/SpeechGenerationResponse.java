package com.study.day04multimodal.dto;

/** 합성된 WAV 오디오를 &lt;audio src="data:audio/wav;base64,..."&gt;로 재생할 수 있도록 base64로 감싼 응답. */
public record SpeechGenerationResponse(String audioBase64) {
}
