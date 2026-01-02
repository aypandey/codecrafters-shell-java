import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final String HOME_DIR = System.getenv("HOME");
    private final Set<String> builtinCommands;
    private final Path[] searchPaths;
    private final Map<String, Path> executableCache;
    private Path currentDirectory;

    public Main() {
        this.builtinCommands = Set.of("exit", "echo", "type", "pwd", "cd");
        this.searchPaths = initializeSearchPaths();
        this.executableCache = new HashMap<>();
        this.currentDirectory = Paths.get("").toAbsolutePath();
    }

    private Path[] initializeSearchPaths() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return new Path[0];

        return Arrays.stream(pathEnv.split(File.pathSeparator))
                .map(Paths::get)
                .filter(Files::isDirectory)
                .toArray(Path[]::new);
    }

    private Optional<Path> findExecutable(String command) {
        // Check cache first
        if (executableCache.containsKey(command)) {
            Path cached = executableCache.get(command);
            if (Files.exists(cached) && Files.isExecutable(cached)) {
                return Optional.of(cached);
            }
            executableCache.remove(command);
        }

        // Search in PATH
        for (Path dir : searchPaths) {
            Path executable = dir.resolve(command);
            if (Files.isRegularFile(executable) && Files.isExecutable(executable)) {
                executableCache.put(command, executable);
                return Optional.of(executable);
            }
        }

        return Optional.empty();
    }

    private Optional<Path> handleCd(String arg) {
        if (arg == null || arg.isEmpty()) {
            arg = HOME_DIR;
        }

        arg = arg.replace("~", HOME_DIR);
        Path target = Paths.get(arg);

        if (!target.isAbsolute()) {
            target = currentDirectory.resolve(target);
        }

        target = target.normalize();

        if (Files.isDirectory(target)) {
            return Optional.of(target);
        }

        return Optional.empty();
    }

    private void executeEcho(String[] args) {
        if (args.length > 1) {
            System.out.println(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        } else {
            System.out.println();
        }
    }

    private void executePwd() {
        System.out.println(currentDirectory);
    }

    private void executeCd(String[] args) {
        String target = args.length > 1 ? args[1] : HOME_DIR;
        Optional<Path> newDir = handleCd(target);

        if (newDir.isPresent()) {
            currentDirectory = newDir.get();
        } else {
            System.out.println("cd: " + target + ": No such file or directory");
        }
    }

    private void executeType(String[] args) {
        if (args.length < 2) {
            System.out.println("type: missing argument");
            return;
        }

        String command = args[1];

        if (builtinCommands.contains(command)) {
            System.out.println(command + " is a shell builtin");
        } else {
            Optional<Path> executable = findExecutable(command);
            if (executable.isPresent()) {
                System.out.println(command + " is " + executable.get());
            } else {
                System.out.println(command + ": not found");
            }
        }
    }

    private void executeExternal(String[] args) throws IOException {
        Optional<Path> executable = findExecutable(args[0]);

        if (executable.isEmpty()) {
            System.out.println(args[0] + ": command not found");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(currentDirectory.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String[] parseInput(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                // Single quote toggles single quote mode (unless in double quotes)
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                // Double quote toggles double quote mode (unless in single quotes)
                inDoubleQuote = !inDoubleQuote;
            } else if (Character.isWhitespace(c) && !inSingleQuote && !inDoubleQuote) {
                // Whitespace outside quotes - argument separator
                if (currentArg.length() > 0) {
                    args.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                // Regular character or character inside quotes
                currentArg.append(c);
            }
        }

        // Add the last argument if any
        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }

        return args.toArray(new String[0]);
    }

    private void processCommand(String input) throws IOException {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] args = parseInput(input);
        String command = args[0];

        switch (command) {
            case "exit":
                throw new ExitShellException();
            case "echo":
                executeEcho(args);
                break;
            case "pwd":
                executePwd();
                break;
            case "cd":
                executeCd(args);
                break;
            case "type":
                executeType(args);
                break;
            default:
                executeExternal(args);
        }
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("$ ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine();

                try {
                    processCommand(input);
                } catch (ExitShellException e) {
                    break;
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static class ExitShellException extends RuntimeException {}

    public static void main(String[] args) throws Exception {
        Main shell = new Main();
        shell.run();
    }
}