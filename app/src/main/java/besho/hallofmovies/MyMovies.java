package besho.hallofmovies;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bisho on 23-Mar-16.
 */
public class MyMovies implements Parcelable{

    String poster_path;
    int item_id;
    boolean adult;
    String overview;
    String release_date;
    String original_title;
    int vote_count;
    boolean video;
    double vote_average;
    boolean fav;
    boolean isMovie;

    public MyMovies
            (String poster_path, int item_id, boolean adult, String overview, String release_date
            , String original_title, int vote_count, boolean video, double vote_average, boolean fav , boolean isMovie)
    {
        this.poster_path = poster_path;
        this.adult = adult;
        this.overview = overview;
        this.release_date = release_date;
        this.original_title = original_title;
        this.vote_count = vote_count;
        this.video = video;
        this.vote_average = vote_average;
        this.item_id = item_id;
        this.fav = fav;
        this.isMovie=isMovie;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
