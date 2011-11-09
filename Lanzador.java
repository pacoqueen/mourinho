/**
 * Ver http://unattended.sourceforge.net/installers.php para opciones 
 * de instalación desatendida en MS-Windows.
 */
package lanzador;

import java.io.*;
import java.lang.IndexOutOfBoundsException;

/**
 * @author bogado
 *
 */
public class Lanzador {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		String ruta_ejecutable;
		try{
			ruta_ejecutable = new String(args[0]);
		}catch (IndexOutOfBoundsException e){
			String ruta_xterm = new String("/usr/bin/xterm");
			String ruta_notepad = new String("%SystemRoot%\\notepad.exe");
			boolean hay_xterm = (new File(ruta_xterm)).exists();
			if (hay_xterm)
				ruta_ejecutable = ruta_xterm;
			else
				ruta_ejecutable = ruta_notepad;
		}
		final ProcessBuilder pb = new ProcessBuilder(ruta_ejecutable);
		// Unir su salida de errores con salida estándar. 
		pb.redirectErrorStream(true);
		final Process p = pb.start();
		OutputStream output = p.getOutputStream();
		InputStream input = p.getInputStream();
		// Dos hilos para manejar los flujos de entrada y salida.
		new Thread(new Receiver(input)).start();
		new Thread(new Sender(output)).start();
		try{
			p.waitFor();
		}catch (InterruptedException e){
			Thread.currentThread().interrupt();
		}
		System.out.printf("Proceso hijo %s finalizado.\n", ruta_ejecutable);
		// El proceso hijo ya ha finalizado. El hilo Receiver continuará 
		// activo hasta que haya procesado toda la salida. Ahora cierro la 
		// salida de errores. La de entrada y salida se cierran ellas mismas 
		// en los propios hilos.
		p.getErrorStream().close();
	}
}

/**
 * thread to send output to the child.
 */
final class Sender implements Runnable
    {
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * e.g. \n \r\n or \r, whatever system uses to separate lines in a text file. Only used inside multiline fields. The
     * file itself should use Windows format \r \n, though \n by itself will alsolineSeparator work.
     */
    private static final String lineSeparator = System.getProperty( "line.separator" );

    // ------------------------------ FIELDS ------------------------------

    /**
     * stream to send output to child on
     */
    private final OutputStream os;
    // -------------------------- PUBLIC INSTANCE  METHODS --------------------------

    /**
     * method invoked when Sender thread started.  Feeds dummy data to child.
     */
    public void run()
        {
        try
            {
            final BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( os ), 50 /* keep small for tests */ );
            for ( int i = 99; i >= 0; i-- )
                {
                bw.write( "There are " + i + " bottles of beer on the wall, " + i + " bottles of beer." );
                bw.write( lineSeparator );
                }
            bw.close();
            }
        catch ( IOException e )
            {
            throw new IllegalArgumentException( "IOException sending data to child process." );
            }
        }

    // --------------------------- CONSTRUCTORS ---------------------------

    /**
     * constructor
     *
     * @param os stream to use to send data to child.
     */
    Sender( OutputStream os )
        {
        this.os = os;
        }
    }

/**
 * thread to read output from child
 */
class Receiver implements Runnable
    {
    // ------------------------------ FIELDS ------------------------------

    /**
     * stream to receive data from child
     */
    private final InputStream is;
    // -------------------------- PUBLIC INSTANCE  METHODS --------------------------

    /**
     * method invoked when Receiver thread started.  Reads data from child and displays in on System.out.
     */
    public void run()
        {
        try
            {
            final BufferedReader br = new BufferedReader( new InputStreamReader( is ), 50 /* keep small for testing */ );
            String line;
            while ( ( line = br.readLine() ) != null )
                {
                System.out.println( line );
                }
            br.close();
            }
        catch ( IOException e )
            {
            throw new IllegalArgumentException( "IOException receiving data from child process." );
            }
        }

    // --------------------------- CONSTRUCTORS ---------------------------

    /**
     * contructor
     *
     * @param is stream to receive data from child
     */
    Receiver( InputStream is )
        {
        this.is = is;
        }
    }
