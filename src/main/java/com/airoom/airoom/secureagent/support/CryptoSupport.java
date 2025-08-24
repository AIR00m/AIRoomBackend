package com.airoom.airoom.secureagent.support;

import com.fasterxml.jackson.databind.JsonNode;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CryptoSupport {

    public static String aesDecryptBase64(String base64Cipher, String key) throws Exception {
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec k = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        c.init(Cipher.DECRYPT_MODE, k);
        byte[] out = c.doFinal(Base64.getDecoder().decode(base64Cipher));
        return new String(out, StandardCharsets.UTF_8);
    }

    public static String hmacHex(String canonical, String secret, int hexLen) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] d = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(d.length * 2);
        for (byte b : d) sb.append(String.format("%02x", b));
        int n = Math.max(4, Math.min(hexLen, sb.length()));
        return sb.substring(0, n);
    }

    /** PayloadManager.canonicalString 과 동일 규칙 */
    public static String canonical(JsonNode p) {
        return get(p,"ver") + "|" + get(p,"app") + "|" + get(p,"uid") + "|" + get(p,"deviceId") + "|"
                + get(p,"contentId") + "|" + get(p,"action") + "|" + get(p,"ts");
    }

    private static String get(JsonNode o, String k) {
        JsonNode n = o.get(k);
        return (n == null || n.isNull()) ? "-" : n.asText();
    }

    private CryptoSupport() {}
}
