package news.turndigital.com.turndigitalnews;

import java.io.Serializable;
import java.util.ArrayList;

import datamodels.TagItem;

/**
 * Created by mahmoud on 10/29/2014.
 */
public class TagsHolder implements Serializable {
    public ArrayList<TagItem> tags;

    public TagsHolder() {
        tags = new ArrayList<TagItem>();
    }
}