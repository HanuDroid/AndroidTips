package org.varunverma.androidtips;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.varunverma.hanu.Application.Application;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class DisplayFile extends Activity {
	
	private String html_text;
	private WebView my_web_view;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);       
        setContentView(R.layout.info);
        
        my_web_view = (WebView) findViewById(R.id.info);
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