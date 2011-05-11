
/* JAAS login configuration file for server */

org.mca.Worker {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="worker"
	keyStoreURL="file:${mca.home}/conf/security/keystore.worker"
	keyStorePasswordURL="file:worker.password";
};
