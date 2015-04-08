package views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import datamodels.TagItem;
import news.turndigital.com.turndigitalnews.R;

public class TagsAdapter extends ArrayAdapter<TagItem> {
    private Context context;
    private int layoutResourceId;
    private List<TagItem> data = null;

    public TagsAdapter(Context context, int layoutResourceId, List<TagItem> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.textName = (CheckedTextView) row.findViewById(R.id.text_name);
            row.setTag(holder);

        } else {
            holder = (ViewHolder) row.getTag();
        }

        final TagItem tagItem = data.get(position);

        // set data
        holder.textName.setText(tagItem.getName());

        return row;
    }

    static class ViewHolder {
        CheckedTextView textName;
    }
}
