package com.airoom.airoom.secureagent.model.dto;

import java.util.Map;

public class StegoDecodeResponse {
    public boolean ok;              // 처리 자체 성공 여부 (형식 판별·파싱 성공)
    public String fileName;         // 업로드 파일명
    public String fileType;         // png | jpeg | pdf | unsupported
    public boolean hasStego;        // 메타데이터에 stego 키 존재 여부
    public String reason;           // 에러/사유 (unsupported, no-stego, bad-payload 등)
    public String encPayloadB64;    // 메타데이터에서 읽은 암호문(Base64) - 있으면 반환
    public String payload;          // 복호화 결과(텍스트)
    public Map<String,Object> payloadJson; // payload가 JSON이면 파싱해서 제공(가독성용)
}