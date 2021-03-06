package at.fhj.swd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import at.fhj.swd.Customer;
import at.fhj.swd.Order;
import at.fhj.swd.OrderItem;

public class OrderProcessing {

	private static String logFile = System.getProperty("user.dir") + "/log.txt";
	private static File folder;
	
	/**
	 * Create a Dummy Order with Customer and one Order Item for testing
	 * @return order ... Dummy Order
	 */
	
	private static Order createDummyOrders() {
		Customer customer = new Customer ("Andreas", "Dietler", "Somewhere", "over the Rainbow", "666", "Austr(al)ia");
		ArrayList<OrderItem> items = new ArrayList<>();
		items.add(new OrderItem("B123", "Bier", 1.5f, 4));
		Order order = new Order(1000001, customer, items);
		return order;
	}
	
	/**
	 * Read CSV-File with Order Information (Customer, Order Item ...)
	 * File can be found anywhere on the File System.
	 * @param fileName ... Path where the order can be found
	 * @return parsed file with Order inforamtion
	 */
	private static Order readOrder(File fileName) {
		Order readOrder = null;
		Customer readCustomer = null;
		ArrayList<OrderItem> readItems = new ArrayList<OrderItem>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			String line = null;
			// gibt die anzahl der gelesen Zeilennummern an
			int index = 1;
			String[] fullName = reader.readLine().split(" ");
			index++;
			String address = reader.readLine();
			index++;
			String zipCode = reader.readLine();
			index++;
			String city = reader.readLine();
			index++;
			String country = reader.readLine();
			index++;
			readCustomer = new Customer(fullName[0], fullName[1], address, zipCode, city, country);
			// es wird Zeile fuer Zeile eingelesen bis keine mehr vorhanden ist.
			String header[] = reader.readLine().split(";");
			if(checkCSV(header)) {
				while ((line = reader.readLine()) != null) {
					// gelesenen Zeile wird aufgeteilt mit dem Seperator ";"
					String[] splited = line.split(";");
					try {
						// Zuweisen und Umwandeln der seperierten Werte
						String articleNumber = splited[0];
						String name = splited[1];
						float price = Float.parseFloat(splited[2]);
						int quantity = Integer.parseInt(splited[3]);
						// Erstellen eines neuen Order Items und Uebergeben der gelesenen Werte
						readItems.add(new OrderItem(articleNumber, name, price, quantity));
					} catch (Exception error) {
						System.out.println("Fehlerhafte Werte in Zeile " + index + ".");
						System.out.println("Details unter \"" + logFile + "\"");
						LogFileWriter(error);
					}
					index++;
				}
			} else {
				System.out.println("Das CSV-File Format ist falsch.");
			}
			readOrder = new Order(1001, readCustomer, readItems);
		} catch (Exception error) {
			System.out.println("Fehler beim Lesen der Datei" + fileName + ". Bitte Eingaben pruefen");
			System.out.println("Details unter \"" + logFile + "\"");
			LogFileWriter(error);
		} finally {
			if (reader != null){
				try {
					reader.close();
					System.out.println("["+ actualDateTime() + "] - " + "Das Lesen des CSV-Files wurde beendet.");
				} catch (Exception e){}
			}
		}	
		return readOrder;
	}
	
	private static void HTMLWriter(String FileName, Order order) {
		PrintWriter writer = null;
		try {
			/* Da die Codierung mit dem FileReader nicht korrekt funktioniert
			 * und anscheinend nicht umgestellt werden konnte, wurde ein
			 * FileInputStream und InputStreamReader verwendet.
			 */
			// writer = new PrintWriter(new FileOutputStream("C:\\temp\\order\\test"));
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileName), "UTF-8"));
			
			
			// Header wird eingefuegt
			writer.println(dietlera15HTML.getHeader());
			
			writer.println("<body>\n"
				+ "\t<header>\n"
					+ "\t\t<h1>Bestellbest�tigung: "+ order.getOrderID() + "</h1>\n"
				+ "\t</header>\n\n\n"
				+ "\t<main>\n"
					+ "\t\t<h3>Adresse:</h3>\n"
					+ dietlera15HTML.customerToHTML(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
					+ dietlera15HTML.customerToHTML(order.getCustomer().getAddress())
					+ dietlera15HTML.customerToHTML(order.getCustomer().getZipCode())
					+ dietlera15HTML.customerToHTML(order.getCustomer().getCity())
					+ dietlera15HTML.customerToHTML(order.getCustomer().getCountry())
					+ "\n"
					+ "\t\t<table>\n"
						+ "\t\t\t<thead>\n"
							+ "\t\t\t\t<tr id=\"table-header\">"
								+ "<td>Pos</td>"
								+ "<td>Artikelnummer</td>"
								+ "<td>Artikel</td>"
								+ "<td>Einzelpreis</td>"
								+ "<td>Anzahl</td>"
								+ "<td>Summe</td></tr>\n"
						+ "\t\t\t</thead>\n"
						+ "\t\t\t<tbody>\n");
			/* Content einfügen (Länderstatistik)
			 * Für jedes Land wird eine Table-Row eingefügt
			 */ 
			for(int i = 0; i < order.getOrderItem().size(); i++){
				/* fügt eine normale Table-Row ein wenn ein Land weniger als x% Internetnutzer besitzt.
				 * Nur gültig wenn Länder mit weniger als x% geschreiben werden sollen.
				 * Wenn alle Länder geschreiben werden sollen, werden die else if ausgeführt
				 */
					writer.println("\t\t\t\t<tr><td>" + (i+1) + "</td>\n"
							+ "\t\t\t\t<td>" + order.getOrderItem().get(i).getArticleNumber() + "</td>\n"
							+ "\t\t\t\t<td>" + order.getOrderItem().get(i).getName() + "</td>\n"
							+ "\t\t\t\t<td>" + order.getOrderItem().get(i).getPrice() + "</td>\n"
							+ "\t\t\t\t<td>" + order.getOrderItem().get(i).getQuantity() + "</td>\n"
							+ "\t\t\t\t<td>" + order.getOrderItem().get(i).calcTotal() + "</td></tr>\n");
			}		
			
			writer.println("\t\t\t</tbody>\n"
							+ "\t\t</table>\n"
						+ "\t</main>\n");
			
			//Footer schreiben.
			writer.println(dietlera15HTML.getFooter());
					
		} catch (IOException error) {
			System.out.println("Fehler beim Schreiben der Datei. Bitte Pfadangabe prüfen.");
			System.out.println("Details unter \"" + logFile + "\"");
			LogFileWriter(error);
		} finally {
			if (writer != null){
				try {
					writer.close();
					System.out.println("["+ actualDateTime() + "] - " + "Das Schreiben des HTML-Files wurde beendet.");
				} catch (Exception e){}
			}
		}
	}
	
	/**
	 * writes a exception into a log file with actual date and time
	 * @param message for log file
	 */
	private static void LogFileWriter(Exception msg){
		PrintWriter writer = null;
		try {
			// neue Zeile ins Log-File hinzufügen
			writer = new PrintWriter(new FileOutputStream(logFile, true));
			writer.println("["+ actualDateTime() + "] - " + msg);								
		} catch (IOException error) {
			/* wenn das Schreiben ins log file nicht funktioniert,
			 * wird in der Console eine Fehlermeldung ausgegeben
			 */
			System.out.println("Error during writing the Log-File to \"" + logFile + "\"");
			System.out.println("Error-Details: " + error);
		} finally {
			if (writer != null){
				try {
					writer.close();
				} catch (Exception e){}
			}
		}
	}

	/**
	 * writes a message into a log file with actual date and time
	 * @param message for log file
	 */
	private static void LogFileMessage(String msg){
		PrintWriter writer = null;
		try {
			// neue Zeile ins Log-File hinzufügen
			writer = new PrintWriter(new FileOutputStream(logFile, true));
			writer.println("["+ actualDateTime() + "] - " + msg);								
		} catch (IOException error) {
			/* wenn das Schreiben ins log file nicht funktioniert,
			 * wird in der Console eine Fehlermeldung ausgegeben
			 */
			System.out.println("Error during writing the Log-File to \"" + logFile + "\"");
			System.out.println("Error-Details: " + error);
		} finally {
			if (writer != null){
				try {
					writer.close();
				} catch (Exception e){}
			}
		}
	}
	
	
	public static void main(String[] args) {
		if(args.length == 1) {
			folder = new File(args[0]);
			File[] listOfFiles = folder.listFiles();
			for(int i = 0; i < listOfFiles.length; i++) {
				if(listOfFiles[i].getName().endsWith(".csv")) {
					logFile = listOfFiles[i].getParentFile() + "\\"
							+ listOfFiles[i].getName().replaceFirst(".csv", "_log.txt");
					Order order = readOrder(listOfFiles[i]);
					System.out.println(logFile);
					String Destination = listOfFiles[i].getParentFile()
							+ "\\"
							+ listOfFiles[i].getName().replaceFirst(".csv", ".xhtml");
					HTMLWriter(Destination, order);
				}
			}
		}
		// Order order = createDummyOrders();
		// float total = order.getTotalSum();
		// System.out.println("Total price: " + total);
	}
	
	/**
	 * check every parameter of the CSV-File
	 * @param first string of file
	 * @return header is ok
	 */
	private static boolean checkCSV(String[] header){
		boolean[] col = new boolean[] {false, false, false, false};
		if(header.length != 4) return false;
		if(header[0].equals("articelnr")) col[0] = true;
		if(header[1].equals("name")) col[1] = true;
		if(header[2].equals("price")) col[2] = true;
		if(header[3].equals("quant")) col[3] = true;
		return (col[0] && col[1] && col[2] && col[3]);
	}
	
	/**
	 * create actual date and time
	 * @return actual date and time
	 */
	private static String actualDateTime() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}	
}
