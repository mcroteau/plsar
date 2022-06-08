package plsar.annotate

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Dependency 