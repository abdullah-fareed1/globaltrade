package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.User;

import java.util.List;

@Local
public interface UserAccountBeanLocal {

    User register(String name, String email, String rawPassword);

    User authenticate(String email, String rawPassword);

    void updateRole(int userId, User.Role newRole);

    User findByEmail(String email);

    List<User> findAll();
}