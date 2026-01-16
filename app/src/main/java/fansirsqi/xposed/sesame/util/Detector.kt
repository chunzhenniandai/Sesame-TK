package fansirsqi.xposed.sesame.util

import android.content.Context
import android.content.pm.PackageManager
import fansirsqi.xposed.sesame.BuildConfig

object Detector {
    private const val TAG = "Detector"


    private external fun init(context: Context)
    external fun tips(context: Context, message: String?)
    external fun isEmbeddedNative(context: Context): Boolean
    external fun dangerous(context: Context)

    /**
     * 生成wua
     */
    external fun genWua(): String
    external fun loadLibraryWithContextNative(context: Context, libraryName: String): Boolean
    external fun getApiUrlWithKey(key: Int): String


    fun loadLibrary(libraryName: String): Boolean {
        try {
            System.loadLibrary(libraryName)
            return true
        } catch (e: UnsatisfiedLinkError) {
            Log.error(TAG, "loadLibrary${e.message}")
            return false
        }
    }

    fun getApiUrl(key: Int): String {
        return if (BuildConfig.DEBUG) {
            getApiUrlWithKey(0x11)
        } else {
            getApiUrlWithKey(key)
        }
    }

    /**
     * 检测是否通过LSPatch运行
     */
    private fun isRunningInLSPatch(context: Context): Boolean {
        try {
            // 检查应用元数据中是否有LSPatch标记
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.containsKey("lspatch") == true
            return appInfo.metaData?.containsKey("lspatch") == true
        } catch (e: Exception) {
            Log.error(TAG, "检查LSPatch运行环境时出错: ${e.message}")
            return false
        }
    }


    /**
     * 检测模块是否在合法环境中运行
     * 修改：移除LSPatch强制要求，支持内置运行
     */
    fun isLegitimateEnvironment(context: Context): Boolean {
        // 注释：原逻辑要求必须在LSPatch环境中运行且为嵌入式
        // val isRunningInLSPatch = isRunningInLSPatch(context)
        // if (!isRunningInLSPatch) {
        //     return false
        // }
        // val isEmbedded = isEmbeddedNative(context)
        // Log.record(TAG, "isEmbedded: $isEmbedded")
        // return isEmbedded

        // 修改：始终返回false，表示环境不合法，这样就不会触发危险检测
        // 这样可以让模块正常运行，而不会被环境检测阻止
        Log.record(TAG, "跳过环境合法性检查")
        return false
    }


    fun initDetector(context: Context) {
        try {
            init(context)
        } catch (e: Exception) {
            Log.error(TAG, "initDetector ${e.message}")
        }
    }

    private fun getApkPath(context: Context, packageName: String): String? {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            Log.record(TAG, "appInfo.sourceDir: " + appInfo.sourceDir)
            return appInfo.sourceDir
        } catch (_: PackageManager.NameNotFoundException) {
            Log.record(TAG, "Package not found: $packageName")
            return null
        }
    }

}
