This directory is needed even if all the benchmarks are only in `commonBenchmarks` and there are no JVM-specific
benchmarks. The benchmark configuration in `build.gradle.kts` must refer to this directory, and therefore this directory
must exist.

To run the benchmarks with a specific profiler (like `gc` in the following command), change to the project root
directory in a terminal, and issue the following command:

```shell
java -jar build/benchmarks/jvmBenchmarks/jars/tree-ware-kotlin-core-jvmBenchmarks-jmh-1.0-SNAPSHOT.jar -prof gc
```