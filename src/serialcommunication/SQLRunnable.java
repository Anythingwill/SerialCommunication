/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serialcommunication;

import java.sql.SQLException;

/**
 *
 * @author Colin
 */
public interface SQLRunnable {
    void SQLRun ( ) throws ClassNotFoundException, SQLException;
}