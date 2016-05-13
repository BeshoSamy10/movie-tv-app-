package besho.hallofmovies;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
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
import java.util.List;


/**
 * Created by bisho on 20-Mar-16.
 */
    public class MovieFragment extends Fragment implements AdapterView.OnItemClickListener,NavigationDrawerResponder {


    GridView myGridView ;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<MyMovies> myMovies = new ArrayList<MyMovies>();

    MyCommunicator communicator;
    SearchView searchView;
    SharedPreferences isPhone;

    int myState=0;
    int myCase = 1;
    int itemSelectedPosition=0;
    boolean firstTime=true;
    boolean isMovie=true;
    boolean searchRefresh=false;
    public FetchMovies fetchMovies;
    String mySearchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null)
        {
            myState=savedInstanceState.getInt("myState", 0);
            myCase=savedInstanceState.getInt("myCase",1);
            firstTime=savedInstanceState.getBoolean("firstTime", true);
            isMovie=savedInstanceState.getBoolean("isMovie",true);
            mySearchQuery=savedInstanceState.getString("mySearchQuery","");
            itemSelectedPosition=savedInstanceState.getInt("itemSelectedPosition",0);
        }
        else myState=0;

        /*Log.d("myFilter",myState+"");
        Log.d("myFilter",firstTime +"");*/

        communicator = (MyCommunicator) getActivity();
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
    public void onStart() {
        super.onStart();

        if (isNetworkConnected())
        {

            if (!firstTime)
            {
                if (searchView != null && !searchView.getQuery().toString().equals(""))
                {
                    mySearchQuery = searchView.getQuery().toString();
                    updateMovies(6);
                }
                if (mySearchQuery == null || mySearchQuery.equals("")) updateMovies(myState);
            }

            if (firstTime)
            {
                firstTime = false;
                updateMovies(myState);
            }
        }
        else showFav();
        //else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
        drawerToggle.syncState();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        myGridView.setOnItemClickListener(this);
        drawerToggle.syncState();
    }

    public void updateMovies (int s)
    {
        if (s == 5)showFav();

        else
        {
            fetchMovies = new FetchMovies();
            fetchMovies.buildURL(s);
            fetchMovies.execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.movie_fragment, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("enter a name");

        if (mySearchQuery!=null && !mySearchQuery.equals(""))
        {
            searchView.setQuery(mySearchQuery,true);
            if(isNetworkConnected())updateMovies(6);
            else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!s.equals(""))
                {
                    mySearchQuery=s;
                    if(!searchRefresh)searchRefresh=true;
                    if(isNetworkConnected())updateMovies(6);
                    else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (searchRefresh) {
                        mySearchQuery=s;
                        if(isNetworkConnected())updateMovies(myState);
                        else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
                        searchRefresh=false;
                    }

                }
                //Log.d("myFilter","submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.equals(""))
                {
                    mySearchQuery=s;
                    if(!searchRefresh)searchRefresh=true;
                    if(isNetworkConnected())updateMovies(6);
                    else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (searchRefresh) {
                        mySearchQuery=s;
                        if(isNetworkConnected())updateMovies(myState);
                        else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
                        searchRefresh=false;
                    }
                }
                //Log.d("myFilter","ch");
                return false;
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            //Log.d("myFilter","do code");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("myState", myState);
        outState.putBoolean("firstTime", firstTime);
        outState.putInt("myCase",myCase);
        outState.putString("mySearchQuery",searchView.getQuery().toString());
        outState.putBoolean("isMovie",isMovie);
        outState.putInt("itemSelectedPosition",itemSelectedPosition);
    }



    @Override
    public void listenToNavigation(int position) {

        Log.d("myFilter","l");

        if (position == 4)
        {
            mySearchQuery="";
            searchView.setQuery("",true);
            myState=1;
            if(isNetworkConnected())
            {
                updateMovies(myState);
                drawerLayout.closeDrawers();
            }
            else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
        }

        if (position == 3)
        {
            mySearchQuery="";
            searchView.setQuery("",true);
            myState=2;
            if(isNetworkConnected())
            {
                updateMovies(myState);
                drawerLayout.closeDrawers();
            }
            else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
        }

        if (position == 1)
        {
            mySearchQuery="";
            searchView.setQuery("",true);
            searchView.setQueryHint("enter show name");
            myState=3;
            if(isNetworkConnected())
            {
                updateMovies(myState);
                drawerLayout.closeDrawers();
            }
            else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();
        }

        if (position == 0)
        {
            mySearchQuery="";
            searchView.setQuery("",true);
            searchView.setQueryHint("enter movie name");
            myState=4;
            if(isNetworkConnected())
            {
                updateMovies(myState);
                drawerLayout.closeDrawers();
            }
            else Toast.makeText(getActivity(),"NO INTERNET ACCESS AVAILABLE",Toast.LENGTH_SHORT).show();

        }

        if (position == 2)
        {
            mySearchQuery="";
            searchView.setQuery("",true);
            myState=5;
            showFav();
            drawerLayout.closeDrawers();
        }

    }

    public void showFav ()
    {

        if(getMovies().isEmpty())
            Toast.makeText(getActivity(),"sorry,you don`t have any favourite movies",Toast.LENGTH_SHORT).show();

        else
        {
            myMovies.clear();
            myMovies = getMovies();
            MoviesAdapter myAdapter = new MoviesAdapter(getActivity());
            myGridView.setAdapter(myAdapter);
            if (!isPhone.getBoolean("singleFrag",true)) communicator.respond(myMovies.get(0));
        }
    }

    public ArrayList<MyMovies> getMovies()
    {
        ArrayList<MyMovies> favMovies = new ArrayList<MyMovies>();
        Cursor cursor = getActivity().getContentResolver().query(MyContentProvider.CONTENT_URI, null, null, null, null);

        try {
            while (cursor.moveToNext())
            {
                String poster_path = cursor.getString(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.POSTER_PATH));
                int movie_id = cursor.getInt(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.MOVIE_ID));
                String overview = cursor.getString(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.OVERVIEW));
                String release_date = cursor.getString(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.RELEASE_DATE));
                String original_title = cursor.getString(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.ORIGINAL_TITLE));
                double vote_average = cursor.getDouble(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.VOTE_AVERAGE));
                int is_movie        = cursor.getInt(cursor.getColumnIndex(MyContentProvider.MyMoviesDataHelper.IS_MOVIE));
                MyMovies temp = new MyMovies(poster_path,movie_id,false,overview,release_date,original_title,0,false,vote_average,true,is_movie==1);
                favMovies.add(temp);
            }
        }finally {
            cursor.close();
        }

        return favMovies;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        isPhone = getActivity().getSharedPreferences("myBool", Context.MODE_PRIVATE);

        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        myGridView = (GridView) view.findViewById(R.id.gridView);
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);

        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(),loadData(),this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        drawerToggle = new ActionBarDrawerToggle(getActivity(),drawerLayout,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //drawerToggle.syncState();
        return view ;
    }

    public static List<Row> loadData ()
    {
        List<Row> list = new ArrayList<>();

        int[] imagesID = {R.drawable.movies,R.drawable.tv,R.drawable.favourite,R.drawable.top_rated,R.drawable.most_popular};
        String[] dataText = {"Movies","TV Shows","Favourites","Top Rated","Most Popular"};

        for (int i=0;i<imagesID.length && i<dataText.length;i++)
        {
            Row item = new Row();
            item.imageId=imagesID[i];
            item.text=dataText[i];

            list.add(item);

        }

        return list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        if (isPhone.getBoolean("singleFrag",true)) {
            Intent i = new Intent(getActivity(), MovieDetailActivity.class);
            i.putExtra("title", myMovies.get(position).original_title);  //5
            i.putExtra("poster", myMovies.get(position).poster_path);    //1
            i.putExtra("id", myMovies.get(position).item_id);            //2
            //Log.d("myFilter",""+myMovies.get(position).item_id);
            i.putExtra("date", myMovies.get(position).release_date);     //4
            i.putExtra("rate", myMovies.get(position).vote_average);     //6
            i.putExtra("overview", myMovies.get(position).overview);    //3
            i.putExtra("tvORmov", myMovies.get(position).isMovie);

            startActivity(i);
        }
        else
        {
            itemSelectedPosition = position;
            communicator.respond(myMovies.get(position));
        }

    }


    class FetchMovies extends AsyncTask<Void,Void,ArrayList<MyMovies>> {


        final String baseUrl = "https://api.themoviedb.org/3/";
        final String movie = "movie/";
        final String tv    =    "tv/";
        final String apiKey  = "api_key=f06b4506f14971248b2838078370f394";
        final String popular= "popular?";
        final String top_Rated = "top_rated?";
        final String searchTV ="http://api.themoviedb.org/3/search/tv?api_key=f06b4506f14971248b2838078370f394&query=";
        final String searchMovie ="http://api.themoviedb.org/3/search/movie?api_key=f06b4506f14971248b2838078370f394&query=";
        String urlAdress = baseUrl + movie +popular+ apiKey;

        String myJSON = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        InputStream inputStream = null;

        FetchMovies ()
        {

        }

        public void buildURL (int s)
        {

            if (s == 1)
            {
                if (isMovie)
                {
                    urlAdress = baseUrl + movie + popular + "&" + apiKey;
                    myCase=1;
                }
                else
                {
                    urlAdress = baseUrl + tv + popular + "&" + apiKey;
                    myCase = 2;
                }
                //Log.d("myFilter",urlAdress+"    s=1");
            }
            if (s == 2)
            {
                if (isMovie)
                {
                    urlAdress = baseUrl + movie + top_Rated + "&" + apiKey;
                    myCase=1;
                }
                else
                {
                    urlAdress = baseUrl + tv + top_Rated + "&" + apiKey;
                    myCase = 2;
                }
                //Log.d("myFilter",urlAdress+"    s=1");
            }
            if (s == 3)
            {
                urlAdress = baseUrl + tv + popular + "&" + apiKey;
                isMovie=false;
                myCase = 2;

                //Log.d("myFilter",urlAdress+"    s=3");
            }

            if (s == 4)
            {
                urlAdress = baseUrl + movie + popular + "&" + apiKey;
                isMovie=true;
                myCase=1;

               // Log.d("myFilter",urlAdress+"    s=4");
            }

            if (s == 6)
            {
                if (isMovie)//movie
                {
                    urlAdress = searchMovie + mySearchQuery;
                    myCase=1;
                }
                else//tv
                {
                    urlAdress = searchTV + mySearchQuery;
                    myCase=2;
                }
            }


        }

        @Override
        protected ArrayList<MyMovies> doInBackground(Void... params) {



            try {
                URL url = new URL(urlAdress);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                int code = urlConnection.getResponseCode();


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
            try {
                return parseMyJson(myJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d("myFilter",myJSON);

            return null;
        }

        public ArrayList<MyMovies> parseMyJson (String json) throws JSONException
        {
            myMovies.clear();
            final String poster_path="poster_path";
            final String adult="adult";
            final String overview="overview";
            final String release_date="release_date";
            final String original_title="title";
            final String air_date="first_air_date";
            final String original_name="name";
            final String vote_count = "vote_count";
            final String video="video";
            final String vote_average="vote_average";
            final String item_id ="id";

            JSONObject allMovies = new JSONObject(json);
            JSONArray results = allMovies.getJSONArray("results");

            for (int i=0;i<results.length();i++)
            {

                JSONObject film = results.getJSONObject(i);
                String poster = film.getString(poster_path);

                if (!poster.equals("null")) {
                    boolean ad = false;
                    boolean vid = false;
                    String release = "";
                    String title = "";

                    String review = film.getString(overview);
                    int votes = film.getInt(vote_count);
                    int id = film.getInt(item_id);
                    double voteAverage = film.getDouble(vote_average);

                    if (myCase == 1) {
                        ad = film.getBoolean(adult);
                        release = film.getString(release_date);
                        title = film.getString(original_title);
                        vid = film.getBoolean(video);
                    }
                    if (myCase == 2) {
                        release = film.getString(air_date);
                        title = film.getString(original_name);
                    }

                    MyMovies temp = new MyMovies(poster, id, ad, review, release, title, votes, vid, voteAverage, false,isMovie);
                    //Log.d("myFilter",""+temp.adult);
                    myMovies.add(temp);
                }
            }
            return myMovies;
        }

        @Override
        protected void onPostExecute(ArrayList<MyMovies> s) {
            super.onPostExecute(s);
            MoviesAdapter myAdapter = new MoviesAdapter(getActivity());
            myGridView.setAdapter(myAdapter);

            if (!isPhone.getBoolean("singleFrag",true))
            {
                communicator.respond(s.get(0));
            }

            //Log.d("myFilter",myMovies.get(0).poster_path);

        }


    }

    class MoviesAdapter extends BaseAdapter
    {
        ArrayList<MyMovies> movies;
        Context context;

        MoviesAdapter (Context c)
        {

            this.context=c;
            movies = myMovies;

        }

        @Override
        public int getCount() {
            return movies.size();
        }

        @Override
        public Object getItem(int position) {
            return movies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ImageViewHolder
        {
            ImageView myImage;
            ImageViewHolder(View v)
            {
                myImage = (ImageView) v.findViewById(R.id.imageView);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item=convertView;
            ImageViewHolder myHolder;
            if (item == null)
            {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                item = inflater.inflate(R.layout.item_layout,parent,false);
                myHolder = new ImageViewHolder(item);
                item.setTag(myHolder);
            }

            else
            {
                myHolder = (ImageViewHolder) item.getTag();
            }

            MyMovies temp = movies.get(position);

            Picasso.with(context).load("http://image.tmdb.org/t/p/w185" + temp.poster_path).into(myHolder.myImage);


            return item;
        }
    }


}





