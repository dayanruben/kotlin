// RUN_PIPELINE_TILL: BACKEND
open class Base(x: String, y: Int)

fun test(x: Any, y: Int?) {
  if (x !is String) return
  if (y == null) return

  class Local: Base(<!DEBUG_INFO_SMARTCAST!>x<!>, <!DEBUG_INFO_SMARTCAST!>y<!>) {
  }
}

/* GENERATED_FIR_TAGS: classDeclaration, equalityExpression, functionDeclaration, ifExpression, isExpression, localClass,
nullableType, primaryConstructor, smartcast */
