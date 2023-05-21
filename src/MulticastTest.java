import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class MulticastTest {

    public static void main(String[] args) {
        try {
            // A multicast group is specified by a class D IP address and by a standard UDP port number.
            // Class D IP addresses are in the range 224.0.0.0 to 239.255.255.255, inclusive.
            InetAddress group = InetAddress.getByName("224.2.2.3");
            int port = 8888;
            NetworkInterface netIf = NetworkInterface.getByInetAddress(group);

            // Prepare to join multicast group
            InetSocketAddress groupAddr = new InetSocketAddress(group, port);
            MulticastSocket socket = new MulticastSocket(port); // create a multicast socket
            socket.joinGroup(groupAddr, netIf); // Tell the router to associate the multicast socket
            // with the chosen interface and multicast address

            // Get user name
            System.out.print("Enter your name: ");
            Scanner scanner = new Scanner(System.in);
            String userName = scanner.nextLine().trim();
            System.out.println("Welcome, " + userName + "!");
            System.out.println("You can type 'quit()' to left the chat room.");
            // Create and start sender thread
            MulticastSender sender = new MulticastSender(socket, group, port, userName);
            sender.start();

            // Create and start receiver thread
            MulticastReceiver receiver = new MulticastReceiver(socket);
            receiver.start();

        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    static class MulticastSender extends Thread {
        private MulticastSocket socket;
        private InetAddress group;
        private int port;
        private String userName;

        MulticastSender(MulticastSocket socket, InetAddress group, int port, String userName) {
            this.socket = socket;
            this.group = group;
            this.port = port;
            this.userName = userName;
        }

        public void run() {
            try {
                // Loop to read user input and send multicast messages
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    String message = scanner.nextLine().trim();
                    String formattedMessage = "[" + userName + "]: " + message;
                    if(message.equals("quit()")){
                        System.out.println("You have left the chat room.");
                        System.exit(0);
                    }
                    byte[] buf = formattedMessage.getBytes();

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
                    socket.send(packet);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }

    static class MulticastReceiver extends Thread {
        private MulticastSocket socket;

        MulticastReceiver(MulticastSocket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Loop to receive multicast messages and print them to console
                while (true) {
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(received);
                }
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }
    }
}
