import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;

public class StoreLocator extends JFrame {
    private Graph graph;
    private Map<String, String[]> storeItems;
    private JTextArea outputArea;
    private JComboBox<String> itemBox;
    private JComboBox<String> locationBox;
    private JPanel graphPanel;
    private static final String[] PLACES = {
            "Electronic city", "Banashankari", "Sarjapur", "Whitefield", "Devanahalli",
            "Indiranagar", "Jayanagar", "Koramangala", "HSR Layout", "Bannerghatta",
            "Central business district", "Malleswaram", "Frazer town", "Cooke town",
            "Basavanagudi", "Kasavanahalli", "BTM layout", "Ulsoor", "Rajaji nagar", "Sadashivanagar"
    };
    private static final String[] ITEMS = {
            "Clothes", "Electronics", "Vegetables", "Toys", "Medicines", "Seafood", 
            "Bakery items", "Shoes", "Cleaning supplies", "Stationery", "Dairy products", 
            "Fruits", "Meat", "Groceries", "Household items","CoolDrinks","PetFood"
    };

    public StoreLocator() {
        graph = new Graph();
        storeItems = new HashMap<>();
        initializeGraph();
        initializeStoreItems();
        setupUI();
    }

    private void setupUI() {
        setTitle("Store Locator");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        JLabel searchLabel = new JLabel("Search Item:");
        itemBox = new JComboBox<>(ITEMS);
        JLabel locationLabel = new JLabel("Your Location:");
        locationBox = new JComboBox<>(PLACES);

        JButton searchButton = new JButton("Find Nearest Store");
        searchButton.addActionListener(new SearchButtonListener());

        topPanel.add(searchLabel);
        topPanel.add(itemBox);
        topPanel.add(locationLabel);
        topPanel.add(locationBox);
        topPanel.add(searchButton);

        outputArea = new JTextArea(10, 50);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 16));
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(1000, 600));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(graphPanel, BorderLayout.SOUTH);
    }

    private void initializeGraph() {
        for (String place : PLACES) {
            graph.addVertex(place);
        }
        graph.addEdge("Electronic city", "Banashankari", 10);
        graph.addEdge("Banashankari", "Sarjapur", 15);
        graph.addEdge("Sarjapur", "Whitefield", 20);
        graph.addEdge("Whitefield", "Devanahalli", 25);
        graph.addEdge("Devanahalli", "Indiranagar", 30);
        graph.addEdge("Indiranagar", "Jayanagar", 5);
        graph.addEdge("Jayanagar", "Koramangala", 10);
        graph.addEdge("Koramangala", "HSR Layout", 20);
        graph.addEdge("HSR Layout", "Bannerghatta", 25);
        graph.addEdge("Bannerghatta", "Central business district", 30);
        graph.addEdge("Central business district", "Malleswaram", 35);
        graph.addEdge("Malleswaram", "Frazer town", 40);
        graph.addEdge("Frazer town", "Cooke town", 45);
        graph.addEdge("Cooke town", "Basavanagudi", 50);
        graph.addEdge("Basavanagudi", "Kasavanahalli", 55);
        graph.addEdge("Kasavanahalli", "BTM layout", 60);
        graph.addEdge("BTM layout", "Ulsoor", 65);
        graph.addEdge("Ulsoor", "Rajaji nagar", 70);
        graph.addEdge("Rajaji nagar", "Sadashivanagar", 75);
        graph.addEdge("Sadashivanagar", "Electronic city", 80);
    }

    private void initializeStoreItems() {
        storeItems.put("Electronic city", new String[]{"Clothes", "Electronics", "Vegetables", "Toys", "Medicines"});
        storeItems.put("Sarjapur", new String[]{"Seafood", "Bakery items", "Shoes", "Cleaning supplies", "Stationery"});
        storeItems.put("Whitefield", new String[]{"Dairy products", "Vegetables", "Clothes", "Fruits", "Medicines"});
        storeItems.put("Indiranagar", new String[]{"Shoes", "Meat", "Toys", "Vegetables", "Medicines"});
        storeItems.put("Jayanagar", new String[]{"Clothes", "Seafood", "Dairy products", "Fruits", "Stationery"});
        storeItems.put("Koramangala", new String[]{"Cleaning supplies", "Groceries", "Electronics", "Bakery items", "Toys"});
        storeItems.put("HSR Layout", new String[]{"Clothes", "Vegetables", "Shoes", "Dairy products", "Medicines"});
        storeItems.put("Basavanagudi", new String[]{"Fruits", "Vegetables", "Bakery items", "Electronics", "Toys"});
        storeItems.put("Kasavanahalli", new String[]{"Groceries", "Clothes", "Shoes", "Seafood", "Dairy products"});
        storeItems.put("BTM layout", new String[]{"Electronics", "Meat", "Fruits", "Household items", "Vegetables"});
        storeItems.put("Rajaji nagar", new String[]{"Medicines", "Bakery items", "Vegetables", "Seafood", "Shoes"});
        storeItems.put("Sadashivanagar", new String[]{"Fruits", "Household items", "Electronics", "Stationery", "Clothes"});
    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String item = (String) itemBox.getSelectedItem();
            String location = (String) locationBox.getSelectedItem();
            if (item.isEmpty() || location == null) {
                outputArea.setText("======Please enter an item and select your location=======");
                return;
            }

            boolean itemFound = false;
            Set<String> storesWithItem = new HashSet<>();
            for (Map.Entry<String, String[]> entry : storeItems.entrySet()) {
                if (Arrays.asList(entry.getValue()).contains(item)) {
                    itemFound = true;
                    storesWithItem.add(entry.getKey());
                }
            }

            if (!itemFound) {
                outputArea.setText("=========   ITEM NOT FOUND IN ANY STORE    ==========");
                return;
            }

            
            List<String> pathDijkstra = new ArrayList<>();
            String nearestStoreDijkstra = graph.findNearestStore(location, storesWithItem, true, pathDijkstra);

            List<String> pathBellmanFord = new ArrayList<>();
            String nearestStoreBellmanFord = graph.findNearestStore(location, storesWithItem, false, pathBellmanFord);

            outputArea.setText("Item found in the following stores:\n");
            for (String store : storesWithItem) {
                outputArea.append(store + "\n");
            }

            outputArea.append("\nNearest store using Dijkstra's algorithm: " + nearestStoreDijkstra);
            outputArea.append("\nNearest store using Bellman-Ford algorithm: " + nearestStoreBellmanFord);

          
            long startTime = System.nanoTime();
            graph.findNearestStore(location, storesWithItem, true, new ArrayList<>());
            long endTime = System.nanoTime();
            long dijkstraTime = endTime - startTime;

            startTime = System.nanoTime();
            graph.findNearestStore(location, storesWithItem, false, new ArrayList<>());
            endTime = System.nanoTime();
            long bellmanFordTime = endTime - startTime;

            outputArea.append("\n\nExecution times (in nanoseconds):");
            outputArea.append("\nDijkstra's algorithm: " + dijkstraTime);
            outputArea.append("\nBellman-Ford algorithm: " + bellmanFordTime);

           
            if (dijkstraTime < bellmanFordTime) {
                outputArea.append("\n\nDijkstra's algorithm is more efficient.");
            } else {
                outputArea.append("\n\nBellman-Ford algorithm is more efficient.");
            }

            
            graphPanel.repaint();
        }
    }

    private void drawGraph(Graphics2D g2d) {
        Map<String, Point> nodeLocations = new HashMap<>();
        nodeLocations.put("Ulsoor", new Point(1000, 250));
        nodeLocations.put("Sadashivanagar", new Point(300, 100));
        nodeLocations.put("Electronic city", new Point(200, 300));
        nodeLocations.put("Banashankari", new Point(600, 400));
        nodeLocations.put("Rajaji nagar", new Point(500, 300));
        nodeLocations.put("Indiranagar", new Point(700, 100));
        nodeLocations.put("Koramangala", new Point(800, 250));
        nodeLocations.put("HSR Layout", new Point(900, 200));
        nodeLocations.put("Devanahalli", new Point(800, 400));
        nodeLocations.put("Malleswaram", new Point(100, 200));
        nodeLocations.put("Frazer town", new Point(200, 400));
        nodeLocations.put("Cooke town", new Point(300, 450));
        nodeLocations.put("Basavanagudi", new Point(400, 500));
        nodeLocations.put("Central business district", new Point(200, 500));
        nodeLocations.put("Sarjapur", new Point(300, 300));
        nodeLocations.put("Whitefield", new Point(500, 200));
        nodeLocations.put("Jayanagar", new Point(600, 200));
        nodeLocations.put("Kasavanahalli", new Point(700, 400));
        nodeLocations.put("BTM layout", new Point(900, 350));

        String userLocation = (String) locationBox.getSelectedItem();
        String searchItem = (String) itemBox.getSelectedItem();

        List<String> pathDijkstra = new ArrayList<>();
        String nearestStoreDijkstra = graph.findNearestStore(userLocation, storeItems.keySet(), true, pathDijkstra);

        List<String> pathBellmanFord = new ArrayList<>();
        String nearestStoreBellmanFord = graph.findNearestStore(userLocation, storeItems.keySet(), false, pathBellmanFord);

        
        for (String vertex : graph.adjList.keySet()) {
            Point location = nodeLocations.get(vertex);
            if (location != null) {
                if (vertex.equals(userLocation)) {
                    g2d.setColor(Color.YELLOW); 
                } else if (Arrays.asList(storeItems.getOrDefault(vertex, new String[]{})).contains(searchItem)) {
                    g2d.setColor(Color.GREEN); 
                } else if (vertex.equals(nearestStoreDijkstra) || vertex.equals(nearestStoreBellmanFord)) {
                    g2d.setColor(Color.WHITE); 
                } else {
                    g2d.setColor(Color.CYAN); 
                }
                g2d.fillOval(location.x - 15, location.y - 15, 30, 30); 
                g2d.setColor(Color.BLACK);
                g2d.drawOval(location.x - 15, location.y - 15, 30, 30);
                g2d.drawString(vertex, location.x - 5, location.y + 5);
            }
        }

        
        for (String from : graph.adjList.keySet()) {
            for (Map.Entry<String, Integer> toEntry : graph.adjList.get(from).entrySet()) {
                String to = toEntry.getKey();
                int weight = toEntry.getValue();
                Point fromLocation = nodeLocations.get(from);
                Point toLocation = nodeLocations.get(to);
                if (fromLocation != null && toLocation != null) {
                    if (isPartOfPath(from, to, pathDijkstra) || isPartOfPath(from, to, pathBellmanFord)) {
                        g2d.setColor(Color.PINK); 
                    } else {
                        g2d.setColor(Color.BLACK);
                    }
                    g2d.drawLine(fromLocation.x, fromLocation.y, toLocation.x, toLocation.y);
                    int midX = (fromLocation.x + toLocation.x) / 2;
                    int midY = (fromLocation.y + toLocation.y) / 2;
                    g2d.drawString(String.valueOf(weight), midX, midY);
                }
            }
        }
    }


    private boolean isPartOfPath(String from, String to, List<String> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            if ((path.get(i).equals(from) && path.get(i + 1).equals(to)) ||
                (path.get(i).equals(to) && path.get(i + 1).equals(from))) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String database = "C:\\Users\\Shanmukha Reddy\\Desktop\\user_database.csv";
            LoginPage loginPage = new LoginPage(database);
            loginPage.show();
        });
    }
}


