/*
Search activity is same as Main activity
 */
package org.varunverma.androidtips;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Search extends AppCompatActivity {

	private Application app;
	private int postIndex;
	private PostPagerAdapter pagerAdapter;
	private ViewPager viewPager;

	private HanuFragmentInterface fragmentUI;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		MobileAds.initialize(this, "ca-app-pub-4571712644338430~3762977902");

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		// Show Ad.
		Bundle extras = new Bundle();
		extras.putString("max_ad_content_rating", "G");

		AdRequest adRequest = new AdRequest.Builder()
				.addNetworkExtrasBundle(AdMobAdapter.class, extras)
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("9F11CAC92EB404500CAA3F8B0BBA5277").build();

		AdView adView = (AdView) findViewById(R.id.adView);

		// Start loading the ad in the background.
		adView.loadAd(adRequest);

        // Get Application Instance.
        app = Application.getApplicationInstance();

		// Load Posts  that match search criteria !!
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	
          String query = intent.getStringExtra(SearchManager.QUERY);
          setTitle("Search results for: '" + query + "'");
          app.performSearch(query);
          
          // Save Query for future
          SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        		  SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
          
          suggestions.saveRecentQuery(query, null);
          
        }

		// Create view Pager
		viewPager = (ViewPager) findViewById(R.id.post_pager);

		viewPager.setClipToPadding(false);
		viewPager.setPageMargin(-50);

		pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
		viewPager.setAdapter(pagerAdapter);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
			case android.R.id.home:
				finish();
				return true;
				
			default:
	            return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	protected void onDestroy(){
		// This will load all posts.
		Application.getApplicationInstance().getPostsByAuthor(null);
		super.onDestroy();
	}

}