package borsh.a15_puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Borsh on 11.07.2016.
 */
public class SplashScreen extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, main.class);
        startActivity(intent);
        finish();
    }
}
