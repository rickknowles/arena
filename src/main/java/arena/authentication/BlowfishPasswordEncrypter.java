package arena.authentication;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import arena.utils.StringUtils;


public class BlowfishPasswordEncrypter implements PasswordEncrypter, InitializingBean {
    private final Log log = LogFactory.getLog(BlowfishPasswordEncrypter.class);

    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private String key = "dt56H";

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(this.key.getBytes(), "Blowfish");

            this.encryptCipher = Cipher.getInstance("Blowfish");
            this.encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            this.decryptCipher = Cipher.getInstance("Blowfish");
            this.decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } catch (Exception e) {
            log.error("Error initialising ciphers", e);
        }
    }

    public String encrypt(String password) {
        if (password == null) {
            return null;
        } else if (password.equals("")) {
            return "";
        } else try {
            byte[] passwordBytes = padToBlockSize(password.getBytes());
            return StringUtils.hexEncode(encryptCipher.doFinal(passwordBytes));
        } catch (Throwable err) {
            throw new RuntimeException("Error encrypting", err);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        } else if (encrypted.equals("")) {
            return "";
        } else try {
            byte[] decryptedBytes = decryptCipher.doFinal(StringUtils.hexDecode(encrypted));

            return new String(trimNullBytes(decryptedBytes));
        } catch (Throwable err) {
            throw new RuntimeException("Error decrypting", err);
        }
    }

    public byte[] padToBlockSize(byte[] b) {
        if ((b.length % 8) != 0) {
            byte[] b1 = new byte[b.length + (8 - (b.length % 8))];
            System.arraycopy(b, 0, b1, 0, b.length);

            return b1;
        } else {
            return b;
        }
    }

    public byte[] trimNullBytes(byte[] b) {
        if ((b.length == 0) || (b[b.length - 1] != 0)) {
            return b;
        }

        int l = b.length - 1;

        while (l >= 0 && b[l] == 0) {
            l--;
        }

        byte[] b1 = new byte[l + 1];
        System.arraycopy(b, 0, b1, 0, b1.length);

        return b1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
