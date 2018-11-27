package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    String[] m_PuzzleListStringArray;
    DatabaseContract.PuzzleDBHelper mDbHelper = new DatabaseContract.PuzzleDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView NonDownloadedPuzzlesListView = (ListView) findViewById(R.id.PuzzlesListView);
        ListView DownloadedPuzzlesListView = (ListView) findViewById(R.id.DownladedPuzzlesListView);

        TextView resizeText = (TextView) findViewById(R.id.DownloadedPuzzlesTextView);
        resizeText.setTextSize(24);
        resizeText= (TextView) findViewById(R.id.UnloadedPuzzlesTextView);
        resizeText.setTextSize(24);

        if(savedInstanceState == null) {
            LoadPuzzleIndex listDownload = new LoadPuzzleIndex();
            listDownload.execute("http://www.simongrey.net/08027/slidingPuzzleAcw/index.json");
        }
        else
        {
            m_PuzzleListStringArray = savedInstanceState.getStringArray("PuzzleArrayList");
            ArrayAdapter<String> puzzleArray = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, m_PuzzleListStringArray);
            NonDownloadedPuzzlesListView.setAdapter(puzzleArray);
        }
        final ListView finalDownloadedPuzzleListView = DownloadedPuzzlesListView;
        final ListView finalAllPuzzleListView = NonDownloadedPuzzlesListView;
        NonDownloadedPuzzlesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), PuzzleActivity.class);
                String puzzle = finalAllPuzzleListView.getAdapter().getItem(position).toString();
                puzzle = puzzle.replaceAll("[\\u00A0\\u2007\\u202F]+", " ");
                puzzle = puzzle.trim();
                String[] formattedPuzzle = puzzle.split(" - ", 2);
                intent.putExtra("PuzzleSelected",formattedPuzzle[0]);
                startActivity(intent);
            }
        });
        finalDownloadedPuzzleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), PuzzleActivity.class);
                String puzzle = finalDownloadedPuzzleListView.getAdapter().getItem(position).toString();
                puzzle = puzzle.replaceAll("[\\u00A0\\u2007\\u202F]+", " ");
                puzzle = puzzle.trim();
                String[] formattedPuzzle = puzzle.split(" - ", 2);
                intent.putExtra("PuzzleSelected",formattedPuzzle[0]);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        LoadPuzzleIndex listDownload = new LoadPuzzleIndex();
        listDownload.execute("http://www.simongrey.net/08027/slidingPuzzleAcw/index.json");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArray("PuzzleArrayList",m_PuzzleListStringArray);
    }

    public String StreamReader(InputStream stream){
        String unParsedJSON = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            while (line != null) {
                unParsedJSON += line;
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unParsedJSON;
    }

    class LoadPuzzleIndex extends AsyncTask<String, Void, String[]> {
        protected String[] doInBackground(String... args) {
            String result = "";
            String[] formattedResult = null;
            DownloadManager load = new DownloadManager();
            Context context = MainActivity.this;
            result = load.LoadJSON(context,"PuzzleIndex",args[0]);
            if(result.equals("")){
                return formattedResult;
            }
            try { //Format the JSON
                JSONObject json = new JSONObject(result);
                JSONArray puzzles = json.getJSONArray("PuzzleIndex");
                formattedResult = new String[puzzles.length()];

                for(int i = 0; i < puzzles.length(); ++i)
                {
                    formattedResult[i] = puzzles.get(i).toString();
                }
            } catch (JSONException e) {
                formattedResult = null;
                return formattedResult;
            }
            return formattedResult;
        }

        protected void onPostExecute(String[] pResult)
        {
            Context context = MainActivity.this;
            DownloadManager manager = new DownloadManager();
            if(pResult != null) { //Successful load
                ListView listView = (ListView) findViewById(R.id.PuzzlesListView);
                m_PuzzleListStringArray = pResult;
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                for(int i = 0; i < m_PuzzleListStringArray.length; ++i){
                    try{
                        DownloadManager loader = new DownloadManager();
                        String puzzleName = m_PuzzleListStringArray[i];
                        try {
                            JSONObject puzzle = new JSONObject(manager.LoadJSON(context, puzzleName));
                            JSONObject layout = new JSONObject(manager.LoadJSON(context, puzzle.getString("layout")));
                            JSONArray layoutArray = layout.getJSONArray("layout");
                            int width = layoutArray.getJSONArray(0).length();
                            int height = layoutArray.length();
                            m_PuzzleListStringArray[i] = m_PuzzleListStringArray[i] + getResources().getString(R.string.Layout_Menu_Icon) + width + "*" + height;
                            String score = "";
                            try {
                                Cursor c = db.rawQuery("SELECT SCORE FROM HIGHSCORES WHERE PUZZLE ='" + puzzleName + "'ORDER BY SCORE Limit 1", null);
                                if (c.moveToFirst()) {
                                    score = c.getString(0);
                                    m_PuzzleListStringArray[i] = m_PuzzleListStringArray[i] + getResources().getString(R.string.Pre_Highscore) + score;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }catch (Exception e){

                    }
                }
                //TODO: Build two arraylists from it
                ArrayAdapter<String> puzzleArray = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, GetNonDownloaded(m_PuzzleListStringArray));
                listView.setAdapter(puzzleArray);
                puzzleArray = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, GetDownloaded(m_PuzzleListStringArray));
                ListView downloaded = (ListView) findViewById(R.id.DownladedPuzzlesListView);
                downloaded.setAdapter(puzzleArray);
            }
            else{ // Error downloading reading or parsing info
                ErrorChecking errorChecking = new ErrorChecking();
                if(!errorChecking.isNetworkAvailable(context)){//Network issues
                    errorChecking.LaunchErrorScreen(context, getResources().getString(R.string.Network_Error_Msg));
                }
                else{ //Reading issues
                    errorChecking.LaunchErrorScreen(context, getResources().getString(R.string.Reading_Data_Error));
                }
            }
        }
    }

    public ArrayList<String> GetDownloaded(String[] inArray){
        ArrayList<String> downloaded = new ArrayList<>();
        for(int i = 0; i < inArray.length; ++i){
            if(inArray[i].contains(getString(R.string.Layout_Menu_Icon))){
                downloaded.add(inArray[i]);
            }
        }
        return downloaded;
    }

    public ArrayList<String> GetNonDownloaded(String[] inArray){
        ArrayList<String> downloaded = new ArrayList<>();
        for(int i = 0; i < inArray.length; ++i){
            if(!inArray[i].contains(getString(R.string.Layout_Menu_Icon))){
                downloaded.add(inArray[i]);
            }
        }
        return downloaded;
    }
}
