package jo.beacon.project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jo.beacon.project.R;
import jo.beacon.project.util.PreferenceUtil;
import jo.beacon.project.network.Task;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.et_id)
    EditText etId;

    @BindView(R.id.et_password)
    EditText etPassword;

    // 키보드 내리기
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // 키보드 내리기
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({R.id.bt_login_ok, R.id.r1})
    void buttonEvents(View view) {
        switch (view.getId()) {
            case R.id.bt_login_ok: {
                String strId = etId.getText().toString();
                String strPw = etPassword.getText().toString();
                if (strId.length() <= 0 || strPw.length() <= 0) {
                    Toast.makeText(LoginActivity.this, "빈칸이 존재합니다.", Toast.LENGTH_SHORT).show();
                    break;
                }
                String strReceive = null;
                try {
                    strReceive = new Task("LOGIN", this).execute(strId, strPw).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (strReceive.equals("true")) { // 로그인 성공
                    PreferenceUtil.getInstance(LoginActivity.this).put(PreferenceUtil.PreferenceKey.userId, strId); // id 임시저장
                    Toast.makeText(LoginActivity.this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    this.finish();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else { // 로그인 실패
                    Toast.makeText(LoginActivity.this, "로그인 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.r1: {
                imm.hideSoftInputFromWindow(etId.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                break;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // onBackPressed
    //----------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        this.finish(); // 종료
    }

    //모든 인스턴스들을 닫아 준다.
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}