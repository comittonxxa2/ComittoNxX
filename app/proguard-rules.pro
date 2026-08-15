# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn javax.naming.**
-dontwarn org.slf4j.**

# このへんをkeepnamesに変更したらSMBにアクセスできなくなった
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }

# リリースビルドにはデバッグログを含めない。
-assumenosideeffects public class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# 7-Zip-JBindingの全クラス・全メソッドをそのまま保持する
-keep class net.sf.sevenzipjbinding.** { *; }

# 警告を無視する
-dontwarn net.sf.sevenzipjbinding.**

# ネイティブコード(JNI)との紐付けを保持する
-keepclasseswithmembernames class * {
    native <methods>;
}

# ExternalFilterData だけを対象にする
-keep class src.comitton.common.ExternalFilterData {
    *;
}

# SMBJ のメインパッケージを難読化から保護
-keep class com.hierynomus.** { *; }
-keep interface com.hierynomus.** { *; }

# BouncyCastle (SMB2/3の暗号化・認証処理でリフレクションが使われるため保持)
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ASN.1 / Spnego 関連 (SMBJのNTLM/Kerberos認証で使用)
-keep class com.hierynomus.protocol.commons.** { *; }
-keep class com.hierynomus.asn1.** { *; }

# SMBJがオプションで参照している未読み込みクラスの警告を抑制
-dontwarn com.hierynomus.smbj.**
-dontwarn net.engio.mbassy.**
