
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.codehaus.groovy.antlr.java.JavaLexer
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("generateProjectStatistic") {

        }
    }
}