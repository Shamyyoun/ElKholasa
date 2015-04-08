package json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import datamodels.FeedItem;

public class NewFeedItemsHandler {
	private String response;

	public NewFeedItemsHandler(String response) {
		this.response = response;
	}

	public ArrayList<FeedItem> handle() {
        ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            JSONObject catJO = jsonArray.getJSONObject(0);
            JSONArray postsJA = catJO.getJSONArray("posts");

            for (int i = 0; i < postsJA.length(); i++) {
                JSONObject jsonObject1 = postsJA.getJSONObject(i);
                int id = jsonObject1.getInt("id");
                String title = jsonObject1.getString("title");
                String thumbnail = jsonObject1.getString("thumbnail");
                String url = jsonObject1.getString("url");
                String description = jsonObject1.getString("content");
                String date = jsonObject1.getString("date");
                String provider = jsonObject1.getString("provider");

                FeedItem feedItem = new FeedItem();
                feedItem.setId(id);
                feedItem.setTitle(title);
                feedItem.setThumbnail(thumbnail);
                feedItem.setUrl(url);
                feedItem.setContent(description);
                feedItem.setDate(date);
                feedItem.setProvider(provider);

                feedItems.add(feedItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            feedItems = null;
        }

        return  feedItems;
	}
}
