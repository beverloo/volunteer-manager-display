package team.animecon.display;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import team.animecon.display.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'display' library on application startup.
    static {
        System.loadLibrary("display");
    }

    private BrightnessController mBrightnessController;
    private KioskController mKioskController;
    private LightController mLightController;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBrightnessController = new BrightnessController(this, 10);
        mKioskController = new KioskController(this);
        mLightController = new LightController("/dev/ttyS3", 9600);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText("Hello 2.0!");

        mBrightnessController.initialise();
        mKioskController.initialise();

        // BrightnessController test:
        {
            mBrightnessController.update(10);
        }

        // KioskController test:
        {
            mKioskController.hideUserInterface();
            //mKioskController.enable();
        }

        // LightController test:
        {
            mLightController.open();
            mLightController.sendCommand("KEEP:RED:0:255");
            SystemClock.sleep(30);
            mLightController.sendCommand("KEEP:GREEN:0:255");
            SystemClock.sleep(40);
            mLightController.sendCommand("KEEP:BLUE:0:25");
            mLightController.close();
        }
    }
}