package mobile.labs.acw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.SearchRecentSuggestions;

public class DatabaseContract
{
    private void DatabaseContract(){

    }
    public static abstract class HighscoreEntry implements BaseColumns{
        public static final String TABLE_NAME = "Highscores";
        public static final String COLUMN_NAME_PUZZLE = "Puzzle";
        public static final String COLUMN_NAME_SCORE = "Score";
    }

    public static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_HIGHSCORES_TABLE = "CREATE TABLE " + HighscoreEntry.TABLE_NAME + " (" + HighscoreEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP + HighscoreEntry.COLUMN_NAME_PUZZLE + COMMA_SEP + HighscoreEntry.COLUMN_NAME_SCORE + " )";
    public static final String SQL_DELETE_HIGHSCORES_TABLE = "DROP TABLE IF EXISTS " + HighscoreEntry.TABLE_NAME;

    public static class PuzzleDBHelper extends SQLiteOpenHelper{
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Puzzles.db";
        public PuzzleDBHelper(Context pContext){
            super(pContext, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase pDb){
            pDb.execSQL(DatabaseContract.SQL_CREATE_HIGHSCORES_TABLE);
        }

        public void onUpgrade(SQLiteDatabase pDb, int i, int v){};
    }
}
