package security.eula;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * <h1>EulaFast</h1>
 * Provides fast encryption and decryption functionalities for files using AES keys and LZ4 compression.
 * This class is designed to efficiently process large files by compressing them before encryption,
 * which significantly reduces file size and encryption time.
 * <p>
 * <h2>Usage:</h2>
 * <ul>
 *     <li>
 *         <b>Encryption:</b>
 *         {@code EulaFast.encrypt(secretKey, inputFile, true);} - Encrypts and optionally deletes the original file.
 *     </li>
 *     <li>
 *         <b>Decryption:</b>
 *         {@code EulaFast.decrypt(secretKey, encryptedFile, true);} - Decrypts and optionally deletes the encrypted file.
 *     </li>
 * </ul>
 * </p>
 * @see <a href="https://github.com/rxxuzi/Eula">Eula on GitHub</a>
 * @author rxxuzi
 */
public class EulaFast {
    private static final int BUFFER_SIZE = 8192;
    private static final String EXTENSION = ".eula";

    // 暗号化メソッド
    public static void encrypt(SecretKey key, File inputFile, boolean delete) throws EulaException {
        ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();

        try (FileInputStream fis = new FileInputStream(inputFile);
             LZ4BlockOutputStream lz4OutputStream = new LZ4BlockOutputStream(compressedOutputStream);
             CipherOutputStream cos = new CipherOutputStream(lz4OutputStream, getCipher(Cipher.ENCRYPT_MODE, key))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new EulaException("Error reading file", e);
        }

        try (FileOutputStream fos = new FileOutputStream(inputFile.getAbsolutePath() + EXTENSION)) {
            fos.write(compressedOutputStream.toByteArray());
        } catch (IOException e){
            throw new EulaException("Error writing encrypted file", e);
        }

        if (delete) inputFile.delete();
    }


    // 復号化メソッド
    public static void decrypt(SecretKey key, File inputFile, boolean delete) throws EulaException{
        if (inputFile.getPath().endsWith(EXTENSION)) {
            try (FileInputStream fis = new FileInputStream(inputFile);
                 LZ4BlockInputStream lz4InputStream = new LZ4BlockInputStream(fis);
                 CipherInputStream cis = new CipherInputStream(lz4InputStream, getCipher(Cipher.DECRYPT_MODE, key));
                 FileOutputStream fos = new FileOutputStream(EulaAES.removeExtension(inputFile.getAbsolutePath()))) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new EulaException("Error reading encrypted file", e);
            }

            if (delete) inputFile.delete();
        }
    }

    // Cipherオブジェクトを取得するユーティリティメソッド
    private static Cipher getCipher(int cipherMode, SecretKey secretKey) throws EulaException {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);
            return cipher;
        } catch (NoSuchPaddingException e) {
            throw new EulaException("Padding problem in encryption/decryption", e);
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in encryption/decryption", e);
        } catch (InvalidKeyException e) {
            throw new EulaException("Invalid key in encryption/decryption", e);
        }
    }
}
