[1mdiff --git a/serialization-protobuf/build.gradle.kts b/serialization-protobuf/build.gradle.kts[m
[1mindex 9db9d20..cb04a20 100644[m
[1m--- a/serialization-protobuf/build.gradle.kts[m
[1m+++ b/serialization-protobuf/build.gradle.kts[m
[36m@@ -6,7 +6,7 @@[m [mplugins {[m
 }[m
 [m
 dependencies {[m
[31m-    implementation(project(":core"))[m
[32m+[m[32m    api(project(":core"))[m
     val serializationLibVersion: String by project[m
     api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationLibVersion")[m
 }[m