class Utils {
    public static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}


class LoginPage {
 private JFrame frame;
 private JTextField usernameField;
 private JPasswordField passwordField;
 private String database;

 public LoginPage(String database) {
     this.database = database;
     this.frame = new JFrame("Nearest Store Allocator");
     this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     this.frame.setSize(300, 200);
     this.frame.setLayout(new GridLayout(6, 1));
 }

 public void show() {
     frame.add(new JLabel("Username:"));
     usernameField = new JTextField();
     frame.add(usernameField);

     frame.add(new JLabel("Password:"));
     passwordField = new JPasswordField();
     frame.add(passwordField);

     JButton loginButton = new JButton("Login");
     loginButton.addActionListener(e -> login());
     frame.add(loginButton);

     JLabel registerLabel = new JLabel("Don't have an account? Register below.");
     frame.add(registerLabel);

     JButton registerButton = new JButton("Register");
     registerButton.addActionListener(e -> showRegisterPage());
     frame.add(registerButton);

     frame.setLocationRelativeTo(null); 
     frame.setVisible(true);
 }

 private void login() {
     String username = usernameField.getText();
     String password = new String(passwordField.getPassword());

     if (checkCredentials(username, password)) {
         JOptionPane.showMessageDialog(frame, "Login Successful. Welcome, " + username + "!");
         frame.dispose();
         StoreLocator app = new StoreLocator();
         app.setVisible(true);
     } else {
         JOptionPane.showMessageDialog(frame, "Login Failed. Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
     }
 }

 private boolean checkCredentials(String username, String password) {
     try (BufferedReader reader = new BufferedReader(new FileReader(database))) {
         String line;
         String hashedPassword = Utils.hashString(password);
         while ((line = reader.readLine()) != null) {
             String[] parts = line.split(",");
             if (parts[0].equals(username) && parts[1].equals(hashedPassword)) {
                 return true;
             }
         }
     } catch (IOException ex) {
         ex.printStackTrace();
     }
     return false;
 }

 private void showRegisterPage() {
     RegisterPage registerPage = new RegisterPage(database);
     registerPage.show();
     frame.dispose();
 }
}


class RegisterPage {
 private JFrame frame;
 private JTextField usernameField;
 private JPasswordField passwordField;
 private String userDatabase;

