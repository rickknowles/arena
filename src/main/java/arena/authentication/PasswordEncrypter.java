package arena.authentication;

public interface PasswordEncrypter {
    public String encrypt(String rawPassword);
    public String decrypt(String encrypted);
}
