package org.varunverma.androidtips;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.ayansh.hanudroid.Application;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DisplayFile extends AppCompatActivity {
	
	private String html_text;
	private WebView my_web_view;
	private boolean show_ad = false;
	private String contentType = "";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);       
        setContentView(R.layout.display_file);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);
        
        my_web_view = (WebView) findViewById(R.id.webview);
       	my_web_view.setBackground(getResources().getDrawable(R.mipmap.background));
		my_web_view.setBackgroundColor(Color.TRANSPARENT);

		WebSettings webSettings = my_web_view.getSettings();
		webSettings.setJavaScriptEnabled(true);

        // Get the Intent.
		Intent intent = getIntent();

		String title = intent.getStringExtra("Title");
		if(title == null || title.contentEquals("")){
			title = getResources().getString(R.string.app_name);
		}

		contentType = intent.getStringExtra("ContentType");
		if(contentType.contentEquals("Post")){

			my_web_view.setBackgroundColor(Color.parseColor("#757575"));
			String postContent = intent.getStringExtra("PostContent");
			html_text = postContent;
		}
		else if(contentType.contentEquals("File")){

			String fileName = getIntent().getStringExtra("File");
			getHTMLFromFile(fileName);

			if(fileName.contains("about")){
				show_ad = true;
			}

		}
		else if(contentType.contentEquals("Notification")){

			String subject = getIntent().getStringExtra("Subject");
			String content = getIntent().getStringExtra("Content");
			html_text = "<html><body>" +
					"<h3>" + subject + "</h3>" +
					"<p>" + content + "</p>" +
					"</body></html>";
		}
		else{

			html_text = "<html><body>" +
					"<h3>(c) Ayansh TechnoSoft</h3>" +
					"<p>No Content</p>" +
					"</body></html>";
		}
        
        this.setTitle(title);

       	showFromRawSource();
       			
	}

	@Override
	protected void onDestroy(){

		if(show_ad){
			showInterstitialAd();
		}

		super.onDestroy();
	}

	private void showInterstitialAd(){

		InterstitialAd iad = MyInterstitialAd.getInterstitialAd(this);
		if(iad.isLoaded()){
			iad.show();
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

	private void showFromRawSource() {
		// Show from a RAW Source
		//my_web_view.clearCache(true);
        my_web_view.loadDataWithBaseURL("fake://not/needed", html_text, "text/html", "UTF-8", "");
	}

	private void getHTMLFromFile(String fileName) {
		// Get HTML File from RAW Resource
		Resources res = getResources();
        InputStream is;
        
		try {
			
			is = res.getAssets().open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        html_text = "";
	        String line = "";
	        
	        while((line = reader.readLine()) != null){
				html_text = html_text + line;
			}
	        
		} catch (IOException e) {
			Log.e(Application.TAG, e.getMessage(), e);
		}
	}
}