#!/usr/bin/env bash
set -euo pipefail
: "${GITHUB_WORKSPACE:?}" "${RUNNER_TEMP:?}"
cd "$GITHUB_WORKSPACE"
export IC4J_CI_MAVEN_REPO="$RUNNER_TEMP/ic4j-gradle-maven"
reports="$RUNNER_TEMP/ic4j-gradle-reports"
work="$(mktemp -d "$RUNNER_TEMP/ic4j-gradle-fixture.XXXXXX")"
mkdir "$IC4J_CI_MAVEN_REPO" "$reports"
for name in candid agent management plugin; do
    sha="$(git -C "$name" rev-parse HEAD)"
    printf '%s %s\n' "$name" "$sha"
    if [[ "$name" != plugin ]]; then
        var="${name^^}_REV"
        test "$sha" = "${!var}"
    fi
done | tee "$reports/source-commits.txt"

run_gradle() {
    local label="$1" attempt rc log
    shift
    for attempt in 1 2 3; do
        log="$reports/$label-$attempt.log"
        if gradle --no-daemon --console=plain "$@" 2>&1 | tee "$log"; then
            return 0
        else
            rc=$?
        fi
        if [[ "$attempt" == 3 ]] || ! grep -Eq 'Received status code 429|HTTP (status )?429' "$log"; then
            return "$rc"
        fi
        sleep "$((attempt * 12))"
    done
}
g() {
    local project="$1"
    shift
    run_gradle "$project" -Dmaven.repo.local="$IC4J_CI_MAVEN_REPO" \
        --init-script "$GITHUB_WORKSPACE/agent/ci/local-artifacts.init.gradle" -p "$project" "$@"
}
g candid -PrunJavacc=true clean publishToMavenLocal
g agent clean publishToMavenLocal
g management clean publishToMavenLocal
g plugin clean check publishToMavenLocal
python3 - <<'PY'
from pathlib import Path
from xml.etree import ElementTree
results = list(Path('plugin/build/test-results/test').glob('TEST-*.xml'))
cases = [c for result in results for c in ElementTree.parse(result).iter('testcase')]
assert any(c.attrib['classname'].endswith('.PluginTest') and c.find('skipped') is None for c in cases)
PY
(cd "$IC4J_CI_MAVEN_REPO"; find org/ic4j -type f \( -name '*.jar' -o -name '*.pom' -o -name '*.module' \) -print0 | sort -z | xargs -0 sha256sum) >"$reports/inputs.sha256"

archive="$work/dfx.tar.gz"
curl --fail --silent --show-error --location --retry 3 --max-time 180 \
    'https://github.com/dfinity/sdk/releases/download/0.32.0/dfx-0.32.0-x86_64-linux.tar.gz' -o "$archive"
printf '%s  %s\n' 218dad11e0519e11c7a310b5c7cb1eadff109bb5db1ea3432f08c870432a80fc "$archive" | sha256sum -c -
mkdir "$work/bin" "$work/replica" "$work/consumer" "$work/dfx-config"
tar -xzf "$archive" -C "$work/bin" dfx
export PATH="$work/bin:$PATH" DFX_CONFIG_ROOT="$work/dfx-config"
dfx --version | tee "$reports/dfx-version.txt"
test "$(dfx --version)" = 'dfx 0.32.0'
cp plugin/usage-example/hello.wasm "$work/consumer/"
cp plugin/ci/consumer/*.gradle "$work/consumer/"
cd "$work/replica"
cat >dfx.json <<'JSON'
{"canisters":{"fixture":{"type":"custom","candid":"fixture.did","wasm":"../consumer/hello.wasm"}}}
JSON
printf 'service : (text) -> { greet : (text) -> (text); peek : () -> (text) query }\n' >fixture.did
sha256sum "$work/consumer/hello.wasm" >"$reports/fixture.sha256"
export IC4J_CI_IDENTITY="$work/identity.pem"
trap 'cd "$work/replica"; dfx stop >> "$reports/dfx.log" 2>&1 || true; rm -f "$IC4J_CI_IDENTITY"; rm -rf "$DFX_CONFIG_ROOT"' EXIT
(umask 077; openssl ecparam -name secp256k1 -genkey -noout | openssl pkcs8 -topk8 -nocrypt >"$IC4J_CI_IDENTITY")
dfx identity import ci-smoke "$IC4J_CI_IDENTITY" --storage-mode plaintext >"$work/identity.log" 2>&1
dfx start --clean --host 127.0.0.1:4943 --background >"$reports/dfx.log" 2>&1
curl --fail --silent --show-error --retry 20 --retry-delay 1 --retry-connrefused --max-time 3 \
    http://127.0.0.1:4943/api/v2/status >/dev/null
dfx canister create fixture --network local --identity ci-smoke --no-wallet 2>&1 | sed '/^Your seed phrase:/d' | tee "$reports/fixture.log"
export IC4J_CI_FIXTURE
IC4J_CI_FIXTURE="$(dfx canister id fixture --network local)"
[[ "$IC4J_CI_FIXTURE" =~ ^[a-z0-9]+(-[a-z0-9]+)+$ ]]
(cd "$IC4J_CI_MAVEN_REPO"; sha256sum -c "$reports/inputs.sha256")
# Retry only artifact resolution in the harmless configuration preflight.
run_gradle consumer-config -p "$work/consumer" tasks
# Do not retry mutating lifecycle operations.
consumer_task() {
    local label="$1"
    shift
    timeout 180s gradle --no-daemon --console=plain -p "$work/consumer" "$@" --info 2>&1 | tee "$reports/$label.log"
}
consumer_task install install
grep -Fq "Canister $IC4J_CI_FIXTURE is running" "$reports/install.log"
consumer_task update call -PciCall=update
grep -Fxq 'arg0=Hello, Gradle CI!' "$reports/update.log"
consumer_task query call -PciCall=query
grep -Fxq 'arg0=Gradle CI' "$reports/query.log"
consumer_task uninstall uninstall
grep -Fq "Canister $IC4J_CI_FIXTURE deleted" "$reports/uninstall.log"
if dfx canister status "$IC4J_CI_FIXTURE" --network local --identity ci-smoke >"$reports/deleted-status.log" 2>&1; then
    echo "Canister still exists after uninstall"; exit 1
fi
grep -Fq "$IC4J_CI_FIXTURE" "$reports/deleted-status.log"
grep -Eiq 'not found|does not exist' "$reports/deleted-status.log"
printf 'PASS: Gradle plugin install, UPDATE, QUERY, stop and delete on loopback ICP\n' | tee "$reports/result.txt"
