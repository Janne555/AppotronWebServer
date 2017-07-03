/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import daos.BugReportDao;
import daos.FoodstuffDao;
import daos.PermissionDao;
import daos.SessionControlDao;
import daos.UserDao;
import daos.mealdiary.IngredientCollectionDao;
import daos.mealdiary.IngredientDao;
import daos.mealdiary.MealComponentDao;
import daos.mealdiary.MealDao;
import database.Database;
import mealdiary.WebMethods;
import storables.User;
import utilities.DaoContainer;
import utilities.PasswordUtil;

/**
 *
 * @author Janne
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws Exception {
        String serverAddress = null;
        String username = null;
        String password = null;
        String dbName = null;

        for (String s : args) {
            if (s.contains("-address")) {
                serverAddress = s.substring(s.indexOf("=") + 1);
            } else if (s.contains("-username")) {
                username = s.substring(s.indexOf("=") + 1);
            } else if (s.contains("-password")) {
                password = s.substring(s.indexOf("=") + 1);
            } else if (s.contains("-database")) {
                dbName = s.substring(s.indexOf("=") + 1);
            } else {
                System.out.println("Argument \"" + s + "\" not recognized");
                System.exit(1);
            }
        }

        if (serverAddress == null) {
            System.out.println("No address given");
            System.exit(1);
        } else if (username == null) {
            System.out.println("No username given");
        } else if (password == null) {
            System.out.println("No password given");
        } else if (dbName == null) {
            System.out.println("No database given");
        }
        
        

        Database database = new Database("org.postgresql.Driver",
                "jdbc:postgresql:" + serverAddress + "/" + dbName,
                username,
                password);
        
        DaoContainer daos = new DaoContainer(
                new UserDao(database),
                new MealDao(database),
                new MealComponentDao(database),
                new SessionControlDao(database),
                new IngredientDao(database),
                new IngredientCollectionDao(database),
                new FoodstuffDao(database),
                new BugReportDao(database),
                new PermissionDao(database));
        
//        daos.getUser().store(new User("jannetar", "janne", PasswordUtil.hashPassword("salis"), "asdasdasd", "asdasdas"));

        new WebMethods(daos);
    }

}
