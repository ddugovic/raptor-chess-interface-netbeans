package raptor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

/**
 * A proxy class to interface with Log4j. This will be useful if we would like
 * to break from Log4j (or logging altogether) at some point.
 */
public class Logger {
	/**
	 * Forces log4j to check for changes to its properties file and reload them.
	 */
	static {
		PropertyConfigurator.configureAndWatch("resources/log4j.properties");
	}
	
	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz);
	}
	
	private final Log serfLogger;
	
	private Logger(Class<?> clazz) {
		serfLogger = LogFactory.getLog(clazz);
	}

	public void info(Object string, Throwable t) {
		serfLogger.info(string, t);		
	}

	public void error(Object string, Throwable t) {
		serfLogger.error(string, t);	
	}
	
	public void fatal(Object string, Throwable t) {
		serfLogger.fatal(string, t);	
	}
	
	public void debug(Object string, Throwable t) {
		serfLogger.debug(string, t);	
	}
	
	public void trace(Object string, Throwable t) {
		serfLogger.trace(string, t);	
	}
	
	public void warn(Object string, Throwable t) {
		serfLogger.warn(string, t);	
	}
	
	public void info(Object string) {
		serfLogger.info(string);		
	}

	public void error(Object string) {
		serfLogger.error(string);	
	}
	
	public void fatal(Object string) {
		serfLogger.fatal(string);	
	}
	
	public void debug(Object string) {
		serfLogger.debug(string);	
	}
	
	public void trace(Object string) {
		serfLogger.trace(string);	
	}
	
	public void warn(Object string) {
		serfLogger.warn(string);	
	}

	public boolean isWarnEnabled() {
		return serfLogger.isWarnEnabled();
	}

	public boolean isErrorEnabled() {
		return serfLogger.isErrorEnabled();
	}

	public boolean isFatalEnabled() {
		return serfLogger.isFatalEnabled();
	}

	public boolean isDebugEnabled() {
		return serfLogger.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return serfLogger.isTraceEnabled();
	}

	public boolean isInfoEnabled() {
		return serfLogger.isInfoEnabled();
	}

	public static void releaseAll() {
		LogFactory.releaseAll();	
	}
}
