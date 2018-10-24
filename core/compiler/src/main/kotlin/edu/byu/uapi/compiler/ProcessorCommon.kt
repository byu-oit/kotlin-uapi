package edu.byu.uapi.compiler

import org.slf4j.LoggerFactory
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * Process:
 *
 * For each annotated element (@CollectionParams)
 *  - Validate and build model
 */
abstract class ProcessorCommon(
    val supportedAnnotations: Set<KClass<*>>
) : AbstractProcessor() {

    protected val LOG = LoggerFactory.getLogger(javaClass)

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

    override fun getSupportedAnnotationTypes() = supportedAnnotations.map { it.java.canonicalName }.toSet()

    protected lateinit var elements: Elements
    protected lateinit var types: Types
    protected lateinit var messages: Messager
    protected lateinit var files: Filer

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elements = processingEnv.elementUtils
        types = processingEnv.typeUtils
        messages = processingEnv.messager
        files = processingEnv.filer
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val annotationMirrors = this.supportedAnnotations.map { elements.getTypeElement(it.java.canonicalName) to it }.toMap()
        val map = annotations.map {
            annotationMirrors.getValue(it) to roundEnv.getElementsAnnotatedWith(it) as Set<Element>
        }.toMap()

        this.afterRound(map, roundEnv)

        if (roundEnv.processingOver()) {
            afterFinalRound(map, roundEnv)
        }

        return this.doProcess(map, roundEnv)
    }

    abstract fun doProcess(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    ): Boolean

    abstract fun afterRound(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    )

    abstract fun afterFinalRound(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    )

    protected fun logInfo(message: String, element: Element, annotation: AnnotationMirror, annotationValue: AnnotationValue) {
        messages.printMessage(Diagnostic.Kind.NOTE, message, element, annotation, annotationValue)
        LOG.info(message)
    }

    protected fun logInfo(message: String, element: Element) {
        messages.printMessage(Diagnostic.Kind.NOTE, message, element)
        LOG.info(message)
    }

    protected fun logInfo(message: String) {
        messages.printMessage(Diagnostic.Kind.NOTE, message)
        LOG.info(message)
    }

    protected fun logWarn(message: String) {
        messages.printMessage(Diagnostic.Kind.WARNING, message)
        LOG.warn(message)
    }

    protected fun logError(message: String) {
        messages.printMessage(Diagnostic.Kind.ERROR, message)
        LOG.error(message)
    }

}
