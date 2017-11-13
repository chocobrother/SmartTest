LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := filtfilt
LOCAL_SRC_FILES := filtfilt.cpp

LOCAL_LDLIBS := -llog -fno-exceptions
LOCAL_C_INCLUDES := C:/Users/sun/Downloads/eigen-eigen-b9cd8366d4e8/eigen_library
include $(BUILD_SHARED_LIBRARY)