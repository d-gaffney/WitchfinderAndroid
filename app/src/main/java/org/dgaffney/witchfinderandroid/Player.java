package org.dgaffney.witchfinderandroid;

/**
 * Created by Davin on 11/05/2016.
 */
public class Player {

    private int playerID;
    private int gameID;
    private int playerClientID;

    public Player(int playerID, int gameID, int playerClientID){
        this.playerID = playerID;
        this.gameID = gameID;
        this.playerClientID = playerClientID;
    }

    public int getPlayerID(){
        return playerID;
    }

    public int getGameID(){
        return gameID;
    }
}
