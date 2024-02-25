package security.eula;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <h1>EulaRSA</h1>
 * <p>
 *     This class provides functionalities to encrypt and decrypt AES keys using RSA encryption.
 *     It supports generating RSA key pairs, encrypting AES keys with the public key, and decrypting AES keys with the private key.
 *     Additionally, it offers methods to retrieve and set public and private keys as Base64 encoded strings.
 * </p>
 *
 * @author rxxuzi
 */
public final class EulaRSA {
    // RSAに使うアルゴリズム
    public static final String ALGORITHM = "RSA";
    public static final String AES_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    // RSA鍵ペアの公開鍵と秘密鍵を格納する変数
    public transient final PublicKey publicKey;
    public transient final PrivateKey privateKey;

    // それぞれのキーサイズ
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;


    public EulaRSA() throws EulaException {
        try {
            // RSA鍵ペアを生成
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in gen RSA", e);
        }
    }

    // AES鍵を公開鍵で暗号化し、取得するメソッド
    public static byte[] encAES(SecretKey key, PublicKey publickey) throws EulaException {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publickey);
            return cipher.doFinal(key.getEncoded()); // 暗号化されたAES鍵を返す
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in encryption/decryption", e);
        } catch (NoSuchPaddingException e) {
            throw new EulaException("Padding problem in encryption/decryption", e);
        } catch (InvalidKeyException e) {
            throw new EulaException("Invalid key in encryption/decryption", e);
        } catch (IllegalBlockSizeException e) {
            throw new EulaException("Illegal block size in encryption/decryption", e);
        } catch (BadPaddingException e) {
            throw new EulaException("Bad padding in encryption/decryption", e);
        }
    }

    public byte[] encAES(SecretKey key, String publicKey) throws EulaException {
        return encAES(key, toPublicKey(publicKey));
    }

    public static SecretKey decAES(byte[] encryptedAESKey, PrivateKey privateKey) throws EulaException {
        try {
            // RSAで暗号化されたAES鍵を復号化
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedKey = cipher.doFinal(encryptedAESKey);

            // 復号化された鍵を基にSecretKeyを生成
            return new SecretKeySpec(decryptedKey, "AES");
        } catch (NoSuchPaddingException e) {
            throw new EulaException("Padding problem in encryption/decryption", e);
        } catch (IllegalBlockSizeException e) {
            throw new EulaException("Illegal block size in encryption/decryption", e);
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in encryption/decryption", e);
        } catch (BadPaddingException e) {
            throw new EulaException("Bad padding in encryption/decryption", e);
        } catch (InvalidKeyException e) {
            throw new EulaException("Invalid key in encryption/decryption", e);
        }
    }

    public SecretKey decAES(byte[] encryptedAESKey, String privateKey) throws EulaException {
        return decAES(encryptedAESKey, toPrivateKey(privateKey));
    }

    // 公開鍵をBase64エンコードされた文字列として取得
    public String getPublicKeyString() {
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }

    // 私密鍵をBase64エンコードされた文字列として取得
    public String getPrivateKeyString() {
        return Base64.getEncoder().encodeToString(this.privateKey.getEncoded());
    }

    // 文字列から公開鍵を取得
    public PublicKey toPublicKey(String key) throws EulaException {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in to gen PublicKey from String", e);
        } catch (InvalidKeySpecException e) {
            throw new EulaException("Invalid key in to gen PublicKey from String", e);
        }
    }

    // 文字列から秘密鍵を取得
    public PrivateKey toPrivateKey(String key) throws EulaException{
        try{
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new EulaException("Algorithm not found in to gen PrivateKey from String", e);
        } catch (InvalidKeySpecException e) {
            throw new EulaException("Invalid key in to gen PrivateKey from String", e);
        }
    }


}
