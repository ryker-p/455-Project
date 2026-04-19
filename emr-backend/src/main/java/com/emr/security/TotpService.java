package com.emr.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TotpService {
  private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
  private static final int TIME_STEP_SECONDS = 30;
  private static final int CODE_DIGITS = 6;

  private final SecureRandom secureRandom = new SecureRandom();

  public String generateBase32Secret() {
    byte[] bytes = new byte[20];
    secureRandom.nextBytes(bytes);
    return base32Encode(bytes);
  }

  public boolean verifyCode(String base32Secret, String code) {
    if (base32Secret == null || base32Secret.isBlank() || code == null) return false;
    String normalized = code.replaceAll("\\s+", "");
    if (!normalized.matches("\\d{6}")) return false;

    long t = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
    for (long offset = -1; offset <= 1; offset++) {
      String expected = generateCode(base32Secret, t + offset);
      if (expected.equals(normalized)) return true;
    }
    return false;
  }

  public String buildOtpAuthUri(String username, String base32Secret) {
    String label = "EMR:" + username;
    String issuer = "EMR";
    return "otpauth://totp/"
        + url(label)
        + "?secret=" + url(base32Secret)
        + "&issuer=" + url(issuer)
        + "&period=" + TIME_STEP_SECONDS
        + "&digits=" + CODE_DIGITS;
  }

  private String generateCode(String base32Secret, long counter) {
    try {
      byte[] key = base32Decode(base32Secret);
      byte[] msg = ByteBuffer.allocate(8).putLong(counter).array();

      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] hash = mac.doFinal(msg);

      int offset = hash[hash.length - 1] & 0x0F;
      int binary = ((hash[offset] & 0x7F) << 24)
          | ((hash[offset + 1] & 0xFF) << 16)
          | ((hash[offset + 2] & 0xFF) << 8)
          | (hash[offset + 3] & 0xFF);

      int otp = binary % (int) Math.pow(10, CODE_DIGITS);
      return String.format("%0" + CODE_DIGITS + "d", otp);
    } catch (Exception ex) {
      return "";
    }
  }

  private String base32Encode(byte[] data) {
    StringBuilder out = new StringBuilder();
    int buffer = 0;
    int bitsLeft = 0;
    for (byte b : data) {
      buffer = (buffer << 8) | (b & 0xFF);
      bitsLeft += 8;
      while (bitsLeft >= 5) {
        int idx = (buffer >> (bitsLeft - 5)) & 0x1F;
        bitsLeft -= 5;
        out.append(BASE32_ALPHABET.charAt(idx));
      }
    }
    if (bitsLeft > 0) {
      int idx = (buffer << (5 - bitsLeft)) & 0x1F;
      out.append(BASE32_ALPHABET.charAt(idx));
    }
    return out.toString();
  }

  private byte[] base32Decode(String base32) {
    String normalized = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
    ByteBuffer out = ByteBuffer.allocate(normalized.length() * 5 / 8 + 1);

    int buffer = 0;
    int bitsLeft = 0;
    for (int i = 0; i < normalized.length(); i++) {
      char c = normalized.charAt(i);
      int val = BASE32_ALPHABET.indexOf(c);
      if (val < 0) continue;
      buffer = (buffer << 5) | val;
      bitsLeft += 5;
      if (bitsLeft >= 8) {
        out.put((byte) ((buffer >> (bitsLeft - 8)) & 0xFF));
        bitsLeft -= 8;
      }
    }
    out.flip();
    byte[] bytes = new byte[out.remaining()];
    out.get(bytes);
    return bytes;
  }

  private String url(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}

