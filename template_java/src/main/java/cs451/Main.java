package cs451;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

	private static Process process;
	
    private static void handleSignal(Parser parser) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        process.stopBroadcasting();
        
        try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        //write/flush output file if necessary
        System.out.println("Writing output.");
        process.writeLogs();
//        try {
//            File outputFile = new File(parser.output());
//            if (!outputFile.exists()) {
//            	outputFile.createNewFile();
//            }
//            FileWriter outputFileWriter = new FileWriter(outputFile);
//            outputFileWriter.write("Process " + Integer.toString(parser.myId()) + " interrupted.");
//            outputFileWriter.close();
//            System.out.println("The output has been written.");
//          } catch (IOException e) {
//        	System.out.println("An exception occured, when logging shutdown signals handling.");
//            e.printStackTrace();
//          }
    }

    private static void initSignalHandlers(Parser parser) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(parser);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers(parser);

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");
        
        // Read the content of config file
        String configFilePath = parser.config();
        File configFile = new File(configFilePath);
		Scanner configScanner = null;
        String line = null;
        try {
			configScanner = new Scanner(configFile);
			configScanner.hasNextLine();
			line = configScanner.nextLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (configScanner != null) {
				configScanner.close();
			}
		}
        
        if (line == null) {
        	System.out.println("Config file empty!");
        	System.exit(0);
        }
        
        String[] splits = line.split("\\s+");
        //int messagesReceiverProcessId = Integer.parseInt(splits[1]);
        int noOfMessagesToSend = Integer.parseInt(splits[0]);
        
        
        // Prepare the list of hosts list for the process
        int myId = parser.myId();
        for (var host: parser.hosts()) {
        	if(myId == host.getId()) {
        		List<Host> hosts = parser.hosts();
        		//hosts.remove(host);
        		//noOfMessagesToSend = myId != messagesReceiverProcessId ? noOfMessagesToSend : 0;
        		process = new Process(host.getId(), host.getIp(), host.getPort(), hosts, noOfMessagesToSend, parser.output());
        		break;
        	}
        }
        

        System.out.println("Broadcasting and delivering messages...\n");
        
        process.startBroadcast();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
