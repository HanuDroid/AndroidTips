package org.varunverma.androidtips;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.Post;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class ShowPostFullScreen extends AppCompatActivity {
	
	private String html_text;
	private WebView my_web_view;
	private boolean show_ad = false;
	private Post post;

	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_file);

		Application app = Application.getApplicationInstance();
		app.setContext(this);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();

		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);

		// Get the Intent.
		// ATTENTION: This was auto-generated to handle app links.
		Intent intent = getIntent();
		int postID = intent.getIntExtra("PostID", 0);
		String title = getResources().getString(R.string.app_name);

		if(postID > 0){
			// We have a valid Post ID !
			post = app.getPostById(postID);
			displayActivity();
		}
		else{

			String appLinkAction = intent.getAction();
			Uri appLinkData = intent.getData();
			List<String> pathSegments = appLinkData.getPathSegments();

			// Check what to do with this URI Data.
			// 1. Is it the base url ?
			if(pathSegments.size() == 0){
				// Url is : https://androidtricks.app/ --> Start Main Activity
				startMainActivity(title);
			}

			// Is it a Direct post link
			if(pathSegments.size() == 1){

				String postName = pathSegments.get(0);
				post = app.loadPostByName(postName);
				displayActivity();
			}

			// Is it a tag or category or may be month !
			if(pathSegments.size() == 2){

				String part1 = pathSegments.get(0);
				String part2 = pathSegments.get(1);

				if(part1.contentEquals("category")){
					app.loadPostByCategory(part2);
					title = "In category: " + part2;
				}
				else if(part1.contentEquals("tag")){
					app.loadPostByTag(part2);
					title = "Tagged with: " + part2;
				}
				else{

					try{

						int year = Integer.valueOf(part1);
						int month = Integer.valueOf(part2);
						month--;
						GregorianCalendar calendar = new GregorianCalendar(year, month,1);
						title = "In month: " + calendar.getDisplayName(Calendar.MONTH,Calendar.SHORT, Locale.getDefault()) + " " + part1;
						long fromTime = calendar.getTimeInMillis();

						calendar.add(Calendar.MONTH,1);
						long toTime = calendar.getTimeInMillis();

						app.loadPostsInDateRange(fromTime, toTime);

					} catch (Exception e){
						// Ignore.
						startMainActivity(title);
					}

				}

				startMainActivity(title);
			}

		}

	}

	private void startMainActivity(String title){

		Log.i(Application.TAG, "Start the main screen");
		Intent start = new Intent(ShowPostFullScreen.this, Main.class);
		start.putExtra("Title", title);
		ShowPostFullScreen.this.startActivity(start);
		ShowPostFullScreen.this.finish();

	}

	private void displayActivity(){

		my_web_view = (WebView) findViewById(R.id.webview);
		my_web_view.setBackground(getResources().getDrawable(R.mipmap.background));
		my_web_view.setBackgroundColor(Color.TRANSPARENT);

		WebSettings webSettings = my_web_view.getSettings();
		webSettings.setJavaScriptEnabled(true);

		String title = getResources().getString(R.string.app_name);

		if (post == null || post.getId() == 0) {

			html_text = "<html><body>" +
					"<h3>(c) Ayansh TechnoSoft</h3>" +
					"<p>Could not find the content you are looking for</p>" +
					"<p>Please re-launch the app</p>" +
					"</body></html>";
		} else {

			title = post.getTitle();
			html_text = getPostHTML(post);
		}

		my_web_view.setBackgroundColor(Color.parseColor("#757575"));
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

	private void showFromRawSource() {
		// Show from a RAW Source
		//my_web_view.clearCache(true);
        my_web_view.loadDataWithBaseURL("fake://not/needed", html_text, "text/html", "UTF-8", "");
	}

	private String getPostHTML(Post post) {

		String contentColor = getString(R.string.content_color);
		String contentFont = getString(R.string.content_font);

		String html = "";
		if(post != null){

			html = "<HTML>" +
					// HTML Head
					"<head>" +
					// CSS
					"<style>" +
					"#content {color:"	+ contentColor + ";font-family:" + contentFont + "; font-size:16px;}" +
					//".image {height: 400px; width: 224px}" +
					"</style>" +
					"</head>" +
					// HTML Body
					"<body>" +
					// Content
					"<div id=\"content\">" + post.getContent(false) + "</div>" +
					"</body>" +
					"</html>";

		}

		return html;
	}
}