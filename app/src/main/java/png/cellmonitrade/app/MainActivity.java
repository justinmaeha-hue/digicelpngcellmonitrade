
package png.cellmonitrade.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;

public class MainActivity extends Activity {

    private static final String HOME =
        "https://delicate-water-1c33.justinmaeha.workers.dev/";

    private static final String HOST =
        "delicate-water-1c33.justinmaeha.workers.dev";

    private WebView web;
    private ProgressBar progress;
    private LinearLayout offline;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);

        web = new WebView(this);
        root.addView(web);

        progress = new ProgressBar(
            this, null,
            android.R.attr.progressBarStyleHorizontal
        );

        FrameLayout.LayoutParams progressParams =
            new FrameLayout.LayoutParams(-1, 8);

        root.addView(progress, progressParams);

        offline = new LinearLayout(this);
        offline.setOrientation(LinearLayout.VERTICAL);
        offline.setPadding(35, 90, 35, 35);
        offline.setBackgroundColor(Color.WHITE);

        TextView message = new TextView(this);
        message.setText(
            "No internet connection or website unavailable."
        );
        message.setTextSize(18);
        offline.addView(message);

        Button retry = new Button(this);
        retry.setText("Try Again");

        retry.setOnClickListener(v -> {
            offline.setVisibility(View.GONE);
            web.loadUrl(HOME);
        });

        offline.addView(retry);
        root.addView(offline);
        offline.setVisibility(View.GONE);

        setContentView(root);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                progress.setProgress(p);
                progress.setVisibility(
                    p >= 100 ? View.GONE : View.VISIBLE
                );
            }
        });

        web.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
            ) {
                Uri url = request.getUrl();

                if ("https".equals(url.getScheme())
                    && HOST.equals(url.getHost())) {
                    return false;
                }

                if (!request.isForMainFrame()) {
                    return true;
                }

                String scheme = url.getScheme();

                if (!"https".equals(scheme)
                    && !"http".equals(scheme)
                    && !"mailto".equals(scheme)
                    && !"tel".equals(scheme)) {
                    return true;
                }

                try {
                    startActivity(
                        new Intent(Intent.ACTION_VIEW, url)
                    );
                } catch (Exception e) {
                    // No compatible application installed.
                }

                return true;
            }

            @Override
            public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
            ) {
                if (request.isForMainFrame()) {
                    offline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(
                WebView view, String url
            ) {
                progress.setVisibility(View.GONE);
            }
        });

        web.loadUrl(HOME);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (offline.getVisibility() == View.VISIBLE) {
                offline.setVisibility(View.GONE);
                web.loadUrl(HOME);
                return true;
            }

            web.evaluateJavascript(
                "(function(){" +
                "if(typeof backToProducts==='function' " +
                "&& document.getElementById('payment') " +
                "&& getComputedStyle(document.getElementById('payment')).display!=='none')" +
                "{backToProducts();return true;}" +
                "return false;" +
                "})()",
                result -> {
                    if (!"true".equals(result)) {
                        if (web.canGoBack()) {
                            web.goBack();
                        } else {
                            finish();
                        }
                    }
                }
            );
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
        }
        super.onDestroy();
    }
}
