package besho.hallofmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by bisho on 27-Mar-16.
 */
public class MyContentProvider extends ContentProvider
{

    MyMoviesDataHelper dataHelper;
    SQLiteDatabase db;
    static final String CONTENT_NAME = "besho.hallofmovies.provider.Movie";
    static final String CONTENT_URL  = "content://"+CONTENT_NAME+"/movies";
    static final Uri    CONTENT_URI  = Uri.parse(CONTENT_URL);

    public MyContentProvider() {
    }



    @Override
    public boolean onCreate() {

        Context context = getContext();
        dataHelper = new MyMoviesDataHelper(context);
        db = dataHelper.getWritableDatabase();
        return (db == null)?false:true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortorder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(dataHelper.TABLE_NAME);

        Cursor cursor = qb.query(db,projection,selection,selectionArgs,null,null,null);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "Movies Database";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        long rowID = db.insert(dataHelper.TABLE_NAME,null,contentValues);
        if (rowID > 0)
        {
            Uri uri1 = ContentUris.withAppendedId(CONTENT_URI,rowID);
            getContext().getContentResolver().notifyChange(uri1,null);
            return uri1;

        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] whereArgs) {
        int rowsAffected = db.delete(dataHelper.TABLE_NAME,selection,whereArgs);
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs)
    {
        int rowsAffected = db.update(dataHelper.TABLE_NAME,contentValues,selection,selectionArgs);
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsAffected;
    }



    static class MyMoviesDataHelper extends SQLiteOpenHelper
    {
        static final String DATABASE_NAME = "moviesData";
        static final String TABLE_NAME    = "favouriteMovies";
        static final int DATABASE_VERSION = 9;
        static final String MOVIE_NO      = "_No";
        static final String POSTER_PATH   = "poster_path";
        static final String MOVIE_ID      = "movie_id";
        static final String OVERVIEW      = "overview";
        static final String RELEASE_DATE  = "release_date";
        static final String ORIGINAL_TITLE= "original_title";
        static final String VOTE_AVERAGE  = "vote_average";
        static final String IS_MOVIE      = "is_movie";
        static final String DROP_TABLE    = "DROP TABLE IF EXISTS "+TABLE_NAME;
        static final String CREATE_TABLE  = "CREATE TABLE "+TABLE_NAME+" ("+MOVIE_NO+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                                                    POSTER_PATH+" VARCHAR(255), "+MOVIE_ID+" INTEGER, "+OVERVIEW+" TEXT, "+
                                                    RELEASE_DATE+" VARCHAR(255), "+ORIGINAL_TITLE+" VARCHAR(255), "+VOTE_AVERAGE+" INTEGER, "+IS_MOVIE+" REAL"
                                                    +");";
        private Context context;

        public MyMoviesDataHelper(Context context)
        {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
            this.context=context;
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(CREATE_TABLE);

                //Toast.makeText(context, "created successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLException e)
            {
                Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
                Toast.makeText(context,"database upgraded",Toast.LENGTH_SHORT).show();
            }catch(SQLException e){
                Toast.makeText(context,"error on upgrading",Toast.LENGTH_SHORT).show();
            }

        }

    }

}
