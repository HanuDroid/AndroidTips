package org.varunverma.androidtips;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.ayansh.hanudroid.Application;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private Application app;
	private int postIndex;
	private PostPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private DrawerLayout mDrawerLayout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		mDrawerLayout = findViewById(R.id.drawer_layout);

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setCheckedItem(R.id.AllPosts);
		navigationView.setNavigationItemSelectedListener(this);
		//navigationView.setItemIconTintList(null);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

		// Get Application Instance.
		app = Application.getApplicationInstance();
		app.setContext(this);

		// Load all posts, if we havent loaded before
		if(app.getPostList().isEmpty()){
			app.getAllPosts();
		}

		if(savedInstanceState != null){
        	postIndex = savedInstanceState.getInt("PostIndex");
        }
        else{
        	postIndex = 0;
		}

		// If title is provided.
		Intent intent = getIntent();
		if(intent != null && intent.getStringExtra("Title") != null){
			setTitle(intent.getStringExtra("Title"));
		}

		// Start the Main Activity
		startMainScreen();
    }

	private void startMainScreen() {

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

		MyInterstitialAd.getInterstitialAd(this);
		MyInterstitialAd.requestNewInterstitial();

		// Create view Pager
		viewPager = (ViewPager) findViewById(R.id.post_pager);

		viewPager.setClipToPadding(false);
		viewPager.setPageMargin(-50);

		pagerAdapter = new PostPagerAdapter(getSupportFragmentManager(),app.getPostList().size());
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(postIndex);
	
	}

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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){

			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				break;

			case R.id.Search:
				onSearchRequested();
				return true;

		}

		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		savedInstanceState.putInt("PostIndex", viewPager.getCurrentItem());
		super.onSaveInstanceState(savedInstanceState);

	}

	@Override
	protected void onDestroy(){
		app.close();
		super.onDestroy();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		//menuItem.setChecked(true);
		mDrawerLayout.closeDrawers();

		switch (menuItem.getItemId()){

			case R.id.AllPosts:
				// Load Posts.
				Application.getApplicationInstance().getAllPosts();
				setTitle(R.string.app_name);
				updateUI();
				break;

			case R.id.MyFavs:
				// Load Posts.
				app.getFavouritePosts();
				setTitle("Favourite Tips");
				updateUI();
				break;

			case R.id.Help:
				Intent help = new Intent(Main.this, DisplayFile.class);
				help.putExtra("ContentType", "File");
				help.putExtra("File", "help.html");
				help.putExtra("Title", "Help: ");
				Main.this.startActivity(help);
				break;

			case R.id.ShowEula:
				Intent eula = new Intent(Main.this, DisplayFile.class);
				eula.putExtra("ContentType", "File");
				eula.putExtra("File", "eula.html");
				eula.putExtra("Title", "Terms and Conditions: ");
				Main.this.startActivity(eula);
				break;

			case R.id.About:
				Intent info = new Intent(Main.this, DisplayFile.class);
				info.putExtra("ContentType", "File");
				info.putExtra("File", "about.html");
				info.putExtra("Title", "About: ");
				Main.this.startActivity(info);
				break;

			case R.id.MyApps:
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Ayansh+TechnoSoft+Pvt.+Ltd"));
				startActivity(browserIntent);
				break;
		}

		return true;
	}

	private void updateUI(){

		pagerAdapter.setNewSize(app.getPostList().size());
		pagerAdapter.notifyDataSetChanged();
		viewPager.setAdapter(pagerAdapter);
		if(app.getPostList().size() < 1){
			// Show warning
			Toast toast = Toast.makeText(this,"Posts for selected criteria not found",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER,0,0);
			toast.show();
		}
	}
}