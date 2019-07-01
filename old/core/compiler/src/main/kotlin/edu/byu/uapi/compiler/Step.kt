package edu.byu.uapi.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.SetMultimap
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import kotlin.reflect.KClass

abstract class Step<Type: Annotation>(
    val type: KClass<Type>,
    val env: ProcessingEnvironment
): BasicAnnotationProcessor.ProcessingStep {

    protected val elementUtil = env.elementUtils
    protected val typeUtil = env.typeUtils
    protected val log = env.messager
    protected val files = env.filer

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        val annotations: Set<Element> = elementsByAnnotation[type.java]

        annotations.forEach { processElement(it, env) }

        return emptySet()
    }

    protected abstract fun processElement(element: Element, env: ProcessingEnvironment)

    override fun annotations() = mutableSetOf(type.java)
}

typealias StepFactory<Type> = (env: ProcessingEnvironment) -> Step<Type>
