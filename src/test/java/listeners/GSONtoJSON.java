package listeners;

import com.google.gson.Gson;

public class GSONtoJSON {
    public static String convertTOJSON(Object obj){

        return(new Gson().toJson(obj));
    }

}
