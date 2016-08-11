package maximbravo.com.booklisting;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    public static final String LOG_TAG = BookActivity.class.getSimpleName();

    /** URL to query the USGS dataset for earthquake information */
    private static String BOOKS_REQUEST_URL;
    private ListView bookListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);
        bookListView = (ListView) findViewById(R.id.list);
        BookAsyncTask task = new BookAsyncTask();
        task.execute();

    }
    private EditText searchField;
    private EditText numberOfResultsField;
    public void search(View view){
        bookListView.setAdapter(null);
        Toast p = Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT);
        p.show();
        searchField = (EditText) findViewById(R.id.search_field);
        numberOfResultsField = (EditText) findViewById(R.id.number_of_results_field);
        String maxResults = numberOfResultsField.getText().toString();
        String searchable = searchField.getText().toString();
        if(searchable.length() != 0){
            SearchEvent searchEvent;
            if(maxResults.length() != 0) {
                searchEvent = new SearchEvent(searchable, maxResults);
            } else {
                searchEvent = new SearchEvent(searchable);
            }
            searchEvent.buildUrl();
            BOOKS_REQUEST_URL = searchEvent.getURL();
            BookAsyncTask task = new BookAsyncTask();
            task.execute();
            searchEvent.clearUrl();
        } else {
            Toast t = Toast.makeText(this, "No results found.", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    private ArrayList<Book> bookArrayList;
    private void updateUi(ArrayList<Book> books) {
        bookArrayList = books;
        BookAdapter bookAdapter = new BookAdapter(this, books);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = bookArrayList.get(position).getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        bookListView.setAdapter(bookAdapter);
    }


    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {
        private Toast t = Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_SHORT);
        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {
            URL url = createUrl(BOOKS_REQUEST_URL);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            ArrayList<Book> book = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return book;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> book) {
            if (book == null){
                return;
            }
            updateUi(book);
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if(url == null){
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if(urlConnection.getResponseCode() == 200){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("MakeHttpRequest", "Status code is " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                // TODO: Handle the exception
                Log.e("MakeHttpRequest", "IOException thrown");

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private ArrayList<Book> extractFeatureFromJson(String bookJSON) {
            ArrayList<Book> books = new ArrayList<>();
            try {

                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                // If there are results in the features array
                if (itemsArray.length() > 0) {
                    // Extract out the first feature (which is an earthquake)
                    for(int i = 0; i < itemsArray.length(); i++) {
                        JSONObject currentBook = itemsArray.getJSONObject(i);
                        JSONObject properties = currentBook.getJSONObject("volumeInfo");
                        String author;
                        if(properties.has("authors")) {
                            JSONArray authors = properties.getJSONArray("authors");
                            String appendable;
                            if(authors.length() >1){
                                appendable = "\n+" + (authors.length()-1) + " others";
                            } else {
                                appendable = "";
                            }
                            author = authors.getString(0) + appendable;
                        } else {
                            author = "Unknown.";
                        }
                        // Extract out the title, time, and tsunami values
                        String rating;
                        if( properties.has("averageRating")) {
                            rating = "" + properties.getDouble("averageRating");
                        } else {
                            rating = "N/A";
                        }
                        String preview = properties.getString("previewLink");
                        String title = properties.getString("title");
                        String description;
                        if(properties.has("description")) {
                            description = properties.getString("description");
                        } else {
                            description = "";
                        }


                        books.add(new Book(rating, title, description, author, preview));
                    }
                } else {
                    Toast t = Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_SHORT);
                    t.show();
                }
                return books;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);

                t.show();
            }
            return null;
        }
    }
}
