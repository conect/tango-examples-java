/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.experiments.motiontrackingrajawali;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.TouchViewHandler;
import com.projecttango.rajawali.ScenePoseCalcuator;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import org.rajawali3d.renderer.RajawaliRenderer;

public class MotionTrackingRajawaliRenderer extends RajawaliRenderer {

    private static final float CAMERA_NEAR = 0.01f;
    private static final float CAMERA_FAR = 200f;

    private Object3D mCity;
    private DirectionalLight mLight1;
    private DirectionalLight mLight2;

    private TouchViewHandler mTouchViewHandler;

    // Latest available device pose;
    private Pose mDevicePose = new Pose(Vector3.ZERO, Quaternion.getIdentity());
    private boolean mPoseUpdated = false;

    public MotionTrackingRajawaliRenderer(Context context) {
        super(context);
        mTouchViewHandler = new TouchViewHandler(mContext, getCurrentCamera());
        mTouchViewHandler.setFirstPersonView();
    }

    @Override
    protected void initScene() {
        mLight1 = new DirectionalLight(1.0f, -1.0f, -0.5f);
        mLight2 = new DirectionalLight(-1.0f, -1.0f, 0.5f);
        mLight1.setColor(0.75f, 0.75f, 0.75f);
        mLight1.setPower(0.5f);
        mLight2.setColor(0.8f, 0.8f, 0.8f);
        mLight2.setPower(1);
        getCurrentScene().addLight(mLight1);
        getCurrentScene().addLight(mLight2);

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                mTextureManager, R.raw.city_obj);
        try {
            objParser.parse();
            mCity = objParser.getParsedObject();
            mCity.setPosition(0, -1.3f, 0);
            getCurrentScene().addChild(mCity);

        } catch (ParsingException e) {
            e.printStackTrace();
        }

        getCurrentScene().setBackgroundColor(Color.WHITE);
        getCurrentCamera().setNearPlane(CAMERA_NEAR);
        getCurrentCamera().setFarPlane(CAMERA_FAR);
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        synchronized (this) {
            if (mPoseUpdated) {
                mPoseUpdated = false;
                mTouchViewHandler.updateCamera(mDevicePose.getPosition(), mDevicePose.getOrientation());
            }
        }

        super.onRender(ellapsedRealtime, deltaTime);
    }

    public synchronized void updateDevicePose(TangoPoseData tangoPoseData) {
        mDevicePose = ScenePoseCalcuator.toOpenGLPose(tangoPoseData);
        mPoseUpdated = true;
    }

    @Override
    public void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }
}