@_implementationOnly import KotlinBridges_main
import KotlinRuntime
import KotlinRuntimeSupport

public final class MyObject: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged {
    public static var shared: main.MyObject {
        get {
            return main.MyObject(__externalRCRef: __root___MyObject_get())
        }
    }
    private override init() {
        fatalError()
    }
    package override init(
        __externalRCRef: Swift.UnsafeMutableRawPointer?
    ) {
        super.init(__externalRCRef: __externalRCRef)
    }
}
public func getMainObject() -> KotlinRuntime.KotlinBase {
    return KotlinRuntime.KotlinBase(__externalRCRef: __root___getMainObject())
}
public func isMainObject(
    obj: KotlinRuntime.KotlinBase
) -> Swift.Bool {
    return __root___isMainObject__TypesOfArguments__KotlinRuntime_KotlinBase__(obj.__externalRCRef())
}
