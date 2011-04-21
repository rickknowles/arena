package arena.authentication;

import java.util.Random;

public class RandomPasswordGenerator implements PasswordGenerator {

    private int length = 12;
    
    @Override
    public String makeNewPassword() {        
        StringBuffer newPassword = new StringBuffer();
        Random random = new Random();
        for (int n = 0; n < length; n++) {
            if (random.nextBoolean()) {
                newPassword.append(random.nextInt(9)); // add digit
            } else {
                newPassword.append((char) ((random.nextBoolean() ? 65 : 97) + random.nextInt(25)));
            }
        }
        return newPassword.toString();
    }

    public void setLength(int length) {
        this.length = length;
    }

}
