import os
import subprocess
import time
import sys

# --- Configuration ---
PROJECT_ROOT = "../language"  # Folder containing pom.xml
EXAMPLES_DIR = "."
JAR_NAME_PATTERN = "language-1.0-SNAPSHOT.jar" # Maven default name usually

# List of benchmarks: (filename, [arguments])
BENCHMARKS = [
    ("factorial.sr", ["10"]),
    ("factorial.sr", ["20"]),
    ("factorial.sr", ["21"]),
    ("factorial.sr", ["100"]),
    ("factorial.sr", ["5000"]),
    ("factorial_iterative.sr", ["10"]),
    ("factorial_iterative.sr", ["20"]),
    ("factorial_iterative.sr", ["21"]),
    ("factorial_iterative.sr", ["100"]),
    ("factorial_iterative.sr", ["5000"]),
    ("fibonacci.sr", ["10"]),
    ("fibonacci.sr", ["100"]),
    ("fibonacci.sr", ["1000"]),
    ("fibonacci.sr", ["10000"]),
    ("fibonacci.sr", ["100000"]),
    ("merge_sort.sr", ["1000", "-1000000000", "1000000000"]),
    ("quick_sort.sr", ["1000", "-1000000000", "1000000000"]),
    ("quick_sort.sr", ["10000", "-1000000000", "1000000000"]),
    ("quick_sort.sr", ["100000", "-1000000000", "1000000000"]),
    ("quick_sort.sr", ["1000000", "-1000000000", "1000000000"]),
    ("sieve.sr", ["10000"]),
    ("sieve.sr", ["100000"]),
    ("sieve.sr", ["1000000"]),
    ("sieve.sr", ["10000000"]),
    ("nbody.sr", []),
    ("jit_arithmetic_identities.sr", ["50000"]),
    ("jit_arithmetic_identities.sr", ["60000"]),
    ("jit_arithmetic_identities.sr", ["70000"]),
    ("jit_arithmetic_identities.sr", ["80000"]),
    ("jit_arithmetic_identities.sr", ["90000"]),
    ("jit_dead_code_elimination.sr", ["500"]),
    ("jit_dead_code_elimination.sr", ["600"]),
    ("jit_dead_code_elimination.sr", ["700"]),
    ("jit_dead_code_elimination.sr", ["800"]),
    ("jit_dead_code_elimination.sr", ["900"])
]

def build_project():
    print("Building project with Maven...")
    # Using shell=True to find 'mvn' in path on Windows/Linux
    cmd = "mvn clean package -DskipTests"

    # On Windows "mvn.cmd", on Linux/Mac "mvn"
    mvn_executable = "mvn.cmd" if os.name == 'nt' else "mvn"

    try:
        subprocess.check_call([mvn_executable, "clean", "package", "-DskipTests"], cwd=PROJECT_ROOT)
        print("Build successful.\n")
    except subprocess.CalledProcessError:
        print("Build failed. Please check Maven output.")
        sys.exit(1)
    except FileNotFoundError:
        print(f"Maven executable '{mvn_executable}' not found in PATH.")
        sys.exit(1)

def find_jar():
    target_dir = os.path.join(PROJECT_ROOT, "target")
    if not os.path.exists(target_dir):
        print(f"Target directory {target_dir} does not exist.")
        sys.exit(1)

    for file in os.listdir(target_dir):
        if file.endswith(".jar") and "original" not in file and "shaded" not in file:
            # We are looking for the jar with dependencies if created by shade/assembly plugin
            # Or the standard jar if dependencies are on classpath.
            # Assuming your pom generates a standard jar or you configured a fat jar.
            # Let's pick the one matching the name or containing 'dependencies' if you used assembly plugin
            if "dependencies" in file:
                return os.path.join(target_dir, file)
            # Fallback to standard jar
            if file == JAR_NAME_PATTERN:
                return os.path.join(target_dir, file)

    # If using maven-assembly-plugin as discussed before, name is usually ...-jar-with-dependencies.jar
    # Try finding any jar that looks executable
    for file in os.listdir(target_dir):
        if file.endswith(".jar") and "original" not in file:
             return os.path.join(target_dir, file)

    print("Could not find executable JAR file.")
    sys.exit(1)

def run_benchmark(jar_path, source_file, args, use_jit):
    full_path = os.path.join(EXAMPLES_DIR, source_file)
    if not os.path.exists(full_path):
        print(f"File {full_path} not found.")
        return None

    cmd = ["java", "-jar", jar_path]
    if not use_jit:
        cmd.append("--no-jit")
    cmd += ["run", full_path] + args

    start_time = time.time()
    try:
        subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
        end_time = time.time()
        return (end_time - start_time) * 1000 # ms
    except subprocess.CalledProcessError as e:
        print(f"Error running {source_file}: {e.stderr.decode('utf-8')}")
        return None

def main():
    # 1. Build
    build_project()

    # 2. Find Jar
    jar_path = find_jar()
    print(f"Using JAR: {jar_path}\n")

    # 3. Run Benchmarks
    print(f"{'Benchmark':<25} | {'Args':<30} | {'JIT (ms)':<10} | {'No-JIT (ms)':<12} | {'Speedup':<10}")
    print("-" * 100)

    for filename, args in BENCHMARKS:
        t_jit = run_benchmark(jar_path, filename, args, True)
        t_nojit = run_benchmark(jar_path, filename, args, False)
        arg_str = " ".join(args)
        s_jit = f"{t_jit:.2f}" if t_jit else "FAIL"
        s_nojit = f"{t_nojit:.2f}" if t_nojit else "FAIL"

        speedup = "N/A"
        if t_jit and t_nojit and t_jit > 0:
            ratio = t_nojit / t_jit
            speedup = f"{ratio:.2f}x"

            print(f"{filename:<25} | {arg_str:<30} | {s_jit:>10} | {s_nojit:>12} | {speedup:>10}")

    print("-" * 100)

if __name__ == "__main__":
    main()