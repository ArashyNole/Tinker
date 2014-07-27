package tk.skyrnet.tinker;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class ListUsersActivity extends ListActivity {
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
		Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
		intent.putExtra("RECIPIENT_ID", UserDetailsActivity.userList.get(position).getObjectId());
		startActivity(intent);
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
	
	private void startDetailsActivity() {
		Intent intent = new Intent(this, UserDetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
