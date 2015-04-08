package datamodels;

import java.util.ArrayList;

/**
 * Created by Ahmed on 30-Jun-14.
 */
public class GetFeedResponse {

    int id;
    String name;
    ArrayList<FeedItem> posts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<FeedItem> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<FeedItem> posts) {
        this.posts = posts;
    }
}
