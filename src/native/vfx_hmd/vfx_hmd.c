/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                                C Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

#include "vfx_hmd.h"

static int stereo_loaded;

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    initializeVFX
 * Signature: ()V
 */
JNIEXPORT jboolean JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_initializeVFX
  (JNIEnv *env, jobject obj)
{
    stereo_loaded = 0;
    return (VFXLoadDll() == VFX_OK);
}

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    resetTrackerZero
 * Signature: ()V
 */
JNIEXPORT void JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_resetTrackerZero
  (JNIEnv *env, jobject obj)
{
    VFXZeroSet();
}

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    readTrackerPosition
 * Signature: ([F)V
 */
JNIEXPORT void JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_readTrackerPosition
  (JNIEnv *env, jobject obj, jfloatArray output)
{
    long yaw;
    long pitch;
    long roll;

    VFXGetTracking(&yaw, &pitch, &roll);
    // yaw in range 0 - 655535, horziontal direction
    // yaw 0 = -179.99 deg
    // yaw 32768 = 0 deg
    // yaw 65535 = 180.00 deg
    float heading=(((float)yaw * 360) / 65535) - 180;
    
    // pitch range is 20025 - 45511, head up-down
    // pitch 45511 = 70 deg
    // pitch 20025 = -70 deg
    // it must be minus from some reason ??
    float up_down=-(((float)pitch * 360) / 65535) - 180;
                    
    // roll shuld handled as pitch
    float look_roll=(((float)roll * 360) / 65535) - 180;

    jfloat *arr = (*env)->GetFloatArrayElements(env, output, 0);
    arr[0] = heading;
    arr[1] = up_down;
    arr[2] = look_roll;

    (*env)->ReleaseFloatArrayElements(env, output, arr, 0);
}

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    enableVFXStereo
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_enableVFXStereo
  (JNIEnv *env, jobject obj, jboolean isDirect3D)
{
    stereo_loaded = 1;

    VFXSetVideoMode(isDirect3D ? VFX_DIRECTX : VFX_OVERRIDE);
}

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    disableVFXStereo
 * Signature: ()V
 */
JNIEXPORT void JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_disableVFXStereo
  (JNIEnv *env, jobject obj) 
{
}

/*
 * Class:     org_j3d_device_input_vfx_VFXDriver
 * Method:    shutdown
 * Signature: ()V
 */
JNIEXPORT void JNICALL 
Java_org_j3d_device_input_vfx_VFXDriver_shutdown
  (JNIEnv *env, jobject obj)
{
    VFXFreeDll();

    if(stereo_loaded)
        VFXFreeStereoDll();
}
