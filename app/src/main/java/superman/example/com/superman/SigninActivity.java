package superman.example.com.superman;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;


public class SigninActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, View.OnClickListener {
    // Constants
    private static final int SIGNED_IN = 0;
    private static final int STATE_SIGNING_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 0;

    //Object references for API Client
    private GoogleApiClient mGoogleApiClient;
    private int mSignInProgress;
    private PendingIntent mSignInIntent;

    //References for the Views
    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //all views
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
        mStatus =(TextView) findViewById(R.id.statuslabel);

        //click listeners for the button
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);

        //google API Client
        mGoogleApiClient = buildGoogleAPIClient();
    }
    /*
    * Start the connection after the activity has started
    * and disconnect whenever the activity stops
    * */

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        /*
        * When the connection to the API is made, disable the sign in button
        * and enable the sign out one
        * */
        mSignInButton.setEnabled(false);
        mSignOutButton.setEnabled(true);
        mRevokeButton.setEnabled(true);

        //indicate the process of the sign in
        mSignInProgress = SIGNED_IN;
        /*
        * Get the user that has signed in using the app.
        * If not throw an exception and log it
        * */
        try{
            String emailAddress = Plus.AccountApi.getAccountName(mGoogleApiClient);
            mStatus.setText(String.format("Signed in as %s", emailAddress));
        }catch (Exception e){
            String exception = e.getLocalizedMessage();
            String exceptionString = e.toString();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(mSignInProgress != STATE_IN_PROGRESS){
            mSignInIntent = connectionResult.getResolution();
            if(mSignInProgress == STATE_SIGNING_IN){
                resolveSignInError();
            }
        }
        //when you've signed out
        onSignedOut();
    }

    private void resolveSignInError(){
        if(mSignInIntent != null){
            try{
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null
                0,0,0);
            }catch (IntentSender.SendIntentException e){
                mSignInProgress = STATE_SIGNING_IN;
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case RC_SIGN_IN:
                if(resultCode == RESULT_OK){
                    mSignInProgress = STATE_SIGNING_IN;
                }else {
                    mSignInProgress = SIGNED_IN;
                }

                if(!mGoogleApiClient.isConnecting()){
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    private void onSignedOut(){
        //update UI to reflect the changes first

    }

    /*
        * Setup the GoogleAPIClient using the BUilder
        *
        * */
    private GoogleApiClient buildGoogleAPIClient(){
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope("email"))
                .build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
        * disconnect when the activity stops
        * */
        mGoogleApiClient.disconnect();
    }
}
