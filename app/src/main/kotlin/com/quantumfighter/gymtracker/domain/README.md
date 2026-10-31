# Pure rule boundary

Place only reusable/complex pure Kotlin rules and their value types here. Inputs include
observed times and immutable data; rules must not import Android, Compose, repositories,
Room or feature UI. JUnit tests run without an emulator. Ordinary repository operations
do not need use-case wrappers.

B-02 adds the validated `Exercise` value model and measurement/load/origin tokens.
Its recording eligibility excludes archived and non-repetition definitions. There is
no workout or progression policy. JVM architecture checks enforce dependency direction.
