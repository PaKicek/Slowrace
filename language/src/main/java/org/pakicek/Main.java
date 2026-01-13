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

        String command = args[0];
        String inputFile = args[1];
        String[] programArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];

        try {
            switch (command) {
                case "run" -> handleRunSource(inputFile, programArgs);
                case "compile" -> {
                    String outputFile = (args.length >= 3) ? args[2] : addExtension(inputFile);
                    handleCompile(inputFile, outputFile);
                }
                case "exec" -> handleExecBytecode(inputFile, programArgs);
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

    /**
     * Mode 1: Compile source code directly to memory and execute it.
     */
    private static void handleRunSource(String filename, String[] args) throws IOException {
        long start = System.currentTimeMillis();

        // 1. Read Source
        String source = Files.readString(Path.of(filename));

        // 2. Compile pipeline
        ProgramImage image = compilePipeline(source);

        // 3. Run VM
        VirtualMachine vm = new VirtualMachine();
        vm.run(image, args);

        long end = System.currentTimeMillis();
        System.out.println("\n[Finished in " + (end - start) + "ms]");
    }

    /**
     * Mode 2: Compile source code and save to .srbyte file.
     */
    private static void handleCompile(String inputFile, String outputFile) throws IOException {
        System.out.println("Compiling " + inputFile + "...");

        String source = Files.readString(Path.of(inputFile));
        ProgramImage image = compilePipeline(source);

        BytecodeIO.write(image, outputFile);
        System.out.println("Output written to " + outputFile);
    }

    /**
     * Mode 3: Load compiled bytecode from file and execute it.
     */
    private static void handleExecBytecode(String filename, String[] args) throws IOException {
        ProgramImage image = BytecodeIO.read(filename);

        VirtualMachine vm = new VirtualMachine();
        vm.run(image, args);
    }

    /**
     * Shared logic: Lexer -> Parser -> Compiler
     */
    private static ProgramImage compilePipeline(String source) {
        // 1. Lexing
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        // 2. Parsing
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parse();

        // 3. Compiling
        BytecodeCompiler compiler = new BytecodeCompiler();
        return compiler.compile(ast);
    }

    private static void printUsage() {
        System.out.println("Slowrace Language Compiler & VM");
        System.out.println("Usage:");
        System.out.println("  run     <source.sr>             Compile and run source code immediately");
        System.out.println("  compile <source.sr> [out.file]  Compile source to bytecode file");
        System.out.println("  exec    <program.srbyte>        Execute compiled bytecode");
    }

    private static String addExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return filename + ".srbyte";
        }
        return filename.substring(0, dotIndex) + ".srbyte";
    }
}