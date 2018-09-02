package bookstore;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** 
 *  Huey Padua
 *  CNT 4714 - Summer 2018
 *  Program 1 - Event Driven Programming
 *  Tuesday May 29, 2018
 */
public class BookStore  {

    //class constants in pixels
    private static final int window_width = 800;
    private static final int window_height = 350;
    private static final int field_width = 60;
    private static final FlowLayout layout_style = new FlowLayout();

    //instance variables
    //window for GUI
    private JFrame window = new JFrame("Huey's e-Store...for Books!");
    private JPanel panel = new JPanel();
    private JLabel itemsTag = new JLabel("Enter number of items in this order: ");
    private JTextField itemsText = new JTextField(field_width);
    private JLabel idTag = new JLabel("Enter Book ID for item #"+ String.valueOf(orderCounter+1) + ":");
    private JTextField idText = new JTextField(field_width);
    private JLabel quantityTag = new JLabel("Enter quantity for item #"+String.valueOf(orderCounter+1));
    private JTextField quantityText = new JTextField(field_width);
    private JLabel itemInfoTag = new JLabel("Item #"+String.valueOf(orderCounter+1) +" information:");
    private JTextField itemInfoText = new JTextField(field_width);
    private JLabel subtotalTag = new JLabel("Order subtotal for items: ");
    private JTextField subtotalText = new JTextField(field_width);
    private JButton processButton = new JButton("Process Item #" + String.valueOf(orderCounter+1)+":");
    private JButton confirmButton = new JButton("Confirm Item #" + String.valueOf(orderCounter+1)+":");
    private JButton viewButton = new JButton("View Order");
    private JButton finishButton = new JButton("Finish Order");
    private JButton newButton = new JButton("New Order");
    private JButton exitButton = new JButton("Exit");

    private final String file_in = "inventory.txt";
    private final String file_out = "transaction.txt";
    private final float TAX = 0.06f;
    
    ArrayList<Book> bookList = new ArrayList<>();
    ArrayList<Order> orderList = new ArrayList<>();
    Invoice invoice = new Invoice();
    Order order = new Order();
    private static int orderCounter;

    //bookstore constructor
    public BookStore() {
        
        //configure GUI
        window.setSize(window_width, window_height);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(panel);
        panel.setSize(window_width, window_height);

        //add components to the container
        Container c = window.getContentPane();
        c.setLayout(layout_style);

        c.add(itemsTag);
        c.add(itemsText);
        c.add(idTag);
        c.add(idText);
        c.add(quantityTag);
        c.add(quantityText);
        c.add(itemInfoTag);
        c.add(itemInfoText);
        c.add(subtotalTag);
        c.add(subtotalText);
        c.add(processButton);
        c.add(confirmButton);
        c.add(viewButton);
        c.add(finishButton);
        c.add(newButton);
        c.add(exitButton);
        
        itemInfoText.setEditable(false);
        subtotalText.setEditable(false);

        confirmButton.setEnabled(false);
        viewButton.setEnabled(false);
        finishButton.setEnabled(false);
        newButton.setEnabled(true);
        exitButton.setEnabled(true);
        
        //display GUI
        window.show();
    }
    public static void main(String[] args) throws IOException {
        orderCounter = 0;
        BookStore gui = new BookStore();
        gui.readInventory();
        gui.buttonTapped();
    }

