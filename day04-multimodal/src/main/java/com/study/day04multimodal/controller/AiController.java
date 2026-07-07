package com.study.day04multimodal.controller;

import com.study.day04multimodal.dto.*;
import com.study.day04multimodal.service.MediaCategory;
import com.study.day04multimodal.service.MultimodalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AiController {

    private final MultimodalService multimodalService;

    public AiController(MultimodalService multimodalService) {
        this.multimodalService = multimodalService;
    }

    // --- 영수증 / 일반 이미지 / PDF / 오디오 ---

    @PostMapping("/image-analysis")
    public ReceiptInfo imageAnalysis(@RequestParam MultipartFile file,
                                     @RequestParam String conversationId) {
        String prompt = "업로드된 영수증 이미지에서 상호명, 총 금액, 날짜, 구매 항목을 추출해주세요.";

        return multimodalService.analyzeFileToEntity(file,
                MediaCategory.IMAGE,
                conversationId,
                prompt,
                ReceiptInfo.class);
    }

    @PostMapping("/image-describe")
    public String imageDescribe(@RequestParam MultipartFile file,
                                @RequestParam String conversationId) {
        String prompt = "입력받은 이미지가 무엇인지 친절하게 설명해주세요.";

        return multimodalService.analyzeFileToString(file,
                MediaCategory.IMAGE,
                conversationId,
                prompt);
    }

    @PostMapping("/pdf-analysis")
    public String pdfAnalysis(@RequestParam MultipartFile file,
                              @RequestParam String conversationId) {
        String prompt = "이 문서의 내용을 요약해주세요.";

        return multimodalService.analyzeFileToString(file,
                MediaCategory.PDF,
                conversationId,
                prompt);
    }

    @PostMapping("/pdf-describe")
    public PdfSummary pdfDescribe(@RequestParam MultipartFile file,
                                  @RequestParam String conversationId) {
        String prompt = "입력받은 PDF 파일의 내용을 친절하게 요약해서 설명해주세요.";

        return multimodalService.analyzeFileToEntity(file,
                MediaCategory.PDF,
                conversationId,
                prompt,
                PdfSummary.class);
    }

    @PostMapping("/audio-describe")
    public String audioDescribe(@RequestParam MultipartFile file,
                                @RequestParam String conversationId) {
        String prompt = "오디오 파일을 듣고 내용을 설명해주세요.";

        return multimodalService.analyzeFileToString(file,
                MediaCategory.AUDIO,
                conversationId,
                prompt);
    }

    // --- 회의록 올인원 비서 기능 ---

    @PostMapping("/meeting/board")
    public MeetingBoard analyzeBoard(@RequestParam MultipartFile file,
                                     @RequestParam String conversationId) {
        String prompt = "이 화이트보드 사진 속 주제들과 액션 아이템을 정리해주세요.";

        return multimodalService.analyzeFileToEntity(file,
                MediaCategory.IMAGE,
                conversationId,
                prompt,
                MeetingBoard.class);
    }

    @PostMapping("/meeting/agenda")
    public AgendaSummary analyzeAgenda(@RequestParam MultipartFile file, @RequestParam String conversationId) {
        String prompt = "이 안건 PDF의 핵심 내용을 한 문단으로 요약해주세요.";

        return multimodalService.analyzeFileToEntity(file,
                MediaCategory.PDF,
                conversationId,
                prompt,
                AgendaSummary.class);
    }

    @PostMapping("/meeting/audio")
    public String analyzeMeetingAudio(@RequestParam MultipartFile file,
                                      @RequestParam String conversationId) {
        String prompt = "이 회의 녹음에서 나온 핵심 발언과 결정 사항을 정리해주세요. 특별한 내용이 없으면 없다고 솔직하게 말해주세요.";

        return multimodalService.analyzeFileToString(file,
                MediaCategory.AUDIO,
                conversationId,
                prompt);
    }

    @PostMapping("/meeting/ask")
    public String ask(@RequestParam String question,
                      @RequestParam String conversationId) {
        return multimodalService.ask(question, conversationId);
    }

}
