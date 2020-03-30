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
import jo.beacon.project.network.Task;

public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.et_registerID)
    EditText etId;

    @BindView(R.id.et_registerPW)
    EditText etPassword;


    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    //----------------------------------------------------------------------------------------------
    // OnClick
    //----------------------------------------------------------------------------------------------
    @OnClick({R.id.bt_registerBT, R.id.r1})
    void buttonEvents(View view) {
        switch (view.getId()) {
            case R.id.bt_registerBT: {
                String strId = etId.getText().toString();
                String strPw = etPassword.getText().toString();
                if (strId.length() <= 0 || strPw.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "빈칸이 존재합니다.", Toast.LENGTH_SHORT).show();
                    break;
                }

                String strReceive = null;
                try {
                    strReceive = new Task("REGISTER", this).execute(strId, strPw).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (strReceive.equals("true")) { // 로그인 성공
                    Toast.makeText(RegisterActivity.this, "회원가입 성공했습니다.", Toast.LENGTH_SHORT).show();
                    this.finish();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else { // 로그인 실패
                    Toast.makeText(RegisterActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
    // onDestroy
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
