package tk.skyrnet.tinker;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

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
		JSONObject userProfile = new JSONObject();
		
		try {
			userProfile.put("facebookId", facebookId);
			userProfile.put("name", userName);
			userProfile.put("location", userLocationView.getText().toString());
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
}
