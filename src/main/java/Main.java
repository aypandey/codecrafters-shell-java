import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        Set<String> set = Set.of("exit", "echo", "type");
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
             if(s.length == 2 && set.contains(s[1])) {
                 System.out.printf("%s is a shell builtin", s[1]);
             } else  {
                 System.out.printf("%s: not found", Arrays.stream(s).skip(1).collect(Collectors.joining(" ")));
             }
         } else System.out.printf("%s: command not found", input);
         System.out.println();
        }
        scanner.close();
    }
}
