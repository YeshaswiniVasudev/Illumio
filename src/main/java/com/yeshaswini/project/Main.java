package main.java.com.yeshaswini.project;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // HashMap to store the lookup data with destination port and protocol as the key and tag as the value.
    public static final HashMap<String, String> lookUpMap = new HashMap<>();

    // HashMap to count the occurrences of each tag.
    public static final HashMap<String, Integer> tagMatchMap = new HashMap<>();

    // HashMap to count the occurrences of each port/protocol combination.
    public static final HashMap<String, Integer> portProtocolCombination = new HashMap<>();

    // Method to parse the flow log data from an InputStream.
    public static void flowLogParse(InputStream inputStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            // Read each line of the flow log.
            while ((line = br.readLine()) != null) {
                processFlowLogs(line);
            }
            // Write the output counts to a file.
            writeOutputToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to write the tag counts and port/protocol combination counts to an output file.
    public static void writeOutputToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output/output.txt"))) {
            writer.write("Tag Counts:\nTag,Count\n");
            // Write the tag counts to the file.
            for (Map.Entry<String, Integer> entry : tagMatchMap.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            writer.write("\nPort/Protocol Combination Counts:\nPort,Protocol,Count\n");
            // Write the port/protocol combination counts to the file.
            for (Map.Entry<String, Integer> entry : portProtocolCombination.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to parse the lookup data from an InputStream.
    public static void lookUpParse(InputStream inputStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            // Read each line of the lookup data.
            while ((line = br.readLine()) != null) {
                processLookUp(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to process a single line from the lookup data.
    public static void processLookUp(String line) {
        String[] parts = line.split(",");
        // Ensure the line contains at least 3 parts (destPort, protocol, tag).
        if (parts.length < 3) {
            return;
        }

        // Extract destination port, protocol, and tag from the line.
        String destPort = parts[0].trim();
        String protocol = parts[1].trim();
        String tag = parts[2].trim();

        // Create a key by combining destination port and protocol in lowercase.
        String key = destPort.toLowerCase() + " " + protocol.toLowerCase();
        // Add the key and tag to the lookup map.
        lookUpMap.put(key, tag);
    }

    // Method to process a single line from the flow logs.
    public static void processFlowLogs(String line) {
        String[] parts = line.split(" ");
        // Ensure the line contains enough parts to extract relevant data.
        if (parts.length < 8) {
            return;
        }

        String destPort = parts[6]; // Extract destination port from the log line.
        String protocolNumber = parts[7]; // Extract protocol number from the log line.
        int protoNum;
        try {
            // Parse the protocol number.
            protoNum = Integer.parseInt(protocolNumber);
        } catch (NumberFormatException e) {
            return; // If parsing fails, skip processing this line.
        }

        // Get the protocol name from the ProtocolMap based on the protocol number.
        String protocol = ProtocolMap.getProtocolName(protoNum);
        if (protocol == null) {
            return; // If protocol is not recognized, skip processing this line.
        }

        // Create a key by combining destination port and protocol in lowercase.
        String key = destPort.toLowerCase() + " " + protocol.toLowerCase();
        // Check if the key exists in the lookup map.
        if (lookUpMap.containsKey(key)) {
            String tag = lookUpMap.get(key); // Retrieve the associated tag.
            // Increment the count for the found tag.
            tagMatchMap.put(tag, tagMatchMap.getOrDefault(tag, 0) + 1);
        } else {
            // If no tag is found, increment the count for "Untagged".
            tagMatchMap.put("Untagged", tagMatchMap.getOrDefault("Untagged", 0) + 1);
        }

        // Create a key for the port/protocol combination.
        key = destPort + "," + protocol;
        // Increment the count for the port/protocol combination.
        portProtocolCombination.put(key, portProtocolCombination.getOrDefault(key, 0) + 1);
    }

    // Main method to initiate the program.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt user for the lookup file and flow log file
        System.out.print("Enter the lookup file name (e.g., lookup.txt): ");
        String lookupFileName = scanner.nextLine();

        System.out.print("Enter the flow log file name (e.g., logs.txt): ");
        String flowLogFileName = scanner.nextLine();

        ClassLoader classLoader = Main.class.getClassLoader();


        // Load flow log and lookup files from resources.
        try (InputStream flowLogStream = classLoader.getResourceAsStream(flowLogFileName);
             InputStream lookupStream = classLoader.getResourceAsStream(lookupFileName)) {

            // Check if both files were successfully loaded.
            if (flowLogStream != null && lookupStream != null) {
                lookUpParse(lookupStream); // Parse the lookup file.
                flowLogParse(flowLogStream); // Parse the flow logs file.
            } else {
                System.out.println("Required files not found."); // Print an error message if files are missing.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
