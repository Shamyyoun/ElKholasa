package datamodels;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mahmoud on 10/29/2014.
 */
public class FeedItemsHolder implements Serializable {
    public ArrayList<FeedItem> feedItems;

    public FeedItemsHolder() {
        feedItems = new ArrayList<FeedItem>();
    }
}