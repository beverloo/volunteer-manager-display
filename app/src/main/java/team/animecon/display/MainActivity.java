// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;

import team.animecon.display.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'display' library on application startup.
    static {
        System.loadLibrary("display");
    }

    private BrightnessController mBrightnessController;
    private KioskController mKioskController;
    private LightController mLightController;
    private VolumeController mVolumeController;

    private WebMessageListener mWebMessageListener;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mBrightnessController = new BrightnessController(this, 5);
        this.mKioskController = new KioskController(this);
        this.mLightController = new LightController("/dev/ttyS3", 9600);
        this.mVolumeController = new VolumeController(this);

        this.mWebMessageListener = new WebMessageListener(
                this.mBrightnessController, this.mKioskController, this.mLightController,
                this.mVolumeController);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.mBrightnessController.initialise();
        this.mKioskController.initialise();

        // Initialise WebView:
        {
            WebView webView = binding.webview;
            webView.setBackgroundColor(Color.parseColor("#211a1a"));

            WebSettings webSettings = webView.getSettings();
            webSettings.setDomStorageEnabled(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);

            HashSet<String> allowedOriginRules =
                    new HashSet<String>(Collections.singletonList("*"));

            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                WebViewCompat.addWebMessageListener(
                        webView, "animeCon", allowedOriginRules,
                        this.mWebMessageListener);
            }

            CookieManager.getInstance().setAcceptCookie(true);

            webView.setWebViewClient(new WebViewClientCompat()
            {
                // no behaviour necessary (yet)
            });
        }

        // Always hide the user interface. Kiosk mode can be enabled independently.
        this.mKioskController.hideUserInterface();

        // Open the serial port with the light controller. It can be re-opened programmatically.
        this.mLightController.open();

        // Attach an uncaught exception handler to automagically restart the app when a crash is
        // observed. This is not ideal, but beats relying on end user interaction.
        final Activity defaultActivity = this;
        final Thread.UncaughtExceptionHandler defaultHandler =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                StringWriter stackTraceString = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stackTraceString));
                System.err.println(stackTraceString);

                Intent launchIntent = new Intent(defaultActivity.getIntent());
                @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getActivity(
                        defaultActivity.getApplicationContext(), 0, launchIntent,
                        defaultActivity.getIntent().getFlags());

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(
                        AlarmManager.RTC, System.currentTimeMillis() + 2000,
                        pendingIntent);

                System.exit(2);
            }
        });

        // Load the Volunteer Manager's display subapp. Provisioning of the display will have to
        // be done by one of the volunteering leads, until that moment it's idle.
        binding.webview.loadUrl("https://animecon.team/display");
    }
}