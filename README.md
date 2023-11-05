# Описание
Созданный плагин использует `ANTLR4` (v 4.7.1) с автоматически сгенерированными грамматиками под `Kotlin` и `Java`. Для сериализации результата в формат Json используется библиотека `Jackson`.
Для примера в качестве анализируемого кода были использованы сэмплы из предыдущих домашних работ. Для большего удобства был пеализован способ создания gradle-плагина через `buildSrc`.
Для рекурсивного обхода всех директорий внутри ```src``` исользованы средства стандартной библиотеки.
# Использование
```
./gradlew build
./gradlew generateProjectStatistic
```
## Генерация ресурсов грамматики
```
./gradlew generateGrammarSource
```
По умолчанию отчет об анализе сохраняется в папку build корневого проекта.
```
GradlePlugin
├──────────build
│   └─────────output
│       └───────statistic.json
```
## Пример (анализ сэмплов)
```javascript
{
  "lineCount" : 525,
  "fileCount" : 10,
  "classCount" : 22,
  "functionCount" : 57,
  "objectCount" : 10,
  "enumCount" : 1,
  "annotationCount" : 5,
  "codeBlockCounter" : 52,
  "importCount" : 6
}
```
## Основная логика плагина
```kotlin
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
```
```kotlin
import java.util.concurrent.atomic.AtomicInteger

class KotlinParserListenerImpl : KotlinParserBaseListener() {

    val classCounter = AtomicInteger()
    val importCounter = AtomicInteger()
    val fileCounter = AtomicInteger()
    val functionCounter = AtomicInteger()
    val enumCounter = AtomicInteger()
    val objectCounter = AtomicInteger()
    val codeBlockCounter = AtomicInteger()
    val annotationCounter = AtomicInteger()

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        annotationCounter.incrementAndGet()
    }

    override fun enterBlock(ctx: KotlinParser.BlockContext?) {
        codeBlockCounter.incrementAndGet()
    }

    override fun enterImportHeader(ctx: KotlinParser.ImportHeaderContext?) {
        importCounter.incrementAndGet()
    }

    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        objectCounter.incrementAndGet()
    }

    override fun enterEnumEntries(ctx: KotlinParser.EnumEntriesContext?) {
        enumCounter.incrementAndGet()
    }

    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext) {
        fileCounter.incrementAndGet()
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext) {
        classCounter.incrementAndGet()
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext) {
        functionCounter.incrementAndGet()
    }
}
```
```kotlin
data class CodeStatistic(
    val lineCount: Int,
    val fileCount: Int,
    val classCount: Int,
    val functionCount: Int,
    val objectCount: Int,
    val enumCount: Int,
    val annotationCount: Int,
    val codeBlockCounter: Int,
    val importCount: Int
)
```
