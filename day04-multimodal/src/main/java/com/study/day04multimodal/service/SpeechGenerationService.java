package com.study.day04multimodal.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.PrebuiltVoiceConfig;
import com.google.genai.types.SpeechConfig;
import com.google.genai.types.VoiceConfig;

import com.study.day04multimodal.advisor.WavAudio;
import com.study.day04multimodal.config.GenAiClientConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gemini 3.1 Flash TTS (gemini-3.1-flash-tts-preview) 텍스트→음성 합성.
 * 별도의 SpeechModel/TTS 전용 인터페이스 대신, 일반 generateContent() 호출에
 * responseModalities("AUDIO")와 speechConfig()를 실어 보내는 것이 Gemini TTS의 호출 방식이다
 * (OpenAI의 별도 audio.speech 엔드포인트와 달리, Gemini는 채팅 모델 하나가 답한다).
 * 응답은 WAV가 아니라 16bit·모노·24000Hz raw PCM이므로 WavAudio로 감싸야 재생 가능하다.
 */
@Service
public class SpeechGenerationService {

    private static final String MODEL = "gemini-3.1-flash-tts-preview";
    private static final int SAMPLE_RATE = 24_000;
    private static final int CHANNELS = 1;
    private static final int BITS_PER_SAMPLE = 16;

    private final Client client;

    public SpeechGenerationService(GenAiClientConfig genAiClientConfig) {
        this.client = genAiClientConfig.client();
    }

    /**
     * @param text      읽어 줄 텍스트
     * @param voiceName 프리셋 음성 이름 (예: "Kore", "Puck" 등 Gemini가 제공하는 이름)
     * @return 재생 가능한 WAV 바이트 (24000Hz·모노·16bit)
     */
    public byte[] speak(String text, String voiceName) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities("AUDIO")
                .speechConfig(SpeechConfig.builder()
                        .voiceConfig(VoiceConfig.builder()
                                .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder()
                                        .voiceName(voiceName)
                                        .build())
                                .build())
                        .build())
                .build();

        GenerateContentResponse response = client.models.generateContent(MODEL, text, config);

        byte[] pcm = response.parts().stream()
                .map(Part::inlineData)
                .flatMap(java.util.Optional::stream)
                .findFirst()
                .flatMap(blob -> blob.data())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "응답에 오디오 데이터가 없습니다."));

        return WavAudio.pcmToWav(pcm,
                SAMPLE_RATE,
                CHANNELS,
                BITS_PER_SAMPLE);
    }
}
