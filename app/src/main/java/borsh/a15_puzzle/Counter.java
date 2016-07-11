package borsh.a15_puzzle;

import android.util.Log;
import android.widget.TextView;

/**
 * Created by Borsh on 10.07.2016.
 */
public class Counter extends Thread
{
    //-----------------------------------------Constants--------------------------------------------
    private final static String TAG      = "---Counter---";
    //-----------------------------------------Fields-----------------------------------------------
    private boolean             _isPause = true;
    private TextView            _TVcounter;

    //-----------------------------------------Constructor------------------------------------------
    public Counter(TextView TVcounter, double time)
    {
        _TVcounter = TVcounter;
    }
    //-----------------------------------------Getters----------------------------------------------
    public boolean get_isPause()
    {
        return _isPause;
    }

    //-----------------------------------------Methods----------------------------------------------
    public synchronized void pauseOn()
    {
        if(!this._isPause)
        {
            this._isPause = true;
        }
    }
    public synchronized void pauseOff()
    {
        if(this._isPause)
        {
            this._isPause = false;
            this.notify();
        }
    }
    public void resetCounter()
    {
        main._curTime = 0;
        _TVcounter.setText("0:0");
    }

    //-----------------------------------------Run method-------------------------------------------
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                Thread.sleep(1000);
                synchronized(this)
                {
                    if (_isPause)
                    {
                        this.wait();
                    }
                }
                main._curTime++;
                _TVcounter.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        _TVcounter.setText("" + main._curTime / 60 + ":" + main._curTime % 60);
                    }
                });
            }
        }
        catch (InterruptedException ie)
        {
            Log.d("=====", Thread.currentThread().getName() + " Ошибка : " + ie.getMessage());
        }

        Log.d("=====", "Counter finished running");
    }
}
