LOCAL_PATH := $(call my-dir)
MAIN_LOCAL_PATH := $(LOCAL_PATH)

# ========== Main Shared Library (blackbox) ==========
include $(CLEAR_VARS)

# --- Module Name ---
LOCAL_MODULE := blackbox

# --- Compiler Flags ---
LOCAL_CFLAGS := -Wno-error=format-security -fvisibility=hidden -ffunction-sections -fdata-sections -w
LOCAL_CPPFLAGS := -std=c++17 -Wno-error=format-security -Wno-error=c++11-narrowing -fvisibility=hidden -ffunction-sections -fdata-sections -fms-extensions -fexceptions -frtti

# --- Linker Flags ---
LOCAL_LDFLAGS := -Wl,--gc-sections,--strip-all
LOCAL_LDLIBS := -llog -landroid -lz

# --- ARM Mode (32-bit only) ---
LOCAL_ARM_MODE := arm

# --- Include Paths ---
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Hook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/IO
LOCAL_C_INCLUDES += $(LOCAL_PATH)/JniHook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/SandHook

# --- Source Files ---
BLACKBOX_SRC := $(wildcard $(LOCAL_PATH)/*.cpp)
BLACKBOX_SRC += $(wildcard $(LOCAL_PATH)/Hook/*.cpp)
BLACKBOX_SRC += $(wildcard $(LOCAL_PATH)/IO/*.cpp)
BLACKBOX_SRC += $(wildcard $(LOCAL_PATH)/JniHook/*.cpp)
BLACKBOX_SRC += $(wildcard $(LOCAL_PATH)/SandHook/*.cpp)

LOCAL_SRC_FILES := $(BLACKBOX_SRC:$(LOCAL_PATH)/%=%)

# --- Static Libraries ---
LOCAL_STATIC_LIBRARIES := 

# --- Build Shared Library ---
include $(BUILD_SHARED_LIBRARY)

# include $(LOCAL_PATH)/fb/Android.mk