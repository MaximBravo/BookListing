package maximbravo.com.booklisting;

/**
 * Created by wendy on 8/7/2016.
 */
public class SearchEvent {
    private static String BOOKS_REQUEST_URL;
    private String base = "https://www.googleapis.com/books/v1/volumes";
    private String end;
    private String query;
    private String maxResults = "";
    public SearchEvent(String q){
        query = q.toLowerCase();
    }
    public SearchEvent(String q, String m){
        query = q.toLowerCase();
        maxResults = m;
    }
    public void buildUrl(){
        end = "?q=" + query;
        if(!maxResults.equals("")){
            end += "&maxResults=" + maxResults;
        }
        BOOKS_REQUEST_URL = base + end;
    }
    public String getURL(){
        return BOOKS_REQUEST_URL;
    }
    public void clearUrl(){
        BOOKS_REQUEST_URL = "";
        maxResults = "";
    }
}
