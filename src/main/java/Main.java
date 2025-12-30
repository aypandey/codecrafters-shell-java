import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner scanner = new Scanner(System.in);
        while (true) {
         System.out.print("$ ");
         String input = scanner.nextLine();
         if("exit".equals(input)) {
             break;
         }
         else if(input.startsWith("echo")) {
             input = input.substring(4);
             System.out.print(input.trim());
         }
         else System.out.printf("%s: command not found", input);
         System.out.println();
        }
        scanner.close();
    }
}
