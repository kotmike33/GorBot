package WebParser;

import DEBUG.Debug;
import Telegram.MyBot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashGenerator {
    @Test
    public void test(){
        System.out.println(getHash("Приметы про долги"));
    }
    public static String getHash(String title) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((title.getBytes(StandardCharsets.UTF_8)));

            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            MyBot bot = new MyBot();
            bot.reportErrorCode(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] hashBytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static boolean isSHA256Hash(String input) {
        if(input!=null && input.length()>1) {
            String sha256Pattern = "^[0-9a-fA-F]{64}$";
            Pattern pattern = Pattern.compile(sha256Pattern);
            Matcher matcher = pattern.matcher(input);
            return matcher.matches();
        }else {
            return false;
        }
    }
}
