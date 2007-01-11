/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                                C Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

/*****************************************************************************/
#ifndef _VFX_HMD_H
#define _VFX_HMD_H

#ifdef _WIN32
/* Windoze silliness. */
#include <io.h>
#include <fcntl.h>

#ifdef __GNUC__
#define _HAVE_INT64
#define _INTEGRAL_MAX_BITS 64
#undef __int64
#define __int64 long long
#endif

#endif /* win32 */

#include <jni.h>
#include "org_j3d_device_input_vfx_VFXDriver.h"
#include "VFXsdk.h"

#endif /* _VFX_HMD_H */
