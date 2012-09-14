package org.varunverma.androidtips;

import java.util.List;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.Application;
import org.varunverma.hanu.Application.HanuGestureAnalyzer;
import org.varunverma.hanu.Application.HanuGestureListener;
import org.varunverma.hanu.Application.Post;
import org.varunverma.hanu.Application.Tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class Main extends Activity implements Invoker, HanuGestureListener {

	private Application app;
	private ProgressDialog dialog;
	private boolean firstUse;
	private int position;
	WebView wv;
	private GestureDetector detector;
	List<Post> postList;
	private boolean appClosing;
	private AdView adView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Get Application Instance.
        app = Application.getApplicationInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());
        
        // Accept my Terms
        if(!app.isEULAAccepted()){
        	// Show EULA.
        	Intent eula = new Intent(Main.this, DisplayFile.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			Main.this.startActivityForResult(eula, Application.EULA);
        }
        else{
        	// Start the Main Activity
        	startMainActivity();
        }
        
    }

    private void startMainActivity() {
		// Register application.
        app.registerAppForGCM();
        
        AdRequest adRequest = new AdRequest();
        
        adView = (AdView) findViewById(R.id.adView);
        
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
        
        // Initialize app...
        if(app.isThisFirstUse()){
        	// This is the first run ! 
        	
        	String message = "Please wait while the application is initialized for first usage..." +
        			"\n" +
        			"This may take a couple of minutes depending upon your internet connection.";
    		dialog = ProgressDialog.show(Main.this, "", message, true);
    		app.initializeAppForFirstUse(this);
    		firstUse = true;
        }
        else{
        	firstUse = false;
        	app.initialize(this);
        	
        	// Start Main Activity.
        	startMainScreen();
        }

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	switch (requestCode) {
    	
    	case Application.EULA:
    		if(!app.isEULAAccepted()){
    			finish();
    		}
    		else{
    			// Start Main Activity
    			startMainActivity();
    		}
    		break;
    	
    	}
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()){
    	
    	case R.id.New:
    		Tracker.getInstance().trackPageView("/NewFeatures");
    		Intent newFeatures = new Intent(Main.this, DisplayFile.class);
    		newFeatures.putExtra("File", "NewFeatures.html");
    		newFeatures.putExtra("Title", "New Features: ");
			Main.this.startActivity(newFeatures);
    		break;
    		
    	case R.id.Help:
    		Tracker.getInstance().trackPageView("/Help");
    		Intent help = new Intent(Main.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			Main.this.startActivity(help);
    		break;
    		
    	case R.id.About:
    		Tracker.getInstance().trackPageView("/Help");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;
    	
    	}
    	
    	return true;
    }
    
	private void startMainScreen() {

		showWhatsNew();
		
		position = 0;
		
		// Find the ListView resource.   
		wv = (WebView) findViewById( R.id.webview );
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new PostJavaScriptInterface(), "Main");
		wv.setBackgroundColor(0);
		
		postList = app.getAllPosts();
		
		showPost(position);
		
		// Fling handling
		detector = new GestureDetector(this, new HanuGestureAnalyzer(this));
    	wv.setOnTouchListener(new OnTouchListener() {
	  	    public boolean onTouch(View view, MotionEvent e) {
	  	        detector.onTouchEvent(e);
	  	        return false;
	  	    }
	  	});
		
	}
	
	private void showWhatsNew() {
		// Show what's new in this version.
		int oldFrameworkVersion = app.getOldFrameworkVersion();
		int newFrameworkVersion = app.getNewFrameworkVersion();
		
		int oldAppVersion = app.getOldAppVersion();
		int newAppVersion;
		try {
			newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			newAppVersion = 0;
			Log.e(Application.TAG, e.getMessage(), e);
		}
		
		if(newAppVersion == 1){
			return;
		}
		
		if(newAppVersion > oldAppVersion ||
			newFrameworkVersion > oldFrameworkVersion){
			
			app.updateVersion();
			
			Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "NewFeatures.html");
			info.putExtra("Title", "What's New?");
			Main.this.startActivity(info);
			
		}
		
	}

	private void showPost(int pos) {
		// Show Post 
		if(postList.size() < 1){
			return;
		}
		
		Post post = postList.get(pos);
		
		wv.loadDataWithBaseURL("fake://not/needed", post.getHTMLCode(), "text/html", "UTF-8", "");
		
	}
	
	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		if(appClosing && result.getResultStatus() == ResultObject.ResultStatus.CANCELLED){
			app.close();
		}
		
		if(!result.isCommandExecutionSuccess()){
			
			if(result.getResultCode() == 420){
				// Application is not registered.
				String message = "This application is not registered with Hanu-Droid.\n" +
						"Please inform the developer about this error.";
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder
					.setTitle("Application not registered !")
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,int id) {
													Main.this.finish();	}})
					.create()
					.show();
			}
			
			Log.e(Application.TAG, result.getErrorMessage(), result.getException());
			/*	No need to show in real app..
			Toast.makeText(getApplicationContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
			//*/
		}
		
		if(firstUse){
			
			dialog.dismiss();
			
			// Start Main Activity.
			startMainScreen();
		}		
	}
	
	@Override
	protected void onDestroy(){
		appClosing = true;
		if (adView != null) {
			adView.destroy();
		}
		app.close();
		super.onDestroy();
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		// Nothing to do !
	}

	public void swipeRight() {
		// Show previous
		if(position == 0){
			position = postList.size() - 1;
		}
		else{
			position--;
		}
		
		showPost(position);
	}

	public void swipeLeft() {
		// Show Next
		if(position == postList.size() - 1){
			position = 0;
		}
		else{
			position++;
		}
		
		showPost(position);
	}

	@Override
	public void swipeUp() {
		// Nothing
	}

	@Override
	public void swipeDown() {
		// Nothing
	}
	
	class PostJavaScriptInterface{
		
		public void loadPosts(String taxonomy, String name){
			
			Tracker.getInstance().trackEvent("Clicks", "Hyperlink", taxonomy, 1);
			
			if(taxonomy.contentEquals("category")){
				postList = app.getPostsByCategory(name);
			}
			else if(taxonomy.contentEquals("post_tag")){
				postList = app.getPostsByTag(name);
			}
			else if(taxonomy.contentEquals("author")){
				postList = app.getPostsByAuthor(name);
			}
			
			position = 0;
			showPost(position);
		}
		
	}

}