package mobile.labs.acw;

import android.graphics.Bitmap;

import java.util.HashMap;

public class Puzzle {
    private String m_puzzle = "";
    private String m_layout = "";
    private String m_pictureSet = "";
    private HashMap<String, Bitmap> m_bitmaps = new HashMap<String, Bitmap>();
    private String[][] m_pictureSetArray;
    private int m_width;
    private int m_totalSize;
    private int[] m_emptyPosition = new int[2];
    private int m_movesMade = 0;
    private boolean m_solved = false;

    public void Puzzle(){

    }

    public void SetPuzzle(String in){
        m_puzzle = in;
    }
    public String GetPuzzle(){
        return m_puzzle;
    }
    public void SetLayout(String in){
        m_layout = in;
    }
    public String GetLayout(){
        return m_layout;
    }
    public void SetPictureSet(String in){
        m_pictureSet = in;
    }
    public String GetPictureSet(){
        return m_pictureSet;
    }
    public void SetBitmaps(HashMap<String, Bitmap> in){
        m_bitmaps = in;
    }
    public HashMap<String, Bitmap> GetBitmaps(){
        return m_bitmaps;
    }
    public void SetPictureSetArray(String[][] in){
        m_pictureSetArray = in;
    }
    public void SetPictureSetArray(int x, int y, String content){
        m_pictureSetArray[x][y] = content;
    }
    public String[][] GetPictureSetArray(){
        return m_pictureSetArray;
    }
    public void SetWidth(int in){
        m_width = in;
    }
    public int GetWidth(){
        return m_width;
    }
    public void SetTotalSize(int in){
        m_totalSize = in;
    }
    public int GetTotalSize(){
        return m_totalSize;
    }
    public void SetEmptyPosition(int[] in){
        m_emptyPosition = in;
    }
    public int[] GetEmptyPosition(){
        return m_emptyPosition;
    }
    public void IncrementMovesMade(){
        m_movesMade++;
    }
    public void SetMovesMade(int in){
        m_movesMade = in;
    }
    public int GetMovesMade(){
        return m_movesMade;
    }
    public void SetSolved(boolean in){
        m_solved = in;
    }
    public boolean GetSolved(){
        return m_solved;
    }
}
