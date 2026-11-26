# Build and consumer smoke test

GitHub Actions builds Gradle plugin 0.8.5 with Gradle 8.14 and Java 21.
Candid, Agent and Management are built from pinned revisions into an empty
Maven repository for this run. Codegen is not used. The main project runs
its unit tests and checks. A separate build then applies the plugin via its
locally built marker, not its source or a released artifact.

On a disposable, checksum-verified DFX 0.32.0 replica on loopback, DFX creates
an empty canister with a throwaway CI identity. The Gradle plugin installs the
tracked example WASM, sends an UPDATE, reads the value through a QUERY, and
stops, uninstalls and deletes the canister. An independent DFX status must
report the exact deleted canister missing. No workstation identity, mainnet,
release or remote publishing is involved. The reports contain source revisions,
artifact hashes, JUnit results and smoke logs, not the throwaway key.
