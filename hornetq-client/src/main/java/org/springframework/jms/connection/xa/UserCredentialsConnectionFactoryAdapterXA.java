package org.springframework.jms.connection.xa;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;

/**
 * This class was taken from https://jira.springsource.org/browse/SPR-7952.
 * <p/>
 * An adapter for a target JMS {@link ConnectionFactory}, applying the
 * given user credentials to every standard {@code createConnection()} call,
 * that is, implicitly invoking {@code createConnection(username, password)}
 * on the target. All other methods simply delegate to the corresponding methods
 * of the target ConnectionFactory.
 * <p/>
 * <p>Can be used to proxy a target JNDI ConnectionFactory that does not have user
 * credentials configured. Client code can work with the ConnectionFactory without
 * passing in username and password on every {@code createConnection()} call.
 * <p/>
 * <p>In the following example, client code can simply transparently work
 * with the preconfigured "myConnectionFactory", implicitly accessing
 * "myTargetConnectionFactory" with the specified user credentials.
 * <p/>
 * <pre class="code">
 * &lt;bean id="myTargetConnectionFactory" class="org.springframework.jndi.JndiObjectFactoryBean"&gt;
 * &lt;property name="jndiName" value="java:comp/env/jms/mycf"/&gt;
 * &lt;/bean&gt;
 * <p/>
 * &lt;bean id="myConnectionFactory" class="org.springframework.jms.connection.xa.UserCredentialsConnectionFactoryAdapter"&gt;
 * &lt;property name="targetConnectionFactory" ref="myTargetConnectionFactory"/&gt;
 * &lt;property name="username" value="myusername"/&gt;
 * &lt;property name="password" value="mypassword"/&gt;
 * &lt;/bean></pre>
 * <p/>
 * <p>If the "username" is empty, this proxy will simply delegate to the standard
 * {@code createConnection()} method of the target ConnectionFactory.
 * This can be used to keep a UserCredentialsConnectionFactoryAdapter bean
 * definition just for the <i>option</i> of implicitly passing in user credentials
 * if the particular target ConnectionFactory requires it.
 *
 * @author Juergen Hoeller
 * @author Josh Long
 * @see #createConnection
 * @see #createQueueConnection
 * @see #createTopicConnection
 * @since 1.2
 */
public class UserCredentialsConnectionFactoryAdapterXA implements XAConnectionFactory, ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, InitializingBean {

	private ConnectionFactory targetConnectionFactory;

	private String username;

	private String password;

	private final ThreadLocal<JmsUserCredentials> threadBoundCredentials =
			new NamedThreadLocal<JmsUserCredentials>("Current JMS user credentials");

	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 *
	 * @see #doCreateXaConnection
	 */
	public XAConnection createXAConnection() throws JMSException {
		JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateXaConnection(threadCredentials.username, threadCredentials.password);
		} else {
			return doCreateXaConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target XAConnectionFactory
	 */
	public XAConnection createXAConnection(String username, String password) throws JMSException {
		return this.doCreateXaConnection(username, password);
	}

	/**
	 * This implementation delegates to the <code>createXAConnection(username, password)</code> method
	 * of the target <code>XAConnectionFactory</code>, passing in the specified user credentials. If
	 * the specified username is empty, it will simply delegate to the standard
	 * <code>createXAConnection()</code> method of the target ConnectionFactory.
	 *
	 * @param username the username to use
	 * @param password the password to use
	 * @return the XAConnection
	 * @see XAConnectionFactory#createXAConnection()
	 * @see XAConnectionFactory#createXAConnection(String, String)
	 */
	protected XAConnection doCreateXaConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
		if (!(this.targetConnectionFactory instanceof XAConnectionFactory)) {
			throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a XAConnectionFactory");
		}
		XAConnectionFactory xaConnectionFactory = (XAConnectionFactory) this.targetConnectionFactory;
		if (StringUtils.hasLength(username)) {
			return xaConnectionFactory.createXAConnection(username, password);
		} else {
			return xaConnectionFactory.createXAConnection();
		}
	}

	/**
	 * Set the target ConnectionFactory that this ConnectionFactory should delegate to.
	 */
	public void setTargetConnectionFactory(ConnectionFactory targetConnectionFactory) {
		Assert.notNull(targetConnectionFactory, "'targetConnectionFactory' must not be null");
		this.targetConnectionFactory = targetConnectionFactory;
	}

