package lk.globaltrade.web.security;

import jakarta.security.enterprise.identitystore.PasswordHash;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

public class BcryptPasswordHash implements PasswordHash {

    private static final int BCRYPT_COST = 10;

    @Override
    public void initialize(Map<String, String> parameters) {
    }

    @Override
    public String generate(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(BCRYPT_COST));
    }

    @Override
    public boolean verify(char[] password, String hashedPassword) {
        try {
            return BCrypt.checkpw(new String(password), hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}