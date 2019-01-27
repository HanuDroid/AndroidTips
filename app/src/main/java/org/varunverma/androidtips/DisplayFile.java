package org.varunverma.androidtips;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
	
	@SuppressWarnings("deprecation")
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
        	my_web_view.setBackground(getResources().getDrawable(R.mipmap.background));
        }
        else{
        	my_web_view.setBackgroundDrawable(getResources().getDrawable(R.mipmap.background));
        }

        WebSettings webSettings = my_web_view.getSettings();
		webSettings.setJavaScriptEnabled(true);
		my_web_view.addJavascriptInterface(new FileJavaScriptInterface(), "File");
                      
        String title = this.getIntent().getStringExtra("Title");
        if(title == null || title.contentEquals("")){
        	title = getResources().getString(R.string.app_name);
        }
        
        this.setTitle(title);
        
       	String fileName = getIntent().getStringExtra("File");
       	
       	if(fileName != null){
       		// If File name was provided, show from file name.
       		getHTMLFromFile(fileName);

			if(fileName.contains("about")){
				show_ad = true;
			}
       	}
       	else{
       		// Else, show data directly.
       		String subject = getIntent().getStringExtra("Subject");
       		String content = getIntent().getStringExtra("Content");
       		html_text = "<html><body>" +
       				"<h3>" + subject + "</h3>" +
       				"<p>" + content + "</p>" +
       				"</body></html>";
       	}
       	
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
		my_web_view.clearCache(true);
        my_web_view.setBackgroundColor(0);
        my_web_view.loadData(html_text, "text/html", "utf-8");
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

	class FileJavaScriptInterface{
		
		@JavascriptInterface
		public void buttonClick(String buttonName){
			
			if(buttonName.contentEquals("accept")){
				Application.getApplicationInstance().setEULAResult(true);
			}
			else if(buttonName.contentEquals("reject")){
				Application.getApplicationInstance().setEULAResult(false);
			}
			
			setResult(RESULT_OK);       
			finish();
			
		}
		
	}
}