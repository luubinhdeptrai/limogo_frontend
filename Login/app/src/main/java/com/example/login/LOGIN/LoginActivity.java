package com.example.login.LOGIN;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.login.API.ApiClient;
import com.example.login.API.ApiService;
import com.example.login.API.TokenManager;
import com.example.login.HOME.MainActivity;
import com.example.login.R;
import com.example.login.SEND_VERIFICATION.EmailRequest;
import com.example.login.SEND_VERIFICATION.SendCodeResponse;
import com.example.login.SIGNUP.SignUpActivity;
import com.example.login.VERIFY_VERIFICATION.VerifiedActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etMail, etPass;
    private Button btnLogin;
    private TextView tvRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        etMail = findViewById(R.id.editTextEmailLogin);
        etPass = findViewById(R.id.editTextPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.textViewRegister);

        // Khởi tạo ApiService KHÔNG CẦN TOKEN cho màn hình đăng nhập
        apiService = ApiClient.getNoAuthAPI();

        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = etMail.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String token = response.body().getData().getToken();
                    TokenManager.saveToken(LoginActivity.this, token); // Lưu token

                    Log.d("Login", "Đăng nhập thành công, token đã lưu.");
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    if (response.body().getData().isVerified()) {
                        // Nếu tài khoản đã được xác thực, chuyển đến MainActivity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Nếu chưa xác thực, gửi mã xác nhận và chuyển sang VerifiedActivity
                        sendVerificationCode(email);
                    }
                } else {
                    String errorMessage = "Đăng nhập thất bại.";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) { /* ignore */ }
                    }
                    Log.e("Login", "Đăng nhập thất bại: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("Login", "Lỗi kết nối khi đăng nhập: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCode(String email) {
        // Lưu ý: apiService ở đây là getNoAuthAPI(), nhưng sau khi login đã có token thì
        // ApiClient.getAuthAPI() sẽ được sử dụng cho VerifiedActivity
        // Đối với việc gửi lại mã xác nhận sau khi đăng nhập, nếu server yêu cầu Auth,
        // thì ApiClient.getAuthAPI(this) sẽ cần được sử dụng
        // Trong trường hợp này, vì email là đủ để gửi mã xác nhận, chúng ta vẫn có thể dùng apiService hiện tại.
        // Tuy nhiên, để đảm bảo token được đính kèm (nếu server yêu cầu cho send code),
        // bạn nên tạo một instance ApiService mới với AuthInterceptor.
        ApiService authApiService = ApiClient.getAuthAPI(this); // Lấy service có token

        authApiService.sendVerificationCode(new EmailRequest(email)).enqueue(new Callback<SendCodeResponse>() {
            @Override
            public void onResponse(Call<SendCodeResponse> call, Response<SendCodeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d("Login", "Gửi mã xác nhận thành công.");
                    Toast.makeText(LoginActivity.this, "Mã xác nhận đã được gửi đến email của bạn.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, VerifiedActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "Gửi mã xác nhận thất bại.";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) { /* ignore */ }
                    }
                    Log.e("Login", "Gửi mã xác nhận thất bại: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SendCodeResponse> call, Throwable t) {
                Log.e("Login", "Lỗi kết nối khi gửi mã xác nhận: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối khi gửi mã xác nhận.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}