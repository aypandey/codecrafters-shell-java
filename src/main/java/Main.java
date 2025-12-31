import java.io.File;
import java.io.IOException;
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

    private static String checkCommand(String command, String[] paths, int i) throws IOException {
        if(i >= paths.length) {
            return "0";
        }
        Path dir = Paths.get(paths[i]);
        try (Stream<Path> allFiles = Files.list(dir)) {
            Optional<Path> commandFile = allFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(command))
                    .filter(Files::isExecutable)
                    .findFirst();
            if (commandFile.isPresent()) {
                return "1-"+commandFile.get().toString();
            } else {
                return checkCommand(command, paths, i+1);
            }
        }
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
         if("exit".equals(input)) {
             break;
         }
         else if(input.startsWith("echo")) {
             input = input.substring(4);
             System.out.print(input.trim());
         } else if (input.startsWith("type")) {
             String s[] = input.split(" ");
             if(s.length == 2) {
                 if(set.contains(s[1])) {
                     System.out.printf("%s is a shell builtin", s[1]);
                 } else {
                     String checkCommand = checkCommand(s[1], paths, 0);
                     if(checkCommand.startsWith("1"))
                         System.out.printf("%s is %s", s[1], checkCommand.substring(2));
                     else
                         System.out.printf("%s: not found", s[1]);
                 }
             } else {
                 System.out.printf("%s: not found", Arrays.stream(s).skip(1).collect(Collectors.joining(" ")));
             }
         } else System.out.printf("%s: command not found", input);
         System.out.println();
        }
        scanner.close();
    }
}
