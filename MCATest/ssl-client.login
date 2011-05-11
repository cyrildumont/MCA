
/* JAAS login configuration file for client */

org.mca.Client {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:/home/cyril/MCA/conf/security/keystore.server"
	keyStorePasswordURL="file:server.password";
};
