# find-aot-deps

A Clojure library to identify .jars containing transitive AOT dependencies.

Q: What does that mean?
A: When you AOT a namespace, all functions in the namespace are compiled, _as well as all of the namespaces dependencies_.

Q: So?
A: This can cause problems in dependency management, . Consider:

You depend on version 1.0.0 of library A, and 1.0.0 of library
B. Library B depends on 0.9.0 of library A, and AOTs a namespace that
depends on library A. When you build an uberjar containing your app
code plus your dependencies, it will contain the .clj source files for
1.0.0 of library A, and _as well as the AOT'd class files for
0.9.0_. That can break your app in nasty ways, because of interface
and protocol mismatches.

## Usage

(require '[find-aot-deps.core :as fad])

```clojure
(fad/aot-clj? "~/.m2/repository/foo/bar/bar-1.0.0.jar")
```

And for your convenience:

```clojure
(fad/offending-jars)
```

Which scans your ~/.m2 and prints to the repl all jars that contain AOT'd CLJ code that doesn't belong to the project.


## License

Copyright © 2018 Allen Rohner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
