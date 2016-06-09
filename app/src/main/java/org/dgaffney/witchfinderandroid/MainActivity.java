package org.dgaffney.witchfinderandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity{

    final String url = "http://10.0.2.2:9000/"; // localhost
    // String url = "http://witchfinder.heroku.com/"; // cloud service
    public Button mConnectButton;
    public TextView mConnectText;
    public Button mStartButton;
    public Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayer = new Player(0,0,0);

        mConnectButton = (Button) findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectRequest();
            }
        });

        mConnectText = (TextView) findViewById(R.id.connection_text);

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActionSelectActivity.class);
                startActivity(intent);
            }
        });

    }

    public void ConnectRequest(){
        String startUrl = url + "start";
        StringRequest stringRequest = new StringRequest(startUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                VolleyLog.v("Response:%n %s", response);
                mConnectText.setText(response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                VolleyLog.e("Error: ", error.getMessage());
                mConnectText.setText("Connection Error");
            }
        });
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void joinGame()throws JSONException {
        String joinUrl = url + "join";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(joinUrl, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject jsonResponse) {
                VolleyLog.v("Response: %s", jsonResponse.toString());
                try{
                    mPlayer = parseToPlayer(jsonResponse);
                    //mConnectText.setText(playerID);
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

    private Player parseToPlayer(JSONObject object) throws JSONException {

        int PlayerID = object.getInt("player_id");
        int GameID = object.getInt("game_id");
        int ClientID = object.getInt("client_id");

        Player player = new Player(PlayerID, GameID, ClientID);

        return player;
    }

}
