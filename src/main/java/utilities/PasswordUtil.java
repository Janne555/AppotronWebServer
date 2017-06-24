/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

	public static String hashPassword(String pwd) {
		String hashed = BCrypt.hashpw(pwd, BCrypt.gensalt());
		
		return hashed;
	}
	
	public static boolean verifyPassword(String pwd, String hash) {
		boolean b = BCrypt.checkpw(pwd, hash);
		
		return b;
	}
}
