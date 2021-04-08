//NAME:         Peter McKendry
//STUDENT ID:   S1915350

package org.me.gcu.equakestartercode;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;


public class ListViewAdapter extends ArrayAdapter<Earthquake> implements View.OnClickListener{

    private Context mContext;

    private static class ViewHolder {
        RelativeLayout container;
        TextView txtTitle;
        TextView txtMag;
        TextView txtDate;
        TextView txtDepth;
    }


    ListViewAdapter(ArrayList<Earthquake> data, Context context) {
        super(context, R.layout.row_item, data);
        ArrayList<Earthquake> dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
    }

    private int lastPosition = -1;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Earthquake dataModel = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.container = convertView.findViewById(R.id.container);
            viewHolder.txtTitle = convertView.findViewById(R.id.title);
            viewHolder.txtMag = convertView.findViewById(R.id.txtMag);
            viewHolder.txtDate = convertView.findViewById(R.id.txtDate);
            viewHolder.txtDepth = convertView.findViewById(R.id.txtDepth);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        viewHolder.container.setBackgroundColor(Color.WHITE);

        //Set animations
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        //Set colours according to the magnitude value
        if(dataModel.getMagColour()!=null){
            if(dataModel.getMagColour().equals("green")) {
                viewHolder.txtMag.setBackgroundColor(Color.GREEN);
            }
            if(dataModel.getMagColour().equals("orange")) {
                viewHolder.txtMag.setBackgroundColor(Color.YELLOW);
            }
            if(dataModel.getMagColour().equals("red")) {
                viewHolder.txtMag.setBackgroundColor(Color.RED);
                //viewHolder.txtMag.setTextColor(Color.WHITE);
            }
        }

        //Set colours according to the depth value
        if(dataModel.getDepthColour()!=null){
            if(dataModel.getDepthColour().equals("green")) {
                viewHolder.txtDepth.setBackgroundColor(Color.GREEN);
            }
            if(dataModel.getDepthColour().equals("orange")) {
                viewHolder.txtDepth.setBackgroundColor(Color.YELLOW);
            }
            if(dataModel.getDepthColour().equals("red")) {
                viewHolder.txtDepth.setBackgroundColor(Color.RED);
                //viewHolder.txtDepth.setTextColor(Color.WHITE);
            }
        }

        //Set values of appropriate ui components
        viewHolder.txtTitle.setText(dataModel.getLocation());
        viewHolder.txtDate.setText(" " + dataModel.getPubDate());
        viewHolder.txtMag.setText(" Magnitude: " + String.valueOf(dataModel.getMagnitude()));
        viewHolder.txtDepth.setText(" Depth: " + String.valueOf(dataModel.getDepth() + "km"));

        // Return the completed view to render on screen
        return convertView;
    }
}

