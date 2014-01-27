/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serialcommunication;

/**
 *
 * @author Colin
 */
//public class SerialCommunication {
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//    }
//}

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class SerialCommunication
{

    Integer box;    
    
    public SerialCommunication()
    {
        super();
    }
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /**test */
    public static class SerialReader implements Runnable
    {
        public static final String url = "jdbc:mysql://sql4.freemysqlhosting.net:3306/";
        public static final String user = "sql427309";
        public static final String pwd = "aH4*uJ5%";

        public static Connection OpenSqlConnection()
                throws ClassNotFoundException, SQLException {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pwd);
            Statement stmt = null;
            try {
                stmt = con.createStatement();
                stmt.execute("USE sql427309");
                stmt.execute("SET time_zone = '-04:00'");
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            return con;
        }
        
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            Connection con = null;
            Statement stmt = null;
            
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                con = OpenSqlConnection();
                String FlowData = "";
                String FlowDataPoint = "";                
                String FlowDataSQL = "";
                String DeviceID = "";
                while ( ( len = this.in.read(buffer)) > -1 )
                {
                    //System.out.print(new String(buffer,0,len));
                    FlowData = FlowData + new String(buffer, 0, len);
                    if (FlowData.indexOf(';') != -1) {
//                        System.out.print(FlowData.substring(0,FlowData.indexOf(',')) + "\n");
                        DeviceID = FlowData.substring(0,FlowData.indexOf(';'));
                        FlowData = FlowData.substring(FlowData.indexOf(';')+1);
                        FlowDataPoint = FlowData.substring(0,FlowData.indexOf(','));
                        System.out.print(FlowDataPoint);
                        if (DeviceID == "Kitchen Sink") {
                            FlowDataSQL = "Insert into Sensor1 values(" + FlowDataPoint + ", NOW())";
                        }
                        stmt = con.createStatement();
                        stmt.executeUpdate(FlowDataSQL);
                        FlowData = FlowData.substring(FlowData.indexOf(',')+1);
                    }
//                    System.out.print("FlowData:" + FlowData + "\n");
//                    FlowDataSQL = "Insert into Sensor1 values(" + FlowData + ", NOW())";
//                    stmt = con.createStatement();
//                    stmt.executeUpdate(FlowDataSQL);
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                
            }
            catch(SQLException e)
            {
                
            }
            finally
            {
                try
                {
                    if (stmt != null) {
                        stmt.close();
                    }
                    if (con != null) {
                        con.close();
                    }
                }
                catch(SQLException e)
                {

                }
            }
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }
    
    public static void main ( String[] args )
    {
        String ArduinoComPort = "COM7";
        System.out.print("Hello World\n");
        try
        {
            (new SerialCommunication()).connect(ArduinoComPort);
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}