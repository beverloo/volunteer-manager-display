// Copyright 2024 Peter Beverloo & AnimeCon. All rights reserved.
// Use of this source code is governed by a MIT license that can be found in the LICENSE file.

package team.animecon.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.CookieManager;
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

        // Load the Volunteer Manager's display subapp. Provisioning of the display will have to
        // be done by one of the volunteering leads, until that moment it's idle.
        binding.webview.loadUrl("https://animecon.team/display");
    }
}