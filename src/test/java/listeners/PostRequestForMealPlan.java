package listeners;

import datamodel.*;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.*;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static utilities.ReadDataFromExcelFile.readExcel;

public class PostRequestForMealPlan extends TestBase{

   private Response response;
   public static List<MealPlan> weekList = new ArrayList<>();
   public static ArrayList IDList= new ArrayList();
   public static ArrayList ingList = new ArrayList();
   public static ArrayList ingredientNameList = new ArrayList();
   int dinner_id;
   String day,dinner_recipe;
   float budgetAmount;
   ArrayList ingredientNotAvailableRecipeIDList = new ArrayList();
   ArrayList shoppingList = new ArrayList();
   SoftAssert softAssert = new SoftAssert();

   /*------Creating the request object for meal planner and posting the request-----*/
    @Test(priority =1)
    public void createMealPlanner() throws Exception {

       Object[][] mealDetails  ;
       mealDetails = readExcel(excelFilePath, "mealdetails.xlsx", "Sheet1");
       System.out.println("mealDetails length : "+mealDetails.length);
       for(int i=0;i<mealDetails.length;i++) {

          HashMap<String,String> mealDetailsData;
          mealDetailsData = (HashMap<String, String>) mealDetails[i][0];

          Ingredients ingredients1 = new Ingredients();
          //ingredients.setId(Integer.parseInt(mealDetailsData.get("BID")));
          ingredients1.setName("1 Banana");

          Ingredients ingredients = new Ingredients();
          //ingredients.setId(Integer.parseInt(mealDetailsData.get("BID")));
          IDList.add(Integer.parseInt(mealDetailsData.get("BID")));
          ingredients.setName(mealDetailsData.get("Bname"));
          ingredients.setUnit(mealDetailsData.get("Bunit"));
          ingredients.setAmount(Integer.parseInt(mealDetailsData.get("Bamount")));
          ingredients.setImage(mealDetailsData.get("BImageURL"));

          List<Ingredients> ingredientsList = new ArrayList<>();
          ingredientsList.add(ingredients1);
          ingredientsList.add(ingredients);

          CustomType customLunch = new CustomType();
          customLunch.setId(Integer.parseInt(mealDetailsData.get("custom_lunch_id")));
          IDList.add(mealDetailsData.get("custom_lunch_id"));
          customLunch.setServings(Integer.parseInt(mealDetailsData.get("custom_lunch_servings")));
          customLunch.setTitle(mealDetailsData.get("custom_lunch_title"));
          customLunch.setImage(mealDetailsData.get("custom_lunch_imageURL"));

          CustomType customDinner = new CustomType();
          customDinner.setId(Integer.parseInt(mealDetailsData.get("custom_dinner_id")));
          IDList.add(mealDetailsData.get("custom_dinner_id"));
          customDinner.setServings(Integer.parseInt(mealDetailsData.get("custom_dinner_servings")));
          customDinner.setTitle(mealDetailsData.get("custom_dinner_title"));
          customDinner.setImage(mealDetailsData.get("custom_dinner_imageURL"));

          Recipes b_recipes = new Recipes();
          b_recipes.setIngredients(ingredientsList);

          MealPlan b_mealPlan = new MealPlan();
          b_mealPlan.setDay(i+1);
          b_mealPlan.setSlot(Integer.parseInt(mealDetailsData.get("Bslot")));
          b_mealPlan.setPosition(0);
          b_mealPlan.setType("INGREDIENTS");
          b_mealPlan.setValue(b_recipes);

          MealPlan l_mealPlan = new MealPlan();
          l_mealPlan.setDay(i+1);
          l_mealPlan.setSlot(Integer.parseInt(mealDetailsData.get("Lslot")));
          l_mealPlan.setPosition(0);
          l_mealPlan.setType("RECIPE");
          l_mealPlan.setValue(customLunch);

          MealPlan d_mealPlan = new MealPlan();
          d_mealPlan.setDay(i+1);
          d_mealPlan.setSlot(Integer.parseInt(mealDetailsData.get("Dslot")));
          d_mealPlan.setPosition(0);
          d_mealPlan.setType("RECIPE");
          d_mealPlan.setValue(customDinner);

          MealPlan d_mealPlan1 = new MealPlan();
          d_mealPlan1.setDays(mealDetailsData.get("Day"));
          String str = d_mealPlan1.getDays();

          if(str.equals("Saturday")) {
             day = str;
             dinner_id = customDinner.getId();
             dinner_recipe = customDinner.getTitle();
          }

          weekList.add(b_mealPlan);
          weekList.add(l_mealPlan);
          weekList.add(d_mealPlan);
       }

       System.out.println("Size of the list(Number of meals): "+weekList.size());
       WeekPlan weekPlan = new WeekPlan();
       weekPlan.setName("My new meal plan for the week.");
       weekPlan.setItems(weekList);
       weekPlan.setPublishAsPublic(false);

       String postObject =GSONtoJSON.convertTOJSON(weekPlan);    // to convert java object to JSON using GSON library.
       //System.out.println("JSON body of post request: \n"+postObject);

       response = postRequest(baseURI,postObject);   //method to post the request.
       //response.then().log().body();
       Assert.assertEquals(response.statusCode(),200);

   }

