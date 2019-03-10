package org.varunverma.androidtips;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ayansh.hanudroid.Application;
import com.ayansh.hanudroid.HanuFragmentInterface;
import com.ayansh.hanudroid.Post;
import com.ayansh.hanudroid.PostComment;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

public class PostDetailFragment extends Fragment implements View.OnClickListener{

	private Post post;
	private WebView wv;
	private Application app;
	private int postIndex;
	private ImageButton postFav;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		app = Application.getApplicationInstance();
		
		if(app.getPostList().isEmpty()){
			return;
		}
		
		if(getArguments() != null){
			if (getArguments().containsKey("postIndex")) {
				postIndex = getArguments().getInt("postIndex");
	        	if(postIndex >= app.getPostList().size()){
					postIndex = app.getPostList().size() - 1;	// index is 0 based
	        	}
        		post = app.getPostList().get(postIndex);
	        }
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.post_detail, container, false);

		if(post == null){
			return rootView;
		}

		RelativeLayout rLL = rootView.findViewById(R.id.ll);
		rLL.setBackgroundColor(getRandomColorForBackground());

		// Set title
		TextView tv_post_title = rootView.findViewById(R.id.post_title);
		tv_post_title.setText(post.getTitle());

		// Set Content
		wv = (WebView) rootView.findViewById(R.id.webview);
		
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		wv.setBackgroundColor(Color.TRANSPARENT);
		//wv.setBackgroundColor(getRandomColorForBackground());
		//wv.addJavascriptInterface(new PostJavaScriptInterface(), "Main");
		wv.loadDataWithBaseURL("fake://not/needed", getPostHTML(), "text/html", "UTF-8", "");

		// Set Metadata
		TextView tv_post_meta = rootView.findViewById(R.id.post_meta);
		tv_post_meta.setText(getMetaText());

		// Button Actions
		postFav = rootView.findViewById(R.id.Favourite);
		setFavoutieIcon();
		postFav.setOnClickListener(this);

		ImageButton waShare = rootView.findViewById(R.id.WAShare);
		waShare.setOnClickListener(this);
		waShare.setVisibility(View.GONE);

		ImageButton Share = rootView.findViewById(R.id.Share);
		Share.setOnClickListener(this);

		ImageButton postRate = rootView.findViewById(R.id.Rate);
		postRate.setOnClickListener(this);

		ImageButton fullScreen = rootView.findViewById(R.id.FullScreen);
		fullScreen.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			if(post == null){
				post = Application.getApplicationInstance().getPostList().get(0);
			}
			if(post != null){
				post.incrementViewCount(1);
			}
		}
	}

	private String getPostHTML() {

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

	private CharSequence getMetaText() {

		SimpleDateFormat df = new SimpleDateFormat();

		String metaText = "Published On: " + df.format(post.getPublishDate());

		// Ratings
		if (post.getMetaData().size() > 0
				&& !post.getMetaData().get("ratings_users").contentEquals("0")) {

			metaText += "\n" + "Rating: "
					+ String.format("%.2g", Float.valueOf(post.getMetaData().get("ratings_average")))
					+ "/5 (by " + post.getMetaData().get("ratings_users") + " users)";
		}

		return metaText;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()){
			case R.id.WAShare:
				shareContent("WhatsApp");
				break;

			case R.id.Share:
				shareContent("Normal");
				break;

			case R.id.Rate:
				Intent rate = new Intent(getActivity(), PostRating.class);
				rate.putExtra("PostIndex", postIndex);
				startActivity(rate);
				break;

			case R.id.Favourite:
				post.toggleFavourite();
				setFavoutieIcon();
				break;

			case R.id.FullScreen:
				Intent showPost = new Intent(getActivity(), ShowPostFullScreen.class);
				showPost.putExtra("PostID", post.getId());
				startActivity(showPost);
				break;
		}
	}

	private int getRandomColorForBackground() {

		Random r = new Random();
		int randomNo = r.nextInt(7);
		int colorId;

		switch (randomNo){
			case 0:
				colorId = R.color.colorPurple;
				break;

			case 1:
				colorId = R.color.colorIndigo;
				break;

			case 2:
				colorId = R.color.colorPrimary;
				break;

			case 3:
				colorId = R.color.colorLime;
				break;

			case 4:
				colorId = R.color.colorAccent;
				break;

			case 5:
				colorId = R.color.colorBrown;
				break;

			case 6:
				colorId = R.color.colorGray;
				break;

			default:
				colorId = R.color.colorPurple;
				break;
		}

		return getResources().getColor(colorId);
	}

	private void setFavoutieIcon(){

		if(post.isFavourite()){
			postFav.setImageResource(R.drawable.ic_favorite);
		}
		else{
			postFav.setImageResource(R.drawable.ic_favorite_empty);
		}

	}

	private void shareContent(String sharingApp){

		String post_content = post.getContent(true);
		post_content += "\n\n Full content: ayansh.com/at?id=" + post.getId();
		Intent send = new Intent(Intent.ACTION_SEND);
		send.setType("text/plain");
		send.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
		send.putExtra(Intent.EXTRA_TEXT, post_content);

		if(sharingApp.contentEquals("WhatsApp")){
			send.setPackage("com.whatsapp");
		}

		startActivity(Intent.createChooser(send, "Share with..."));

		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, post.getTitle());
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "post_share");
		app.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);


	}

}