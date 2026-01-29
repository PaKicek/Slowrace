package org.pakicek;

import org.pakicek.compiler.BytecodeCompiler;
import org.pakicek.compiler.BytecodeIO;
import org.pakicek.parser.Parser;
import org.pakicek.parser.ast.node.ProgramNode;
import org.pakicek.parser.lexer.Lexer;
import org.pakicek.parser.lexer.Token;
import org.pakicek.runtime.ProgramImage;
import org.pakicek.runtime.VirtualMachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }

        boolean jitEnabled = true;
        int argStartIndex = 0;

        if (args[0].equals("--no-jit")) {
            jitEnabled = false;
            argStartIndex = 1;
        }

        if (args.length < argStartIndex + 2) {
            printUsage();
            return;
        }

        String command = args[argStartIndex];
        String inputFile = args[argStartIndex + 1];
        String[] programArgs = args.length > argStartIndex + 2 ? Arrays.copyOfRange(args, argStartIndex + 2, args.length) : new String[0];

        try {
            switch (command) {
                case "run" -> handleRunSource(inputFile, programArgs, jitEnabled);
                case "compile" -> {
                    String outputFile = (args.length >= argStartIndex + 3) ? args[argStartIndex + 2] : addExtension(inputFile);
                    handleCompile(inputFile, outputFile);
                }
                case "exec" -> handleExecBytecode(inputFile, programArgs, jitEnabled);
                default -> {
                    System.err.println("Unknown command: " + command);
                    printUsage();
                }
            }
        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Internal error details:", e);
            System.exit(1);
        }
    }

    private static void handleRunSource(String filename, String[] args, boolean jitEnabled) throws IOException {
        long start = System.currentTimeMillis();
        String source = Files.readString(Path.of(filename));
        ProgramImage image = compilePipeline(source);
        VirtualMachine vm = new VirtualMachine();
        vm.setJitEnabled(jitEnabled);
        vm.run(image, args);
        long end = System.currentTimeMillis();
        System.out.println("\n[Finished in " + (end - start) + "ms]");
    }

    private static void handleCompile(String inputFile, String outputFile) throws IOException {
        System.out.println("Compiling " + inputFile + "...");
        String source = Files.readString(Path.of(inputFile));
        ProgramImage image = compilePipeline(source);
        BytecodeIO.write(image, outputFile);
        System.out.println("Output written to " + outputFile);
    }

    private static void handleExecBytecode(String filename, String[] args, boolean jitEnabled) throws IOException {
        ProgramImage image = BytecodeIO.read(filename);
        VirtualMachine vm = new VirtualMachine();
        vm.setJitEnabled(jitEnabled);
        vm.run(image, args);
    }

    private static ProgramImage compilePipeline(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parse();
        BytecodeCompiler compiler = new BytecodeCompiler();
        return compiler.compile(ast);
    }

    private static void printUsage() {
        System.out.println("Slowrace Language Compiler & VM");
        System.out.println("Usage:");
        System.out.println("  [--no-jit] run     <source.sr>             Compile and run source code immediately");
        System.out.println("  compile <source.sr> [out.file]  Compile source to bytecode file");
        System.out.println("  [--no-jit] exec    <program.srbyte>        Execute compiled bytecode");
    }

    private static String addExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return filename + ".srbyte";
        }
        return filename.substring(0, dotIndex) + ".srbyte";
    }
}