package tk.skyrnet.tinker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import tk.skyrnet.tinker.MessageService.MessageServiceInterface;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

public class MessagingActivity extends Activity implements
ServiceConnection, MessageClientListener {
	
	private String recipientId;
    private Button sendButton;
    private String messageBody;
    private EditText messageBodyField;
	private MessageServiceInterface messageService;
	private ListView messagesList;
	private MessageAdapter messageAdapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        doBind();
        
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        
        sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        
        messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        messagesList = (ListView) findViewById(R.id.listMessages);
        
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
    }
    
    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
			messageBody = "<a href='tk.skyrnet.tinker://UserDetailsActivity/"
						+ ParseUser.getCurrentUser().getObjectId() + "'>"
                        + ParseUser.getCurrentUser().getJSONObject("profile").get("name")
                        + "</a> " + messageBody;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        messageService.sendMessage(recipientId, messageBody);
    }
    
    private void doBind() {
        Intent serviceIntent = new Intent(this, MessageService.class);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }
    
    @Override
    public void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }
    
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        //Define the messaging service and add a listener
        messageService = (MessageService.MessageServiceInterface) iBinder;
        messageService.addMessageClientListener(this);

        //Notify the user if they are not connected to the Sinch client.
        //Otherwise, for example, if your app key & secret are typed
        //in wrong, the user might keep hitting the send button
        //with no feedback
        if (!messageService.isSinchClientStarted()) {
            Toast.makeText(this, "The message client did not start."
            ,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        messageService = null;
    }

    @Override
    public void onMessageDelivered(MessageClient client,
                                  MessageDeliveryInfo deliveryInfo) {
        //Intentionally  left blank
    }

    @Override
    public void onMessageFailed(MessageClient client, Message message,
                                MessageFailureInfo failureInfo) {
        //Notify the user if message fails to send
        Toast.makeText(this, "Message failed to send.", Toast.LENGTH_LONG).show();
        Log.d("MessagingActivity", "Failed to send because: "+failureInfo.getSinchError().getMessage());
    }

    @Override
    public void onIncomingMessage(MessageClient client, Message message) {
    	messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
    }

    @Override
    public void onMessageSent(MessageClient client, Message message,
                             String recipientId) {
    	messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
    }

    @Override
    public void onShouldSendPushData(MessageClient client, Message message,
                                    List<PushPair> pushPairs) {
        //Intentionally left blank
    }
    
    public class MessageAdapter extends BaseAdapter {
    	//For every message, define a corresponding integer (0 or 1)
    	//that describes if the message is incoming or outgoing
    	private List<Pair<Message, Integer>> messages;
    	private LayoutInflater layoutInflater;
    	public static final int DIRECTION_INCOMING = 0;
    	public static final int DIRECTION_OUTGOING = 1;

    	public MessageAdapter(Activity activity) {
    	    layoutInflater = activity.getLayoutInflater();
    	    messages = new ArrayList<Pair<Message, Integer>>();
    	}

    	//Gets called every time you update the view with an
    	//incoming or outgoing message
    	public void addMessage(Message message, int direction) {
    	    messages.add(new Pair<Message, Integer>(message, direction));
    	    notifyDataSetChanged();
    	}

    	//Returns how many messages are in the list
    	@Override
    	public int getCount() {
    	    return messages.size();
    	}

    	@Override
    	public Object getItem(int i) {
    	    return messages.get(i);
    	}

    	@Override
    	public long getItemId(int i) {
    	    return i;
    	}

    	@Override
    	public View getView(int i, View convertView, ViewGroup viewGroup) {
    		int direction = messages.get(i).second;
    		//Log.d("n", Integer.toString(messages.get(i).second));

    		//if (convertView == null) {
    		    int res = 0;
    		    if (direction == DIRECTION_INCOMING) {
    		        res = R.layout.message_left;
    		    } else if (direction == DIRECTION_OUTGOING) {
    		        res = R.layout.message_right;
    		    }
    		    convertView = layoutInflater.inflate(res, viewGroup, false);
    		//}
    		
    		Message message = messages.get(i).first;

    		TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
    		txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
    		txtMessage.setText(Html.fromHtml(message.getTextBody()));

    		return convertView;
    	}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//menu.add(0, PROFILE_EDIT, 0, "Edit Profile");
		//menu.add(0, FIND_USER, 1, "Find User");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
	}
	
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    	case R.id.profile:
    		Toast.makeText(MessagingActivity.this, "My Profile", Toast.LENGTH_SHORT).show();
    		UserDetailsActivity.viewingUser = ParseUser.getCurrentUser();
    		startDetailsActivity();
            return true;
    	case R.id.userlist:
			Toast.makeText(MessagingActivity.this, "Saved User List", Toast.LENGTH_SHORT).show();
			startSavedListActivity();
			return true;
    	case R.id.edit:
    		Toast.makeText(MessagingActivity.this, "Edit Profile", Toast.LENGTH_SHORT).show();
    		startEditActivity();
            return true;
		case R.id.find:
			Toast.makeText(MessagingActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
			findUser();
			return true;
		case R.id.chat:
			Toast.makeText(MessagingActivity.this, "Chat", Toast.LENGTH_SHORT).show();
    		Intent serviceIntent = new Intent(getApplicationContext(),
    	            MessageService.class);
    		startService(serviceIntent);
    		Intent intent = new Intent(getApplicationContext(), ListUsersActivity.class);
    		startActivity(intent);
			return true;
		}
        return false;
    }
    
	private void findUser() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("username", currentUser.getUsername());
		params.put("viewingObjectId", UserDetailsActivity.viewingUser.getObjectId());
		ParseCloud.callFunctionInBackground("getNearbyProfile", params, new FunctionCallback<ParseUser>() {
		   public void done(ParseUser parseUser, ParseException e) {
		       if (e == null) {
		    	  UserDetailsActivity.viewingUser = parseUser;
		    	  Log.d("UserDetailsActivity", parseUser.getJSONObject("profile").toString());
		    	  startDetailsActivity();
		       }
		       else
		       {
		    	   Log.d("UserDetailsActivity", "Something went wrong!");
		       }
		   }
		});
		
	}
    
    private void startDetailsActivity() {
		Intent intent = new Intent(this, UserDetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
    
	private void startEditActivity() {
		Intent intent = new Intent(this, UserEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	private void startSavedListActivity() {
		Intent intent = new Intent(this, SavedListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}