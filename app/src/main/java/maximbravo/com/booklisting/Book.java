package maximbravo.com.booklisting;

/**
 * Created by wendy on 8/5/2016.
 */
public class Book {
    public String getRating() {
        return rating;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    private String rating;
    private String title;
    private String description;
    private String author;

    public String getUrl() {
        return url;
    }

    private String url;
    public Book (String r, String t, String d, String a, String u){
        rating = r;
        title = t;
        description = d;
        author = "By: " + a;
        url = u;
    }
}
