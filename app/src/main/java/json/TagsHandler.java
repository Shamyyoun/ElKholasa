package json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import datamodels.TagItem;

public class TagsHandler {
	private String response;

	public TagsHandler(String response) {
		this.response = response;
	}

	public ArrayList<TagItem> handle() {
        ArrayList<TagItem> tags = new ArrayList<TagItem>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String id = jsonObject1.getString("id");
                String name = jsonObject1.getString("name");

                TagItem tagItem = new TagItem();
                tagItem.setId(id);
                tagItem.setName(name);
                tags.add(tagItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tags = null;
        }

        return  tags;
	}
}
