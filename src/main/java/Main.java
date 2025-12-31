import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {


    private static Optional<Path> findExecutable(String command, String[] paths) {

        for (String p : paths) {
            Path path = Paths.get(p);

            if (Files.isRegularFile(path)
                    && Files.isExecutable(path)
                    && path.getFileName().toString().equals(command)) {
                return Optional.of(path);
            }

            if (!Files.isDirectory(path)) {
                continue;
            }

            try (Stream<Path> files = Files.list(path)) {
                Optional<Path> match = files
                        .filter(Files::isRegularFile)
                        .filter(Files::isExecutable)
                        .filter(f -> f.getFileName().toString().equals(command))
                        .findFirst();

                if (match.isPresent()) {
                    return match;
                }
            } catch (IOException ignored) {
                // skip unreadable dirs
            }
        }

        return Optional.empty();
    }


    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        Set<String> set = Set.of("exit", "echo", "type");
        String path = System.getenv("PATH");
        String[] paths = path.split(File.pathSeparator);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if ("exit".equals(input)) {
                break;
            } else if (input.startsWith("echo")) {
                input = input.substring(4);
                System.out.println(input.trim());
            } else if (input.startsWith("type")) {
                String s[] = input.split(" ");
                if (s.length == 2) {
                    if (set.contains(s[1])) {
                        System.out.printf("%s is a shell builtin", s[1]);
                        System.out.println();
                    } else {
                        Optional<Path> commandExecutable = findExecutable(s[1], paths);
                        if (commandExecutable.isPresent())
                            System.out.printf("%s is %s", s[1], commandExecutable.get());
                        else
                            System.out.printf("%s: not found", s[1]);
                        System.out.println();
                    }
                } else {
                    System.out.printf("%s: not found", Arrays.stream(s).skip(1).collect(Collectors.joining(" ")));
                    System.out.println();
                }
            } else {
                String[] commandArgs = input.split(" ");
                Optional<Path> commandExecutable = findExecutable(commandArgs[0], paths);
                if (commandExecutable.isPresent()) {
                    ProcessBuilder pb = new ProcessBuilder(commandArgs);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                } else {
                    System.out.printf("%s: command not found%n", input);
                    System.out.println();
                }
            }
        }
        scanner.close();
    }
}
