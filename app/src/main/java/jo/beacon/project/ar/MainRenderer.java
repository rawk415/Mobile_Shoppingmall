package jo.beacon.project.ar;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = MainRenderer.class.getSimpleName();

    private boolean mViewportChanged;
    private int mViewportWidth;
    private int mViewportHeight;

    private CameraRenderer mCamera;
    private PointCloudRenderer mPointCloud;
    private PlaneRenderer mPlane;

    private ObjRenderer mObject;

    private boolean mIsDraw = false;

    private RenderCallback mRenderCallback;

    public interface RenderCallback {
        void preRender();
    }

    /* 홈디지인 -> a매개변수에 Context 추가 */
    public MainRenderer(Context context, String Object , RenderCallback callback) {
        mCamera = new CameraRenderer();

        mPointCloud = new PointCloudRenderer();

        mPlane = new PlaneRenderer(Color.BLUE, 0.5f);

        mObject = new ObjRenderer(context, Object+".obj", Object+".jpg");

        mRenderCallback = callback;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();

        mPointCloud.init();

        mPlane.init();

        mObject.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);

        mPointCloud.draw();

        mPlane.draw();

        if (mIsDraw) {
            mObject.draw();
        }
    }

    public int getTextureId() {
        return mCamera == null ? -1 : mCamera.getTextureId();
    }

    public void onDisplayChanged() {
        mViewportChanged = true;
    }

    public boolean isViewportChanged() {
        return mViewportChanged;
    }

    public int getWidth() {
        return mViewportWidth;
    }

    public int getHeight() {
        return mViewportHeight;
    }

    /* 화면의 방향, 크기를 얻어 Sessoin의 디스플레이 방향을 설정 */
    public void updateSession(Session session, int displayRotation) {
        if (mViewportChanged) {
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }
    }

    public void transformDisplayGeometry(Frame frame) {
        mCamera.transformDisplayGeometry(frame);
    }

    public void updatePointCloud(PointCloud pointCloud) {
        mPointCloud.update(pointCloud);
    }

    public void updatePlane(Plane plane) {
        mPlane.update(plane);
    }

    public void setModelMatrix(float[] matrix) {
        mObject.setModelMatrix(matrix);
    }

    public void setProjectionMatrix(float[] matrix) {
        mPointCloud.setProjectionMatrix(matrix);
        mPlane.setProjectionMatrix(matrix);
        mObject.setProjectionMatrix(matrix);
    }

    public void updateViewMatrix(float[] matrix) {
        mPointCloud.setViewMatrix(matrix);
        mPlane.setViewMatrix(matrix);
        mObject.setViewMatrix(matrix);
    }

    public void updateObjectViewMatrix(float[] matrix) {
        mObject.setViewMatrix(matrix);
    }

    public void setModelDraw(boolean bool) {
        mIsDraw = bool;
    }
}
