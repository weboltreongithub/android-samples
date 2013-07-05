package com.sample_service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sample_service.TestService.TestServiceBinder;

public class MainActivity extends Activity {

	private ServiceConnection serviceConnection;
	private TestService service;
	private List<NotificationItem> queue = new ArrayList<NotificationItem>();
	private AtomicBoolean bound = new AtomicBoolean(false);
	private Object lock = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Effettua il binding / collegamento al servizio
				// nel caso questo non fosse collegato.
				Boolean binding_in_corso = bindServiceIfNeeded();
				if (binding_in_corso) {

					synchronized (lock)
					{
						// Il pacchetto viene mandato in un secondo momento quando
						// il binder sará pronto
						log("Accodo il pacchetto..");
						queue.add(new NotificationItem());
					}

				} else {

					// Invio immediato del pacchetto siccome 
					// il servizio é pronto
					log("Push immediato in corso..");
					service.push( new NotificationItem()  );
					log("Push immediato effettuato.");	
				}
			}
		});
	}

	private Boolean bindServiceIfNeeded()
	{
		Boolean result = false;
		if (!bound.get()) {

			// Start an indefinite service
			Intent intent = new Intent(MainActivity.this, TestService.class);
			startService(intent);

			// Create the channel to communicate
			serviceConnection = new ServiceConnection() {

				public void onServiceConnected(ComponentName className, IBinder binder) {

					bound.set( true );

					log("onServiceConnected()");
					service = ((TestServiceBinder)binder).getInstance();

					synchronized (lock) {
						
						for (NotificationItem n : queue) {

							// Invio dei pachetti in coda al servizio
							// che dovrebbe essere stato agganciato
							log("Push differito in corso..");
							service.push( n  );
							log("Push differito effettuato.");
						}

						// Pulizia della coda
						queue.clear();
					}

					// Mi sgancio dal servizio
					unbindService( serviceConnection );
					
					// Reset delle variabili per accedere al servizio
					service = null;
					bound.set( false );
				}

				// Questo evento viene chiamato solo in caso di 
				// sconnessione imprevista dal servizio.
				// Non va utilizzato per ascoltare l'unbind().
				public void onServiceDisconnected(ComponentName className) {
					log("onServiceDisconnected()");
				}
			};

			result = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
			log("Risultato del binder: " + result);
		}
		return result;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (service != null)
			unbindService(serviceConnection);
	}

	private void log(String msg)	{
		Log.d("SampleService", "MainActivity - " + msg);
	}
}
