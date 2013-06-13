adb shell am instrument -w com.cc.cg.tests/android.test.InstrumentationTestRunner
adb shell am instrument -w -e class com.cc.cg.TimeActivityTest com.cc.cg.tests/android.test.InstrumentationTestRunner
adb shell am instrument -w -e class com.cc.cg.testThatTimeGroupIsUpdated#testButtonLabels com.cc.cg.tests/android.test.InstrumentationTestRunner
