
/* JAAS login configuration file for client */

org.mca.Server {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:${mca.home}/conf/security/keystore.server"
	keyStorePasswordURL="file:${mca.home}/conf/security/server.password";
};
