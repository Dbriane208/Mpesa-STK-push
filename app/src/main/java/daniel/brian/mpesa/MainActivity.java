package daniel.brian.mpesa;

import static daniel.brian.mpesa.Constants.BUSINESS_SHORT_CODE;
import static daniel.brian.mpesa.Constants.CALLBACKURL;
import static daniel.brian.mpesa.Constants.PARTYB;
import static daniel.brian.mpesa.Constants.PASSKEY;
import static daniel.brian.mpesa.Constants.TRANSACTION_TYPE;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import daniel.brian.mpesa.databinding.ActivityMainBinding;
import daniel.brian.mpesa.model.AccessToken;
import daniel.brian.mpesa.model.STKPush;
import daniel.brian.mpesa.services.DarajaApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding activityMainBinding;

    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.etAmount)
    EditText mAmount;

    @BindView(R.id.etPhone)
    EditText mPhone;

    @BindView(R.id.btnPay)
    Button mPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        ButterKnife.bind(this);

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true);

        mPay.setOnClickListener(this);
        getAccessToken();

    }

    private void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if(response.isSuccessful()){
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == mPay){
           String phone_number = mPhone.getText().toString();
           String amount = mAmount.getText().toString();
           performSTKPush(phone_number,amount);
        }
    }

    private void performSTKPush(String phoneNumber, String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                Utils.sanitizePhoneNumber(phoneNumber),
                PARTYB,
                Utils.sanitizePhoneNumber(phoneNumber),
                CALLBACKURL,
                "LipaTest", //Account reference
                "Testing"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(Call<STKPush> call, Response<STKPush> response) {
                mProgressDialog.dismiss();
                try{
                   if(response.isSuccessful()){
                       Timber.d("Post submitted to API. %s",response.body());
                   }else{
                       Timber.e("Response %s",response.errorBody().string());
                   }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<STKPush> call, Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t);
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}