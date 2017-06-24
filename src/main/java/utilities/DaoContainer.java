/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import daos.BugReportDao;
import daos.FoodstuffDao;
import daos.PermissionDao;
import daos.SessionControlDao;
import daos.UserDao;
import daos.mealdiary.CookingDao;
import daos.mealdiary.IngredientDao;
import daos.mealdiary.MealComponentDao;
import daos.mealdiary.MealDao;

/**
 *
 * @author Janne
 */
public class DaoContainer {
    private UserDao user;
    private MealDao meal;
    private MealComponentDao mealComponent;
    private SessionControlDao sessionControl;
    private IngredientDao ingredient;
    private CookingDao cooking;
    private FoodstuffDao foodstuff;
    private BugReportDao bugReport;
    private PermissionDao permission;

    public DaoContainer(UserDao user, MealDao meal, MealComponentDao mealComponent, SessionControlDao sessionControl, IngredientDao ingredient, CookingDao cooking, FoodstuffDao foodstuff, BugReportDao bugReport, PermissionDao permission) {
        this.user = user;
        this.meal = meal;
        this.mealComponent = mealComponent;
        this.sessionControl = sessionControl;
        this.ingredient = ingredient;
        this.cooking = cooking;
        this.foodstuff = foodstuff;
        this.bugReport = bugReport;
        this.permission = permission;
    }

    public UserDao getUser() {
        return user;
    }

    public MealDao getMeal() {
        return meal;
    }

    public MealComponentDao getMealComponent() {
        return mealComponent;
    }

    public SessionControlDao getSessionControl() {
        return sessionControl;
    }

    public IngredientDao getIngredient() {
        return ingredient;
    }

    public CookingDao getCooking() {
        return cooking;
    }

    public FoodstuffDao getFoodstuff() {
        return foodstuff;
    }

    public BugReportDao getBugReport() {
        return bugReport;
    }

    public PermissionDao getPermission() {
        return permission;
    }
    
    
    
    
}
