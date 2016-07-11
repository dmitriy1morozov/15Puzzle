package borsh.a15_puzzle;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class main extends AppCompatActivity
{
    //------------------------------Constants-------------------------------------------------------
    private final static String TAG = "---main---";
    private final static String BEST_TIME_FILE_NAME = "bestTime.dat";
    private final static String BEST_MOVES_FILE_NAME = "bestMoves.dat";

    //------------------------------Fields----------------------------------------------------------
    private int     _mainColor;
    private Button  _btnStart;
    private boolean _isPlaying;

    //Info section
    private int _bestMoves;
    private int _curMoves;
    private int _bestTime;
    public static int _curTime;
    private Counter _counter;

    /**
     * Game Table widgets TextView
     */
    private TextView _tvGame[][]    = new TextView[4][4];
    /**
     * Game array
     * int value represents the digit in the field
     */
    private int      _arrayGame[][] = new int[4][4];

    private int _blankI;
    private int _blankJ;

    //-----------------------------Activity LifeCycle Methods---------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main.this._btnStart = (Button)findViewById(R.id.btnStart);
        if (main.this._mainColor == 0)
        {
            main.this._mainColor = getResources().getColor(R.color.colorMainGreen);
        }
        main.this.drawApp();
        main.this.restoreTiles();
        //Set OnclickListener to Start button
        main.this._btnStart.setOnClickListener(new StartOnClickListener());
        //Create counter
        if(_counter != null)
        {
            _counter.interrupt();
        }
        _counter = null;
        _counter = new Counter((TextView)findViewById(R.id.tvTime), main._curTime);
        _counter.start();
        //Restore Game state from Bundle
        if (savedInstanceState != null)
        {
            restoreBundle(savedInstanceState);
        }

        //TODO Restore best results from file NEEDs MODIFICATION
        _bestMoves = restoreBestResults(BEST_MOVES_FILE_NAME);
        ((TextView)findViewById(R.id.tvBestMoves)).setText("Best : " + _bestMoves);
        _bestTime = restoreBestResults(BEST_TIME_FILE_NAME);
        ((TextView)findViewById(R.id.tvBestTime)).setText("" + main.this._bestTime / 60 + ":" + main.this._bestTime % 60);
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause");
        super.onPause();
        if(_counter != null)
        {
            _counter.pauseOn();
        }
    }
    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
        if(_counter != null && _isPlaying)
        {
            _counter.pauseOff();
        }
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy()");
        //_counter.resetCounter();
        if(_counter != null)
        {
            _counter.interrupt();
        }
        _counter = null;
        super.onDestroy();
    }

    //---------------------------------Methods------------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void restoreBundle(Bundle savedInstanceState)
    {
        main.this._mainColor = savedInstanceState.getInt("_mainColor");
        main.this.applyMainColor();
        main.this._isPlaying = (savedInstanceState.getBoolean("_isPlaying"));
        main.this.setStartText(_isPlaying);
        main.this._bestMoves = savedInstanceState.getInt("_bestMoves");
        ((TextView) main.this.findViewById(R.id.tvBestMoves)).setText("Best : " + main.this._bestMoves);
        main.this._curMoves = savedInstanceState.getInt("_curMoves");
        ((TextView) main.this.findViewById(R.id.tvMoves)).setText("Moves : " + main.this._curMoves);
        main.this._bestTime = savedInstanceState.getInt("_bestTime");
        ((TextView) main.this.findViewById(R.id.tvBestTime)).setText("" + main.this._bestTime / 60 + ":" + main.this._bestTime % 60);
        ((TextView) main.this.findViewById(R.id.tvTime)).setText("" + main._curTime / 60 + ":" + main._curTime % 60);

        //Restore game array and game table
        main.this._arrayGame = (int[][]) (savedInstanceState.getSerializable("_gameState"));
        main.this.restoreTiles();
        //Apply OnClickListeners to Tiles if the game already started
        if (_isPlaying)
        {
            main.TileOnClickListener TileListener = new TileOnClickListener();
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    main.this._tvGame[i][j].setOnClickListener(TileListener);
                }
            }
            _counter.pauseOff();
        }
    }

    private int restoreBestResults(String fileName)
    {
        String content;
        int result = 0;
        try
        {
            Log.d(TAG, "Sandbox Catalog : " + this.getFilesDir());
            fileName = this.getFilesDir() + File.separator + fileName;
            //File saveFile = this.getFileStreamPath(fileName);
            File saveFile = new File(fileName);
            if(!saveFile.exists())
            {
                throw new IOException(" not found");
            }

            FileInputStream FIS = new FileInputStream (saveFile);
            LineNumberReader LNR = new LineNumberReader(new InputStreamReader(FIS));
            while (true)
            {
                content = LNR.readLine();
                if (content != null)
                {
                    result = Integer.parseInt(content);
                }
                else
                {
                    break;
                }
            }
            LNR.close();
        }
        catch (IOException ioe)
        {
            Log.d(TAG, "Restore best results failed. Tried to restore " + fileName + " ; " + ioe.getMessage());
            result = 0;
        }

        return result;
    }

    private void drawApp()
    {
        //---------------------Top bar and info-------------------------
        FrameLayout FLstart = (FrameLayout) findViewById(R.id.FLstart);
        FLstart.setBackgroundColor(main.this._mainColor);
        main.this._btnStart.setBackgroundColor(main.this._mainColor);
        TextView tvBestMoves = (TextView)findViewById(R.id.tvBestMoves);
        tvBestMoves.setText("Best : " + main.this._bestMoves);
        TextView tvMoves = (TextView)findViewById(R.id.tvMoves);
        tvMoves.setText("Moves : " + main.this._curMoves);

        //---------------------Game Table-------------------------
        TableLayout TLgameTable = (TableLayout)findViewById(R.id.TLgameTable);
        int gameTableWidth;
        int colorSquareSideLength;
        if(getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            gameTableWidth = getWindowManager().getDefaultDisplay().getWidth() - 8;
            //colorSquareSideLength = getWindowManager().getDefaultDisplay().getWidth() / 12;
            colorSquareSideLength = gameTableWidth / 8 - 4;
            main.this._btnStart.setText("Start");
        }
        else
        {
            gameTableWidth = getWindowManager().getDefaultDisplay().getHeight() - 8 - getStatusBarHeight();
            //colorSquareSideLength = getWindowManager().getDefaultDisplay().getHeight() / 12;
            colorSquareSideLength = gameTableWidth / 8 - 4;
            main.this._btnStart.setText("S\nt\na\nr\nt");
        }
        TLgameTable.getLayoutParams().height = gameTableWidth;
        TLgameTable.getLayoutParams().width = gameTableWidth;

        //TLgameTable.setMinimumWidth(gameTableWidth);
        //TLgameTable.setMinimumHeight(gameTableWidth);
        main.this.addTiles(TLgameTable);

        //-----------------------Palette---------------------------------
        LinearLayout LLcolor = (LinearLayout)findViewById(R.id.LLcolor);
        main.ColorOnClickListener ColorListener = new ColorOnClickListener();
        LinearLayout.LayoutParams LLcolorfieldLP = new LinearLayout.LayoutParams(colorSquareSideLength, colorSquareSideLength);
        LLcolorfieldLP.setMargins(2, 2, 2, 2);
        Button btnColor[] = new Button[8];
        for (int i = 0; i < 8; i++)
        {
            btnColor[i] = new Button(this);
            LLcolor.addView(btnColor[i], LLcolorfieldLP);
            btnColor[i].setOnClickListener(ColorListener);
        }
        btnColor[0].setBackgroundColor(getResources().getColor(R.color.colorMainRed));
        btnColor[1].setBackgroundColor(getResources().getColor(R.color.colorMainOrange));
        btnColor[2].setBackgroundColor(getResources().getColor(R.color.colorMainGreen));
        btnColor[3].setBackgroundColor(getResources().getColor(R.color.colorMainTeal));
        btnColor[4].setBackgroundColor(getResources().getColor(R.color.colorMainIndigo));
        btnColor[5].setBackgroundColor(getResources().getColor(R.color.colorMainBlue));
        btnColor[6].setBackgroundColor(getResources().getColor(R.color.colorMainGrey));
        btnColor[7].setBackgroundColor(getResources().getColor(R.color.colorMainBlueGray));
    }

    private void addTiles(TableLayout TLtable)
    {
        //Adding Rows
        TableRow TRrow[] = new TableRow[4];
        TableLayout.LayoutParams TLrowLP = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        TLrowLP.gravity = Gravity.CENTER;
        TLrowLP.weight = 1;
        for (int i = 0; i < 4; i++)
        {
            TRrow[i] = new TableRow(this);
            TLtable.addView(TRrow[i], TLrowLP);
        }

        //Adding tiles
        TableRow.LayoutParams TRfieldLP = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        TRfieldLP.weight = 1;
        TRfieldLP.setMargins(2, 2, 2, 2);
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                main.this._arrayGame[i][j] = i * 4 + j + 1;
                main.this._tvGame[i][j] = new TextView(this);
                //main.this._tvGame[i][j].setText(String.valueOf(i * 4 + j + 1));
                main.this._tvGame[i][j].setTextSize(getResources().getDimension(R.dimen.textFieldSize));
                main.this._tvGame[i][j].setTextColor(getResources().getColor(R.color.colorBlack));
                main.this._tvGame[i][j].setIncludeFontPadding(false);
                main.this._tvGame[i][j].setGravity(Gravity.CENTER);
                main.this._tvGame[i][j].setBackgroundColor(main.this._mainColor);
                TRrow[i].addView(main.this._tvGame[i][j], TRfieldLP);
                main.this._tvGame[i][j].setContentDescription(String.valueOf(i) + String.valueOf(j));  //save information about row and column
            }
        }
    }

    private void generateArray()
    {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 1; i < 17; i++)
        {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                main.this._arrayGame[i][j] = list.get(i * 4 + j);
            }
        }
    }

    /**
     * Method that restores Tiles from _arrayGame[][]
     */
    private void restoreTiles()
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                if (main.this._arrayGame[i][j] != 16)
                {
                    main.this._tvGame[i][j].setText(String.valueOf(main.this._arrayGame[i][j]));
                    main.this._tvGame[i][j].setBackgroundColor(main.this._mainColor);
                } else
                {
                    main.this._tvGame[i][j].setText("");
                    //Save the place with blank tile
                    main.this._blankI = i;
                    main.this._blankJ = j;
                    main.this._tvGame[i][j].setBackgroundColor(Color.rgb(255, 255, 255));
                }
            }
        }
    }

    private boolean checkWin()
    {
        //Check 1st three columns
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                if (main.this._arrayGame[i][j] != i * 4 + j + 1)
                {
                    return false;
                }
            }
        }
        //Check last columns. Possible win solutions 13,14,15 or 13,15,14
        if (main.this._arrayGame[3][0] != 13)
        {
            return false;
        }
        if ((main.this._arrayGame[3][1] == 14 && main.this._arrayGame[3][2] == 15) ||
                (main.this._arrayGame[3][1] == 15 && main.this._arrayGame[3][2] == 14))
        {
            return true;
        } else
        {
            return false;
        }
    }

    private void gameOver()
    {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout LLwin = (LinearLayout) inflater.inflate(R.layout.custom_toast, null);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(LLwin);
        toast.show();

        //Refresh start button
        _isPlaying = false;
        main.this.setStartText(_isPlaying);
        //Remove listeners from Tiles
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                main.this._tvGame[i][j].setOnClickListener(null);
            }
        }

        //Check if gained best result for MOVES
        if (main.this._bestMoves == 0 || main.this._curMoves < main.this._bestMoves)
        {
            main.this._bestMoves = main.this._curMoves;
            ((TextView) main.this.findViewById(R.id.tvBestMoves)).setText("Best : " + main.this._bestMoves);
            //Save best result into file
            saveBestResult(_bestMoves, BEST_MOVES_FILE_NAME);
        }

        //Check if gained best result for TIME
        if(_bestTime == 0 || main._curTime < _bestTime)
        {
            _bestTime = main._curTime;
            TextView tvTime = (TextView)findViewById(R.id.tvTime);
            ((TextView)findViewById(R.id.tvBestTime)).setText(tvTime.getText());
            //Save bestTime into file
            saveBestResult(_bestTime, BEST_TIME_FILE_NAME);
        }
        //Stop counter
        _counter.resetCounter();
        _counter.pauseOn();
    }

    private void applyMainColor()
    {
        FrameLayout FLstart = (FrameLayout) main.this.findViewById(R.id.FLstart);
        FLstart.setBackgroundColor(main.this._mainColor);
        main.this._btnStart.setBackgroundColor(main.this._mainColor);
        main.this._btnStart.setTextColor(getResources().getColor(R.color.colorBlack));
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                if (main.this._arrayGame[i][j] != 16)
                {
                    main.this._tvGame[i][j].setBackgroundColor(main.this._mainColor);
                } else
                {
                    main.this._tvGame[i][j].setBackgroundColor(Color.rgb(255, 255, 255));
                }
            }
        }
    }

    /**
     * Method that sets text to _btnStart according to the screen orientation
     * STATUS:
     * true - Start
     * false - Restart
     */
    private void setStartText(Boolean isPlaying)
    {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            if (isPlaying)
            {
                main.this._btnStart.setText("Restart");
            }
            else
            {
                main.this._btnStart.setText("Start");
            }
        }
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            if (isPlaying)
            {
                main.this._btnStart.setText("R\ne\ns\nt\na\nr\nt");
            }
            else
            {
                main.this._btnStart.setText("S\nt\na\nr\nt");
            }
        }
    }

    private int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    //==============================================================================================
    //----------------------------OnClickListeners--------------------------------------------------
    //Create ColorOnClickListener for colors
    class ColorOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            ColorDrawable buttonBackground = (ColorDrawable) v.getBackground();
            main.this._mainColor = buttonBackground.getColor();
            main.this.applyMainColor();
        }
    }

    //Create StartOnClickListener to Start/Restart game
    class StartOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //Start action
            _isPlaying = true;
            main.this.setStartText(_isPlaying);
            main.this.generateArray();
            main.this.restoreTiles();
            main.this._curMoves = 0;
            TextView TVcurrentMoves = (TextView) main.this.findViewById(R.id.tvMoves);
            TVcurrentMoves.setText("Moves : " + main.this._curMoves);
            //start Counter
            _counter.resetCounter();
            _counter.pauseOff();

            //Set onClickListener for Tiles
            main.TileOnClickListener TileListener = new TileOnClickListener();
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    main.this._tvGame[i][j].setOnClickListener(TileListener);
                }
            }
        }
    }

    //Create TileOnClickListener for Tiles
    class TileOnClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //Get the position(i and j == row and column) of pressed view (saved in ContentDescription of each Tile TextView onCreate Tiles)
            int curI = Integer.parseInt(v.getContentDescription().subSequence(0, 1).toString());
            int curJ = Integer.parseInt(v.getContentDescription().subSequence(1, 2).toString());

            //Check if pressed is blank -> return
            if (curI == main.this._blankI && curJ == main.this._blankJ)
            {
                return;
            }

            //If pressed Tile is not in the same row/column with the blank Tile then no action required
            if (curI != main.this._blankI && curJ != main.this._blankJ)
            {
                return;
            }

            //Move all tiles from pressed Tile to blank tile (row move)
            if (curI == main.this._blankI)
            {
                //Move tiles to the left
                if (curJ > main.this._blankJ)
                {
                    for (int i = 0; i < (curJ - main.this._blankJ); i++)
                    {
                        main.this._arrayGame[curI][main.this._blankJ + i] = main.this._arrayGame[curI][main.this._blankJ + 1 + i];
                    }
                } else
                //Move Tiles to the right
                {
                    for (int i = 0; i < (main.this._blankJ - curJ); i++)
                    {
                        main.this._arrayGame[curI][main.this._blankJ - i] = main.this._arrayGame[curI][main.this._blankJ - 1 - i];
                    }
                }
            }
            //Move all tiles from pressed Tile to blank tile (column move)
            if (curJ == main.this._blankJ)
            {
                //Move tiles upwards
                if (curI > main.this._blankI)
                {
                    for (int i = 0; i < (curI - main.this._blankI); i++)
                    {
                        main.this._arrayGame[main.this._blankI + i][curJ] = main.this._arrayGame[main.this._blankI + 1 + i][curJ];
                    }
                } else
                //Move Tiles downwards
                {
                    for (int i = 0; i < (main.this._blankI - curI); i++)
                    {
                        main.this._arrayGame[main.this._blankI - i][curJ] = main.this._arrayGame[main.this._blankI - 1 - i][curJ];
                    }
                }
            }

            //Pressed Tile becomes blank
            main.this._arrayGame[curI][curJ] = 16;
            main.this.restoreTiles();
            main.this._curMoves++;
            //Refresh TextView with CurrentMoves counter
            ((TextView) main.this.findViewById(R.id.tvMoves)).setText("Moves : " + main.this._curMoves);
            if (main.this.checkWin())
            {
                main.this.gameOver();
            }
        }
    }

    //---------------------------------------Save instance state------------------------------------
    @Override
    public void onSaveInstanceState(Bundle B)
    {
        super.onSaveInstanceState(B);
        B.putInt("_mainColor", main.this._mainColor);
        B.putBoolean("_isPlaying", _isPlaying);
        B.putInt("_curMoves", main.this._curMoves);
        B.putInt("_bestMoves", main.this._bestMoves);
        B.putInt("_bestTime", main.this._bestTime);
        B.putSerializable("_gameState", _arrayGame);
    }

    private void saveBestResult(int bestResult, String fileName)
    {
        try
        {
            Log.d(TAG, "Sandbox Catalog : " + this.getFilesDir());
            FileOutputStream   FOS = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter OSW = new OutputStreamWriter(FOS);
            OSW.write(String.valueOf(bestResult));
            OSW.flush();
            OSW.close();
        }
        catch (IOException ioe)
        {
            Log.d(TAG, "Error saving file: " + ioe.getMessage());
        }
    }
}