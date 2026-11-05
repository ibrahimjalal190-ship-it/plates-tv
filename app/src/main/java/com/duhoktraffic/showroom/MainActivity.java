package com.duhoktraffic.showroom;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Opens the showroom website full screen. The plates are NOT stored in the app:
 * they come live from the website, so editing them in Manage Inventory updates
 * the TV without installing anything new.
 */
public class MainActivity extends Activity {

    /** If the internet is down, try again every 10 seconds. */
    private static final long RETRY_DELAY_MS = 10_000L;
    /** Reload the page every 6 hours so a newly published website version is picked up. */
    private static final long AUTO_REFRESH_MS = 6L * 60L * 60L * 1000L;

    private WebView web;
    private String startUrl;
    private boolean resumedBefore = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable reloadTask = new Runnable() {
        @Override
        public void run() {
            if (web != null) {
                web.loadUrl(startUrl);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Never let the TV go to sleep while the showroom is on screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startUrl = getString(R.string.start_url);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0B132B"));
        setContentView(web);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // The website looks for this word and skips its own "full screen" button,
        // because the app is already full screen.
        settings.setUserAgentString(settings.getUserAgentString() + " PlatesTV/1.0");

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url != null && url.startsWith(startUrl)) {
                    // Safety net: hide the website's full-screen prompt/exit buttons inside the app.
                    view.evaluateJavascript(
                            "(function(){var s=document.createElement('style');"
                                    + "s.textContent='#fs-prompt,#fs-exit-btn{display:none!important}';"
                                    + "document.head.appendChild(s);})();",
                            null);
                    // Schedule the next automatic refresh.
                    handler.removeCallbacks(reloadTask);
                    handler.postDelayed(reloadTask, AUTO_REFRESH_MS);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    showOffline(view);
                }
            }
        });

        web.loadUrl(startUrl);
    }

    private void showOffline(WebView view) {
        String html = "<html><body style=\"margin:0;background:#0b132b;color:#ffffff;"
                + "font-family:sans-serif;display:flex;align-items:center;justify-content:center;"
                + "height:100vh;text-align:center\"><div>"
                + "<div style=\"font-size:48px;font-weight:bold\">No internet connection</div>"
                + "<div style=\"font-size:28px;color:#a1a1aa;margin-top:16px\">Trying again automatically...</div>"
                + "</div></body></html>";
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        handler.removeCallbacks(reloadTask);
        handler.postDelayed(reloadTask, RETRY_DELAY_MS);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        // When the TV wakes up or the app comes back to the front, load a fresh copy.
        if (resumedBefore) {
            web.loadUrl(startUrl);
        }
        resumedBefore = true;
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
