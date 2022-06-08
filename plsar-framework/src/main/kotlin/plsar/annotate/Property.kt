package plsar.annotate

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Property(val value: String)