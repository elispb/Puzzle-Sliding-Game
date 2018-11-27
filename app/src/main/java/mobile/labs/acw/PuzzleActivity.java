package mobile.labs.acw;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class PuzzleActivity extends AppCompatActivity {
    Puzzle puzzle;
    boolean onResumeDownload = false;
    DatabaseContract.PuzzleDBHelper mDbHelper = new DatabaseContract.PuzzleDBHelper(this);

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int LONG_SWIPE_MIN_DISTANCE = 300;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        puzzle = new Puzzle();

        if (savedInstanceState == null) {
            puzzle.SetPuzzle(getIntent().getStringExtra("PuzzleSelected"));
            downloadPuzzle downloadPuzzle = new downloadPuzzle();
            downloadPuzzle.execute("http://www.simongrey.net/08027/slidingPuzzleAcw/puzzles/" + puzzle.GetPuzzle());
            UpdateUI();
        } else {
            puzzle = (Puzzle) getLastCustomNonConfigurationInstance();
            GridLayout gridlayout = (GridLayout)findViewById(R.id.PuzzleGridLayout);
            final View view = (View)gridlayout.getParent();
            if(view.getHeight() == 0){
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.getHeight();
                        LayoutPuzzle();
                        SetOnClicks();
                        UpdateUI();
                    }
                });
            }
            else
            {
                LayoutPuzzle();
                SetOnClicks();
                UpdateUI();
            }
        }
        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e2.getY() - e1.getY() > LONG_SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeDown), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    View v = g.getChildAt(puzzle.GetEmptyPosition()[0]-1);
                    v.callOnClick();

                } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeDown), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    int pos;
                    if (puzzle.GetEmptyPosition()[1] > 2)
                    {pos = puzzle.GetEmptyPosition()[0]-1+((puzzle.GetEmptyPosition()[1]-1)*puzzle.GetWidth())-puzzle.GetWidth();}
                    else
                    {pos = puzzle.GetEmptyPosition()[0]-1;}
                    View v = g.getChildAt(pos);
                    v.callOnClick();
                } else if (e1.getY() - e2.getY() > LONG_SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeUp), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    int pos = (puzzle.GetTotalSize() - (puzzle.GetWidth() - puzzle.GetEmptyPosition()[0])-1);
                    View v = g.getChildAt(pos);
                    v.callOnClick();
                }else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeUp), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    int pos;
                    if ((puzzle.GetTotalSize()/puzzle.GetWidth()) - puzzle.GetEmptyPosition()[1] < 2)/*Gap is less than 2*/
                    {pos = (puzzle.GetTotalSize() - (puzzle.GetWidth() - puzzle.GetEmptyPosition()[0])-1);}
                    else
                    {pos = (puzzle.GetTotalSize() - ((puzzle.GetWidth()*((puzzle.GetTotalSize()/puzzle.GetWidth())-1)) - puzzle.GetEmptyPosition()[0])-1 + ((puzzle.GetEmptyPosition()[1]-1)*puzzle.GetWidth()));}
                    View v = g.getChildAt(pos);
                    v.callOnClick();
                } else if(e1.getX() - e2.getX() > LONG_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {// right to left swipe
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeLeft), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    View v = g.getChildAt((puzzle.GetWidth()*puzzle.GetEmptyPosition()[1])-1);
                    v.callOnClick();
                } else if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {// right to left swipe
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeLeft), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    int pos = (puzzle.GetWidth()*puzzle.GetEmptyPosition()[1])-(puzzle.GetWidth() - puzzle.GetEmptyPosition()[0]);
                    View v = g.getChildAt(pos);
                    v.callOnClick();
                } else if (e2.getX() - e1.getX() > LONG_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeRight), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    View v = g.getChildAt((puzzle.GetWidth()*puzzle.GetEmptyPosition()[1])-puzzle.GetWidth());
                    v.callOnClick();
                }else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(PuzzleActivity.this, getString(R.string.SwipeRight), Toast.LENGTH_SHORT).show();
                    GridLayout g = (GridLayout)findViewById(R.id.PuzzleGridLayout);
                    View v = g.getChildAt((puzzle.GetWidth()*puzzle.GetEmptyPosition()[1])-puzzle.GetWidth()+(puzzle.GetEmptyPosition()[0]-2));
                    v.callOnClick();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override //Toolbar menu item
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.reset_button:
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.remove(puzzle.GetPuzzle()+"ProgressLayout");
                editor.remove(puzzle.GetPuzzle()+"TotalMoves");
                editor.commit();
                puzzle = new Puzzle();
                puzzle.SetPuzzle(getIntent().getStringExtra("PuzzleSelected"));
                downloadPuzzle downloadPuzzle = new downloadPuzzle();
                downloadPuzzle.execute("http://www.simongrey.net/08027/slidingPuzzleAcw/puzzles/" + puzzle.GetPuzzle());
                UpdateUI();
        }
        return true;
    }

    @Override
    public Puzzle onRetainCustomNonConfigurationInstance() {
        return puzzle;
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        StringBuilder currentLayout = new StringBuilder();
        currentLayout.append("{\"layout\":[[");
        for (int i = 0, c = 0, r = 0; i < puzzle.GetTotalSize(); ++i, ++c) {
            if (c == puzzle.GetWidth()) {
                c = 0;
                r++;
                currentLayout.deleteCharAt(currentLayout.length()-1);
                currentLayout.append("],[");
            }
            currentLayout.append("\"").append(puzzle.GetPictureSetArray()[r][c]).append("\",");
        }
        currentLayout.deleteCharAt(currentLayout.length()-1);
        currentLayout.append("]]}");
        editor.putString(puzzle.GetPuzzle()+"ProgressLayout",currentLayout.toString());
        editor.putInt(puzzle.GetPuzzle()+"TotalMoves",puzzle.GetMovesMade());
        editor.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        String layout = preferences.getString(puzzle.GetPuzzle()+"ProgressLayout", null);
        if(layout != null) {
            String[][] thing = formatAndSaveJsonLayout(layout);
            puzzle.SetPictureSetArray(thing);
        }
        int moves = preferences.getInt(puzzle.GetPuzzle()+"TotalMoves", 0);
        if (moves != 0) {
            puzzle.SetMovesMade(moves);
        }
    }

    protected void LayoutPuzzle() {
        Context context = PuzzleActivity.this;
        GridLayout gridlayout = (GridLayout) findViewById(R.id.PuzzleGridLayout);
        gridlayout.removeAllViews();
        View view = (View) gridlayout.getParent();
        int height = (view.getHeight() / (puzzle.GetTotalSize()/puzzle.GetWidth()));
        int width = (view.getWidth() / puzzle.GetWidth());
        if (height>width)
        {
            height = width;
        }
        else
        {
            width = height;
        }
        for (int i = 0, c = 0, r = 0; i < puzzle.GetTotalSize(); ++i, ++c) {
            if (c == puzzle.GetWidth()) {
                c = 0;
                r++;
            }
            String fileName = puzzle.GetPictureSetArray()[r][c];
            ImageView imageView = new ImageView(context);
            imageView.setContentDescription(fileName + " at: " + (r + 1) + (c + 1));

            if (!fileName.equals("empty")) {
                imageView.setImageBitmap(puzzle.GetBitmaps().get(fileName));
            } else {
                puzzle.GetEmptyPosition()[0] = (c + 1);
                puzzle.GetEmptyPosition()[1] = (r + 1);
            }
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.columnSpec = GridLayout.spec(c);
            layoutParams.rowSpec = GridLayout.spec(r);
            layoutParams.width = width;
            layoutParams.height = height;
            imageView.setLayoutParams(layoutParams);
            gridlayout.addView(imageView);
        }
    }

    protected void SetOnClicks() {
        RelativeLayout puzzleArea = (RelativeLayout) findViewById(R.id.PuzzleArea);
        puzzleArea.setOnTouchListener(gestureListener);
        GridLayout grid = (GridLayout) findViewById(R.id.PuzzleGridLayout);
        final int puzzlePieces = grid.getChildCount();

        for (int i = 0; i < puzzlePieces; ++i) {
            final View puzzlePiece = grid.getChildAt(i);
            puzzlePiece.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MovePiece(view);
                    LayoutPuzzle();
                    SetOnClicks();
                    SolvedState();
                    UpdateUI();
                }
            });
            puzzlePiece.setOnTouchListener(gestureListener);
        }
    }

    protected boolean SolvedState() {
        for (int i = 0, c = 0, r = 0; i < puzzle.GetTotalSize(); ++i, ++c) {
            if (c == puzzle.GetWidth()) {
                c = 0;
                r++;
            }
            String pos = Integer.toString(c + 1) + Integer.toString(r + 1);
            if (!puzzle.GetPictureSetArray()[r][c].equals(pos) && !puzzle.GetPictureSetArray()[r][c].equals("empty")) {
                return false;
            }
        }

        //Save Current Score
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(DatabaseContract.HighscoreEntry.COLUMN_NAME_PUZZLE, puzzle.GetPuzzle());
        contentValues.put(DatabaseContract.HighscoreEntry.COLUMN_NAME_SCORE, puzzle.GetMovesMade());
        db.insert(DatabaseContract.HighscoreEntry.TABLE_NAME, null, contentValues);
        puzzle.SetSolved(true);

        //Win MSG
        Context context = getApplicationContext();
        CharSequence text = getResources().getString(R.string.Win_Prelude) + puzzle.GetMovesMade() + getResources().getString(R.string.Win_Postscript);
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return true;
    }

    protected int TooManyPieces(int distance) {
        if (distance > 2) {
            Context context = getApplicationContext();
            CharSequence text = getResources().getString(R.string.Max_Slide_Msg);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return 2;
        } else return distance;
    }

    protected void MovePiece(View view) {
        //Split and parse file and position information
        String fileAndPosition = (String) view.getContentDescription();
        String[] fileAndPositionArray; //Stored in imageView
        String file; //File name of bitmap
        String position; //Current position of image
        fileAndPositionArray = fileAndPosition.split(" at: ");
        file = fileAndPositionArray[0];
        position = fileAndPositionArray[1];
        String row = "";
        String col = "";
        String[] rowAndCol = {position.substring(0, 1), position.substring(1)};
        row = rowAndCol[0];
        col = rowAndCol[1];
        int rowInt = Integer.parseInt(row);
        int colInt = Integer.parseInt(col);
        int colArr = 0;
        int rowArr = 1;
        boolean moved = false;

        if (puzzle.GetEmptyPosition()[colArr] == colInt && puzzle.GetEmptyPosition()[rowArr] != rowInt) {

            if (rowInt > puzzle.GetEmptyPosition()[rowArr]) {//Move Up
                int distance = rowInt - puzzle.GetEmptyPosition()[rowArr];
                distance = TooManyPieces(distance);
                for (int i = 1; i <= distance; ++i) {
                    String current = puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr]][puzzle.GetEmptyPosition()[colArr] - 1];
                    puzzle.GetPictureSetArray()[(puzzle.GetEmptyPosition()[rowArr] - 1)][puzzle.GetEmptyPosition()[colArr] - 1] = current;
                    puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr]][puzzle.GetEmptyPosition()[colArr] - 1] = "empty";
                    puzzle.GetEmptyPosition()[rowArr] = (puzzle.GetEmptyPosition()[rowArr] + i);
                }
                moved = true;
            } else {//Move Down
                int distance = puzzle.GetEmptyPosition()[rowArr] - rowInt;
                distance = TooManyPieces(distance);
                for (int i = 1; i <= distance; ++i) {
                    String current = puzzle.GetPictureSetArray()[(puzzle.GetEmptyPosition()[rowArr] - 2)][puzzle.GetEmptyPosition()[colArr] - 1];
                    puzzle.GetPictureSetArray()[(puzzle.GetEmptyPosition()[rowArr] - 2)][puzzle.GetEmptyPosition()[colArr] - 1] = "empty";
                    puzzle.GetPictureSetArray()[(puzzle.GetEmptyPosition()[rowArr] - 1)][puzzle.GetEmptyPosition()[colArr] - 1] = current;
                    puzzle.GetEmptyPosition()[rowArr] = (puzzle.GetEmptyPosition()[rowArr] - 1);
                }
                moved = true;
            }
        } else if (puzzle.GetEmptyPosition()[rowArr] == rowInt && puzzle.GetEmptyPosition()[colArr] != colInt) {
            if (colInt < puzzle.GetEmptyPosition()[colArr]) {//Move right
                int distance = puzzle.GetEmptyPosition()[colArr] - colInt;
                distance = TooManyPieces(distance);
                for (int i = 1; i <= distance; ++i) {
                    String current = puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][puzzle.GetEmptyPosition()[colArr] - 2];
                    puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][puzzle.GetEmptyPosition()[colArr] - 2] = "empty";
                    puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][(puzzle.GetEmptyPosition()[colArr]) - 1] = current;
                    puzzle.GetEmptyPosition()[colArr] = (puzzle.GetEmptyPosition()[colArr] - 1);
                }
                moved = true;
            } else {//Move left
                int distance = colInt - puzzle.GetEmptyPosition()[colArr];
                distance = TooManyPieces(distance);
                for (int i = 1; i <= distance; ++i) {
                    String current = puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][puzzle.GetEmptyPosition()[colArr]];
                    puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][puzzle.GetEmptyPosition()[colArr]] = "empty";
                    puzzle.GetPictureSetArray()[puzzle.GetEmptyPosition()[rowArr] - 1][(puzzle.GetEmptyPosition()[colArr] - 1)] = current;
                    puzzle.GetEmptyPosition()[colArr] = (puzzle.GetEmptyPosition()[colArr] + i);
                }
                moved = true;
            }
        }
        if (moved) {
            puzzle.IncrementMovesMade();
        }
    }

    public void UpdateUI() {
        TextView movesTextView = (TextView) findViewById(R.id.TotalMoves);
        movesTextView.setText(getResources().getString(R.string.Current_Score) + puzzle.GetMovesMade());
        TextView highscoreTextView = (TextView) findViewById(R.id.HighScore);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String score = "";
        try {
            Cursor c = db.rawQuery("SELECT SCORE FROM HIGHSCORES WHERE PUZZLE ='" + puzzle.GetPuzzle() + "'ORDER BY SCORE Limit 1", null);
            if (c.moveToFirst()) {
                score = c.getString(0);
            } else {
                score = getResources().getString(R.string.Hisghscore_No_Moves);
            }
            highscoreTextView.setText(getResources().getString(R.string.Hisghscore_Prelude) + score + getResources().getString(R.string.Hisghscore_Postscript));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[][] formatAndSaveJsonLayout(String input){

        JSONObject json = null;
        JSONArray layoutArray;
        String[][] returnArray = null;
        try {
            json = new JSONObject(input);
            layoutArray = json.getJSONArray("layout");
            puzzle.SetWidth(layoutArray.getJSONArray(0).length());
            puzzle.SetTotalSize(layoutArray.length() * puzzle.GetWidth());
            puzzle.SetPictureSetArray(new String[layoutArray.length()][puzzle.GetWidth()]);
            returnArray = new String[layoutArray.length()][puzzle.GetWidth()];
            for (int i = 0; i < layoutArray.length(); ++i) {
                JSONArray arr = layoutArray.getJSONArray(i);
                for (int j = 0; j < arr.length(); ++j) {
                    puzzle.SetPictureSetArray(i, j, arr.getString(j));
                    returnArray[i][j] = arr.getString(j);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnArray;
    }

    class downloadPuzzle extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {
            DownloadManager load = new DownloadManager();
            Context context = PuzzleActivity.this;
            String unParsedJSON = load.LoadJSON(context, puzzle.GetPuzzle(),args[0]);
            return unParsedJSON;
        }

        protected void onPostExecute(String pResult) {
            if (!pResult.equals("")) {
                try {
                    JSONObject json = new JSONObject(pResult);
                    puzzle.SetLayout(json.getString("layout"));
                    puzzle.SetPictureSet(json.getString("PictureSet"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                downloadPuzzleLayout layout = new downloadPuzzleLayout();
                layout.execute("http://www.simongrey.net/08027/slidingPuzzleAcw/layouts/" + puzzle.GetLayout());
            }
            else {//Error occurred
                Context context = PuzzleActivity.this;
                ErrorChecking error = new ErrorChecking();
                if(error.isNetworkAvailable(context)){
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Reading_Data_Error));
                }
                else {
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Network_Error_Msg));
                }
            }
        }
    }

    class downloadPuzzlePictureSet extends AsyncTask<Void, Void, Bitmap> {
        protected Bitmap doInBackground(Void... args) {
            Bitmap picture = null;

            for (String[] s : puzzle.GetPictureSetArray()) {
                for (String as : s) {
                    if (as != "empty") {
                        String url = ("http://www.simongrey.net/08027/slidingPuzzleAcw/images/" + puzzle.GetPictureSet() + "/" + as + ".jpg");
                        try { //Read from file
                            FileInputStream stream = getApplicationContext().openFileInput(puzzle.GetPictureSet() + "_" + as);
                            picture = BitmapFactory.decodeStream(stream);
                            puzzle.GetBitmaps().put(as, picture);
                        } catch (FileNotFoundException fileNotFound){
                            try { // Download File
                                FileOutputStream writer = null;
                                picture = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                                puzzle.GetBitmaps().put(as, picture);
                                try {//Save downloaded file
                                    writer = getApplicationContext().openFileOutput(puzzle.GetPictureSet() + "_" + as, Context.MODE_PRIVATE);
                                    picture.compress(Bitmap.CompressFormat.JPEG, 100, writer);
                                }catch (Exception e){
                                    Log.i("Error", e.getMessage());
                                } finally {
                                    writer.close();
                                }
                            } catch (Exception e) {
                                Log.i("Error", e.getMessage());
                            }
                        }
                    }
                }
            }
            return picture;
        }

        protected void onPostExecute(Bitmap pResult) {
            if(pResult != null) {
                onResume();
                LayoutPuzzle();
                SetOnClicks();
            }else {//Error occurred
                Context context = PuzzleActivity.this;
                ErrorChecking error = new ErrorChecking();
                if(error.isNetworkAvailable(context)){
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Reading_Data_Error));
                }
                else {
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Network_Error_Msg));
                }
            }
        }
    }

    class downloadPuzzleLayout extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... args) {
            DownloadManager load = new DownloadManager();
            Context context = PuzzleActivity.this;
            String unParsedJSON = load.LoadJSON(context, puzzle.GetLayout(),args[0]);
            return unParsedJSON;
        }

        protected void onPostExecute(String pResult) {
            if(!pResult.equals("")) {
                puzzle.SetLayout(pResult);
                formatAndSaveJsonLayout(pResult);
                downloadPuzzlePictureSet pictureDownload = new downloadPuzzlePictureSet();
                pictureDownload.execute();
            } else {//Error occurred
                Context context = PuzzleActivity.this;
                ErrorChecking error = new ErrorChecking();
                if(error.isNetworkAvailable(context)){
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Reading_Data_Error));
                }
                else {
                    error.LaunchErrorScreen(context, getResources().getString(R.string.Network_Error_Msg));
                }
            }
        }
    }
}
