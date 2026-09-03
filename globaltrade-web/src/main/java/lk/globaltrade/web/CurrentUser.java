package lk.globaltrade.web;

import jakarta.servlet.http.HttpServletRequest;
import lk.globaltrade.entities.User;
import lk.globaltrade.session.UserAccountBeanLocal;

final class CurrentUser {

    private CurrentUser() {
    }

    static User resolve(HttpServletRequest request, UserAccountBeanLocal userAccountBean) {
        var principal = request.getUserPrincipal();
        if (principal == null) {
            return null;
        }
        return userAccountBean.findByEmail(principal.getName());
    }
}
