package com.airoom.airoom.secureagent.model.service;

import com.airoom.airoom.secureagent.model.dto.StegoDecodeResponse;
import com.airoom.airoom.secureagent.config.IngestSecurityProperties;
import com.airoom.airoom.secureagent.support.CryptoSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

public class ForensicDecodeService {

    private static final String IMG_KEYWORD = "StegoPayload";
    private static final String PDF_KEY     = "X-Doc-Tracking-Key";

    private final IngestSecurityProperties sec;
    private final ObjectMapper M = new ObjectMapper();

    public ForensicDecodeService(IngestSecurityProperties sec) {
        this.sec = sec;
    }

    public StegoDecodeResponse decode(File tmp, String originalName) {
        StegoDecodeResponse res = new StegoDecodeResponse();
        res.fileName = originalName;

        String lower = originalName.toLowerCase();
        try {
            if (lower.endsWith(".png")) {
                res.fileType = "png";
                handlePng(tmp, res);
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                res.fileType = "jpeg";
                handleJpeg(tmp, res);
            } else if (lower.endsWith(".pdf")) {
                res.fileType = "pdf";
                handlePdf(tmp, res);
            } else {
                res.fileType = "unsupported";
                res.ok = false;
                res.hasStego = false;
                res.reason = "unsupported-file-type";
                return res;
            }

            res.ok = true;
            // payload가 JSON이면 prettify
            if (res.payload != null) {
                try {
                    res.payloadJson = M.readValue(res.payload, Map.class);
                } catch (Exception ignore) { /* 순수 문자열일 수 있음 */ }
            }
            return res;

        } catch (Exception e) {
            res.ok = false;
            res.reason = "decode-error:" + e.getClass().getSimpleName();
            return res;
        } finally {
            try { Files.deleteIfExists(tmp.toPath()); } catch (Exception ignore) {}
        }
    }

    private void handlePng(File f, StegoDecodeResponse out) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(f)) {
            ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
            r.setInput(iis, true);

            IIOMetadata meta = r.getImageMetadata(0);
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(meta.getNativeMetadataFormatName());
            var list = root.getElementsByTagName("tEXtEntry");

            String encB64 = null;
            for (int i = 0; i < list.getLength(); i++) {
                IIOMetadataNode n = (IIOMetadataNode) list.item(i);
                if (IMG_KEYWORD.equals(n.getAttribute("keyword"))) {
                    encB64 = n.getAttribute("value");
                    break;
                }
            }

            if (encB64 == null || encB64.isBlank()) {
                out.hasStego = false;
                out.reason = "no-stego";
                return;
            }
            out.hasStego = true;
            out.encPayloadB64 = encB64;
            out.payload = CryptoSupport.aesDecryptBase64(encB64, sec.getAesKey());
        }
    }

    private void handleJpeg(File f, StegoDecodeResponse out) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(f)) {
            ImageReader r = ImageIO.getImageReadersByFormatName("jpeg").next();
            r.setInput(iis, true);

            IIOMetadata meta = r.getImageMetadata(0);
            IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(meta.getNativeMetadataFormatName());

            var coms = root.getElementsByTagName("com");
            String encB64 = null;
            for (int i = 0; i < coms.getLength(); i++) {
                String comment = ((IIOMetadataNode) coms.item(i)).getAttribute("comment");
                if (comment != null && comment.startsWith(IMG_KEYWORD + ":")) {
                    encB64 = comment.substring(IMG_KEYWORD.length() + 1);
                    break;
                }
            }

            if (encB64 == null || encB64.isBlank()) {
                out.hasStego = false;
                out.reason = "no-stego";
                return;
            }
            out.hasStego = true;
            out.encPayloadB64 = encB64;
            out.payload = CryptoSupport.aesDecryptBase64(encB64, sec.getAesKey());
        }
    }

    private void handlePdf(File f, StegoDecodeResponse out) throws Exception {
        try (PDDocument doc = PDDocument.load(f)) {
            PDDocumentInformation info = doc.getDocumentInformation();
            String encB64 = info.getCustomMetadataValue(PDF_KEY);

            if (encB64 == null || encB64.isBlank()) {
                out.hasStego = false;
                out.reason = "no-stego";
                return;
            }
            // 혹시 잘못 저장된 경우(= 원문이 base64가 아닌 이중 인코딩 등) 간단히 sanity 체크
            // (정상 케이스: encB64는 Base64 문자열)
            try { Base64.getDecoder().decode(encB64); }
            catch (IllegalArgumentException ex) {
                out.hasStego = false;
                out.reason = "invalid-metadata";
                return;
            }

            out.hasStego = true;
            out.encPayloadB64 = encB64;
            out.payload = CryptoSupport.aesDecryptBase64(encB64, sec.getAesKey());
        }
    }
}
