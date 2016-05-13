package besho.hallofmovies;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;


/**
 * Created by bisho on 24-Mar-16.
 */
public class MovieDetailFragment extends Fragment {

    String overVeiw;
    String poster;
    String date;
    String title;
    String trUrl;
    double rate;
    int id;
    boolean tvORmov;
    boolean exist;
    MyContentProvider provider;
    ListView trailersList;
    TextView movieTitle;
    ImageView moviePoster;
    TextView movieDate;
    TextView movieRate;
    TextView movie_overview;
    TextView movie_review;
    ImageView movie_review_image;
    Button mkFavButton2;
    Intent shareIntent;
    SharedPreferences isPhone;

    ArrayList<String> allTrailers;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent intent=getActivity().getIntent();
        id = intent.getIntExtra("id",0);
        tvORmov = intent.getBooleanExtra("tvORmov",true);

        provider = new MyContentProvider();
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        allTrailers = new ArrayList<>();
        createShareIntent();

    }

    public Intent createShareIntent ()
    {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        //String[] toShare = {title,trUrl};
        //Log.d("myFilter",toShare[0]+"       "+toShare[1]);
        if (trUrl != null)shareIntent.putExtra(Intent.EXTRA_TEXT,title+"\n\n"+trUrl);
        else shareIntent.putExtra(Intent.EXTRA_TEXT,title);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(isPhone.getBoolean("singleFrag",true))loadTrAndRe();

        inflater.inflate(R.menu.movie_detail_fragment,menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        //Log.d("muFilter",""+provider);
        if (provider != null) provider.setShareIntent(createShareIntent());


    }

    public boolean isNetworkConnected ()
    {
        ConnectivityManager networkManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkState = networkManager.getActiveNetworkInfo();

        boolean connected = (networkState != null && networkState.isConnected());

        return connected;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        isPhone = getActivity().getSharedPreferences("myBool", Context.MODE_PRIVATE);
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent intent=getActivity().getIntent();

        movieTitle = (TextView) view.findViewById(R.id.textView_title);
        moviePoster = (ImageView) view.findViewById(R.id.imageView_poster);
        movieDate = (TextView) view.findViewById(R.id.textView_date);
        movieRate = (TextView) view.findViewById(R.id.textView_rate);
        movie_overview = (TextView) view.findViewById(R.id.overView);
        mkFavButton2 = (Button) view.findViewById(R.id.makeAsFavourite_button);
        movie_review = (TextView) view.findViewById(R.id.review_text);
        movie_review.setMovementMethod(new ScrollingMovementMethod());
        movie_review_image = (ImageView) view.findViewById(R.id.trailer_pic);

        trailersList = (ListView) view.findViewById(R.id.trailers_list);
        trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(allTrailers.get(i)));
                startActivity(intent);
            }
        });

        if (isPhone.getBoolean("singleFrag",true)) {
            title = intent.getStringExtra("title");
            movieTitle.setText(title);

            poster = intent.getStringExtra("poster");
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w300" + poster).into(moviePoster);

            date = intent.getStringExtra("date");
            movieDate.setText(date);

            rate = intent.getDoubleExtra("rate", 0.0);
            movieRate.setText(rate + "/10");

            overVeiw = intent.getStringExtra("overview");
            movie_overview.setText(overVeiw);


            id = intent.getIntExtra("id", 0);
            tvORmov = intent.getBooleanExtra("tvORmov", true);
        }


        exist = isExist();
        final Button mkFavButton = (Button) view.findViewById(R.id.makeAsFavourite_button);

        if (exist)
            mkFavButton.setText("UNFAVOURITE IT");

        else
            mkFavButton.setText("MAKE AS FAVOURITE");

        mkFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                boolean exist2= isExist();
                String message="";
                if (exist2)
                {
                    int n=removeMovie();
                    if (n==1)
                    {
                        mkFavButton.setText("MAKE AS FAVOURITE");
                        message=title+" was successfully removed";
                    }
                    else message="an error occurred";
                }
                else
                {
                    addMovie();
                    mkFavButton.setText("UNFAVOURITE IT");
                    message=title+" was successfully added";
                }
                Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
            }
        });


        return view ;
    }

    public void addMovie ()
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContentProvider.MyMoviesDataHelper.POSTER_PATH,poster);
        contentValues.put(MyContentProvider.MyMoviesDataHelper.MOVIE_ID,id);
        contentValues.put(MyContentProvider.MyMoviesDataHelper.OVERVIEW,overVeiw);
        contentValues.put(MyContentProvider.MyMoviesDataHelper.RELEASE_DATE,date);
        contentValues.put(MyContentProvider.MyMoviesDataHelper.ORIGINAL_TITLE,title);
        contentValues.put(MyContentProvider.MyMoviesDataHelper.VOTE_AVERAGE, rate);
        if (tvORmov)contentValues.put(MyContentProvider.MyMoviesDataHelper.IS_MOVIE, 1);
        else contentValues.put(MyContentProvider.MyMoviesDataHelper.IS_MOVIE, 0);

        getActivity().getContentResolver().insert(MyContentProvider.CONTENT_URI, contentValues);

    }

    public int removeMovie ()
    {
        String[] whereArgs={""+id};
        int no=getActivity().getContentResolver().delete(MyContentProvider.CONTENT_URI,MyContentProvider.MyMoviesDataHelper.MOVIE_ID+"=?",whereArgs);
        return no;
    }

    public boolean isExist ()
    {

        String[] columns = {MyContentProvider.MyMoviesDataHelper.MOVIE_ID};
        String[] args    = {""+id};
        Cursor cursor = getActivity().getContentResolver().query(MyContentProvider.CONTENT_URI, columns,MyContentProvider.MyMoviesDataHelper.MOVIE_ID+" = ?",args, null);
        int _id=0;

        try {
            while (cursor.moveToNext())
            {
                _id = cursor.getInt(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.MOVIE_ID));
            }
        }finally {
            cursor.close();
        }

        return (_id==id);
    }

    public void changeIT (MyMovies movie)
    {
        title = movie.original_title;
        movieTitle.setText(title);


        poster = movie.poster_path;
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w300" + poster).into(moviePoster);

        date = movie.release_date;
        movieDate.setText(date);

        rate = movie.vote_average;
        movieRate.setText(rate+"/10");

        overVeiw = movie.overview;
        movie_overview.setText(overVeiw);

        id = movie.item_id;
        tvORmov = movie.isMovie;

        exist = isExist();

        if (exist)
            mkFavButton2.setText("UNFAVOURITE IT");

        else
            mkFavButton2.setText("MAKE AS FAVOURITE");

        loadTrAndRe();

    }

    public void loadTrAndRe ()
    {
        if (isNetworkConnected())
        {

            if (id != 0) {
                FetchTR tr = new FetchTR(id, tvORmov);
                tr.execute();
            }


            if (tvORmov)
            {

                if (id != 0)
                {
                    FetchRE re = new FetchRE(id, tvORmov);
                    re.execute();
                }

            }

            else {
                movie_review.setText("");
                //movie_review_image.setBackground(null);
                Toast.makeText(getActivity(), "sorry,no reviews to show", Toast.LENGTH_SHORT).show();
            }
        }

        else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();

    }

    class FetchTR extends AsyncTask<Void,Void,ArrayList<String>>
    {
        int movieID;
        final String baseUrl ="https://api.themoviedb.org/3/";
        final String movie="movie/";
        final String tv="tv/";
        final String sndUrl="/videos?api_key=f06b4506f14971248b2838078370f394";

        String urlAdress ;
        String myJSON = null;
        boolean ex;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        InputStream inputStream = null;

        public FetchTR(int id,boolean tvORmov)
        {
            this.movieID=id;

            if(tvORmov)urlAdress= baseUrl+movie+movieID+sndUrl;
            else urlAdress= baseUrl+tv+movieID+sndUrl;
            //Log.d("myFilter",movieID+"");
            //Log.d("myFilter",urlAdress);

        }

        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {

            try {
                URL url = new URL(urlAdress);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                int code = urlConnection.getResponseCode();
                //Log.d("myFilter",""+code);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line;
            try {
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            myJSON = buffer.toString();
            //Log.d("myFilter",myJSON);
            try {
                return parseMyJson(myJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("myFilter",myJSON);
            return null;
        }

        private ArrayList<String> parseMyJson(String myJSON) throws JSONException {

            JSONObject jsonStr = new JSONObject(myJSON);
            JSONArray results = jsonStr.getJSONArray("results");

            ArrayList<String> numOfTrailers = new ArrayList<>();
            allTrailers.clear();
            for (int i=0;i<results.length();i++)
            {
                JSONObject filmT = results.getJSONObject(i);
                allTrailers.add("https://www.youtube.com/watch?v="+filmT.getString("key"));
                numOfTrailers.add("Trailer "+(i+1)+". ");
            }
            if (allTrailers.size()!=0)trUrl=allTrailers.get(0);
            //Log.d("myFilter",key);
            //getMovieTR=key;

            return numOfTrailers;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {

            if (s.size() != 0)
            {
                String[] notrailer = new String[s.size()];
                s.toArray(notrailer);
                ArrayAdapter<String> trailerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.trailer_listview_row,R.id.trailer_no,notrailer);
                trailersList.setAdapter(trailerAdapter);
                trUrl=allTrailers.get(0);
                shareIntent.putExtra(Intent.EXTRA_TEXT,title+"\n\n"+trUrl);
            }

            else Toast.makeText(getActivity(),"sorry,no trailers to show",Toast.LENGTH_LONG).show();

            super.onPostExecute(s);
        }
    }

    class FetchRE extends AsyncTask<Void,Void,ArrayList<String>>
    {
        int movieID;
        final String baseUrl ="https://api.themoviedb.org/3/";
        final String movie="movie/";
        final String tv="tv/";
        final String sndUrl="/reviews?api_key=f06b4506f14971248b2838078370f394";
        String urlAdress ;
        ArrayList<String> allReviews = new ArrayList<>();
        String myJSON = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        InputStream inputStream = null;

        public FetchRE(int id,boolean tvORmov)
        {
            this.movieID=id;
            if (tvORmov)urlAdress= baseUrl+movie+movieID+sndUrl;
            else urlAdress= baseUrl+tv+movieID+sndUrl;
            //Log.d("myFilter",""+id);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {

            try {
                URL url = new URL(urlAdress);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                int code = urlConnection.getResponseCode();
                //Log.d("myFilter",""+code);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line;
            try {
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            myJSON = buffer.toString();
            //Log.d("myFilter",myJSON);
            try {
                return parseMyJson(myJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("myFilter",myJSON);
            return null;
        }

        private ArrayList<String> parseMyJson(String myJSON) throws JSONException {

            JSONObject jsonStr = new JSONObject(myJSON);
            JSONArray results = jsonStr.getJSONArray("results");
            allReviews.clear();
            for (int i=0;i<results.length();i++)
            {
                JSONObject filmT = results.getJSONObject(i);
                allReviews.add("Review "+(i+1)+" :\n"+filmT.getString("content")+"\n");
            }


            //getMovieRE=urlRE;
            return allReviews;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            //Log.d("myFilter",s+"");
            if (s.size()==0)
            {
                movie_review.setText("");
                Toast.makeText(getActivity(),"sorry,no reviews to show",Toast.LENGTH_SHORT).show();
            }
            else movie_review.setText(s.toString());
            super.onPostExecute(s);
        }
    }


}
