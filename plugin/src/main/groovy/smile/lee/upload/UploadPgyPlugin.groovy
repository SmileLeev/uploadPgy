package smile.lee.upload

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.TaskContainerAdaptor
import com.android.build.gradle.internal.TaskFactory
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.reflect.Instantiator
import smile.lee.upload.task.UploadPgyTask

class UploadPgyPlugin implements Plugin<Project> {

    protected Project project
    protected Instantiator instantiator
    protected TaskFactory taskFactory
    protected boolean isBuildingPlugin = false

    @Override
    void apply(Project project) {
        project.tasks.create("uploadPgy",UploadPgyTask)

        this.project = project

        def startParameter = project.gradle.startParameter
        def targetTasks = startParameter.taskNames

        targetTasks.each {
            if (it.contains("assemblePgy") || it.contains("aP")) {
                isBuildingPlugin = true
            }
        }

        project.extensions.create('pgyApk', UploadPgyExtension)

        taskFactory = new TaskContainerAdaptor(project.tasks)
        project.afterEvaluate {
            project.android.applicationVariants.each { ApkVariant variant ->
                println("each apk variant:${variant.name}")
                def name = variant.buildType.name
                if (name.equalsIgnoreCase("release") || name.equalsIgnoreCase("debug")) {
                    final def variantPluginTaskName = "assemblePgy${variant.name.capitalize()}"
                    final def configAction = new UploadPgyTask.ConfigAction(project, variant)

                    taskFactory.create(variantPluginTaskName, UploadPgyTask, configAction)

                    taskFactory.named("assemblePgy", new Action<Task>() {
                        @Override
                        void execute(Task task) {
                            println("named task ${task.name},dependsOne:${variantPluginTaskName}")
                            task.dependsOn(variantPluginTaskName)
                        }
                    })
                }
            }
        }

        project.task('assemblePgy', dependsOn: "assembleRelease", group: 'build', description: 'Build Pgy apk')
    }
}
