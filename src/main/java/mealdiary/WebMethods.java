/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mealdiary;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import spark.ModelAndView;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import storables.*;
import storables.mealdiary.*;
import static utilities.JsonUtil.json;
import utilities.PasswordUtil;
import utilities.Container;
import utilities.DaoContainer;

/**
 *
 * @author Janne
 */
public class WebMethods {
    public WebMethods(DaoContainer daos) {

        setupRoutes(daos);
    }

    private void setupRoutes(DaoContainer daos) {
        userRoutes(daos);
        addFoodstuffRoutes(daos);
        mealRoutes(daos);
        searchRoutes(daos);
        viewRoutes(daos);
        frontPageRoutes(daos);
        editRoutes(daos);
        cookingRoutes(daos);
        deleteRoutes(daos);
        toolRoutes(daos);

        /**
         * filter for all requests
         */
        before("/*", (req, res) -> {
            if (req.cookies().containsKey("sessioncontrolid")) {
                String cookie = req.cookie("sessioncontrolid");
                User validUser = daos.getSessionControl().getValidUser(cookie);
                req.session(true).attribute("user", validUser);
            }

            if (req.session(true).attribute("user") == null && !req.pathInfo().equals("/login")) {
                res.redirect("/login");
            } else if (req.session(true).attribute("user") != null && req.pathInfo().equals("/login")) {
                res.redirect("/fail?msg=alreadyloggedin");
            }
        });

        /**
         * bug reporting page
         */
        get("/bugreport", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            return new ModelAndView(map, "bugreport");
        }, new ThymeleafTemplateEngine());

        /**
         * fail page
         *
         * in case of something going wrong
         */
        get("/fail", (req, res) -> {
            HashMap map = new HashMap();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            map.put("msg", req.queryParams("msg"));
            return new ModelAndView(map, "fail");
        }, new ThymeleafTemplateEngine());

