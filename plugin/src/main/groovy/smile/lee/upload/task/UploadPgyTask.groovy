package smile.lee.upload.task

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.sun.istack.internal.NotNull
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class UploadPgyTask extends DefaultTask {

    @OutputDirectory
    File pluginApkDir

    @Input
    String appPackageName

    @Input
    String apkTimestamp

    @Input
    File originApkFile

    UploadPgyTask() {
        //任务分组
        group = "upload"
    }

    @TaskAction
    def anyWordJustAnno() {
        println("begin to upload ${project.pgyApk.message}")
        if (!getOriginApkFile().exists()) {
            println('apk is not exists!')
            return
        }
        if (!project.pgyApk.pgy_uk){
            println('user key is necessary!')
            return
        }
        if (!project.pgyApk.pgy_api){
            println('api key is necessary!')
            return
        }
        def process = "curl -F file=@${getOriginApkFile().path} -F uKey=${project.pgyApk.pgy_uk} -F _api_key=${project.pgyApk.pgy_api} -F installType=2 -F password=${project.pgyApk.apk_pwd} ${project.pgyApk.pgy_url}".execute()
        process.in.eachLine { line ->
            println line
        }
    }

    public static class ConfigAction implements Action<UploadPgyTask> {

        @NotNull
        Project project
        @NotNull
        ApkVariant variant

        ConfigAction(@NotNull Project project, @NotNull ApkVariant variant) {
            this.project = project
            this.variant = variant
        }

        @Override
        void execute(UploadPgyTask assemblePluginTask) {
            ConventionMappingHelper.map(assemblePluginTask, "appPackageName") {
                variant.applicationId
            }

            ConventionMappingHelper.map(assemblePluginTask, "apkTimestamp", {
                new Date().format("yyyyMMddHHmmss")
            })

            ConventionMappingHelper.map(assemblePluginTask, "originApkFile") {
                variant.outputs[0].outputFile
            }

            ConventionMappingHelper.map(assemblePluginTask, "pluginApkDir") {
                new File(project.buildDir, "/outputs/plugin/${variant.name}")
            }

            assemblePluginTask.setGroup("build")
            assemblePluginTask.setDescription("Build ${variant.name.capitalize()} plugin apk")
            assemblePluginTask.dependsOn(variant.assemble.name)
        }
    }
}
