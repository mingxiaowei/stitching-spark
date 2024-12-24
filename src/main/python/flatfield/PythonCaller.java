import java.io.BufferedReader; 
import java.io.InputStreamReader; 

public class PythonCaller {
    
    public static void main(String[] args) {
        try { 
            // Execute the Python script 
            Process process = Runtime.getRuntime().exec("python hello.py"); 
 
            // Read the output from the command 
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); 
            String line; 
            while ((line = reader.readLine()) != null) { 
                System.out.println(line); 
            } 
 
            // Wait for the process to complete 
            int exitCode = process.waitFor(); 
            System.out.println("Exited with code: " + exitCode); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
}
