package org.varunverma.androidtips;

import org.varunverma.hanu.Application.HanuGCMIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class GCMIntentService extends HanuGCMIntentService {
	
	@Override
	protected void onError(Context context, String errorId) {
		super.onError(context, errorId);

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		super.onMessage(context, intent);
		createNotification();
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		super.onRegistered(context, regId);
		
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		super.onUnregistered(context, regId);

	}

	private void createNotification() {
		// Create Notification
			
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "New tips are available";
		notification.when = System.currentTimeMillis();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
			
		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "New tips available", "New tips are available", contentIntent);
				
		nm.notify(1, notification);
				
	}
	
}