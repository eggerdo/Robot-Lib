package org.dobots.communication.zmq;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class ZmqActivity extends BaseActivity {
	
	protected ZmqHandler mZmqHandler;
	protected ZmqSettings mSettings;
	
	public abstract void ready();
	
	public abstract void failed();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mZmqHandler = ZmqHandler.getInstance();
		mSettings = mZmqHandler.getSettings();
		addMenuListener(mSettings);
		addDialogListener(mSettings);
	}
	
}

