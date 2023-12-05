// ISSUE: KT-54400
// CHECK_TYPE_WITH_EXACT

fun test() {
    val buildee = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>build<!> {
        typeVariableMutableProperty = {}
    }
    // exact type equality check — turns unexpected compile-time behavior into red code
    // considered to be non-user-reproducible code for the purposes of these tests
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>checkExactType<!><<!CANNOT_INFER_PARAMETER_TYPE!>Buildee<() -> Unit><!>>(buildee)
}




class Buildee<TV> {
    var typeVariableMutableProperty: TV
        get() = storage
        set(value) { storage = value }
    private var storage: TV = null!!
}

fun <PTV> build(instructions: Buildee<PTV>.() -> Unit): Buildee<PTV> {
    return Buildee<PTV>().apply(instructions)
}
