"""Kill acknowledged recording with Room open, then verify the isolated fixture.

Requires installed debug/test APKs. Does not change airplane mode or the normal DB.
Runs on CI and Windows; logs contain only synthetic test results.
"""
import os
import subprocess
import tempfile
import time

ADB = os.environ.get("ADB", "adb")
PACKAGE = "com.quantumfighter.gymtracker.debug"
RUNNER = PACKAGE + ".test/androidx.test.runner.AndroidJUnitRunner"
TEST = "com.quantumfighter.gymtracker.WorkoutRelaunchTest#t06CommittedSetsSurviveProcessDeathAndRetry"
FLAGS = subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0


def adb(*args):
    return subprocess.run([ADB, *args], capture_output=True, text=True, timeout=30, creationflags=FLAGS)


def instrument(phase):
    return [ADB, "shell", "am", "instrument", "-w", "-e", "class", TEST,
            "-e", "workoutPhase", phase, RUNNER]


adb("shell", "run-as", PACKAGE, "rm", "-f", "files/b04-kill-ready", "files/b04-kill-cleanup").check_returncode()
with tempfile.TemporaryFile(mode="w+t") as output:
    process = subprocess.Popen(instrument("abrupt"), stdout=output, stderr=subprocess.STDOUT, creationflags=FLAGS)
    try:
        deadline = time.monotonic() + 90
        while True:
            marker = adb("shell", "run-as", PACKAGE, "cat", "files/b04-kill-ready")
            if marker.returncode == 0 and marker.stdout.strip().isdigit():
                pid = marker.stdout.strip()
                break
            if process.poll() is not None or time.monotonic() >= deadline:
                output.seek(0)
                raise AssertionError("Open-database acknowledgement gate not reached:\n" + output.read())
            time.sleep(0.25)
        assert pid in adb("shell", "pidof", PACKAGE).stdout.split(), "Recorder process already exited"
        assert adb("shell", "run-as", PACKAGE, "test", "-f", "files/b04-kill-cleanup").returncode != 0
        adb("shell", "am", "force-stop", PACKAGE).check_returncode()
        process.wait(timeout=20)
        assert pid not in adb("shell", "pidof", PACKAGE).stdout.split(), "Recorder process survived force-stop"
        assert adb("shell", "run-as", PACKAGE, "test", "-f", "files/b04-kill-cleanup").returncode != 0, "Orderly cleanup ran before kill"
        print("PASS: force-stop killed acknowledged recorder with Room open; finally did not run", flush=True)
    finally:
        if process.poll() is None:
            adb("shell", "am", "force-stop", PACKAGE)
            process.wait(timeout=20)

verified = subprocess.run(instrument("verify"), capture_output=True, text=True, timeout=90, creationflags=FLAGS)
print(verified.stdout)
assert verified.returncode == 0 and "OK (1 test)" in verified.stdout, verified.stderr
