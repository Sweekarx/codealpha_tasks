import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * CodeAlpha Java Internship - Task 2
 * Stock Trading Platform
 *
 * A console-based stock market simulator using OOP.
 * Compatible with Java 21. No external libraries required.
 */
public class StockTradingPlatform {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static class Stock {
        private final String symbol;
        private final String companyName;
        private double price;

        Stock(String symbol, String companyName, double price) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.price = price;
        }

        String getSymbol() {
            return symbol;
        }

        String getCompanyName() {
            return companyName;
        }

        double getPrice() {
            return price;
        }

        void setPrice(double price) {
            this.price = price;
        }
    }

    static class Transaction {
        private final String type;
        private final String symbol;
        private final int quantity;
        private final double price;
        private final LocalDateTime time;

        Transaction(String type, String symbol, int quantity, double price) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
            this.time = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format(
                    "%s | %-4s | %-6s | Qty: %-4d | Price: Rs. %.2f",
                    time.format(TIME_FORMAT), type, symbol, quantity, price);
        }
    }

    static class PerformanceRecord {
        private final LocalDateTime time;
        private final double cash;
        private final double stockValue;
        private final double totalValue;
        private final double profitLoss;

        PerformanceRecord(double cash, double stockValue,
                          double totalValue, double profitLoss) {
            this.time = LocalDateTime.now();
            this.cash = cash;
            this.stockValue = stockValue;
            this.totalValue = totalValue;
            this.profitLoss = profitLoss;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s | Cash: Rs. %.2f | Stocks: Rs. %.2f | "
                            + "Portfolio: Rs. %.2f | P/L: Rs. %.2f",
                    time.format(TIME_FORMAT), cash, stockValue,
                    totalValue, profitLoss);
        }
    }

    static class Portfolio {
        private double balance;
        private final double initialBalance;
        private final Map<String, Integer> holdings = new HashMap<>();
        private final List<Transaction> transactions = new ArrayList<>();
        private final List<PerformanceRecord> performanceHistory = new ArrayList<>();

        Portfolio(double initialBalance) {
            this.initialBalance = initialBalance;
            this.balance = initialBalance;
        }

        void buyStock(Stock stock, int quantity) {
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than 0.");
                return;
            }

            double cost = stock.getPrice() * quantity;
            if (cost > balance) {
                System.out.printf("Insufficient balance. Required: Rs. %.2f%n", cost);
                return;
            }

            balance -= cost;
            holdings.put(stock.getSymbol(),
                    holdings.getOrDefault(stock.getSymbol(), 0) + quantity);
            transactions.add(new Transaction(
                    "BUY", stock.getSymbol(), quantity, stock.getPrice()));

            System.out.printf("Bought %d shares of %s for Rs. %.2f.%n",
                    quantity, stock.getSymbol(), cost);
        }

        void sellStock(Stock stock, int quantity) {
            if (quantity <= 0) {
                System.out.println("Quantity must be greater than 0.");
                return;
            }

            int owned = holdings.getOrDefault(stock.getSymbol(), 0);
            if (quantity > owned) {
                System.out.printf("You own only %d shares of %s.%n",
                        owned, stock.getSymbol());
                return;
            }

            double proceeds = stock.getPrice() * quantity;
            balance += proceeds;

            int remaining = owned - quantity;
            if (remaining == 0) {
                holdings.remove(stock.getSymbol());
            } else {
                holdings.put(stock.getSymbol(), remaining);
            }

            transactions.add(new Transaction(
                    "SELL", stock.getSymbol(), quantity, stock.getPrice()));

            System.out.printf("Sold %d shares of %s for Rs. %.2f.%n",
                    quantity, stock.getSymbol(), proceeds);
        }

        double getStockValue(Map<String, Stock> market) {
            double value = 0.0;
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                Stock stock = market.get(entry.getKey());
                if (stock != null) {
                    value += stock.getPrice() * entry.getValue();
                }
            }
            return value;
        }

        double getTotalValue(Map<String, Stock> market) {
            return balance + getStockValue(market);
        }

        void recordPerformance(Map<String, Stock> market) {
            double stockValue = getStockValue(market);
            double totalValue = balance + stockValue;
            double profitLoss = totalValue - initialBalance;

            performanceHistory.add(new PerformanceRecord(
                    balance, stockValue, totalValue, profitLoss));
        }

        void showPortfolio(Map<String, Stock> market) {
            double stockValue = getStockValue(market);
            double totalValue = balance + stockValue;
            double profitLoss = totalValue - initialBalance;

            System.out.println("\n========== PORTFOLIO ==========");
            System.out.printf("Available Cash : Rs. %.2f%n", balance);

            if (holdings.isEmpty()) {
                System.out.println("No stocks owned.");
            } else {
                System.out.println("Holdings:");
                for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                    Stock stock = market.get(entry.getKey());
                    double value = stock.getPrice() * entry.getValue();
                    System.out.printf(
                            "  %s - %d shares @ Rs. %.2f = Rs. %.2f%n",
                            stock.getSymbol(), entry.getValue(),
                            stock.getPrice(), value);
                }
            }

            System.out.printf("Stock Value    : Rs. %.2f%n", stockValue);
            System.out.printf("Total Value    : Rs. %.2f%n", totalValue);
            System.out.printf("Profit/Loss    : Rs. %.2f%n", profitLoss);
            System.out.println("===============================");
        }

        void showTransactions() {
            System.out.println("\n======= TRANSACTION HISTORY =======");
            if (transactions.isEmpty()) {
                System.out.println("No transactions yet.");
            } else {
                for (Transaction transaction : transactions) {
                    System.out.println(transaction);
                }
            }
            System.out.println("===================================");
        }

        void showPerformanceHistory() {
            System.out.println("\n======= PERFORMANCE HISTORY =======");
            if (performanceHistory.isEmpty()) {
                System.out.println("No performance snapshots recorded yet.");
            } else {
                for (PerformanceRecord record : performanceHistory) {
                    System.out.println(record);
                }
            }
            System.out.println("===================================");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map<String, Stock> market = new HashMap<>();
        market.put("TCS", new Stock("TCS", "Tata Consultancy Services", 3200.00));
        market.put("INFY", new Stock("INFY", "Infosys", 1450.00));
        market.put("RELIANCE", new Stock("RELIANCE", "Reliance Industries", 2500.00));

        Portfolio portfolio = new Portfolio(10000.00);
        portfolio.recordPerformance(market);

        while (true) {
            System.out.println("\n========== STOCK TRADING PLATFORM ==========");
            System.out.println("1. View Market Data");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. Update Simulated Market Price");
            System.out.println("6. View Transaction History");
            System.out.println("7. View Performance History");
            System.out.println("8. Record Current Performance");
            System.out.println("9. Exit");

            int choice = readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1 -> showMarketData(market);
                case 2 -> trade(scanner, market, portfolio, true);
                case 3 -> trade(scanner, market, portfolio, false);
                case 4 -> portfolio.showPortfolio(market);
                case 5 -> updatePrice(scanner, market, portfolio);
                case 6 -> portfolio.showTransactions();
                case 7 -> portfolio.showPerformanceHistory();
                case 8 -> {
                    portfolio.recordPerformance(market);
                    System.out.println("Performance snapshot recorded.");
                }
                case 9 -> {
                    System.out.println("Exiting Stock Trading Platform. Thank you!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid option. Choose 1-9.");
            }
        }
    }

    private static void showMarketData(Map<String, Stock> market) {
        System.out.println("\n============== MARKET DATA ==============");
        System.out.printf("%-10s %-32s %12s%n", "Symbol", "Company", "Price");
        for (Stock stock : market.values()) {
            System.out.printf("%-10s %-32s Rs. %8.2f%n",
                    stock.getSymbol(), stock.getCompanyName(), stock.getPrice());
        }
        System.out.println("==========================================");
    }

    private static void trade(Scanner scanner, Map<String, Stock> market,
                              Portfolio portfolio, boolean buying) {
        showMarketData(market);
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();

        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Stock symbol not found.");
            return;
        }

        int quantity = readInt(scanner, "Enter quantity: ");

        if (buying) {
            portfolio.buyStock(stock, quantity);
        } else {
            portfolio.sellStock(stock, quantity);
        }

        portfolio.recordPerformance(market);
    }

    private static void updatePrice(Scanner scanner, Map<String, Stock> market, Portfolio portfolio) {
        showMarketData(market);

        System.out.print("Enter stock symbol to update: ");
        String symbol = scanner.nextLine().trim().toUpperCase();

        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Stock symbol not found.");
            return;
        }

        double newPrice = readPositiveDouble(scanner, "Enter new simulated price: ");
        stock.setPrice(newPrice);

        System.out.printf("%s price updated to Rs. %.2f.%n",
                stock.getSymbol(), newPrice);
        portfolio.recordPerformance(market);
        System.out.println("Performance snapshot recorded after the market update.");
    }

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ignored) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readPositiveDouble(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Ask again below.
            }

            System.out.println("Please enter a positive number.");
        }
    }
}
