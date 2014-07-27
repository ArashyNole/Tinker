package tk.skyrnet.tinker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {

    //App key and app secret are in your Sinch dashboard
    //Explanatory screenshot below
    private static final String APP_KEY = "cb57e527-a556-4c41-8af2-37549d618f2e";
    private static final String APP_SECRET = "Nww345u9jEaC7Gha8mdkNw==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private final MessageServiceInterface serviceInterface =
        new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;

    //Gets called when you start the service from LoginActivity
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Get the current user id
        currentUserId = ParseUser.getCurrentUser().getObjectId().toString();
        Log.d("MessageService", "OnStartCommand");

        if (currentUserId != null && !isSinchClientStarted()) {
        	Log.d("MessageService",currentUserId);
            startSinchClient(currentUserId);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String userName) {
        sinchClient = Sinch.getSinchClientBuilder().context(this).userId(userName).applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();

        sinchClient.addSinchClientListener(this);

        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        messageClient = sinchClient.getMessageClient();

        sinchClient.checkManifest();
        sinchClient.start();
    }


    //To check if the Sinch client is already started
    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    //Do you want your app to do something if starting the client fails?
    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        sinchClient = null;
    }

    //Do you want your app to do something when the sinch client starts?
    @Override
    public void onClientStarted(SinchClient client) {
        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();
    }

    //Do you want your app to do something when the sinch client stops?
    @Override
    public void onClientStopped(SinchClient client) {
        sinchClient = null;
    }

    //Pretty self explanitory - stop the Sinch client
    public void stop() {
        if (isSinchClientStarted()) {
            sinchClient.stop();
            sinchClient.removeSinchClientListener(this);
        }
        sinchClient = null;
    }

    //Called when you bind this to an activity (I'll get there soon)
    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    @Override
    public void onLogMessage(int level, String area, String message) {
        //Intentionally left blank. You will get errors if you
        //try to remove this method.
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client,
                    ClientRegistration clientRegistration) {
        //Intentionally left blank. You will get errors if you
        //try to remove this method.
    }

    public void sendMessage(String recipientUserId, String textBody) {
        if (messageClient != null) {
            WritableMessage message =
                new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }

    //Methods that you can call from MessagingActivity
    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody) {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }
}