	/**
	 * Set the username that this adapter should use for retrieving Connections.
	 * Default is no specific user.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the password that this adapter should use for retrieving Connections.
	 * Default is no specific password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void afterPropertiesSet() {
		if (this.targetConnectionFactory == null) {
			throw new IllegalArgumentException("Property 'targetConnectionFactory' is required");
		}
	}

	/**
	 * Set user credententials for this proxy and the current thread.
	 * The given username and password will be applied to all subsequent
	 * {@code createConnection()} calls on this ConnectionFactory proxy.
	 * <p>This will override any statically specified user credentials,
	 * that is, values of the "username" and "password" bean properties.
	 *
	 * @param username the username to apply
	 * @param password the password to apply
	 * @see #removeCredentialsFromCurrentThread
	 */
	public void setCredentialsForCurrentThread(String username, String password) {
		this.threadBoundCredentials.set(new JmsUserCredentials(username, password));
	}

	/**
	 * Remove any user credentials for this proxy from the current thread.
	 * Statically specified user credentials apply again afterwards.
	 *
	 * @see #setCredentialsForCurrentThread
	 */
	public void removeCredentialsFromCurrentThread() {
		this.threadBoundCredentials.remove();
	}

	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 *
	 * @see #doCreateConnection
	 */
	public final Connection createConnection() throws JMSException {
		JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateConnection(threadCredentials.username, threadCredentials.password);
		} else {
			return doCreateConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target ConnectionFactory.
	 */
	public Connection createConnection(String username, String password) throws JMSException {
		return doCreateConnection(username, password);
	}

	/**
	 * This implementation delegates to the {@code createConnection(username, password)}
	 * method of the target ConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * {@code createConnection()} method of the target ConnectionFactory.
	 *
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see ConnectionFactory#createConnection(String, String)
	 * @see ConnectionFactory#createConnection()
	 */
	protected Connection doCreateConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
		if (StringUtils.hasLength(username)) {
			return this.targetConnectionFactory.createConnection(username, password);
		} else {
			return this.targetConnectionFactory.createConnection();
		}
	}

	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 *
	 * @see #doCreateQueueConnection
	 */
	public final QueueConnection createQueueConnection() throws JMSException {
		JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateQueueConnection(threadCredentials.username, threadCredentials.password);
		} else {
			return doCreateQueueConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target QueueConnectionFactory.
	 */
	public QueueConnection createQueueConnection(String username, String password) throws JMSException {
		return doCreateQueueConnection(username, password);
	}

	/**
	 * This implementation delegates to the {@code createQueueConnection(username, password)}
	 * method of the target QueueConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * {@code createQueueConnection()} method of the target ConnectionFactory.
	 *
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see QueueConnectionFactory#createQueueConnection(String, String)
	 * @see QueueConnectionFactory#createQueueConnection()
	 */
	protected QueueConnection doCreateQueueConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
		if (!(this.targetConnectionFactory instanceof QueueConnectionFactory)) {
			throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a QueueConnectionFactory");
		}
		QueueConnectionFactory queueFactory = (QueueConnectionFactory) this.targetConnectionFactory;
		if (StringUtils.hasLength(username)) {
			return queueFactory.createQueueConnection(username, password);
		} else {
			return queueFactory.createQueueConnection();
		}
	}

	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 *
	 * @see #doCreateTopicConnection
	 */
	public final TopicConnection createTopicConnection() throws JMSException {
		JmsUserCredentials threadCredentials = this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateTopicConnection(threadCredentials.username, threadCredentials.password);
		} else {
			return doCreateTopicConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target TopicConnectionFactory.
	 */
	public TopicConnection createTopicConnection(String username, String password) throws JMSException {
		return doCreateTopicConnection(username, password);
	}

	/**
	 * This implementation delegates to the {@code createTopicConnection(username, password)}
	 * method of the target TopicConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * {@code createTopicConnection()} method of the target ConnectionFactory.
	 *
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see TopicConnectionFactory#createTopicConnection(String, String)
	 * @see TopicConnectionFactory#createTopicConnection()
	 */
	protected TopicConnection doCreateTopicConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "'targetConnectionFactory' is required");
		if (!(this.targetConnectionFactory instanceof TopicConnectionFactory)) {
			throw new javax.jms.IllegalStateException("'targetConnectionFactory' is not a TopicConnectionFactory");
		}
		TopicConnectionFactory queueFactory = (TopicConnectionFactory) this.targetConnectionFactory;
		if (StringUtils.hasLength(username)) {
			return queueFactory.createTopicConnection(username, password);
		} else {
			return queueFactory.createTopicConnection();
		}
	}

	/**
	 * Inner class used as ThreadLocal value.
	 */
	private static class JmsUserCredentials {

		public final String username;

		public final String password;

		private JmsUserCredentials(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String toString() {
			return "JmsUserCredentials[username='" + this.username + "',password='" + this.password + "']";
		}
	}
}
