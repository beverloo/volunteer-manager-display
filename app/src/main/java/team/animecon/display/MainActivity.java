// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

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

    private WebMessageListener mWebMessageListener;

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mBrightnessController = new BrightnessController(this, 10);
        this.mKioskController = new KioskController(this);
        this.mLightController = new LightController("/dev/ttyS3", 9600);

        this.mWebMessageListener = new WebMessageListener(
                this.mBrightnessController, this.mKioskController, this.mLightController);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.mBrightnessController.initialise();
        this.mKioskController.initialise();

        // Initialise WebView:
        {
            WebView webView = binding.webview;

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            HashSet<String> allowedOriginRules =
                    new HashSet<String>(Collections.singletonList("*"));

            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                WebViewCompat.addWebMessageListener(
                        webView, "animeCon", allowedOriginRules,
                        this.mWebMessageListener);
            }

            webView.setWebViewClient(new WebViewClientCompat()
            {
                // no behaviour necessary (yet)
            });
        }

        // Always hide the user interface. Kiosk mode can be enabled independently.
        this.mKioskController.hideUserInterface();

        // Open the serial port with the light controller. It can be re-opened programmatically.
        this.mLightController.open();

        // Load the Volunteer Manager's display subapp.
        binding.webview.loadUrl("http://192.168.252.161:3000/display");
    }
}