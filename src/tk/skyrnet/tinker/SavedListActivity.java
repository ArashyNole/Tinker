package tk.skyrnet.tinker;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;


public class SavedListActivity extends ListActivity {
	
	ArrayAdapter<String> adapter;
	ArrayList<String> al = new ArrayList<String>();
	@Override
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.savedusers);
		
		for (ParseUser u : UserDetailsActivity.userList) {
			try {
				al.add(u.getJSONObject("profile").get("name").toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		adapter = new ArrayAdapter<String>(this, R.layout.row, R.id.label, al);
  	    setListAdapter(adapter);
  	    registerForContextMenu(getListView());
	}
	
	public void onListItemClick(ListView parent, View v, int position, long id)
	{
		UserDetailsActivity.viewingUser = UserDetailsActivity.userList.get(position);
		startDetailsActivity();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    try {
			menu.setHeaderTitle(UserDetailsActivity.userList.get(info.position).getJSONObject("profile").get("name").toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      menu.add(Menu.NONE, 0, 0, "Remove");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ParseUser user = ParseUser.getCurrentUser();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		ParseRelation<ParseObject> relation = user.getRelation("saved");
		relation.remove(UserDetailsActivity.userList.get(info.position));
		user.saveInBackground();
		al.remove(info.position);
		UserDetailsActivity.userList.remove(info.position);
		adapter.notifyDataSetChanged();
		return true;
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
    		Toast.makeText(SavedListActivity.this, "My Profile", Toast.LENGTH_SHORT).show();
    		UserDetailsActivity.viewingUser = ParseUser.getCurrentUser();
    		startDetailsActivity();
            return true;
    	case R.id.userlist:
			Toast.makeText(SavedListActivity.this, "Saved User List", Toast.LENGTH_SHORT).show();
			return true;
    	case R.id.edit:
    		Toast.makeText(SavedListActivity.this, "Edit Profile", Toast.LENGTH_SHORT).show();
    		startEditActivity();
            return true;
		case R.id.find:
			Toast.makeText(SavedListActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
			findUser();
			return true;
		case R.id.chat:
			Toast.makeText(SavedListActivity.this, "Chat", Toast.LENGTH_SHORT).show();
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
	
}