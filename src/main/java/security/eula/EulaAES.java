package security.eula;


import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * <h1>EulaAES</h1>
 * <p>
 *     Provides functionalities for encrypting and decrypting files using AES keys.
 *     This class allows generating AES keys either from a specified password or a random string.
 *     It supports converting AES keys to and from Base64-encoded strings.
 * </p>
 * <p>
 *     It includes methods for generating keys based on a password using PBKDF2WithHmacSHA256,
 *     generating random strings for key generation, and removing file extensions specific to encrypted files.
 * </p>
 *
 * @author rxxuzi
 */
public final class EulaAES {
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    private static final int ITERATION_COUNT = 65536;

    private static final byte[] SALT = "Eula Lawrence".getBytes();
    private static final String EXTENSION = ".eula";

    public final SecretKey key;
    public final boolean pw;

    public EulaAES() throws EulaException {
        this.key = genKey();
        this.pw = false;
    }

    public EulaAES(String password) throws EulaException {
        this.key = genKey(password);
        this.pw = true;
    }

    // 文字列からAES鍵を取得
    public static SecretKey string2Key(String aesString) {
        return new SecretKeySpec(Base64.getDecoder().decode(aesString), "AES"); // AES鍵を取得
    }

    // AES 鍵をBase64エンコードされた文字列として取得
    public static String key2string(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // 特定の文字列からキーを生成する
    private static SecretKey genKey(String password) throws EulaException {
        try {
            return getKeyFromPassword(password);
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("No such algorithm for key generation", e);
        } catch (InvalidKeySpecException e) {
            throw new EulaException("Invalid key specification for key generation", e);
        }
    }

    // ランダムな文字列からキーを生成する
    private static SecretKey genKey() throws EulaException {
        return genKey(randomString());
    }

    // ファイルの拡張子を削除する。
    public static String removeExtension(String path) {
        if (path.endsWith(EXTENSION)) {
            return path.substring(0, path.length() - EXTENSION.length());
        }
        return path;
    }

    // パスワードからキーを生成する。
    private static SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATION_COUNT, KEY_SIZE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
    }

    // ランダムな文字列を生成する。(32文字)
    public static String randomString() {
        int max = 32;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            sb.append((char) (random.nextInt(79) + '0'));
        }
        return sb.toString();
    }
}