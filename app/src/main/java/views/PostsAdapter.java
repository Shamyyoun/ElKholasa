package views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datamodels.FeedItem;
import news.turndigital.com.turndigitalnews.R;

public class PostsAdapter extends ArrayAdapter<FeedItem> {
    private Context context;
    private int layoutResourceId;
    private List<FeedItem> data = null;

    private ArrayList<Bitmap> thumbnails;
    private int startIndex = 0;

    public PostsAdapter(Context context, int layoutResourceId, List<FeedItem> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;

        thumbnails = new ArrayList<Bitmap>();

        // load all thumbnails to array
        ThumbnailTask task = new ThumbnailTask(startIndex);
        task.execute();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.imageView = (ImageView) row.findViewById(R.id.imageView);
            holder.textTitle = (TextView) row.findViewById(R.id.txt_title);

            row.setTag(holder);

        } else {
            holder = (ViewHolder) row.getTag();
        }

        FeedItem feedItem = data.get(position);

        // set data
        holder.textTitle.setText(feedItem.getTitle());

        // check to load image
        Bitmap bitmap;
        try {
            bitmap = thumbnails.get(position);
        } catch (Exception e) {
            bitmap = null;
        }

        if (bitmap == null) {
            // load it from server
            if (!feedItem.getThumbnail().isEmpty()) {
                int width = (int) context.getResources().getDimension(R.dimen.list_posts_image_width);
                int height = (int) context.getResources().getDimension(R.dimen.list_posts_image_height);
                Picasso.with(context).load(feedItem.getThumbnail()).error(R.drawable.not_found_posts_list).resize(width, height).into(holder.imageView);
            } else {
                Picasso.with(context).load(R.drawable.not_found_posts_list).into(holder.imageView);
            }
        } else {
            // load it from runtime
            holder.imageView.setImageBitmap(bitmap);
        }


        return row;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        // load new thumbnails to array
        startIndex = (getCount() - 1) - startIndex;
        System.out.println("INDEX::: " + startIndex);
        ThumbnailTask task = new ThumbnailTask(startIndex);
        task.execute();
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textTitle;
    }

    private class ThumbnailTask extends AsyncTask<Void, Void, Void> {
        private int startIndex;

        public ThumbnailTask(int startIndex) {
            this.startIndex = startIndex;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = startIndex; i < data.size(); i++) {
                FeedItem feedItem = data.get(i);
                Bitmap bitmap;

                // load it from server
                if (!feedItem.getThumbnail().isEmpty()) {
                    try {
                        int width = (int) context.getResources().getDimension(R.dimen.list_posts_image_width);
                        int height = (int) context.getResources().getDimension(R.dimen.list_posts_image_height);
                        bitmap = Picasso.with(context).load(feedItem.getThumbnail()).resize(width, height).get();
                    } catch (IOException e) {
                        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.not_found_posts_list, null);
                        e.printStackTrace();
                    }

                } else {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.not_found_posts_list, null);
                }

                thumbnails.add(bitmap);
            }

            return null;
        }
    }
}
