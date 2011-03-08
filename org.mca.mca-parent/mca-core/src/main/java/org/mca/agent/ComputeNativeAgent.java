package org.mca.agent;

import java.rmi.RemoteException;

import org.mca.entry.DataHandler;
import org.mca.log.LogUtil;


public abstract class ComputeNativeAgent extends ComputeAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2950513737977664490L;

	private static final String JNI_LIBRARY = "MCA";
	
	protected String byteCode;
	
	private DataHandler byteCodeHandler;
	
	/**
	 * 
	 * @param parameters
	 * @return
	 */
	private native String execute(String bytecodeFile, String functionName, String[] parameters);

	
	public ComputeNativeAgent() throws RemoteException {
		super();
	}
	
	/**
	 * 
	 * @param parameters
	 * @param libraries
	 * @return
	 */
	final protected Object executeNative(String[] parameters, String functionName){
		LogUtil.info("Natif code execution", getClass());
		LogUtil.info(JNI_LIBRARY + " library loading...", getClass());
		System.loadLibrary(JNI_LIBRARY);
		LogUtil.debug("bytecodeFile : " + byteCode, getClass());
		LogUtil.info(JNI_LIBRARY + " library loaded", getClass());
		String tmpDir = System.getProperty("mca.home") + "/work/" ; 
		return execute(byteCode, functionName, parameters);
	}
	
	public void setByteCode(String byteCode) {
		this.byteCode = byteCode;
	}

	public String getByteCode() {
		return byteCode;
	}
	
	public DataHandler getByteCodeHandler() {
		return byteCodeHandler;
	}

	public void setByteCodeHandler(DataHandler byteCodeHandler) {
		this.byteCodeHandler = byteCodeHandler;
	}

}
