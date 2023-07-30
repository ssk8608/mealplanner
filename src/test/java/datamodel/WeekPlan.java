package datamodel;

import java.util.List;

public class WeekPlan {

    String name;
    List<MealPlan> items;
    boolean publishAsPublic;

    public boolean isPublishAsPublic() {
        return publishAsPublic;
    }

    public void setPublishAsPublic(boolean publishAsPublic) {
        this.publishAsPublic = publishAsPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MealPlan> getItems() {
        return items;
    }

    public void setItems(List<MealPlan> items) {
        this.items = items;
    }

}
