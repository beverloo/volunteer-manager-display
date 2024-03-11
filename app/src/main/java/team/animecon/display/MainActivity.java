package team.animecon.display;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import team.animecon.display.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'display' library on application startup.
    static {
        System.loadLibrary("display");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText("Hello!");

        LightController lightController = new LightController("/dev/ttyS3", 9600);
        lightController.open();
        lightController.sendCommand("KEEP:RED:0:25");
        SystemClock.sleep(30);
        lightController.sendCommand("KEEP:GREEN:0:255");
        SystemClock.sleep(40);
        lightController.sendCommand("KEEP:BLUE:0:25");
        lightController.close();
    }
}