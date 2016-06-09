package org.dgaffney.witchfinderandroid;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActionSelectActivity extends ListActivity {

    ActionSelectListAdapter mAdapter;
    TextView footerView;

    String [] descriptions;
    String [] actIDs;

    Player mPlayer;
    PlayerAction selectedAction;
    boolean roundReady;
    int gameRound;
    int pollCounter;

    Handler handler;

    private static final String fragTag = "message";
    String endOfTurnMessage;
    String TAG = "ActionSelectActivity";
    String url = "http://10.0.2.2:9000/"; // localhost
    // String url = "http://witchfinder.heroku.com/"; // cloud service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ActionSelectListAdapter(getApplicationContext());

        mPlayer = null; // To be retrieved from memory after first game.

        roundReady = false;

        gameRound = 0;

        descriptions = getResources().getStringArray(R.array.action_descriptions);
        actIDs = getResources().getStringArray(R.array.action_ids);

        getListView().setFooterDividersEnabled(true);

        footerView = (TextView) getLayoutInflater().inflate(R.layout.footer_view, null, false);

        getListView().addFooterView(footerView);

        // onClick list item - sets that item to selctedAction
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                selectedAction = mAdapter.getItem(position);
            }

        });


        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send volley request for EndOfTurnMessage
                if (selectedAction == null) {
                    Toast.makeText(getBaseContext(), "No action selected", Toast.LENGTH_LONG).show();
                } else {
                    if (roundReady){
                        try {
                            actionListRequest();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            endOfTurnRequest();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        // set layout
        getListView().setAdapter(mAdapter);

        // apply to join server
        try{
            joinGameRequest();
        }catch(Exception e){
            e.printStackTrace();
        }


        // ask for the first round options for the first game
        // actionListRequest();

        // start polling the server to see if first round has started
        //startRequestHandler();
    }

    // JSON parsing. Move to mapper class?
    private List<PlayerAction> parseActionList (JSONArray jsonArray) throws JSONException{
        List<PlayerAction> actionList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject jObject = jsonArray.getJSONObject(i);
            String Title = jObject.getString("title");
            String Description = jObject.getString("description");
            String Action_id = jObject.getString("action_id");

            PlayerAction playerAction = new PlayerAction(Title, Description, Action_id);
            actionList.add(playerAction);
        }

        return actionList;
    }

    private Player parseToPlayer(JSONObject object) throws JSONException {
        Log.i(TAG, object.toString());
        int PlayerID = object.getInt("playerID");
        int GameID = object.getInt("gameID");
        int ClientID = object.getInt("playerClientId");

        Player player = new Player(PlayerID, GameID, ClientID);

        return player;
    }

    private PlayerAction parseToAction(JSONObject object) throws JSONException {
        Log.i(TAG, object.toString());
        String Title = object.getString("title");
        String Description = object.getString("description");
        String Action_id = object.getString("action_id");

        PlayerAction playerAction = new PlayerAction(Title, Description, Action_id);
        Log.i(TAG, playerAction.getAction_id());
        return playerAction;
    }

    private JSONObject parseFromAction(PlayerAction playerAction)throws JSONException{
        JSONObject parsedAction = new JSONObject();
        parsedAction.put("title", playerAction.getTitle());
        parsedAction.put("description", playerAction.getDescription());
        parsedAction.put("action_id", playerAction.getAction_id());

        return parsedAction;
    }

    public void joinGameRequest(){
        String joinUrl = url + "join" + "/0";
        // set to take from mPlayer.playerID
        // String joinUrl = url + "join" + "/" + "mPlayer.getClientID";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(joinUrl, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonResponse) {
                VolleyLog.v("Response: %s", jsonResponse.toString());
                try{
                    mPlayer = parseToPlayer(jsonResponse);
                    Log.i(TAG, mPlayer.toString());
                    //mConnectText.setText(playerID);
                    actionListRequest();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    //retrieve list of playerActions for list adapter
    public void actionListRequest(){
        Log.i(TAG, "actionListRequest");
        String alrURL = url + "actionlist" + "/" + mPlayer.getGameID();
        JsonArrayRequest JArrayRequest = new JsonArrayRequest(alrURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                VolleyLog.v("Response: %s", jsonArray.toString());
                try{
                    mAdapter.clearList();
                    mAdapter.setItemList(parseActionList(jsonArray));
                    roundReady = false;
                    startRequestHandler();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(JArrayRequest);
    }

    public boolean autoSendDoNothingCheck(){

        String selectedActionID = selectedAction.getAction_id();
        if (selectedActionID.equals("waiting_for_players")
                || selectedActionID.equals("end_of_turn")){
            return false;
        } else if(pollCounter == 7){
            return true;
        } else {
            return false;
        }
    }

    public void startRequestHandler(){
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            // is gameStarted initialized?
            if (mPlayer != null){
                startRoundRequest();
            }
            Log.i(TAG, "handler run. round ready: " + roundReady);
            }
        }, 5000);

        Log.i(TAG, " Start Request Handler");
    }

    // poll server every 5 seconds to check if game has started
    // each poll from first player (pID 0) will tick up a counter,
    // server will start the game after 5 polls
    public void startRoundRequest(){

        final String srrUrl = url + "roundStartRequest/" + mPlayer.getGameID() + "/" + mPlayer.getPlayerID();
        StringRequest startRequest = new StringRequest(srrUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response.equals("ready")) {
                    pollCounter = 0;
                    // retrieve end of round response (stop polling)
                    try{
                        endOfTurnRequest();
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                } else {
                    startRequestHandler();
                    pollCounter++;
                }
                Log.i("onResponse", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.removeCallbacksAndMessages(null);
                Log.i("onErrorResponse", error.toString());
            }
        });
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(startRequest);

    }
    // Send selected Player Action.
    private void endOfTurnRequest()throws JSONException {
        String endUrl = url + "endturn/" + mPlayer.getGameID() + "/" + mPlayer.getPlayerID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(endUrl,
                parseFromAction(selectedAction), new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonResponse) {
                VolleyLog.v("Response: %s", jsonResponse.toString());
                try{
                    // Should receive a player action with end of turn dialog as description
                    PlayerAction resultAction = parseToAction(jsonResponse);
                    // Displays this in list and adds it to the
                    mAdapter.clearList();
                    mAdapter.add(resultAction);
                    // Auto selects returned action (set to selected action)
                    selectedAction = resultAction;
                    // Set footer text to Continue if possible?
                    //footerView.setText("Continue");
                    // trigger polling for start of next round
                    if (resultAction.getAction_id().equals("end_of_turn")){
                        roundReady = true;
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

}