   /*------Checking nutrient limits for the day and the whole week-----*/
   @Test(dependsOnMethods = "createMealPlanner",priority =2)
   public void checkNutrientWithinLimits() throws Exception {

      JsonPath jsonPathEvaluator = response.jsonPath();
      String planStatus = jsonPathEvaluator.get("status");
      String planName = jsonPathEvaluator.get("mealPlan.name");
      int planID = jsonPathEvaluator.get("mealPlan.id");
      ArrayList nutrientSummary = jsonPathEvaluator.get("mealPlan.days");
      ArrayList nutrient = jsonPathEvaluator.get("mealPlan.days.nutritionSummary");

      System.out.println("Post request was successfull!!!  status:\n "+planStatus);
      System.out.println("Plan name :\n "+planName);
      System.out.println("Plan ID :\n "+planID);
      System.out.println("Plan's nutrientSummary size :\n "+nutrientSummary.size());

      LinkedHashMap<String,Object> nut;
      float totalCalories=0;
      Object[][] calorieDetails  ;
      calorieDetails = readExcel(excelFilePath, "mealdetails.xlsx", "Sheet2");
      HashMap<String,String> calorieDetailsData;
      calorieDetailsData = (HashMap<String, String>) calorieDetails[0][0];
      float calPerDay= Float.parseFloat(calorieDetailsData.get("CaloriesPerDay"));
      float calPerWeek= Float.parseFloat(calorieDetailsData.get("CaloriesPerWeek"));
      budgetAmount= Float.parseFloat(calorieDetailsData.get("MaximumBudgetValuePerWeek"));
      for (int i=0;i<nutrient.size();i++){
         nut = (LinkedHashMap<String, Object>) nutrient.get(i);
         ArrayList valueSet = (ArrayList) nut.get("nutrients");
         //int sizeOfValueSet = valueSet.size();
         for (Object obj : valueSet) {
            HashMap<String, Object> detailsOfNutrient = (HashMap<String, Object>) obj;
            String nameOfNutrient = (String) detailsOfNutrient.get("name");
            if (nameOfNutrient.equals("Calories")) {
               float amount = (float) detailsOfNutrient.get("amount");
               //int percentOfDailyNeeds = (int) detailsOfNutrient.get("percentOfDailyNeeds");
               totalCalories = totalCalories + amount;

               System.out.println("\nTotal calories consumed for the day " + (i + 1) + " : " + amount);
               softAssert.assertTrue((amount <= calPerDay),"Calorie for the day is beyond the limit!!!");  // If the assertion fails, the assertion message will be displayed.
               //System.out.println("Calorie for the day is within the limit!!!");

            }
         }
      }
      System.out.println("\nTotal calories consumed for the week: "+totalCalories);
      softAssert.assertTrue((totalCalories<=calPerWeek),"Calorie for the week is beyond the limit!!!");  // If the assertion fails, the assertion message will be displayed.
      System.out.println("Calorie for the week is within the limit!!!");
   }

   @Test(priority =3)
   public void checkAvailabilityOfIngredients() throws Exception {

      boolean flag = true;

      /*----------Fetching Ingredients available from the Excel--------------*/

      Object[][] ingredientDetails;
      ingredientDetails = readExcel(excelFilePath, "mealdetails.xlsx", "Sheet3");

      int j = 0;
      while (j < ingredientDetails.length) {
         HashMap<String, String> ingredientDetailsData = (HashMap<String, String>) ingredientDetails[j][0];
         ingList.add(ingredientDetailsData.get("IngredientsAvailable"));
         j++;
      }
      System.out.println("Ingredients available List: " + ingList);

      /*----------Fetching Ingredients required for the recipe using GET request --------------*/

      System.out.println("\nNumber of Recipe IDs : " + IDList.size());
      //System.out.println("\nIDs are : " + IDList);
      int id, i = 0;
      while (i < IDList.size()) {
         id = Integer.parseInt(IDList.get(i).toString());
         String url = "https://api.spoonacular.com/recipes/" + id + "/ingredientWidget.json";
         Response response = getRequest(url);
         //response.then().log().body();
         JsonPath jsonPathEvaluator = response.jsonPath();
         ingredientNameList = (ArrayList) jsonPathEvaluator.get("ingredients.name");
         //System.out.println("Ingredients List for Recipe ID : " + id + " are : " + ingredientNameList);

         /*----------Checking Ingredients and adding it to Shopping list--------------*/

         for (Object str : ingredientNameList) {
            // if ((!ingList.contains(str)) && (!shoppingList.contains(str))) {
            if ((ingList.contains(str)==false) && (shoppingList.contains(str)==false)) {
               flag = false;
               shoppingList.add(str);
               ingredientNotAvailableRecipeIDList.add(id);
            }
         }
         i++;
      }
      System.out.println("Flag value: "+flag);
      softAssert.assertEquals(flag,false,"All ingredients are available. Shopping list is not created.");  // If the assertion fails, the assertion message will be displayed.
   }

