LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := app
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	E:\GitHubProject\Note5\app\src\main\jniLibs\arm64-v8a\libmsc.so \
	E:\GitHubProject\Note5\app\src\main\jniLibs\armeabi-v7a\libmsc.so \

LOCAL_C_INCLUDES += E:\GitHubProject\Note5\app\src\debug\jni
LOCAL_C_INCLUDES += E:\GitHubProject\Note5\app\src\main\jni
LOCAL_C_INCLUDES += E:\GitHubProject\Note5\app\src\main\jniLibs

include $(BUILD_SHARED_LIBRARY)
