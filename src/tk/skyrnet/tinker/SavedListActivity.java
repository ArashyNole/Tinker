// THIS IS A BETA! I DON'T RECOMMEND USING IT IN PRODUCTION CODE JUST YET

/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	
	private void startDetailsActivity() {
		Intent intent = new Intent(this, UserDetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}