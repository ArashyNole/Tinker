package tk.skyrnet.tinker;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
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
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class UserDetailsActivity extends Activity implements OnClickListener {

	private static final int PROFILE_EDIT = 0;
	private static final int FIND_USER = 1;
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
	private String viewingFacebookId;

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
        

		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			//makeMeRequest();
			updateViewsWithProfileInfo();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// Check if the user is currently logged
			// and show any cached content
			buildProfile(currentUser.getJSONObject("profile"));
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}
	}
	
	private void buildProfile(JSONObject userProfile)
	{
		try {
			if (userProfile.getString("facebookId") != null) {
				String facebookId = userProfile.get("facebookId")
						.toString();
				viewingFacebookId = facebookId;
				userProfilePictureView.setProfileId(facebookId);
			} else {
				// Show the default, blank user profile picture
				userProfilePictureView.setProfileId(null);
				viewingFacebookId = "0";
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
		if (currentUser.get("profile") != null) {
			JSONObject userProfile = currentUser.getJSONObject("profile");
			buildProfile(userProfile);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, PROFILE_EDIT, 0, "Edit Profile");
		menu.add(0, FIND_USER, 1, "Find User");
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    	case PROFILE_EDIT:
    		startEditActivity();
            return true;
		case FIND_USER:
			findUser();
			return true;
    	}
        return false;
    }

	private void findUser() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("username", currentUser.getUsername());
		params.put("viewingFacebookId", viewingFacebookId);
		ParseCloud.callFunctionInBackground("getNewProfile", params, new FunctionCallback<HashMap<String, String>>() {
		   public void done(HashMap<String, String> hm, ParseException e) {
		       if (e == null) {
		    	  Log.d("UserDetailsActivity", hm.values().toString());
		    	  JSONObject profile = new JSONObject(hm);
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
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	findUser();
                	Toast.makeText(UserDetailsActivity.this, "Loading User", Toast.LENGTH_SHORT).show();

                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	findUser();
                    Toast.makeText(UserDetailsActivity.this, "User Saved", Toast.LENGTH_SHORT).show();
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