    //function that reads an inventory.txt file
    //parses the required credentials to Book object
    //inventory.txt file should be located within bookStore project folder
    public void readInventory() throws IOException {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file_in));
            String inputLine;
            
            while ((inputLine = reader.readLine()) != null) {
                
                Book nb = new Book();
                String[] newBook = inputLine.split(",", 3);
                nb.setID(Integer.valueOf(newBook[0]));
                nb.setName(newBook[1]);
                nb.setPrice(Float.valueOf(newBook[2]));
                
                bookList.add(nb);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
    }
    //button functionalities
    public void buttonTapped() {  

        //process button clicked
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Checking whether all fields have been filled out
                if (itemsText.getText().equals("") || idText.getText().equals("") 
                    || quantityText.getText().equals("")) {
                    JOptionPane.showMessageDialog(panel, "Please fill out all fields.");
                }
                else { 
                    Integer numItems = Integer.valueOf(itemsText.getText());
                    Integer bookId = Integer.valueOf(idText.getText());
                    Integer itemQuantity = Integer.valueOf(quantityText.getText());
                    //var to check if item has been found in the inventory
                    boolean itemFound = false;

                    //loop thru bookList to check for ID match
                    for(Book b:bookList){
                        if(bookId == b.getID()){
                            setItemInfo(b, itemQuantity);
                            itemInfoText.setText(b.getInfo());
                            //processes the current order
                            processOrder(b, itemQuantity, numItems);
                            //checking to see of orders are complete
                            if (orderCounter < numItems) {
                                confirmButton.setEnabled(true);
                                processButton.setEnabled(false);
                                itemsText.setEditable(false);
                                orderCounter++;
                            }
                            else {
                                processButton.setEnabled(false);
                                confirmButton.setEnabled(true);
                            }
                            itemFound = true;
                            break;
                        }
                    }               
                    if (!itemFound){
                        JOptionPane.showMessageDialog(panel, "Book ID "+bookId+" not in file...");
                    }
                }
            }
        });
        //confirm button clicked
        confirmButton.addActionListener(new ActionListener() {
            //counter to keep track of item #
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "Item #" + String.valueOf(orderCounter) + " accepted!");

                processButton.setEnabled(true);
                confirmButton.setEnabled(false);
                viewButton.setEnabled(true);
                finishButton.setEnabled(true);
                //make sure subtotal is adding when next order persist
                subtotalText.setText("$"+String.format("%.02f",invoice.getSubtotal()));
                idText.setText("");
                quantityText.setText("");
                
                //make sure labels don't update after final order
                //has been processed
                if(orderCounter < Integer.valueOf(itemsText.getText())){
                    updateLabels();
                }
                else 
                    processButton.setEnabled(false);
            }
            
        });
        //view button clicked
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, viewOrder());
            }
            
        });
        //finish button clicked
        finishButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //check to see if the order is finished or not
                //if so, proceed to checkout
                if(orderCounter < Integer.valueOf(itemsText.getText())) {
                    JOptionPane.showMessageDialog(panel, "Please finish your order.");
                }
                else {
                    JOptionPane.showMessageDialog(panel, displayInvoice());
                    writeInvoice();
                    processButton.setEnabled(false);
                    confirmButton.setEnabled(false);
                    finishButton.setEnabled(false);
                }
            }    
        });
        //new button clicked
        //restarts whole gui
        newButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    window.dispose();
                    orderCounter = 0;
                    new BookStore();
                    readInventory();
                    buttonTapped();
                    updateLabels();
                } catch (IOException ex) {
                    System.out.println("Error: Could not restart gui");
                }
            }            
        });
        //exit button clicked to close program
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.dispose();
            }       
        });
    }

    //function that sets item information based on Book class
    //displays item info on textfield
    public void setItemInfo(Book nb, int quantity) {
        nb.setInfo(String.valueOf(nb.getID()) + " " + nb.getName() + " $" + 
                String.format("%.02f",nb.getPrice()) + " " + String.valueOf(quantity)+ " " +
                        String.valueOf(getDiscount(quantity) + "% $" 
                                + String.format("%.02f",calculateDiscount(nb, quantity))));
    }
    
    //function that returns discount amount
    //need to convert to decimal places
    private int getDiscount(int quantity) {
        if(quantity < 5)
            return 0;
        else if(quantity < 10)
            return 10;
        else if(quantity < 15)
            return 15;
        else if(quantity >= 15)
            return 20;
        else
            return 0;
    }
    
    //function to calculate discount based on item quantity
    //remember to keep decimal place at hundredth
    private float calculateDiscount(Book nb, int quantity) {
        float subtotal = nb.getPrice() * (float) quantity;
        
        if(quantity < 5)
            return subtotal;
        else if(quantity<10)
            return subtotal - (subtotal*0.10f);
        else if(quantity<15)
            return subtotal - (subtotal*0.15f);
        else if(quantity >= 15)
            return subtotal - (subtotal * 0.20f);
        else
            return 0f;
    }
    
    //function to return a string containing 
    //info from processed orders
    public String viewOrder() {
        String orderViews="";
        int count=1;
        for(Order order: invoice.getOrders()){
            orderViews = orderViews + String.valueOf(count) + ". "+
                    order.getBook().getInfo()+"\n";
            count++;
        }
        return orderViews;
    }
    
    //function to update labels once item # 
    //has been confirmed
    public void updateLabels() {
        idTag.setText("Enter Book ID for item #"+ String.valueOf(orderCounter+1) + ":");
        quantityTag.setText("Enter quantity for item #"+String.valueOf(orderCounter+1));
        itemInfoTag.setText("Item #"+String.valueOf(orderCounter+1) +" information:");
        subtotalTag.setText("Order subtotal for "+ (orderCounter+1) + " item(s):");
        processButton.setText("Process Item #" + String.valueOf(orderCounter+1)+":");
        confirmButton.setText("Confirm Item #" + String.valueOf(orderCounter+1)+":");
    }
    
    //function that processes item order from Book object
    //passing credentials to invoice 
    //also calculates discount amount
    public void processOrder(Book nb, int quantity, int numItems) {
        Order o = new Order();
        o.setBook(nb);
        o.setItemQuantity(quantity);
        o.setDiscount(Float.valueOf(getDiscount(quantity)));
        o.setSubtotal(calculateDiscount(nb, quantity));
        
        this.invoice.addOrder(o);
        this.invoice.setNumItemsInOrder(numItems);
        this.invoice.setSubtotal(calculateDiscount(nb, quantity));
        this.invoice.setTotal(this.invoice.getSubtotal() + (this.invoice.getSubtotal()*TAX));
    }
    
    //function to display invoice
    //once user selects finishButton
    //Need to write to transaction.txt afterwards
    public String displayInvoice() {
        this.invoice.setDate();
        String taxRate = "6%";
        String currInv = "Date: ";
        
    	currInv = currInv + this.invoice.getTimeStamp() + "\n\n";
    	currInv = currInv + "Number of line items: " + this.invoice.getNumItemsInOrder() + "\n\n";
    	currInv = currInv + "Item# / ID / Title / Price / Qty / Disc % / Subtotal:\n\n";
    	currInv = currInv + viewOrder() + "\n\n";
    	currInv = currInv + "Order subtotal: $" + String.format("%.02f",this.invoice.getSubtotal()) + "\n\n";
    	currInv = currInv + "Tax rate:    " + taxRate + "\n\n";
    	currInv = currInv + "Tax amount:    $" + (String.format("%.02f", this.invoice.getSubtotal() * TAX)) + "\n\n";
    	currInv = currInv + "Order total:    $" + String.format("%.02f",this.invoice.getTotal()) + "\n\n";
    	currInv = currInv + "Thank you for shopping at Huey's e-Store...for Books! \n\n";
        
        return currInv;
    }
    
    //function to write Invoice to transaction.txt
    //transaction.txt file should be located within bookStore project folder
    public void writeInvoice() {
        try {
            FileWriter writer = new FileWriter(file_out, true);
            BufferedWriter bw = new BufferedWriter(writer);
            //loop thru orders once finished
            for(Order order: this.invoice.getOrders()){
                bw.write(this.invoice.getTransactionStamp() + ", " +
                    order.getBook().getID() + ","+
                    order.getBook().getName()+", "+
                    order.getBook().getPrice()+", "+
                    order.getItemQuantity()+", "+ order.getDiscount()+
                    ", "+ String.format("%.02f",order.getSubtotal())+", "+this.invoice.getTimeStamp());
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println("Error: Can't write to txt file!");
        }
    }
}