 public RegisterPage(String userDatabase) {
     this.userDatabase = userDatabase;
     this.frame = new JFrame("Register Page");
     this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     this.frame.setSize(300, 200);
     this.frame.setLayout(new GridLayout(5, 1));
 }

 public void show() {
     frame.add(new JLabel("Choose a username:"));
     usernameField = new JTextField();
     frame.add(usernameField);

     frame.add(new JLabel("Choose a password:"));
     passwordField = new JPasswordField();
     frame.add(passwordField);

     JButton registerButton = new JButton("Register");
     registerButton.addActionListener(e -> register());
     frame.add(registerButton);

     frame.setLocationRelativeTo(null); 
     frame.setVisible(true);
 }

 private void register() {
     String username = usernameField.getText();
     String password = new String(passwordField.getPassword());
     String hashedPassword = Utils.hashString(password);

     try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDatabase, true))) {
         writer.write(username + "," + hashedPassword);
         writer.newLine();
         JOptionPane.showMessageDialog(frame, "Registration successful!\nUsername: " + username);
         frame.dispose();
         LoginPage loginPage = new LoginPage(userDatabase);
         loginPage.show();
     } catch (IOException ex) {
         ex.printStackTrace();
     }
 }
}


class Graph {
    Map<String, Map<String, Integer>> adjList = new HashMap<>();

