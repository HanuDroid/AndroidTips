/**
 * 
 */
package org.varunverma.androidtips;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.Post;

/**
 * @author Varun
 *
 */
public class PostRating extends Activity implements OnRatingBarChangeListener {

	private RatingBar ratingBar;
	Post post;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.rate);

		Intent intent = getIntent();
		int postIndex = intent.getIntExtra("PostIndex", 0);

		try{

			Application app = Application.getApplicationInstance();
			post = app.getPostList().get(postIndex);

			setTitle("Rate this post:");

			ratingBar = (RatingBar) findViewById(R.id.ratingbar);
			ratingBar.setNumStars(5);
			ratingBar.setRating(post.getMyRating());
			ratingBar.setOnRatingBarChangeListener(this);

			Bundle bundle = new Bundle();
			bundle.putString("post_id", post.getTitle());
			app.getFirebaseAnalytics().logEvent("post_rating", bundle);

		}catch (Exception e){
			Log.e(Application.TAG, e.getMessage(), e);
			finish();
		}
	}
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onRatingChanged(RatingBar view, float rating, boolean fromUser) {
		// Rating was changed
		if(fromUser){
			post.addRating(rating);
		}
		
		// Close activity after the rating is finished
		finish();
		
	}
	
}