class Book {
    private int bookID;
    private float bookPrice;
    private String bookName;
    private String bookInfo;

    // Book attributes 
    public void setID(int bookID) {
        this.bookID = bookID;
    }

    public void setName(String bookName) {
        this.bookName = bookName;
    }

    public void setPrice(float bookPrice) {
        this.bookPrice = bookPrice;
    }

    public void setInfo(String bookInfo) {
        this.bookInfo = bookInfo;
    }

    public int getID() {
        return this.bookID;
    }

    public String getName() {
        return this.bookName;
    }

    public float getPrice() {
        return this.bookPrice;
    }

    public String getInfo() {
        return this.bookInfo;
    }
}

class Order {
    private Book nb;
    private int itemQuantity;
    private float discount;
    private float subtotal;
    
    //order attributes
    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
    public void setBook(Book nb) {
        this.nb = nb;
    }
    public void setDiscount(float discount){
        this.discount = discount;
    }
    public void setSubtotal(float subtotal){
        this.subtotal = subtotal;
    }
    public Book getBook(){
        return this.nb;
    }
    public int getItemQuantity() {
        return this.itemQuantity;
    }
    public float getDiscount(){
        return this.discount;
    }
    public float getSubtotal(){
        return this.subtotal;
    }
}

class Invoice {
    private ArrayList<Order> order;
    private int numItemsInOrder = 0;
    private float subtotal=0;
    private float total=0;
    private String timeStamp;
    private String transactionStamp;
    
    //invoice attributes
    public Invoice(){
        this.order = new ArrayList<Order>();
    }
    public void setNumItemsInOrder(int numItemsInOrder){
        this.numItemsInOrder = numItemsInOrder;
    }
    public void setSubtotal(float subtotal){
        this.subtotal += subtotal;
    }
    public void setTotal(float total){
        this.total = total;
    }
    public void addOrder(Order order){
        this.order.add(order);
    }
    public int getNumItemsInOrder(){
        return this.numItemsInOrder;
    }
    public float getSubtotal(){
        return this.subtotal;
    }
    public float getTotal(){
        return this.total;
    }
    public ArrayList<Order> getOrders(){
        return this.order;
    }
    public String getTimeStamp(){
        return this.timeStamp;
    }
    public String getTransactionStamp(){
        return this.transactionStamp;
    }
    public void setDate(){
        Date newDate = new Date();
        DateFormat newDateFormat = new SimpleDateFormat("MM/dd/yy, hh:mm:ss a z");
        this.timeStamp= newDateFormat.format(newDate);
        newDateFormat = new SimpleDateFormat("yyMMddYYhhmm");
        this.transactionStamp = newDateFormat.format(newDate);
    }
}
