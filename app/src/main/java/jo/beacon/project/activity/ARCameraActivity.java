package jo.beacon.project.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import jo.beacon.project.R;
import jo.beacon.project.ar.MainRenderer;

public class ARCameraActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();


    private TextView mTextView;
    private GLSurfaceView mSurfaceView;
    private MainRenderer mRenderer;

    private boolean mUserRequestedInstall = true;

    private Session mSession;
    private Config mConfig;

    private float mCurrentX;
    private float mCurrentY;

    /* 현재 크기와 방향을 저장
     *  물체를 배치 시 평면 위에 올리는 모델 매트릭스에 해당 값을 적용 */
    private float mScaleFactor = 0.002f;
    private float mRotateFactor = 0.0f;

    private boolean mSelectedModel = false;
    private float[] mModelMatrix = new float[16];

    private float[] mObjectModelMatrix = new float[16];

    private boolean mModelInit = false;
    private boolean mModelPut = false;

    private boolean mIsPut = false;

    /* 가상 물체 회전 담당 */
    private GestureDetector mGestureDetector;

    /* 가상 물체 크기 조절 */
    private ScaleGestureDetector mScaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_arcamera);

        mTextView = (TextView) findViewById(R.id.ar_core_text);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                /* 디스플레이 방향이 바뀔때마다 알려준다(CallBack) */
                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);
        }

        /* 터치, 회전에 대한 동작 처리 */
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                mCurrentX = event.getX();
                mCurrentY = event.getY();
                return true;
            }
            /* 터치된 위치를 저장하고, preRender에서 배치하도록 mIsPut=true로 설정 */
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                mCurrentX = event.getX();
                mCurrentY = event.getY();

                mIsPut = true;

                return true;
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mSelectedModel) {
                    mRotateFactor -= (distanceX / 10);
                    Matrix.rotateM(mModelMatrix, 0, -distanceX / 10, 0.0f, 1.0f, 0.0f);
                }
                return true;
            }
        });

        /* 스케일(크기) 조절 처리 */
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mScaleFactor *= detector.getScaleFactor();
                if (mSelectedModel) {
                    Matrix.scaleM(mModelMatrix, 0,
                            detector.getScaleFactor(),
                            detector.getScaleFactor(),
                            detector.getScaleFactor());
                }
                return true;
            }
        });

        /* 원하는 물품 지정하기 */
        Intent intent = getIntent();
        String Object = intent.getExtras().getString("pName");
        mRenderer = new MainRenderer(this, Object, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {
                /* 디스플레이 방향을 얻어와 방향에 따라 설정한다. */
                if (mRenderer.isViewportChanged()) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                /* 렌더러에서 카메라 영상을 그리기 위해 생성한 텍스쳐(id) 객체를 Session 객체와 연결 */
                mSession.setCameraTextureName(mRenderer.getTextureId());

                /* 시스템에서 frame 객체를 반환받은 시점에서의 상태값을 갖는 객체 */
                Frame frame = null;

                /* 새로운 영상을 받았을 때 최신 프레임으로 업데이트 */
                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                /* 디스플레이의 상태가 변경되면 해당 프레임을 변경된 값으로 변환*/
                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.transformDisplayGeometry(frame);
                }

                /* 현재 프레임에서 추출한 포인트 클라우드를 렌더러에 전달 */
                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.updatePointCloud(pointCloud);
                pointCloud.release();

                /* 트래킹 중인 모든 Trackable 객체 중 평면 Plane 객체를 받기*/
                Collection<Plane> planes = mSession.getAllTrackables(Plane.class);
                for (Plane plane : planes) {
                    if (plane.getTrackingState() == TrackingState.TRACKING
                            && plane.getSubsumedBy() == null) {
                        mRenderer.updatePlane(plane);
                    }
                }

                /* ProjectionMatrix와 ViewMatrix 받아오기 (p.414) */
                Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                /* 가구 선택 시 초기 위체에 그리기 */
                if (mSelectedModel) {/* 초기 위치에 물체 놓기 */
                    if (!mModelInit) {
                        float[] position = calculateInitialPosition(mRenderer.getWidth(), mRenderer.getHeight(), projMatrix, viewMatrix);
                        Matrix.setIdentityM(mModelMatrix, 0);
                        Matrix.translateM(mModelMatrix, 0, position[0], position[1], position[2]);
                        Matrix.scaleM(mModelMatrix, 0, 0.005f, 0.005f, 0.005f);

                        mModelInit = true;
                        mModelPut = false;
                    }
                    /* 지정한 위치에 물체 놓기 */
                    if (!mModelPut) {
                        mRenderer.setModelMatrix(mModelMatrix);
                    }
                    mRenderer.setModelDraw(true);
                }

                if (mIsPut) {
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);
                    for (HitResult result : results) {
                        Trackable trackable = result.getTrackable();
                        Pose pose = result.getHitPose();
                        float[] modelMatrix = new float[16];
                        pose.toMatrix(modelMatrix, 0);
                        Matrix.scaleM(modelMatrix, 0, mScaleFactor, mScaleFactor, mScaleFactor);
                        Matrix.rotateM(modelMatrix, 0, mRotateFactor, 0.0f, 1.0f, 0.0f);
                        mScaleFactor = 0.02f;
                        if (trackable instanceof Plane
                                && ((Plane) trackable).isPoseInPolygon(result.getHitPose())) {
                            if (mSelectedModel) {
                                if (!mModelPut) {
                                    mModelPut = true;
                                    mSelectedModel = false;
                                    System.arraycopy(modelMatrix, 0, mObjectModelMatrix, 0, 16);
                                    Matrix.setIdentityM(mModelMatrix, 0);
                                    mIsPut = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            mTextView.setText(getString(R.string.glasses_put));
                                        }
                                    });
                                    TimerTask textTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    mTextView.setText(getString(R.string.not_selected));
                                                }
                                            });
                                        }
                                    };
                                    Timer textTimer = new Timer();
                                    textTimer.schedule(textTask, 2000);
                                }
                            }
                        }
                        if (!mIsPut) {
                            break;
                        }
                    }
                    if (mIsPut) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mTextView.setText(getString(R.string.not_valid_position));
                            }
                        });
                        TimerTask textTask = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mSelectedModel) {
//                                            mTextView.setText(getString(R.string.glasses_selected));
                                        } else {
//                                            mTextView.setText(getString(R.string.not_selected));
                                        }
                                    }
                                });
                            }
                        };
                        Timer textTimer = new Timer();
                        textTimer.schedule(textTask, 2000);
                    }
                    mIsPut = false;
                }
