import KotlinBridges
import KotlinRuntime

public typealias RegularInteger = Swift.Int32
public extension main.typealiases.inner {
    public typealias Foo = main.typealiases.Foo
    public typealias LargeInteger = Swift.Int64
    public class Bar {
        public init() {
            fatalError()
        }
    }
}
public extension main.typealiases {
    public typealias Bar = main.typealiases.inner.Bar
    public typealias SmallInteger = Swift.Int16
    public class Foo {
        public init() {
            fatalError()
        }
    }
}
public enum typealiases {
    public enum inner {
    }
}
