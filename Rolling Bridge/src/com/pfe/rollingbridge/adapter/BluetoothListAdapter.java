package com.pfe.rollingbridge.adapter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.pfe.rollingbridge.R;
import com.pfe.rollingbridge.RollingBridgeApplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BluetoothListAdapter extends BaseAdapter {

	Context mContext;
	LinkedHashMap<String, String> mData;
	private static LayoutInflater mInflater = null;
	private boolean clearData = false;
	
	public BluetoothListAdapter(Context context, LinkedHashMap<String, String> data) {
		mContext = context;
		if(data != null)
			mData = data;
		else {
			mData = new LinkedHashMap<String, String>();
			mData.put("No Device Found", "Try to launch new search");
			clearData = true;
		}
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Entry<String, String> getItem(int position) {
		Iterator<Entry<String, String>> ite = mData.entrySet().iterator();
		int i = 0;
		while(ite.hasNext()) {
			Entry<String, String> entry = (Entry<String, String>) ite.next();
			if(i == position)
				return entry;
			i++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null)
			view = mInflater.inflate(R.layout.bluetoothrow, null);
		
		TextView btname = (TextView) view.findViewById(R.id.BTname);
		TextView btaddr = (TextView) view.findViewById(R.id.BTaddr);
		
		btname.setTypeface(RollingBridgeApplication.getTypeFace("roboto"));
		btname.setSelected(true);
		btname.requestFocus();
		
		btaddr.setTypeface(RollingBridgeApplication.getTypeFace("roboto"));
		
		Entry<String, String> entry = getItem(position);
		btname.setText(entry.getKey());
		btaddr.setText(entry.getValue());
		
		return view;
	}
	
	public void addItem(String name, String addr) {
		
		if(clearData)
			clearData();
		
		mData.put(name, addr);
		notifyDataSetChanged();
	}

	public void clearData() {
		mData.clear();
		clearData = !clearData;
		notifyDataSetChanged();
	}
}
