package edu.byu.uapi.compiler.collections

import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import edu.byu.uapi.compiler.ProcessorCommon
import edu.byu.uapi.spi.annotations.CollectionParams
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import kotlin.reflect.KClass


/**
 * Process:
 *
 * For each annotated element (@CollectionParams)
 *  - Validate and build model
 */
@AutoService(Processor::class)
class CollectionParamsProcessor : ProcessorCommon(setOf(CollectionParams::class)) {

    override fun doProcess(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elementsInRound = annotations[CollectionParams::class]
        elementsInRound?.forEach { this.processElement(it) }

        return false
    }

    private fun processElement(element: Element) {
        logInfo("processing", element)

        generateParamsProvider(ParamsModel(
            element.simpleName.toString(),
            elements.getPackageOf(element).qualifiedName.toString(),
            null,
            null,
            null
        ))

    }

    override fun afterRound(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    ) {
    }

    override fun afterFinalRound(
        annotations: Map<KClass<*>, Set<Element>>,
        roundEnv: RoundEnvironment
    ) {
    }


    private fun generateParamsProvider(model: ParamsModel) {
        GeneratedParamsProvider(model).generate().writeTo(files)
    }

}


