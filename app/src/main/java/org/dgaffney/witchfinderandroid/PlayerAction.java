package org.dgaffney.witchfinderandroid;

/**
 * Created by Davin on 20/04/2016.
 */
public class PlayerAction {

    private String mTitle;
    private String mDescription;
    private String mAction_id;

    PlayerAction(String title, String description, String action_id){
        this.mTitle = title;
        this.mDescription = description;
        this.mAction_id = action_id;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription(){
        return mDescription;
    }
    public void setDescription(String description){
        mDescription = description;
    }

    public String getAction_id(){
        return mAction_id;
    }
}