    public void addVertex(String vertex) {
        adjList.put(vertex, new HashMap<>());
    }

    public void addEdge(String from, String to, int weight) {
        adjList.get(from).put(to, weight);
        adjList.get(to).put(from, weight);
    }

    public String findNearestStore(String start, Set<String> stores, boolean useDijkstra, List<String> path) {
        if (useDijkstra) {
            return findNearestStoreDijkstra(start, stores, path);
        } else {
            return findNearestStoreBellmanFord(start, stores, path);
        }
    }

    private String findNearestStoreDijkstra(String start, Set<String> stores, List<String> path) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        Map<String, Integer> dist = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> prev = new HashMap<>();

        pq.add(new Node(start, 0));
        dist.put(start, 0);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();
            String currentPlace = currentNode.place;

            if (stores.contains(currentPlace)) {
                reconstructPath(currentPlace, prev, path);
                return currentPlace;
            }

            if (!visited.add(currentPlace)) {
                continue;
            }

            for (Map.Entry<String, Integer> neighborEntry : adjList.getOrDefault(currentPlace, Collections.emptyMap()).entrySet()) {
                String neighbor = neighborEntry.getKey();
                int newDist = dist.get(currentPlace) + neighborEntry.getValue();

                if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    dist.put(neighbor, newDist);
                    pq.add(new Node(neighbor, newDist));
                    prev.put(neighbor, currentPlace);
                }
            }
        }

        return "No store reachable";
    }

    private String findNearestStoreBellmanFord(String start, Set<String> stores, List<String> path) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String vertex : adjList.keySet()) {
            dist.put(vertex, Integer.MAX_VALUE);
        }
        dist.put(start, 0);

        int V = adjList.size();
        for (int i = 1; i < V; i++) {
            for (String u : adjList.keySet()) {
                for (Map.Entry<String, Integer> entry : adjList.get(u).entrySet()) {
                    String v = entry.getKey();
                    int weight = entry.getValue();
                    if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                        dist.put(v, dist.get(u) + weight);
                        prev.put(v, u);
                    }
                }
            }
        }

        String nearestStore = "No store reachable";
        int minDist = Integer.MAX_VALUE;
        for (String store : stores) {
            if (dist.get(store) < minDist) {
                minDist = dist.get(store);
                nearestStore = store;
            }
        }

        reconstructPath(nearestStore, prev, path);
        return nearestStore;
    }

    private void reconstructPath(String target, Map<String, String> prev, List<String> path) {
        for (String at = target; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
    }

    private static class Node {
        String place;
        int distance;

        Node(String place, int distance) {
            this.place = place;
            this.distance = distance;
        }
    }
}
