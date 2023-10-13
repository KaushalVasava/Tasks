-keepattributes *Annotation*, Signature, Exception
#-keepnames class androidx.navigation.fragment.NavHostFragment
#-keep class * extends androidx.fragment.app.Fragment{}
-keep class com.lahsuak.apps.tasks.data.model.** { *; }
-keep class com.lahsuak.apps.tasks.model.Category { *; }
-keep class com.lahsuak.apps.tasks.util.AppConstants
-keep class com.lahsuak.apps.tasks.util.NavigationConstants
#
#-keepattributes SourceFile, LineNumberTable
#-keepattributes LocalVariableTable, LocalVariableTypeTable
