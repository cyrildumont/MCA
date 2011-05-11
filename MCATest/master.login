
/* JAAS login configuration file for server */

org.mca.Master {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="master"
	keyStoreURL="file:keystore.master"
	keyStorePasswordURL="file:master.password";
};
