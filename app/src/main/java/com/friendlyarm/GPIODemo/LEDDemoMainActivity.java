package com.friendlyarm.GPIODemo;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.tiny4412_test2.R;
import com.friendlyarm.AndroidSDK.GPIOEnum;
import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.Utils.SystemProperties;

public class LEDDemoMainActivity extends Activity {
	
	private static final String TAG = "GPIODemo";
	private Timer timer = new Timer();
	private int step = 0; 
	private int led_gpio_base;
	private int positive = 0;
	private int negative  = 1;

	
	static int STEP_INIT_GPIO_DIRECTION = 1;
	static int STEP_CLOSE_ALL_LED = 2;
	static int STEP_INIT_VIEW = 3;
	
	@Override
		public void onDestroy() {
			timer.cancel();
			super.onDestroy();
		}


	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					timer.cancel();
					// Generate list View from ArrayList
					displayListView();
					break;
			}
			super.handleMessage(msg);
		}
	};

	private TimerTask init_task = new TimerTask() {
		public void run() {
			Log.d(TAG, "init_task " + step);
			if (step == STEP_INIT_GPIO_DIRECTION) {
				if (HardwareControler.setGPIODirection(led_gpio_base+1, GPIOEnum.IN) == 0) {
				} else {
					Log.v("TimerTask", String.format("setGPIODirection(%d) failed", led_gpio_base+1));
				}
				if (HardwareControler.setGPIODirection(led_gpio_base+13, GPIOEnum.OUT) == 0) {
				} else {
					Log.v("TimerTask", String.format("setGPIODirection(%d) failed", led_gpio_base+13));
				}
				if (HardwareControler.setGPIODirection(led_gpio_base+25, GPIOEnum.OUT) == 0) {
				} else {
					Log.v("TimerTask", String.format("setGPIODirection(%d) failed", led_gpio_base+25));
				}
				step ++;
			}
			else if (step == STEP_CLOSE_ALL_LED) {
				if (HardwareControler.setGPIOValue(led_gpio_base+13, GPIOEnum.HIGH) == 0) {
				} else {
					Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+13));
				}
				if (HardwareControler.setGPIOValue(led_gpio_base+25, GPIOEnum.LOW) == 0) {
				} else {
					Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base + 25));
				}
				step ++;
			} else if (step == STEP_INIT_VIEW) {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}

		}
	}; 
	//////////////////////////////////////

	MyCustomAdapter dataAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leddemo_main);
		setTitle("My test Demo");

		if (SystemProperties.get("ro.build.version.release").contains("4.1.2")) {
			led_gpio_base = 281;
			Log.d(TAG, String.format("Android4.1.2?  led_gpio_base=%d",led_gpio_base) );
		} else {
			led_gpio_base = 79;
			Log.d(TAG, String.format("Android4.2.2?  led_gpio_base=%d",led_gpio_base) );
		}

        int pin = led_gpio_base + 1;

        if (HardwareControler.exportGPIOPin(pin) == 0) {
        } else {
            Toast.makeText(this, String.format("exportGPIOPin(%d) failed!", pin),
                    Toast.LENGTH_SHORT).show();
        }

		step = STEP_INIT_GPIO_DIRECTION;
		timer.schedule(init_task, 100, 2000);
	}

	private void displayListView() {
		Log.d(TAG, "displayListView");

		ArrayList<LED> ledList = new ArrayList<LED>();
		LED led = new LED(0, "LED 1", false);
		ledList.add(led);
		led = new LED(1, "LED 2", false);
		ledList.add(led);
		led = new LED(2, "LED 3", false);
		ledList.add(led);
		led = new LED(3, "LED 4", false);
		ledList.add(led);

		dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_listview_item,
				ledList);
		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(dataAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LED led = (LED) parent.getItemAtPosition(position);
				if (HardwareControler.setGPIOValue(led_gpio_base+led.code,
					led.isSelected()?GPIOEnum.LOW:GPIOEnum.HIGH) == 0) {
				} else {
					Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+led.code));
				}
			}
		});

	}

	private class MyCustomAdapter extends ArrayAdapter<LED> {
		private ArrayList<LED> ledList;
		public MyCustomAdapter(Context context, int textViewResourceId,
				ArrayList<LED> ledList) {
			super(context, textViewResourceId, ledList);
			this.ledList = new ArrayList<LED>();
			this.ledList.addAll(ledList);
		}

		private class ViewHolder {
			CheckBox name;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			Log.v(TAG, String.valueOf(position));

			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.checkbox_listview_item, null);

				holder = new ViewHolder();
				holder.name = (CheckBox) convertView
						.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						LED led = (LED) cb.getTag();
						led.setSelected(cb.isChecked());
						if (HardwareControler.setGPIOValue(led_gpio_base+led.code,
								led.isSelected() ? GPIOEnum.LOW:GPIOEnum.HIGH) == 0) {
						} else {
							Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+led.code));
						}
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			LED led = ledList.get(position);
			holder.name.setText(led.getName());
			holder.name.setChecked(led.isSelected());
			holder.name.setTag(led);

			return convertView;

		}

	}
}