        post("/bugreport.post", (req, res) -> {
            User user = (User) req.session().attribute("user");
            String subject = req.queryParams("subject");
            System.out.println(subject);
            String description = req.queryParams("description");
            Timestamp date = new Timestamp(System.currentTimeMillis());

            daos.getBugReport().create(new BugReport(user.getId(), user, subject, description, date));

            res.redirect("/");

            return "";
        });

    }

    private void addFoodstuffRoutes(DaoContainer daos) {
        /**
         * add item page
         */
        get("/addfoodstuff", (req, res) -> {
            HashMap map = new HashMap();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            map.put("title", "Add Foodstuff");
            map.put("labelname", "Name");
            map.put("labelidentifier", "Serial Number");
            map.put("foodstuff", true);
            map.put("copies", true);
            map.put("calories", 0);
            map.put("carbohydrate", 0);
            map.put("fat", 0);
            map.put("protein", 0);

            map.put("action", "/addfoodstuff.post");
            map.put("locations", daos.getFoodstuff().getLocations(user));
            return new ModelAndView(map, "additem");
        }, new ThymeleafTemplateEngine());

        post("/addfoodstuff.post", (req, res) -> {
            User user = (User) req.session().attribute("user");

            String name = req.queryParams("name");
            String identifier = req.queryParams("identifier");
            String producer = req.queryParams("producer");
            String location = req.queryParams("location");
            String caloriesString = req.queryParams("calories");
            String carbohydrateString = req.queryParams("carbohydrate");
            String fatString = req.queryParams("fat");
            String proteinString = req.queryParams("protein");
            String expirationString = req.queryParams("expiration");

            try {
                float calories = Float.parseFloat(caloriesString) / 100;
                float carbohydrate = Float.parseFloat(carbohydrateString) / 100;
                float fat = Float.parseFloat(fatString) / 100;
                float protein = Float.parseFloat(proteinString) / 100;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate time = LocalDate.parse(expirationString, formatter);
                Timestamp expiration = Timestamp.valueOf(time.atStartOfDay());

                int copies = 1;
                String copyString = req.queryParams("copies");
                if (copyString != null) {
                    copies = Integer.parseInt(req.queryParams("copies"));
                }

                for (int i = 0; i < copies; i++) {
                    Foodstuff foodstuff = new Foodstuff(name,
                            identifier,
                            producer,
                            location,
                            calories,
                            carbohydrate,
                            fat,
                            protein,
                            0, 0, 0,
                            expiration,
                            new Timestamp(System.currentTimeMillis()));

                    daos.getFoodstuff().store(foodstuff, user);
                }
            } catch (NumberFormatException e) {
                res.redirect("/fail?msg=" + e);
            }

            res.redirect("/");
            return "ok";
        });

        get("/inserttofoodstuff", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            String idStr = req.queryParams("id");

            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Cooking cooking = daos.getCooking().findOne(id);
                    if (cooking != null) {
                        Foodstuff foodstuff = new Foodstuff(cooking.getName(), cooking.getIdentifier(), "USER", "COOK BOOK", cooking.getCalories(), cooking.getCarbohydrate(), cooking.getFat(), cooking.getProtein(), 0, 0, 0, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
                        daos.getFoodstuff().store(foodstuff, user);
                    }

                } catch (NumberFormatException e) {
                    res.redirect("/fail?msg=" + e);
                    halt();
                }
            }
            res.redirect("/");
            return "";
        });
    }

    private void userRoutes(DaoContainer daos) {

        /**
         * login page
         */
        get("/login", (req, res) -> {
            HashMap map = new HashMap();
            map.put("user", req.session().attribute("user"));
            map.put("redirect", req.queryParams("redirect"));
            return new ModelAndView(map, "login");
        }, new ThymeleafTemplateEngine());

        /**
         * login post
         */
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            User user = daos.getUser().findByName(username);
            if (user == null) {
                halt();
                return "";
            } else if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                halt();
                return "";
            }

            req.session(true).attribute("user", user);
            //creates session with 6 hours or 3600000 millis validity
            SessionControl sessionControl = new SessionControl(null, user.getId(), new Timestamp(System.currentTimeMillis() + 3600000));

            sessionControl.genSessionId();
            daos.getSessionControl().store(sessionControl);
            res.cookie("sessioncontrolid", sessionControl.getSessionId());
            res.redirect(req.queryParams("redirect"));
            return "";
        });

        /**
         * filter for login page
         */
        before("/login", (req, res) -> {
            if (req.session().attribute("user") != null) {
                halt("Already logged in");
            }
        });

        /**
         * logout page
         */
        get("/logout", (req, res) -> {
            HashMap map = new HashMap();
            req.session().removeAttribute("user");
            if (req.cookies().containsKey("sessioncontrolid")) {
                req.cookies().remove("sessioncontrolid");
                daos.getSessionControl().invalidateSession(req.cookie("sessioncontrolid"));
            }
            res.redirect("/");
            return new ModelAndView(map, "login");
        });

        /**
         * filter for logout page
         */
        before("/logout", (req, res) -> {
            if (req.session().attribute("user") == null) {
                halt("No user logged in");
            }
        });

        /**
         * profile page
         *
         * takes username as an attribute
         *
         * example /profile/user
         */
        get("/profile/:username", (req, res) -> {
            HashMap map = new HashMap();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            String username = req.params(":username");

            if (!user.getUsername().equals(username)) {
                res.redirect("/");
            }

            return new ModelAndView(map, "profile");
        }, new ThymeleafTemplateEngine());

    }

    private void mealRoutes(DaoContainer daos) {
        get("/addmeal", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            String[] ids = req.queryParamsValues("id");
            if (ids != null) {
                List<Foodstuff> selections = new ArrayList<>();
                for (String s : ids) {
                    try {
                        int id = Integer.parseInt(s);
                        selections.add(daos.getFoodstuff().findOne(id));
                    } catch (NumberFormatException e) {
                    }
                }
                map.put("selections", selections);
            }

            map.put("action", "/addmeal.post");
            map.put("title", "Add Meal");
            return new ModelAndView(map, "addmeal");
        }, new ThymeleafTemplateEngine());

        get("/addmealsearch.get", (req, res) -> {
            User user = (User) req.session().attribute("user");

            Object[] split = req.queryParams("param").split(" ");

            List<Foodstuff> list = daos.getFoodstuff().searchGlobal(true, split);
            return list;
        }, json());

        get("/mealdiary", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            String fromStr = req.queryParams("from");
            String toStr = req.queryParams("to");
            String pageStr = req.queryParams("page");

            if (fromStr != null || toStr != null) {
                int offset = 0;
                int page = 1;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate fromDate = LocalDate.parse(fromStr, formatter);
                Timestamp from = Timestamp.valueOf(fromDate.atStartOfDay());

                LocalDate toDate = LocalDate.parse(toStr, formatter);
                Timestamp to = Timestamp.valueOf(toDate.atTime(23, 59, 59));

                try {
                    page = Integer.parseInt(pageStr);
                } catch (NumberFormatException e) {
                }

                offset = page * 10 - 10;

                float count = daos.getMeal().count(user, from, to);
                int pageCount = (int) Math.ceil(count / 10.0);
                for (int i = 0; i < count; i = i + 10) {

                }
                List<Meal> meals = daos.getMeal().findAll(user, from, to, offset, 10);

                //pagination
                map.put("currentPage", page);
                if (pageCount == 0) {
                    map.put("lastPage", 1);
                } else {
                    map.put("lastPage", pageCount);
                }

                if (page > 1) {
                    map.put("prevPage", page - 1);
                } else {
                    map.put("prevPage", 1);
                }

                if (page < pageCount) {
                    map.put("nextPage", page + 1);
                } else {
                    map.put("nextPage", page);
                }

                String type = req.queryParams("type");
                if (type != null && type.equals("today")) {
                    map.put("columnchart", false);
                    map.put("title", "Today's Statistics");
                    map.put("subtitle", "Today's Meals");
                } else if (type != null && type.equals("week")) {
                    map.put("columnchart", true);
                    map.put("title", "Weekly Statistics");
                    map.put("subtitle", "Meals for the Past Week");
                } else {
                    map.put("columnchart", true);
                    map.put("title", "Statistics for period from: " + fromStr + " to " + toStr);
                    map.put("subtitle", "Results");
                }

                float calories = 0;
                float protein = 0;
                float fat = 0;
                float carbs = 0;

                List<Container> dailyTotals = daos.getMeal().dailyTotals(user, from, to);
                for (Container c : dailyTotals) {
                    calories += c.getCalories();
                    protein += c.getProtein();
                    fat += c.getFat();
                    carbs += c.getCarbohydrate();
                    c.round();
                }

                map.put("dailyaverages", dailyTotals);

                map.put("calories", Math.round(calories / dailyTotals.size()));
                map.put("protein", Math.round(protein / dailyTotals.size()));
                map.put("carbs", Math.round(carbs / dailyTotals.size()));
                map.put("fat", Math.round(fat / dailyTotals.size()));

                map.put("fromStr", fromStr);
                map.put("toStr", toStr);

                map.put("results", true);
                map.put("meals", meals);
            }

            return new ModelAndView(map, "mealdiary");
        }, new ThymeleafTemplateEngine());

        get("/mealdiarytoday", (req, res) -> {
            LocalDate beginning = LocalDate.now();
            String beginningString = beginning.getYear() + "-";
            if (beginning.getMonthValue() < 10) {
                beginningString += "0";
            }
            beginningString += beginning.getMonthValue() + "-";
            if (beginning.getDayOfMonth() < 10) {
                beginningString += "0";
            }
            beginningString += beginning.getDayOfMonth();

            LocalDate end = LocalDate.now();
            String endString = end.getYear() + "-";
            if (end.getMonthValue() < 10) {
                endString += "0";
            }
            endString += end.getMonthValue() + "-";
            if (end.getDayOfMonth() < 10) {
                endString += "0";
            }
            endString += end.getDayOfMonth();

            res.redirect("/mealdiary?from=" + beginningString + "&to=" + endString + "&type=today");

            return "";

        });

        get("/mealdiaryweek", (req, res) -> {
            LocalDate beginning = LocalDate.now().minusDays(6);
            String beginningString = beginning.getYear() + "-";
            if (beginning.getMonthValue() < 10) {
                beginningString += "0";
            }
            beginningString += beginning.getMonthValue() + "-";
            if (beginning.getDayOfMonth() < 10) {
                beginningString += "0";
            }
            beginningString += beginning.getDayOfMonth();

            LocalDate end = LocalDate.now();
            String endString = end.getYear() + "-";
            if (end.getMonthValue() < 10) {
                endString += "0";
            }
            endString += end.getMonthValue() + "-";
            if (end.getDayOfMonth() < 10) {
                endString += "0";
            }
            endString += end.getDayOfMonth();

            res.redirect("/mealdiary?from=" + beginningString + "&to=" + endString + "&type=week");

            return "";

        });

        post("/addmeal.post", (req, res) -> {
            User user = (User) req.session().attribute("user");

            Meal meal = new Meal(0, user, new Timestamp(System.currentTimeMillis()), null);
            meal = daos.getMeal().store(meal);

            for (String s : req.queryParamsValues("ingredients")) {
                int id = Integer.parseInt(s);
                float mass = Float.parseFloat(req.queryParams("amountfor:" + s));
                MealComponent component = new MealComponent(0, meal.getId(), mass, daos.getFoodstuff().findOne(id));
                daos.getMealComponent().store(component);
            }

            res.redirect("/viewmeal?id=" + meal.getId());
            return "";
        });
    }

    private void searchRoutes(DaoContainer daos) {
        get("/cookbook", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            map.put("cookings", daos.getCooking().findAll());
            map.put("title", "Cook Book");
            return new ModelAndView(map, "list");
        }, new ThymeleafTemplateEngine());

        get("/pantry", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            map.put("title", "Pantry");

            map.put("foodstuffs", daos.getFoodstuff().findAll(user));

            return new ModelAndView(map, "list");
        }, new ThymeleafTemplateEngine());

        get("/search", (req, res) -> {
            if (req.queryParams("query") == null) {
                res.redirect("/");
                halt();
            }
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            Object[] searchTerms = req.queryParams("query").split(" ");
            List<Foodstuff> foodstuffResult = daos.getFoodstuff().search(user, searchTerms);
            List<Foodstuff> globalResults = daos.getFoodstuff().searchGlobal(searchTerms);

//            List<Cooking> cookingsResult = daos.getCooking().search(searchTerms);
            if (!foodstuffResult.isEmpty()) {
                map.put("foodstuffs", foodstuffResult);
            }

            if (!globalResults.isEmpty()) {
                map.put("globalresults", globalResults);
            }

//            if (!cookingsResult.isEmpty()) {
//                map.put("cookings", cookingsResult);
//            }
            map.put("search", true);
            return new ModelAndView(map, "list");
        }, new ThymeleafTemplateEngine());
    }

    private void viewRoutes(DaoContainer daos) {
        get("/viewfoodstuff", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            int id = 0;

            try {
                id = Integer.parseInt(req.queryParams("id"));
            } catch (NumberFormatException e) {
                res.redirect("/");
                halt();
            }

            Foodstuff foodstuff = daos.getFoodstuff().findOne(user, id);
            if (foodstuff == null) {
                res.redirect("/");
                halt();
            }

            map.put("calories", Math.round(foodstuff.getCalories() * 100));
            map.put("carbohydrate", Math.round(foodstuff.getCarbohydrate() * 100));
            map.put("fat", Math.round(foodstuff.getFat() * 100));
            map.put("protein", Math.round(foodstuff.getProtein() * 100));
            map.put("foodstuff", foodstuff);
            map.put("title", foodstuff.getName());
            map.put("type", "foodstuff");
            map.put("id", foodstuff.getId());

            return new ModelAndView(map, "view");
        }, new ThymeleafTemplateEngine());

        get("/viewcooking", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            int id = 0;

            try {
                id = Integer.parseInt(req.queryParams("id"));
            } catch (NumberFormatException e) {
                res.redirect("/");
                halt();
            }

            Cooking cooking = daos.getCooking().findOne(id);
            if (cooking == null) {
                res.redirect("/");
                halt();
            } else {
                Foodstuff findOne = daos.getFoodstuff().findOne(cooking.getName(), cooking.getIdentifier());
                if (findOne == null) {
                    map.put("notfoodstuffyet", true);
                }

                map.put("calories", Math.round(cooking.getCalories() * 100));
                map.put("carbohydrate", Math.round(cooking.getCarbohydrate() * 100));
                map.put("fat", Math.round(cooking.getFat() * 100));
                map.put("protein", Math.round(cooking.getProtein() * 100));
                map.put("cooking", cooking);
                map.put("title", cooking.getName());
                map.put("type", "cooking");
                map.put("id", cooking.getId());
            }

            return new ModelAndView(map, "view");
        }, new ThymeleafTemplateEngine());

        get("/viewmeal", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            int id = 0;
            try {
                id = Integer.parseInt(req.queryParams("id"));
            } catch (NumberFormatException e) {
                res.redirect("/");
                halt();
            }

            Meal meal = daos.getMeal().findOne(user, id);
            if (meal == null) {
                res.redirect("/");
                halt();
            }

            String url = "/addmeal?";
            List<MealComponent> components;
            if ((components = meal.getComponents()) != null) {
                for (MealComponent c : meal.getComponents()) {
                    url += "&id=" + c.getGlobalReferenceId();
                }
            }

            map.put("url", url);
            map.put("meal", meal);
            map.put("title", meal.getName());
            map.put("type", "meal");
            map.put("id", meal.getId());

            return new ModelAndView(map, "view");
        }, new ThymeleafTemplateEngine());
    }

    private void frontPageRoutes(DaoContainer daos) {
        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            map.put("foodstuffs", daos.getFoodstuff().getExpiring(user, 5));

            List<Container> dailyTotals = daos.getMeal().dailyTotals(user, Timestamp.valueOf(LocalDate.now().atStartOfDay()), Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59)));
            float calories = 0;
            if (!dailyTotals.isEmpty()) {
                calories = dailyTotals.get(0).getCalories();
            }
            map.put("calories", Math.round(calories));

            Meal latest = daos.getMeal().findLatest(user);
            if (latest != null) {
                long until = latest.getDate().toLocalDateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
                long hours = (until - (until % 60)) / 60;
                long minutes = until % 60;
                map.put("sincelastmeal", true);
                map.put("sincelastmealhours", hours);
                map.put("sincelastmealminutes", minutes);
            }

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/expiring.get", (req, res) -> {
            User user = (User) req.session().attribute("user");
            return daos.getFoodstuff().getExpiring(user, 5);
        }, json());

    }

    private void editRoutes(DaoContainer daos) {
        get("/edititem", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            try {
                int id = Integer.parseInt(req.queryParams("id"));
                Foodstuff foodstuff = daos.getFoodstuff().findOne(id);

                map.put("globalreferenceid", foodstuff.getId());
                map.put("title", "Edit " + foodstuff.getName());
                map.put("name", foodstuff.getName());
                map.put("location", foodstuff.getLocation());
                map.put("identifier", foodstuff.getIdentifier());

                map.put("labelname", "Name");
                map.put("labelidentifier", "Serial Number");
                map.put("foodstuff", true);
                map.put("producer", foodstuff.getProducer());
                map.put("expiration", foodstuff.getExpirationString());
                map.put("calories", foodstuff.getCalories() * 100);
                map.put("carbohydrate", foodstuff.getCarbohydrate() * 100);
                map.put("fat", foodstuff.getFat() * 100);
                map.put("protein", foodstuff.getProtein() * 100);
            } catch (NumberFormatException | SQLException e) {
                res.redirect("/");
                halt();
            }

            map.put("action", "/editfoodstuff.post");
            return new ModelAndView(map, "additem");
        }, new ThymeleafTemplateEngine());

        post("/editfoodstuff.post", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            String globalReferenceId = req.queryParams("globalreferenceid");
            String name = req.queryParams("name");
            String identifier = req.queryParams("identifier");
            String location = req.queryParams("location");
            String producer = req.queryParams("producer");
            String expiration = req.queryParams("expiration");
            String caloriesString = req.queryParams("calories");
            String carbohydrateString = req.queryParams("carbohydrate");
            String fatString = req.queryParams("fat");
            String proteinString = req.queryParams("protein");

            try {
                int foodstuffId = Integer.parseInt(globalReferenceId);
                Foodstuff foodstuff = daos.getFoodstuff().findOne(foodstuffId);
                foodstuff.setName(name);
                foodstuff.setIdentifier(identifier);
                foodstuff.setLocation(location);
                foodstuff.setCalories(Float.parseFloat(caloriesString) / 100);
                foodstuff.setCarbohydrate(Float.parseFloat(carbohydrateString) / 100);
                foodstuff.setFat(Float.parseFloat(fatString) / 100);
                foodstuff.setProtein(Float.parseFloat(proteinString) / 100);

            } catch (NumberFormatException | SQLException e) {
                res.redirect("/fail?msg=" + e.toString());
                Logger.getLogger(WebMethods.class.getName()).log(Level.SEVERE, null, e);
                halt();
            }
            res.redirect("/");
            return "";
        });

        get("/editmeal", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            //find id string for the meal to be edited and check whether it's null
            String idStr = req.queryParams("id");
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Meal meal = daos.getMeal().findOne(user, id);
                    //check to see if a meal was found using given id
                    if (meal != null) {
                        map.put("meal", meal);
                        map.put("action", "/editmeal.post");
                        map.put("date", meal.getTime());
                    } else {
                        res.redirect("/");
                        halt();
                    }

                } catch (NumberFormatException e) {
                    res.redirect("/");
                    halt();
                }
            }
            return new ModelAndView(map, "addmeal");
        }, new ThymeleafTemplateEngine());

        post("/editmeal.post", (req, res) -> {
            User user = (User) req.session().attribute("user");

            String mealIdStr = req.queryParams("mealid");
            String dateTimeStr = req.queryParams("datetime");
            Meal newMeal = new Meal(0, user, null, new ArrayList<>());

            if (mealIdStr != null && dateTimeStr != null) {
                try {
                    int id = Integer.parseInt(mealIdStr);
                    newMeal.setId(id);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime time = LocalDateTime.parse(dateTimeStr.replace("T", " "), formatter);
                    Timestamp dateTime = Timestamp.valueOf(time);
                    newMeal.setDate(dateTime);

                    for (String s : req.queryParamsValues("ingredients")) {
                        int globalReferenceId = Integer.parseInt(s);
                        //if this component was generated from a database entry it should have a component id
                        String componentIdStr = req.queryParams("globrefid:" + s);
                        String massStr = req.queryParams("amountfor:" + s);
                        float mass = 0;
                        if (massStr != null) {
                            mass = Float.parseFloat(massStr);
                        }
                        int componentId = 0;
                        if (componentIdStr != null) {
                            componentId = Integer.parseInt(componentIdStr);
                        }
                        MealComponent component = new MealComponent(componentId, id, mass, globalReferenceId);
                        newMeal.getComponents().add(component);
                    }

                    daos.getMeal().update(user, newMeal);
                    res.redirect("/viewmeal?id=" + newMeal.getId());
                } catch (NumberFormatException e) {

                }
            }

            return "";
        });
    }

    private void cookingRoutes(DaoContainer daos) {
        get("/addcooking", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            map.put("addcooking", true);
            map.put("action", "/addcooking.post");
            return new ModelAndView(map, "addmeal");
        }, new ThymeleafTemplateEngine());

        post("/addcooking.post", (req, res) -> {
            List<MealComponent> components = new ArrayList<>();
            User user = (User) req.session().attribute("user");

            String name = req.queryParams("cookingname");
            String directions = req.queryParams("directions");
            String description = req.queryParams("description");
            String cookingType = req.queryParams("cookingtype");
            String totalMassString = req.queryParams("totalmass");
            float totalMass = 0;
            try {
                if (totalMassString != null) {
                    totalMass = Float.parseFloat(totalMassString);
                }
            } catch (NumberFormatException e) {
            }

            Cooking cooking = new Cooking(0, name, totalMass, new Timestamp(System.currentTimeMillis()), null);
            cooking = daos.getCooking().store(cooking);

            for (String s : req.queryParamsValues("ingredients")) {
                int id = Integer.parseInt(s);
                float mass = Float.parseFloat(req.queryParams("amountfor:" + s));
                Ingredient ingredient = new Ingredient(0, id, cooking.getId(), mass, 0, 0, 0, 0, daos.getFoodstuff().findOne(id));
                daos.getIngredient().store(ingredient);
            }
            res.redirect("/viewcooking?id=" + cooking.getId());
            return "";
        });
    }

    private void deleteRoutes(DaoContainer daos) {
        get("/deletemeal", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            int id = 0;

            try {
                id = Integer.parseInt(req.queryParams("id"));
            } catch (NumberFormatException e) {
                res.redirect("/");
                halt();
            }

            Meal meal = daos.getMeal().findOne(user, id);

            daos.getMeal().delete(user, id);
            res.redirect("/");
            return "";
        });

        get("/deletefoodstuff", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);
            int id = 0;

            try {
                id = Integer.parseInt(req.queryParams("id"));
            } catch (NumberFormatException e) {
                res.redirect("/");
                halt();
            }

            daos.getFoodstuff().delete(user, id);
            res.redirect("/");
            return "";
        });
    }

    private void toolRoutes(DaoContainer daos) {
        get("/foodstuffdump", (req, res) -> {
            List<Foodstuff> list = daos.getFoodstuff().globalDump();
            return list;
        }, json());

        get("/uploadfoodstuffdatabase", (req, res) -> {
            HashMap map = new HashMap<>();
            User user = (User) req.session().attribute("user");
            map.put("user", user);

            return new ModelAndView(map, "addfoodstuffdatabase");
        }, new ThymeleafTemplateEngine());

        post("/addfoodstuffdatabase.post", (req, res) -> {
            try {
                String input = req.queryParams("input");
                JsonArray jsonArray = new JsonParser().parse(input).getAsJsonArray();
                Iterator<JsonElement> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    JsonObject next = iterator.next().getAsJsonObject();
                    String name = next.get("name").getAsString();
                    String identifier = next.get("identifier").getAsString();
                    String producer = next.get("producer").getAsString();
                    float calories = next.get("calories").getAsFloat();
                    float carbohydrate = next.get("carbohydrate").getAsFloat();
                    float fat = next.get("fat").getAsFloat();
                    float protein = next.get("protein").getAsFloat();

                    Foodstuff foodstuff = new Foodstuff(name, identifier, producer, null, calories, carbohydrate, fat, protein, 0, 0, 0, null, null);

                    daos.getFoodstuff().storeGlobal(foodstuff);
                }
            } catch (JsonSyntaxException | IllegalStateException exception) {
                res.redirect("/fail?msg=" + exception);
                System.out.println(exception);
            }
            res.redirect("/");
            return "";
        });
    }

}
