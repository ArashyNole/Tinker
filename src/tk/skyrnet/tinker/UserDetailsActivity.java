package tk.skyrnet.tinker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class UserDetailsActivity extends Activity implements OnClickListener {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	private ProfilePictureView userProfilePictureView;
	private TextView userNameView;
	private TextView userLocationView;
	private TextView userGenderView;
	private TextView userDateOfBirthView;
	private TextView userRelationshipView;
	private TextView userDescription;
	private TextView userMusic;
	private TextView userMovies;
	private TextView userBooks;
	private TextView userTelevision;
	private ScrollView userScroll;
	private Button logoutButton;
	public static ParseUser viewingUser;
	public static ArrayList<ParseUser> userList = new ArrayList<ParseUser>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.userdetails);

		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		userNameView = (TextView) findViewById(R.id.userName);
		userLocationView = (TextView) findViewById(R.id.userLocation);
		userGenderView = (TextView) findViewById(R.id.userGender);
		userDateOfBirthView = (TextView) findViewById(R.id.userDateOfBirth);
		userRelationshipView = (TextView) findViewById(R.id.userRelationship);
		userDescription = (TextView) findViewById(R.id.userDescription);
		userMusic = (TextView) findViewById(R.id.userMusic);
		userMovies = (TextView) findViewById(R.id.userMovies);
		userBooks = (TextView) findViewById(R.id.userBooks);
		userTelevision = (TextView) findViewById(R.id.userTelevision);
		userScroll = (ScrollView) findViewById(R.id.scroll);

		logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLogoutButtonClicked();
			}
		});
		
        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        userScroll.setOnClickListener(UserDetailsActivity.this); 
        userScroll.setOnTouchListener(gestureListener);
        
        if (!ParseUser.getCurrentUser().getBoolean("viewedTut")) {
        	  AlertDialog.Builder builder = new AlertDialog.Builder(this);
              builder.setTitle(R.string.tutorial_title);
              builder.setMessage(R.string.tutorial_message);
              builder.setNegativeButton(R.string.tutorial_dismiss, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface arg0, int arg1) {
                	  ParseUser.getCurrentUser().put("viewedTut", true);
                	  ParseUser.getCurrentUser().saveInBackground();
                  }
              });
              builder.show();
        }
        Session session = ParseFacebookUtils.getSession();
		
		if (session != null && session.isOpened()) {
			//makeMeRequest();
			if (viewingUser == null)
			{
				viewingUser = ParseUser.getCurrentUser();
				updateViewsWithProfileInfo();
			}
			
			else
			{
				buildProfile(viewingUser.getJSONObject("profile"));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			buildProfile(viewingUser.getJSONObject("profile"));
		} else {
			startLoginActivity();
		}
	}
	
	private void buildProfile(JSONObject userProfile)
	{
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

	private void updateViewsWithProfileInfo() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		viewingUser = currentUser;
		
		if (currentUser.get("profile") != null) {
			JSONObject userProfile = currentUser.getJSONObject("profile");
			buildProfile(userProfile);
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
    		Toast.makeText(UserDetailsActivity.this, "My Profile", Toast.LENGTH_SHORT).show();
    		updateViewsWithProfileInfo();
            return true;
    	case R.id.userlist:
			Toast.makeText(UserDetailsActivity.this, "Saved User List", Toast.LENGTH_SHORT).show();
			ParseRelation<ParseObject> relation = ParseUser.getCurrentUser().getRelation("saved");
			userList.clear();
			
			relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
			    public void done(List<ParseObject> results, ParseException e) {
			      if (e != null) {
			        // There was an error
			      } else {
			    	  for (int i = 0; i < results.size(); ++i)
			    	  {
			    		Log.d("UserDetailsActivity", results.get(i).getJSONObject("profile").toString());
						userList.add((ParseUser) results.get(i));
			    	  }
			    	  
			    	  startSavedListActivity();
			      }
			    }
			});
			return true;
    	case R.id.edit:
    		Toast.makeText(UserDetailsActivity.this, "Edit Profile", Toast.LENGTH_SHORT).show();
    		startEditActivity();
            return true;
		case R.id.find:
			Toast.makeText(UserDetailsActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
			findUser();
			return true;
		case R.id.chat:
			Toast.makeText(UserDetailsActivity.this, "Chat", Toast.LENGTH_SHORT).show();
			return true;
		}
        return false;
    }

	private void findUser() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("username", currentUser.getUsername());
		params.put("viewingObjectId", viewingUser.getObjectId());
		ParseCloud.callFunctionInBackground("getNearbyProfile", params, new FunctionCallback<ParseUser>() {
		   public void done(ParseUser parseUser, ParseException e) {
		       if (e == null) {
		    	  viewingUser = parseUser;
		    	  Log.d("UserDetailsActivity", parseUser.getJSONObject("profile").toString());
		    	  JSONObject profile = parseUser.getJSONObject("profile");
		          buildProfile(profile);
		       }
		       else
		       {
		    	   Log.d("UserDetailsActivity", "Something went wrong!");
		       }
		   }
		});
		
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
	
	private void startSavedListActivity() {
		Intent intent = new Intent(this, SavedListActivity.class);
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
	

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {
                	findUser();
                	Toast.makeText(UserDetailsActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
                }
                
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                {
                	ParseUser user = ParseUser.getCurrentUser();
                	if (user.getObjectId() != viewingUser.getObjectId())
                	{
                		ParseRelation<ParseObject> relation = user.getRelation("saved");
                		relation.add(viewingUser);
                		user.saveInBackground();
                		findUser();
                		Toast.makeText(UserDetailsActivity.this, "User Saved", Toast.LENGTH_SHORT).show();
                	}
                	else
                	{
                		findUser();
                    	Toast.makeText(UserDetailsActivity.this, "Loading User", Toast.LENGTH_SHORT).show();
                	}
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

            @Override
        public boolean onDown(MotionEvent e) {
              return true;
        }
    }
    
    public void onClick(View v) {
        Toast.makeText(UserDetailsActivity.this, "onClick", Toast.LENGTH_SHORT).show();
    }
}
