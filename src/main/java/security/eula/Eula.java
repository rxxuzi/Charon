package security.eula;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * <h1>Eula</h1>
 * Provides a comprehensive solution for file encryption and decryption using a combination of AES and RSA algorithms.
 * <p>
 * This class encapsulates the complex process of encrypting and decrypting files with high security.
 * It utilizes AES (Advanced Encryption Standard) for fast and secure file encryption and RSA (Rivest–Shamir–Adleman) for safely exchanging the encryption keys.
 * The class offers a streamlined interface for clients to easily encrypt files for secure storage or transmission and decrypt them upon receipt.
 * <p>
 *     <b>Usage Examples:</b>
 * {@snippet lang="java" :
 *     File file = new File("sample.txt");
 *     Eula a = new Eula();
 *     Eula b = new Eula();
 *
 *     String publicB = b.share(); // 公開鍵を生成
 *     a.encrypt(file, false); // 暗号化する. 暗号化したファイルは"sample.txt.eula"になる.
 *     String openA = a.openKey(publicB); // 公開鍵でキーを暗号化して共有
 *
 *
 *     File file2 = new File("sample.txt.eula"); // 暗号化されたファイルのパス
 *     b.decrypt(b.closeKey(openA),file2, false); // 共有されたキーで暗号化されたファイルを復号化する.
 * }
 *
 * </p>
 *
 * <h2>Note</h2>
 * This class requires careful management of cryptographic keys and files to ensure security.
 * Improper use can lead to security vulnerabilities.
 *
 * @author rxxuzi
 * @see EulaRSA
 * @see EulaAES
 */
public class Eula {
    // rsaとaes
    private transient final EulaRSA rsa;
    private transient final EulaAES aes;

    // 暗号化に使う鍵
    private final transient SecretKey key;

    // 公開鍵と秘密鍵
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Eula() throws EulaException {
        this.rsa = new EulaRSA();
        this.aes = new EulaAES();
        this.key = aes.key;
        this.publicKey = rsa.publicKey;
        this.privateKey = rsa.privateKey;
    }

    public void encrypt(SecretKey key, File file, boolean del) throws EulaException, IOException {
        EulaFast.encrypt(key, file, del);
    }

    public void encrypt(File file, boolean del) throws EulaException {
        EulaFast.encrypt(this.key, file, del);
    }

    public void decrypt(SecretKey key, File file, boolean del) throws EulaException {
        EulaFast.decrypt(key, file, del);
    }

    public void decrypt(File file, boolean del) throws EulaException, IOException {
        EulaFast.decrypt(this.key, file, del);
    }

    // 公開鍵を共有する
    public String share() {
        return rsa.getPublicKeyString(); // 公開鍵を文字列で返す
    }

    // 公開鍵でキーを暗号化し、文字列を返す.
    // 公開鍵は文字列で与えられる
    public String openKey(String pubkey) throws EulaException {
        // 引数から公開鍵を取得
        PublicKey publicKey = rsa.toPublicKey(pubkey);
        try {
            byte[] encryptedKey = EulaRSA.encAES(this.key, publicKey);
            return Base64.getEncoder().encodeToString(encryptedKey); // Base64エンコードされた文字列を返す
        } catch (Exception e) {
            throw new EulaException("Failed to encrypt AES key with public key.", e);
        }
    }

    // 秘密鍵でキーを復号化し、SecretKeyを返す.
    // 秘密鍵は自分の秘密鍵を使う
    public SecretKey closeKey(String base64) throws EulaException {
        try {
            // Base64でエンコードされた文字列をデコードしてバイト配列に変換
            byte[] encryptedKey = Base64.getDecoder().decode(base64);
            // 秘密鍵を使用してAES鍵を復号化
            return EulaRSA.decAES(encryptedKey, this.privateKey);
        } catch (Exception e) {
            throw new EulaException("Failed to decrypt AES key with private key.", e);
        }
    }

    public static void move(String sourcePath  , String targetPath) throws EulaException {
        Path  source = Paths.get(sourcePath);
        Path  target = Paths.get(targetPath);

        try {
            Files.move(source, target);
        } catch (IOException e) {
            throw new EulaException("Failed to move file.", e);
        }
    }

    @SuppressWarnings("unused")
    public static void copy(String sourcePath, String targetPath) throws EulaException {
        Path  source = Paths.get(sourcePath);
        Path  target = Paths.get(targetPath);

        try {
            Files.copy(source, target);
        } catch (IOException e) {
            throw new EulaException("Failed to copy file.", e);
        }
    }

    public static void zip(String sourcePath) throws EulaException {
        // TODO
    }

    public static void unzip(String sourcePath) throws EulaException {
        // TODO
    }

}
