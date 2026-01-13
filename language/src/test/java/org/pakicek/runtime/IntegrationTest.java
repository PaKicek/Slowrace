package org.pakicek.runtime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pakicek.compiler.BytecodeCompiler;
import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

public class IntegrationTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private void runCode(String code) {
        runCode(code, new String[0]);
    }

    private void runCode(String code, String[] args) {
        List<Token> tokens = new Lexer(code).scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();
        BytecodeCompiler compiler = new BytecodeCompiler();
        ProgramImage image = compiler.compile(program);
        VirtualMachine vm = new VirtualMachine();
        vm.run(image, args);
    }

    @Test
    public void testArithmeticAndPrint() {
        String code = """
            main (int argc, array string argv[]) {
                int a = 10;
                int b = 20;
                print(a + b);
            }
        """;
        runCode(code);
        assertEquals("30", outContent.toString().trim());
    }

    @Test
    public void testControlFlowLoop() {
        String code = """
            main (int argc, array string argv[]) {
                int sum = 0;
                for (int i = 0; i < 5; i++) {
                    sum = sum + 1;
                }
                print(sum);
            }
        """;
        runCode(code);
        assertEquals("5", outContent.toString().trim());
    }

    @Test
    public void testFactorial() {
        String code = """
            func int factorial (int n) {
                int result = 1;
                for (int i = 2; i <= n; i++) {
                        result = result * i;
                    }
                return result;
            }
        
            main (int argc, array string argv[]) {
                int n = 21;
                int res = factorial(n);
                print(res);
            }
        """;
        runCode(code);
        assertEquals("51090942171709440000", outContent.toString().trim());
    }

    @Test
    public void testFloatingPoint() {
        String code = """
            main (int argc, array string argv[]) {
                float a = 5.0;
                float b = 2.0;
                print(a / b);
            }
        """;
        runCode(code);
        assertEquals("2.5", outContent.toString().trim());
    }

    @Test
    public void testArraysAndGC() {
        // This test creates garbage arrays in a loop to trigger GC аnd validates that the final array is still intact
        String code = """
            main (int argc, array string argv[]) {
                array int keeper[5];
                keeper[0] = 1337;
        
                // Allocation loop to trigger GC (threshold is 1000)
                for (int i = 0; i < 1500; i++) {
                    array int garbage[10];
                    garbage[0] = i;
                }
        
                print(keeper[0]);
            }
        """;
        runCode(code);
        assertEquals("1337", outContent.toString().trim());
    }

    @Test
    public void testStructs() {
        String code = """
            struct Vector {
                int x;
                int y;
            }
        
            main (int argc, array string argv[]) {
                Vector v;
                v.x = 10;
                v.y = 20;
                print(v.x + v.y);
            }
        """;
        runCode(code);
        assertEquals("30", outContent.toString().trim());
    }

    @Test
    public void testRecursionAndJit() {
        // Recursive Fibonacci to test function calls, stack depth and trigger JIT
        String code = """
            func int fib(int n) {
                if (n <= 1) {
                    return n;
                }
                return fib(n - 1) + fib(n - 2);
            }
        
            main (int argc, array string argv[]) {
                // fib(10) = 55. This makes enough calls to trigger JIT if threshold is low.
                int res = fib(10);
                print(res);
            }
        """;
        runCode(code);
        assertEquals("55", outContent.toString().trim());
    }

    @Test
    public void testLogicShortCircuit() {
        // Tests that second part of && / || is NOT executed if not needed
        String code = """
            func bool side_effect() {
                print("Effect");
                return true;
            }
        
            main (int argc, array string argv[]) {
                // &&: false && ... (should NOT print)
                bool a = false && side_effect();
        
                // ||: true || ... (should NOT print)
                bool b = true || side_effect();
        
                print("Done");
            }
        """;
        runCode(code);
        assertEquals("Done", outContent.toString().trim());
    }

    @Test
    public void testArraySorting() {
        String code = """
            func void bubble_sort(array int arr) {
                int n = len(arr);
                for (int i = 0; i < n - 1; i++) {
                    for (int j = 0; j < n - i - 1; j++) {
                        if (arr[j] > arr[j + 1]) {
                            int temp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = temp;
                        }
                    }
                }
            }
        
            main (int argc, array string argv[]) {
                array int nums[5];
                nums[0] = 5; nums[1] = 1; nums[2] = 4; nums[3] = 2; nums[4] = 8;
                bubble_sort(nums);
                for (int i = 0; i < 5; i++) {
                    print(nums[i]);
                }
            }
        """;
        runCode(code);
        assertEquals("12458", outContent.toString().trim());
    }

    @Test
    public void testCommandLineArgs() {
        String code = """
            main (int argc, array string argv[]) {
                if (argc > 0) {
                    print(argv[0]);
                }
            }
        """;
        runCode(code, new String[]{"Hello"});
        assertEquals("Hello", outContent.toString().trim());
    }
}