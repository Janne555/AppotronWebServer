/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import daos.UserDao;
import java.sql.SQLException;
import storables.User;

/**
 *
 * @author Janne
 */
public class Service {

    public static LoginResult checkUser(User user, UserDao userDao) throws SQLException {
        LoginResult result = new LoginResult();
        User userFound = userDao.findByName(user.getUsername());
        if (userFound == null) {
            result.setError("Invalid username");
        } else if (!PasswordUtil.verifyPassword(user.getPassword(), userFound.getPassword())) {
            result.setError("Invalid password");
        } else {
            result.setUser(userFound);
        }

        return result;
    }
    
}
