package org.dgaffney.witchfinderandroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davin on 20/04/2016.
 */
public class ActionSelectListAdapter extends BaseAdapter {

    String TAG = "ListAdapter";

    private final List<PlayerAction> mActions = new ArrayList<>();

    private final Context mContext;

    public ActionSelectListAdapter(Context context){
        mContext = context;
    }

    @Override
    public PlayerAction getItem(int pos) {

        return mActions.get(pos);

    }

    public void add(PlayerAction pAction) {

        mActions.add(pAction);
        notifyDataSetChanged();
        Log.i(TAG, "add");

    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public int getCount() {

        return mActions.size();

    }

    public void setItemList(List<PlayerAction> actions){
        for (PlayerAction playerAction: actions){
            add(playerAction);
        }
        notifyDataSetChanged();
    }

    public void clearList(){
        mActions.clear();
    }



    @Override
    public View getView(final int position, final View convertView, ViewGroup parent){

        final PlayerAction playerAction = mActions.get(position);

        // Inflate layout
        RelativeLayout actionLayout = (RelativeLayout) LayoutInflater
                .from(mContext).inflate(R.layout.action_item, parent, false);

        final TextView actionTitle = (TextView) actionLayout.findViewById(R.id.title);
        actionTitle.setText(playerAction.getTitle());

        final TextView actionDescription = (TextView) actionLayout.findViewById(R.id.description);
        actionDescription.setText(playerAction.getDescription());

        final String action_id = playerAction.getAction_id();

        return actionLayout;
    }
}
