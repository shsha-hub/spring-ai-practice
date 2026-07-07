package com.study.day04multimodal.advisor;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Gemini TTS는 오디오를 WAV 컨테이너가 아니라 순수 PCM 바이트로 돌려준다
 * (16bit·모노·24000Hz raw PCM). 브라우저 &lt;audio&gt; 태그에서 재생하려면
 * WAV 컨테이너로 감싸야 하므로 이 클래스가 그 변환을 맡는다.
 */
public final class WavAudio {

    private WavAudio() {
    }

    /** raw PCM 데이터를 44바이트 WAV 헤더로 감싼다. */
    public static byte[] pcmToWav(byte[] pcm, int sampleRate, int channels, int bitsPerSample) {
        int blockAlign = channels * bitsPerSample / 8;
        int byteRate = sampleRate * blockAlign;
        int dataSize = pcm.length;

        ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        header.put("RIFF".getBytes());
        header.putInt(36 + dataSize);
        header.put("WAVE".getBytes());
        header.put("fmt ".getBytes());
        header.putInt(16); // fmt 청크 길이 (PCM)
        header.putShort((short) 1); // audioFormat = 1 (PCM, 무압축)
        header.putShort((short) channels);
        header.putInt(sampleRate);
        header.putInt(byteRate);
        header.putShort((short) blockAlign);
        header.putShort((short) bitsPerSample);
        header.put("data".getBytes());
        header.putInt(dataSize);

        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize);
        out.writeBytes(header.array());
        out.writeBytes(pcm);

        return out.toByteArray();
    }

}
