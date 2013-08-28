package org.dobots.communication.zmq;

import org.dobots.R;
import org.dobots.utilities.BaseActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class ZmqActivity extends BaseActivity {

	private static final int MENU_ZMQ_SETTINGS	= 100;
	
	protected ZmqHandler mZmqHandler;
	protected ZmqSettings mSettings;
	
	public abstract void ready();
	
	public abstract void failed();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mZmqHandler = ZmqHandler.getInstance();
		mSettings = mZmqHandler.getSettings();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, MENU_ZMQ_SETTINGS, Menu.NONE, getString(R.string.zmq_settings));
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_ZMQ_SETTINGS:
			ZmqHandler.getInstance().getSettings().showDialog(this);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    	case ZmqSettings.DIALOG_SETTINGS_ID:
    		return mZmqHandler.getSettings().onCreateDialog(this, id);
    	}
    	return null;
    }

	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case ZmqSettings.DIALOG_SETTINGS_ID:
    		mZmqHandler.getSettings().onPrepareDialog(id, dialog);
    	}
	}

}

