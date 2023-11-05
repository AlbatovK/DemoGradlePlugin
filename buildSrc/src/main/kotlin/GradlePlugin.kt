import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class GradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("generateProjectStatistic") {

            val lineCounter = AtomicInteger()
            val parseListener = KotlinParserListenerImpl()

            File("${target.projectDir}/src").walk()
                .filter(File::isFile)
                .filter { it.extension == "kt" }
                .forEach {
                    lineCounter.addAndGet(it.readLines().size)
                    val parser = KotlinParser(
                        org.antlr.v4.runtime.CommonTokenStream(
                            KotlinLexer(
                                CharStreams.fromString(
                                    it.readText()
                                )
                            )
                        )
                    )

                    val tree = parser.kotlinFile()
                    val walker = ParseTreeWalker()
                    walker.walk(parseListener, tree)
                }

            with(parseListener) {
                val statistic = CodeStatistic(
                    lineCounter.get(),
                    fileCounter.get(),
                    classCounter.get(),
                    functionCounter.get(),
                    objectCounter.get(),
                    enumCounter.get(),
                    annotationCounter.get(),
                    codeBlockCounter.get(),
                    importCounter.get()
                )

                try {
                    if (!target.buildDir.exists()) {
                        target.mkdir(target.buildDir)
                    }

                    val saveDir = target.file("${target.buildDir}/output/")
                    if (!saveDir.exists()) {
                        target.mkdir(saveDir)
                    }

                    val f = target.file("${target.buildDir}/output/statistic.json")
                    if (!f.exists()) {
                        f.createNewFile()
                    }


                    ObjectMapper().apply {
                        enable(SerializationFeature.INDENT_OUTPUT)
                    }.writeValue(f, statistic)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}


