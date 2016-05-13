package besho.hallofmovies;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MyCommunicator {

    MovieFragment movieFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            movieFragment = new MovieFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            SharedPreferences prefs = getSharedPreferences("myBool", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();


            if (findViewById(R.id.main_activity_duplicated) == null) {

                transaction.add(R.id.main_activity, movieFragment, "movieFragment");
                edit.putBoolean("singleFrag", true);
                edit.commit();
                transaction.commit();
            } else {
                edit.putBoolean("singleFrag", false);
                edit.commit();
            }

        }
        //else movieFragment = (movieFragment)getSupportFragmentManager().findFragmentById(R.id.drawer_layout);

    }


    @Override
    public void respond(MyMovies movie)
    {
        MovieDetailFragment fragment ;
        fragment = (MovieDetailFragment) getFragmentManager().findFragmentById(R.id.fragment2);
        fragment.changeIT(movie);
    }

}
