package tk.skyrnet.tinker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class IntegratingFacebookTutorialApplication extends Application {

	static final String TAG = "MyApp";

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "1CYzOY4wtioVdMlVPLNKSuzWykXBd0rzApjwR7vR", "Qr3znHel8h6g7lFMGBcku6awyebqlMLICcg4Zww1");

		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));

	}
}
