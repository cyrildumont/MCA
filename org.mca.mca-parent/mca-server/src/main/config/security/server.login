
/* JAAS login configuration file for server */

org.mca.security.MCA {
    	com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:${mca.home}/conf/security/keystore.server"
	keyStorePasswordURL="file:${mca.home}/conf/security/server.password";
};
