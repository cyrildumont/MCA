
/* JAAS login configuration file for worker */

org.mca.security.MCA {
    	com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="worker"
	keyStoreURL="file:${mca.home}/conf/security/keystore.worker"
	keyStorePasswordURL="file:${mca.home}/conf/security/worker.password";
};
