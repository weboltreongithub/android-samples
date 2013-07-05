package com.sample_service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service {

	public class TestServiceBinder extends Binder {

		public TestService getInstance() {
			return TestService.this;
		}
	}

	private final IBinder binder = new TestServiceBinder();
	private List<NotificationItem> list = new ArrayList<NotificationItem>();
	private List<NotificationItem> syncList = Collections.synchronizedList(list);

	// Numero di volte dopo che prova e non trova pacchetti.
	// Si distrugge il servizio
	private int RETRY_TIMES = 2;

	// Intervallo entro il quale vengono elaborati e processati
	// i pacchetti.
	private int PERIOD_SECS = 5;

	private int retry ;

	@Override
	public void onCreate() {
		super.onCreate();
		log("onCreate()");

		this.retry = RETRY_TIMES;
		new Handler().postDelayed(process, PERIOD_SECS * 1000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log("onDestroy() - ("+syncList.size()+" items)");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("onStartCommand()");
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		log("onBind()");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		log("onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		log("onRebind()");
		super.onRebind(intent);
	}

	private void log(String msg)	{
		Log.d("SampleService", "TestService - " + msg);
	}

	public void push(NotificationItem notificationItem)
	{
		syncList.add(notificationItem);
	}

	private Runnable process = new Runnable()
	{
		@Override
		public void run()
		{
			// Verifico i pacchetti da elaborare. Dopo <N> volte
			// che non trovo pacchetti allora mi distruggo
			if (syncList.size() == 0) { 

				if (--retry == 0) {
					
					// Distruzione del servizio
					log("Nessun pacchetto per " + RETRY_TIMES + " volte. Distruzione.");
					TestService.this.stopSelf();
					
					return;

				} else {
					log("Nessun pacchetto da elaborare. Ancora " + retry + " tentativ" + (retry==1?"o":"i") + ".");
				}

			} else {

				// Se ci sono dei pacchetti da elaborare 
				// aggiorno il contatore dei retry
				retry = RETRY_TIMES;

				for (NotificationItem packet : syncList) {
					log("Elaborazione della Notifica [" + packet + "]");
				}

				// Pulizia della lista
				syncList.clear();
			}

			// Avvio di un nuovo timer per la prossima
			// elaborazione dei pacchetti.
			new Handler().postDelayed(process, PERIOD_SECS * 1000);
		}
	};
}