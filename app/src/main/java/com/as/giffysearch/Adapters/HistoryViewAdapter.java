package com.as.giffysearch.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.as.giffysearch.R;

import java.util.List;

/**
 *
 * Created by Andrejs Skorinko on 12/9/2017.
 */

public class HistoryViewAdapter extends BaseAdapter
{
    private Context context_;
    private LayoutInflater layoutInflater_;
    private List<String> historyViewItems_;

    public HistoryViewAdapter(Context context, List<String> historyViewItems)
    {
        context_ = context;
        historyViewItems_ = historyViewItems;
        layoutInflater_ = (LayoutInflater)context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return historyViewItems_.size();
    }

    @Override
    public Object getItem(int position)
    {
        return historyViewItems_.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String historyItemValue = getItem(position).toString();

        HistoryViewHolder historyViewHolder;
        if(convertView == null)
        {
            // Get view for history item's (row) view
            convertView = layoutInflater_.inflate(R.layout.list_item_search_history_layout, parent, false);

            historyViewHolder = new HistoryViewHolder();
            historyViewHolder.setHistoryTextView(
                    (TextView)convertView.findViewById(R.id.list_item_search_history_text_view));

            convertView.setTag(historyViewHolder);
        }
        else
        {
            historyViewHolder = (HistoryViewHolder)convertView.getTag();
        }

        TextView historyItemTextView = historyViewHolder.getHistoryTextView();
        historyItemTextView.setText(historyItemValue);

        return convertView;
    }

    private static class HistoryViewHolder
    {
        private TextView historyTextView_;

        private void setHistoryTextView(TextView historyTextView)
        {
            historyTextView_ = historyTextView;
        }

        private TextView getHistoryTextView()
        {
            return historyTextView_;
        }
    }
}