   /*----------Fetching alternative recipes for those ingredients not available using GET request --------------*/

   @Test(priority =4)
   public void getSimilarRecipe(){
      System.out.println("\nIngredient Not Available Recipe ID List : "+ingredientNotAvailableRecipeIDList);
      System.out.println("\nSize of the list : "+ingredientNotAvailableRecipeIDList.size());
      for (int i=0;i<ingredientNotAvailableRecipeIDList.size();i++) {
         int id = Integer.parseInt(ingredientNotAvailableRecipeIDList.get(i).toString());
         System.out.println("\nAlternative Recipes for the recipe ID: "+id+" are as follows: \n");
         String url = "https://api.spoonacular.com/recipes/"+id+"/similar";
         Response response = getRequest(url);
         response.then().log().body();
         softAssert.assertEquals(response.getStatusCode(),200,"Similar recipes are not found for some recipe for which ingredient not available.");  // If the assertion fails, the assertion message will be displayed.
      }
   }

   /*----------Adding unavailable ingredients to the shopping list--------------*/
   @Test(priority =5)
   public void createShoppingList(){

      System.out.println("\nShopping list: " + shoppingList + "\nShopping list size: " + shoppingList.size());
      String url = "https://api.spoonacular.com/mealplanner/"+UserName+"/shopping-list/items?hash="+hash;
      ShoppingList shop = new ShoppingList();

      /*---------- Add an item to the current shopping list of a user ------------*/   // shoppingList.size()

      for(int m=0;m<shoppingList.size();m++) {
         String ing = shoppingList.get(m).toString();
         shop.setItem(ing);
         shop.setAsile("");
         shop.setParse(true);
         String postObject =GSONtoJSON.convertTOJSON(shop);
         Response response = postRequest(url,postObject);
      }
      System.out.println("\nShopping list for the week: \n");
      Response response = getRequest("https://api.spoonacular.com/mealplanner/"+UserName+"/shopping-list?hash="+hash);
      response.then().log().body();
      softAssert.assertEquals(response.getStatusCode(),200,"Shopping list is not generated.");  // If the assertion fails, the assertion message will be displayed.
      System.out.println("Unavailable ingredients are added to the shopping List.");

      /*----------Checking if shopping cost is within budget amount--------------*/

      JsonPath jsonPathEvaluator = response.jsonPath();
      float cost = jsonPathEvaluator.get("cost");
      System.out.println("\nTotal cost expected for shopping is :"+cost+"\nBudget Amount : "+budgetAmount);
      softAssert.assertTrue(cost<=budgetAmount,"Shopping cost exceeds budget amount.");  // If the assertion fails, the assertion message will be displayed.
      System.out.println("Shopping cost within budget amount.");
   }

   /*----------Checking for Wine Pairing for Saturday dinner--------------*/
   @Test(priority =6)
   public void winePairing(){
      String url = "https://api.spoonacular.com/recipes/"+dinner_id+"/information?includeNutrition=false";
      Response response = getRequest(url);
      //response.then().log().body();
      JsonPath jsonPathEvaluator = response.jsonPath();
      ArrayList pairedWines = (ArrayList)jsonPathEvaluator.get("winePairing.pairedWines");
      String pairingText = jsonPathEvaluator.get("winePairing.pairingText");
      System.out.println("\nWines that can be paired for the Weekend Recipe:\nDay : "+day);
      System.out.println("Dinner ID : "+dinner_id);
      System.out.println("Recipe Name : "+dinner_recipe);
      System.out.println("Wines that can be paired : "+ pairedWines);
      System.out.println("Description :\n"+pairingText);
      softAssert.assertNotNull(pairedWines,"Wine is not paired for the Saturday party meal.");  // If the assertion fails, the assertion message will be displayed.
   }

    public Response postRequest(String url, String postRequestBody){
        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("apiKey",apiKey)
                .and()
                .body(postRequestBody)
                .when()
                .post(url)
                .then()
                .extract().response();
        return response;
    }

   public Response getRequest(String url){
      Response response = given()
              .contentType(ContentType.JSON)
              .queryParam("apiKey",apiKey)
              .when()
              .get(url)
              .then()
              .extract().response();
      return response;
   }
}
