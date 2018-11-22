package edu.byu.uapi.validation.hibernate

import edu.byu.uapi.spi.validation.ValidationConstraint
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.spi.validation.ValidationFailure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import javax.validation.metadata.BeanDescriptor
import javax.validation.metadata.ConstraintDescriptor
import kotlin.reflect.KClass
import edu.byu.uapi.spi.validation.Validator as UAPIValidator

private val validationFactory: ValidatorFactory by lazy { Validation.buildDefaultValidatorFactory() }

object HibernateValidationEngine: ValidationEngine {

    private val LOG: Logger = LoggerFactory.getLogger(HibernateValidationEngine::class.java)

    private val validator: Validator by lazy { validationFactory.validator}

    override fun <T : Any> validatorFor(type: KClass<T>): UAPIValidator<T> {
        LOG.debug("Creating validator for {}", type)
        val desc = validator.getConstraintsForClass(type.java)
        return if (desc.isBeanConstrained) {
            LOG.debug("Creating Hibernate validator")
            HibernateValidator(validator, desc)
        } else {
            LOG.debug("Class has no constraints; using no-op validator")
            UAPIValidator.noop()
        }
    }
}

internal class HibernateValidator<T: Any>(private val validator: Validator, beanDescriptor: BeanDescriptor): UAPIValidator<T> {
    override fun validate(subject: T): Set<ValidationFailure> {
        return validator.validate(subject).map { f ->
            ValidationFailure(
                f.propertyPath.joinToString(".") { it.name },
                f.message
            )
        }.toSet()
    }

    private val constraints: Set<ValidationConstraint> by lazy {
        val classLevel: Set<ConstraintDescriptor<*>> = beanDescriptor.constraintDescriptors
        val propertyLevel: List<ConstraintDescriptor<*>> = beanDescriptor.constrainedProperties.flatMap { it.constraintDescriptors }

        (classLevel + propertyLevel ).map { c -> ValidationConstraint(
            "", c.messageTemplate
        ) }.toSet()
    }

    override fun describeConstraints(): Set<ValidationConstraint> = constraints
}
