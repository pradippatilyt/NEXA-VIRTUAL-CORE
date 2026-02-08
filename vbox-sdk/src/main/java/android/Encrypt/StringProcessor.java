package android.encrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class StringProcessor {

    private static final Random RANDOM = new Random(42);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java StringProcessor <source_directory>");
            System.exit(1);
        }
        try {
            processJavaFiles(new File(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processJavaFiles(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                processJavaFiles(file);
            } else if (file.getName().endsWith(".java")) {
                processJavaFile(file);
            }
        }
    }

    private static void processJavaFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        if (!content.contains("@StringEncrypt")) return;

        Pattern pattern = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"");
        Matcher matcher = pattern.matcher(content);
        StringBuffer result = new StringBuffer();
        int count = 0;

        while (matcher.find()) {
            String originalString = matcher.group();
            String cleanString = originalString.substring(1, originalString.length() - 1);

            if (cleanString.length() < 2 ||
                originalString.contains("StringFog.decrypt") ||
                originalString.contains("StringFog.deobfuscate")) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(originalString));
                continue;
            }

            String encryptedString;
            String replacement;

            if (RANDOM.nextBoolean()) {
                encryptedString = encryptString(cleanString);
                replacement = "StringFog.decrypt(\"" + encryptedString + "\")";
            } else {
                encryptedString = obfuscateString(cleanString);
                replacement = "StringFog.deobfuscate(\"" + encryptedString + "\")";
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            count++;
        }
        matcher.appendTail(result);

        if (count > 0) {
            Files.write(file.toPath(), result.toString().getBytes());
            System.out.println("Encrypted " + count + " strings in " + file.getName());
        }
    }

    private static String encryptString(String data) {
        try {
            byte[] bytes = data.getBytes("UTF-8");
            byte[] encrypted = new byte[bytes.length + 1];
            int key = RANDOM.nextInt(255) + 1;
            encrypted[0] = (byte) key;
            for (int i = 0; i < bytes.length; i++) {
                encrypted[i + 1] = (byte) (bytes[i] ^ key);
            }
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return data;
        }
    }

    private static String obfuscateString(String data) {
        try {
            char[] chars = data.toCharArray();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                result.append((char) (chars[i] + (i % 7) + 1));
            }
            return Base64.getEncoder().encodeToString(result.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            return data;
        }
    }
}