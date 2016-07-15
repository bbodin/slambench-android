
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
ROOT_DIR=${LOCAL_PATH}/../../../

PEACH_DIRECTORY=${ROOT_DIR}/dependencies/slambench/
MORE_INCLUDE=${ROOT_DIR}/app/include

DEPENDENCIES_DIR=${ROOT_DIR}/dependencies/

LOCAL_CFLAGS  += -O3
LOCAL_LDFLAGS += -O3

LOCAL_MODULE     :=  recorder


ifeq ($(TARGET_ARCH),arm)

OPENNI_INCLUDE=${DEPENDENCIES_DIR}/OpenNI/Include/
OPENNI2_INCLUDE=${DEPENDENCIES_DIR}/OpenNI2/Include/
OPENNI_LIB_DIR=${ROOT_DIR}/app/build/libs/armeabi-v7a/
OPENNI2_LIB_DIR=${DEPENDENCIES_DIR}/OpenNI2/Packaging/AndroidBuild/libs/armeabi

LOCAL_CFLAGS  +=   -DDO_OPENNI -DDO_OPENNI15
LOCAL_LDFLAGS +=   -DDO_OPENNI -DDO_OPENNI15

LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/cpp/kernels.cpp jnirecorder.cpp
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/include/  $(PEACH_DIRECTORY)/kfusion/thirdparty/  $(MORE_INCLUDE) $(OPENNI_INCLUDE) $(OPENNI2_INCLUDE)
LOCAL_LDLIBS     :=  -L${OPENNI_LIB_DIR} -L${OPENNI2_LIB_DIR} -llog -landroid -lGLESv2  -lGLESv1_CM -lOpenNI -lOpenNI2 -lusb -fuse-ld=bfd

else

ifeq ($(TARGET_ARCH),arm64)

OPENNI_INCLUDE=${DEPENDENCIES_DIR}/OpenNI/Include/
OPENNI2_INCLUDE=${DEPENDENCIES_DIR}/OpenNI2/Include/
OPENNI_LIB_DIR=${ROOT_DIR}/app/build/libs/arm64-v8a/
OPENNI2_LIB_DIR=${DEPENDENCIES_DIR}/OpenNI2/Packaging/AndroidBuild/libs/arm64-v8a/

LOCAL_CFLAGS  +=   -DDO_OPENNI -DDO_OPENNI15
LOCAL_LDFLAGS +=   -DDO_OPENNI -DDO_OPENNI15

LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/cpp/kernels.cpp jnirecorder.cpp
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/include/  $(PEACH_DIRECTORY)/kfusion/thirdparty/ $(MORE_INCLUDE) $(OPENNI_INCLUDE) $(OPENNI2_INCLUDE)
LOCAL_LDLIBS     :=  -L${OPENNI_LIB_DIR} -L${OPENNI2_LIB_DIR} -llog -landroid -lGLESv2  -lGLESv1_CM -lOpenNI -lOpenNI2 -lusb -fuse-ld=bfd

else

LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/cpp/kernels.cpp jnirecorder.cpp
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/include/  $(PEACH_DIRECTORY)/kfusion/thirdparty/  $(MORE_INCLUDE)
LOCAL_LDLIBS     :=  -llog -landroid -lGLESv2  -lGLESv1_CM

endif

endif

include $(BUILD_SHARED_LIBRARY)
