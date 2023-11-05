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