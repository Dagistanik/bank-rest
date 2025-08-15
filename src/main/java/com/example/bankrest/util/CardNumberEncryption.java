package com.example.bankrest.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CardNumberEncryption {

    @Value("${card.encryption.key:myCardEncryptionSecretKey32Bytes!!}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private SecretKey getSecretKey() {
        // Обрезаем или дополняем ключ до 32 байт для AES-256
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, 32));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting card number", e);
        }
    }

    public String decrypt(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting card number", e);
        }
    }

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    public String generateCardNumber() {
        // Генерируем номер карты в формате 4XXX-XXXX-XXXX-XXXX
        StringBuilder cardNumber = new StringBuilder("4"); // Visa starts with 4

        for (int i = 1; i < 16; i++) {
            cardNumber.append((int) (Math.random() * 10));
        }

        return cardNumber.toString();
    }
}
