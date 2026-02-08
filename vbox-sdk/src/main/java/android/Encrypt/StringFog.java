package android.encrypt;

import android.Encrypt.StringEncrypt;
import java.util.Base64;

@StringEncrypt
public class StringFog {
    public static String decrypt(String encryptedData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            if (decoded.length < 2) return "[DECRYPT_ERROR]";
            int key = decoded[0] & 0xFF;
            byte[] decrypted = new byte[decoded.length - 1];
            for (int i = 0; i < decrypted.length; i++) {
                decrypted[i] = (byte) (decoded[i + 1] ^ key);
            }
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            return "[DECRYPT_ERROR]";
        }
    }

    public static String deobfuscate(String obfuscatedData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(obfuscatedData);
            String transformed = new String(decoded, "UTF-8");
            char[] chars = transformed.toCharArray();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                result.append((char) (chars[i] - (i % 7) - 1));
            }
            return result.toString();
        } catch (Exception e) {
            return "[DECRYPT_ERROR]";
        }
    }
}