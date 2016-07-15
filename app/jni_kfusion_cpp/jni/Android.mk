
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

ROOT_DIR=${LOCAL_PATH}/../../../
PEACH_DIRECTORY=${ROOT_DIR}/dependencies/slambench/

MORE_INCLUDE=${ROOT_DIR}/app/include

LOCAL_CFLAGS  += -O3
LOCAL_LDFLAGS += -O3

LOCAL_MODULE     :=  kfusion-cpp
LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/cpp/kernels.cpp jnikfusioncpp.cpp
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/include/  $(PEACH_DIRECTORY)/kfusion/thirdparty/  $(MORE_INCLUDE)
LOCAL_LDLIBS     :=  -llog -landroid -lGLESv2  -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
