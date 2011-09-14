org.mca.User {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="master"
	keyStoreURL="file:${mca.home}/conf/security/keystore.user"
	keyStorePasswordURL="file:${mca.home}/conf/security/user.password";
};
