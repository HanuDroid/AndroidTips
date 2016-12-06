package org.varunverma.androidtips;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.Post;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Main extends AppCompatActivity implements PostListFragment.Callbacks,
												PostDetailFragment.Callbacks {

	private boolean dualPane;
	private Application app;
	private ProgressDialog dialog;
	private boolean firstUse;
	private boolean appClosing;
	private HanuFragmentInterface fragmentUI;
	private int postId;
	private PostPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

        if(savedInstanceState != null){
        	postId = savedInstanceState.getInt("PostId");
        }
        else{
        	postId = 0;
        }
        
        if (findViewById(R.id.post_list) != null) {
            dualPane = true;
        }
		else{
			dualPane = false;
			FrameLayout postDetail = (FrameLayout) findViewById(R.id.post_detail);
	        if(postDetail != null){
	        	postDetail.setVisibility(View.GONE);
	        }
		}

        // Get Application Instance.
        app = Application.getApplicationInstance();

		// Start the Main Activity
		startMainScreen();

		// Show Swipe Help
		showSwipeHelp();

    }

	private void showSwipeHelp(){

		final LinearLayout swipeHelpLayout = (LinearLayout) findViewById(R.id.swipe_help);

		if(swipeHelpLayout == null){
			return;
		}

		String swipeHelp = app.getOptions().get("SwipeHelp");

		if(swipeHelp != null && swipeHelp.contentEquals("Skip")){
			// Skip the swipe help
			swipeHelpLayout.setVisibility(View.GONE);
		}
		else{

			final CheckBox showHelpAgain = (CheckBox) swipeHelpLayout.findViewById(R.id.show_again);

			Button dismissHelp = (Button) swipeHelpLayout.findViewById(R.id.dismiss_help);
			dismissHelp.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					// Hide the swipe help
					swipeHelpLayout.setVisibility(View.GONE);

					if(showHelpAgain.isChecked()){
						Application.getApplicationInstance().addParameter("SwipeHelp", "Skip");
					}
				}
			});

		}

	}

	private void startMainScreen() {

		showWhatsNew();

		// Show Ad.
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

		AdView adView = (AdView) findViewById(R.id.adView);

		// Start loading the ad in the background.
		adView.loadAd(adRequest);

		MyInterstitialAd.getInterstitialAd(this);
		MyInterstitialAd.requestNewInterstitial();

		// Load Posts.
		Application.getApplicationInstance().getAllPosts();

		// Create the Fragment.
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment;

		if (dualPane) {
			// Create Post List Fragment
			fragment = new PostListFragment();
			Bundle arguments = new Bundle();
			arguments.putInt("PostId", postId);
			fragment.setArguments(arguments);
			fm.beginTransaction().replace(R.id.post_list, fragment).commitAllowingStateLoss();
			
			fragmentUI = (HanuFragmentInterface) fragment;
			
		} else {
			
			// Create view Pager
			viewPager = (ViewPager) findViewById(R.id.post_pager);
			
			pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(), app.getPostList().size());
			viewPager.setAdapter(pagerAdapter);
			
		}
	
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
		
		if(app.isThisFirstUse()){
			showHelp();
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

	private void showHelp() {
		// Show Help
		Intent help = new Intent(Main.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		Main.this.startActivity(help);
	}

	@SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
        getMenuInflater().inflate(R.menu.main, menu);
        
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.Search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);

        return true;
    }
    
	@Override
	public void onItemSelected(int id) {
		
		if (dualPane) {
            Bundle arguments = new Bundle();
            arguments.putInt("PostId", id);
            PostDetailFragment fragment = new PostDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.post_detail, fragment)
                    .commit();

        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(fragmentUI != null && dualPane){
			outState.putInt("PostId", fragmentUI.getSelectedItem());
		}
		else if(!dualPane && viewPager != null){
			outState.putInt("PostId", viewPager.getCurrentItem());
		}
	}
	
	@Override
	protected void onDestroy(){
		appClosing = true;
		app.close();
		super.onDestroy();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
    	int id;
    	
    	switch (item.getItemId()){
    	
    	case R.id.Rate:
    		
    		if(dualPane){
    			id = fragmentUI.getSelectedItem();
    		}
    		else{
    			id = viewPager.getCurrentItem();
    		}
    		Intent rate = new Intent(Main.this, PostRating.class);
    		rate.putExtra("PostId", id);
			Main.this.startActivity(rate);
    		break;
    		
    	case R.id.Search:
    		onSearchRequested();
            return true;
    		    		
    	case R.id.Help:
    		showHelp();
    		break;
    		
    	case R.id.Share:
    		try{
        		if(dualPane){
        			id = fragmentUI.getSelectedItem();
        		}
        		else{
        			id = viewPager.getCurrentItem();
        		}
    			Post post = app.getPostList().get(id);
        		Intent send = new Intent(android.content.Intent.ACTION_SEND);
        		send.setType("text/plain");
        		send.putExtra(android.content.Intent.EXTRA_SUBJECT, post.getTitle());
        		send.putExtra(android.content.Intent.EXTRA_TEXT, post.getContent(true));
        		startActivity(Intent.createChooser(send, "Share with..."));
    		}catch(Exception e){
    			Log.e(Application.TAG, e.getMessage(), e);
    			finish();
    		}
    		break;
    		
    	case R.id.About:
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;
    	
    	}
    	
    	return true;
    }
        
	@Override
	public void loadPostsByCategory(String taxonomy, String name) {
		
		if(taxonomy.contentEquals("category")){
			app.getPostsByCategory(name);
		}
		else if(taxonomy.contentEquals("post_tag")){
			app.getPostsByTag(name);
		}
		else if(taxonomy.contentEquals("author")){
			app.getPostsByAuthor(name);
		}
		
		this.runOnUiThread(new Runnable() {
		    public void run(){
		    	if(dualPane){
		    		fragmentUI.reloadUI();
		    	}
		    	else{
		    		pagerAdapter.setNewSize(app.getPostList().size());
		    		pagerAdapter.notifyDataSetChanged();
		    		viewPager.setCurrentItem(0);
		    	}
		    }
		});
	}
	
	@Override
	public boolean isDualPane(){
		return dualPane;
	}

}