// $$$$
                if (mModelPut) {
                    mRenderer.setModelMatrix(mObjectModelMatrix);
                    mRenderer.updateObjectViewMatrix(viewMatrix);
                    mModelInit = false;
                }

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }
        });
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mSession.pause();
    }

    /* Application 실행 시 */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        requestCameraPermission();

        try {
            if (mSession == null) {
                /* ARCore SDK 설치 여부 확인 */
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d(TAG, "ARCore Session created.");
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = false;
                        Log.d(TAG, "ARCore should be installed.");
                        break;
                }
            }
        }
        catch (UnsupportedOperationException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        } catch (UnavailableApkTooOldException | UnavailableDeviceNotCompatibleException | UnavailableUserDeclinedInstallationException | UnavailableArcoreNotInstalledException | UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }

        /* ARCore의 기본 환경 값 설정 */
        mConfig = new Config(mSession);
        if (!mSession.isSupported(mConfig)) {
            Log.d(TAG, "This device is not support ARCore.");
        }
        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        /* 터치한 위치의 3차원 좌표값 읽어오기 */
        /* 터치 시 가상 물체 생성(좌표와 터치 여부 콜백) */
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            mCurrentX = event.getX();
//            mCurrentY = event.getY();
//            mTouched = true;
//        }
        return true;
    }

    /* 선택된 물체를 화면 중앙 하단(프로젝션 매트릭스 & 뷰 매트릭스로 알아낸 값)에 그리기 */
    /* 카메라 속성에 따른 프로젝션 매트릭스 */
    public float[] calculateInitialPosition(int width, int height, float[] projMat, float[] viewMat) {
        return getScreenPoint(width / 2, height - 300, width, height, projMat, viewMat);
    }
    /* 카메라 시점에 대한 뷰 매트릭스 */
    public float[] getScreenPoint(float x, float y, float w, float h, float[] projMat, float[] viewMat) {
        float[] position = new float[3];
        float[] direction = new float[3];

        x = x * 2 / w - 1.0f;
        y = (h - y) * 2 / h - 1.0f;

        float[] viewProjMat = new float[16];
        Matrix.multiplyMM(viewProjMat, 0, projMat, 0, viewMat, 0);

        float[] invertedMat = new float[16];
        Matrix.setIdentityM(invertedMat, 0);
        Matrix.invertM(invertedMat, 0, viewProjMat, 0);

        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];

        Matrix.multiplyMV(nearPlanePoint, 0, invertedMat, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedMat, 0, farScreenPoint, 0);

        position[0] = nearPlanePoint[0] / nearPlanePoint[3];
        position[1] = nearPlanePoint[1] / nearPlanePoint[3];
        position[2] = nearPlanePoint[2] / nearPlanePoint[3];

        direction[0] = farPlanePoint[0] / farPlanePoint[3] - position[0];
        direction[1] = farPlanePoint[1] / farPlanePoint[3] - position[1];
        direction[2] = farPlanePoint[2] / farPlanePoint[3] - position[2];

        normalize(direction);

        position[0] += (direction[0] * 0.1f);
        position[1] += (direction[1] * 0.1f);
        position[2] += (direction[2] * 0.1f);

        return position;
    }
    /* 뷰 매트릭스를 구하는 수식 일부 */
    private void normalize(float[] v) {
        double norm = Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
        v[0] /= norm;
        v[1] /= norm;
        v[2] /= norm;
    }

    /* 카메라 권한 확인 */
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    /* 상태바, 타이틀바를 제거해 전체화면으로 구성 */
    private void hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void onSelectButtonClicked(View view) {
        mSelectedModel = true;
//        mTextView.setText(getString(R.string.glasses_selected));
    }

    /* 종료 */
    @Override
    public void onBackPressed() {
        this.finish();
    }

}

