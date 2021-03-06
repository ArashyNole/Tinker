package tk.skyrnet.tinker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.PushService;

public class UserEditActivity extends Activity {

	private ProfilePictureView userProfilePictureView;
	private TextView userNameView;
	private EditText userLocationView;
	private EditText userGenderView;
	private EditText userDateOfBirthView;
	private EditText userRelationshipView;
	private EditText userDescription;
	private EditText userMusic;
	private EditText userMovies;
	private EditText userBooks;
	private EditText userTelevision;
	private Button saveButton;
    private String facebookId;
    private String userName;
    
	public static ArrayList<ParseUser> userList = new ArrayList<ParseUser>();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.useredit);

		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		userNameView = (TextView) findViewById(R.id.userName);
		userLocationView = (EditText) findViewById(R.id.userLocation);
		userGenderView = (EditText) findViewById(R.id.userGender);
		userDateOfBirthView = (EditText) findViewById(R.id.userDateOfBirth);
		userRelationshipView = (EditText) findViewById(R.id.userRelationship);
		userDescription = (EditText) findViewById(R.id.userDescription);
		userMusic = (EditText) findViewById(R.id.userMusic);
		userMovies = (EditText) findViewById(R.id.userMovies);
		userBooks = (EditText) findViewById(R.id.userBooks);
		userTelevision = (EditText) findViewById(R.id.userTelevision);

		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveButtonClicked();
			}
		});
		
		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
		}
		
		PushService.setDefaultPushCallback(this, UserDetailsActivity.class);

	}

	@Override
	public void onResume() {
		super.onResume();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// Check if the user is currently logged
			// and show any cached content
		//	updateViewsWithProfileInfo();
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
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
	
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	ParseRelation<ParseObject> relation;
    	
        switch (item.getItemId()) {
    	case R.id.profile:
    		Toast.makeText(UserEditActivity.this, "My Profile", Toast.LENGTH_SHORT).show();
    		UserDetailsActivity.viewingUser = ParseUser.getCurrentUser();
    		startDetailsActivity();
            return true;
    	case R.id.userlist:
			Toast.makeText(UserEditActivity.this, "Saved User List", Toast.LENGTH_SHORT).show();
			relation = ParseUser.getCurrentUser().getRelation("saved");
			UserDetailsActivity.userList.clear();
			
			relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			    public void done(List<ParseObject> results, ParseException e) {
			      if (e != null) {
			        // There was an error
			      } else {
			    	  for (int i = 0; i < results.size(); ++i)
			    	  {
			    		Log.d("UserDetailsActivity", results.get(i).getJSONObject("profile").toString());
						UserDetailsActivity.userList.add((ParseUser) results.get(i));
			    	  }
			    	  
			    	  startSavedListActivity();
			      }
			    }
			});
			return true;
    	case R.id.edit:
    		Toast.makeText(UserEditActivity.this, "Edit Profile", Toast.LENGTH_SHORT).show();
            return true;
		case R.id.find:
			Toast.makeText(UserEditActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
			findUser();
			return true;
		case R.id.chat:
			Toast.makeText(UserEditActivity.this, "Chat", Toast.LENGTH_SHORT).show();
			relation = ParseUser.getCurrentUser().getRelation("saved");
			UserDetailsActivity.userList.clear();
			
			relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			    public void done(List<ParseObject> results, ParseException e) {
			      if (e != null) {
			        // There was an error
			      } else {
			    	  for (int i = 0; i < results.size(); ++i)
			    	  {
			    		Log.d("UserDetailsActivity", results.get(i).getJSONObject("profile").toString());
						UserDetailsActivity.userList.add((ParseUser) results.get(i));
			    	  }
			    	  
			    		Intent serviceIntent = new Intent(getApplicationContext(),
			    	            MessageService.class);
			    		startService(serviceIntent);
			    		Intent intent = new Intent(getApplicationContext(), ListUsersActivity.class);
			    		startActivity(intent);
			      }
			    }
			});
			return true;
		}
        return false;
    }

	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							// Populate the JSON object
							facebookId = user.getId();
							userName = user.getName();
							updateViewsWithProfileInfo();

						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d(IntegratingFacebookTutorialApplication.TAG,
										"The facebook session was invalidated.");
								onLogoutButtonClicked();
							} else {
								Log.d(IntegratingFacebookTutorialApplication.TAG,
										"Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}

	private void updateViewsWithProfileInfo() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser.get("profile") != null) {
			JSONObject userProfile = currentUser.getJSONObject("profile");
			try {
				if (userProfile.getString("facebookId") != null) {
					String facebookId = userProfile.get("facebookId")
							.toString();
					userProfilePictureView.setProfileId(facebookId);
				} else {
					// Show the default, blank user profile picture
					userProfilePictureView.setProfileId(null);
				}
				if (userProfile.getString("name") != null) {
					userNameView.setText(userProfile.getString("name"));
				} else {
					userNameView.setText("");
				}
				if (userProfile.getString("location") != null) {
					userLocationView.setText(userProfile.getString("location"));
				} else {
					userLocationView.setText("");
				}
				if (userProfile.getString("gender") != null) {
					userGenderView.setText(userProfile.getString("gender"));
				} else {
					userGenderView.setText("");
				}
				if (userProfile.getString("birthday") != null) {
					userDateOfBirthView.setText(userProfile
							.getString("birthday"));
				} else {
					userDateOfBirthView.setText("");
				}
				if (userProfile.getString("relationship_status") != null) {
					userRelationshipView.setText(userProfile
							.getString("relationship_status"));
				} else {
					userRelationshipView.setText("");
				}
				
				if (userProfile.getString("description") != null) {
					userDescription.setText(userProfile.getString("description"));
				} else {
					userDescription.setText("");
				}
				if (userProfile.getString("music") != null) {
					userMusic.setText(userProfile.getString("music"));
				} else {
					userMusic.setText("");
				}
				if (userProfile.getString("movies") != null) {
					userMovies.setText(userProfile.getString("movies"));
				} else {
					userMovies.setText("");
				}
				if (userProfile.getString("books") != null) {
					userBooks.setText(userProfile
							.getString("books"));
				} else {
					userBooks.setText("");
				}
				if (userProfile.getString("television") != null) {
					userTelevision.setText(userProfile
							.getString("television"));
				} else {
					userTelevision.setText("");
				}
			} catch (JSONException e) {
				Log.d(IntegratingFacebookTutorialApplication.TAG,
						"Error parsing saved user data.");
			}

		}
	}

	private void onSaveButtonClicked() {
		new MyTask().execute(userLocationView.getText().toString());

	}
	
	private void onLogoutButtonClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	private void startDetailsActivity() {
		Intent intent = new Intent(this, UserDetailsActivity.class);
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
	
	// Found at this address:
	// https://stackoverflow.com/questions/5205650/geocoder-getfromlocation-throws-ioexception-on-android-emulator
	public static JSONObject getLocationInfo(String address) {
	    StringBuilder stringBuilder = new StringBuilder();
	    try {

	    address = address.replaceAll(" ","%20");    

	    HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
	    HttpClient client = new DefaultHttpClient();
	    HttpResponse response;
	    stringBuilder = new StringBuilder();


	        response = client.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        InputStream stream = entity.getContent();
	        int b;
	        while ((b = stream.read()) != -1) {
	            stringBuilder.append((char) b);
	        }
	    } catch (ClientProtocolException e) {
	    } catch (IOException e) {
	    }

	    JSONObject jsonObject = new JSONObject();
	    try {
	        jsonObject = new JSONObject(stringBuilder.toString());
	    } catch (JSONException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }

	    return jsonObject;
	}
	
	private class MyTask extends AsyncTask<String, Void, JSONObject> {

		   @Override
		   protected JSONObject doInBackground(String... urls) {
			   return getLocationInfo(urls[0]);
		   }

		   @Override
		   protected void onPostExecute(JSONObject jsonObject) {

				JSONObject userProfile = new JSONObject();
				
				try {		
					userProfile.put("facebookId", facebookId);
					userProfile.put("name", userName);
					userProfile.put("location", userLocationView.getText().toString());
					userProfile.put("lat", ((JSONArray)jsonObject.get("results")).getJSONObject(0)
		                .getJSONObject("geometry").getJSONObject("location")
		                .getDouble("lat"));
					userProfile.put("lng", ((JSONArray)jsonObject.get("results")).getJSONObject(0)
			            .getJSONObject("geometry").getJSONObject("location")
			            .getDouble("lng"));	
					userProfile.put("gender", userGenderView.getText().toString());
					userProfile.put("relationship_status", userRelationshipView.getText().toString());
					userProfile.put("birthday", userDateOfBirthView.getText().toString());
					userProfile.put("description", userDescription.getText().toString());
					userProfile.put("music", userMusic.getText().toString());
					userProfile.put("movies", userMovies.getText().toString());
					userProfile.put("books", userBooks.getText().toString());
					userProfile.put("television", userTelevision.getText().toString());
				}
				catch (JSONException e) {
					Log.d(IntegratingFacebookTutorialApplication.TAG,
							"Error parsing saved user data.");
				}
				
				// Save the user
				ParseUser currentUser = ParseUser
						.getCurrentUser();
				currentUser.put("profile", userProfile);
				currentUser.saveInBackground();

				// Go to the login view
				startDetailsActivity();
			   
		   }
		}
}
