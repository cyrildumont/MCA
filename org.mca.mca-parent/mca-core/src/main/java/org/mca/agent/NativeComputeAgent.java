package org.mca.agent;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Date;

import org.mca.log.LogUtil;


public abstract class NativeComputeAgent extends AbstractComputeAgent {

	private static final long serialVersionUID = -2950513737977664490L;

	private static final String JNI_LIBRARY = "MCA";
	
	private byte[] byteCode;
	
	private String byteCodeFile;
	
	/**
	 * 
	 * @param parameters
	 * @return
	 */
	private native String executeByteCode(String bytecodeFile, String functionName, Object[] parameters);

	
	public NativeComputeAgent() throws RemoteException {
		super();
	}

	public void downloadByteCode(String dir) throws IOException{
		System.out.println(" byteCode length : " + byteCode.length);
		this.byteCodeFile = dir + "/" + new Date().getTime() + ".bc";
		OutputStream output = new FileOutputStream(byteCodeFile);
		output.write(byteCode);
		output.close();
	}
	
	
	/**
	 * 
	 * @param parameters
	 * @param libraries
	 * @return
	 */
	final protected Object executeNative(String functionName, Object[] parameters){
		LogUtil.info("Natif code execution", getClass());
		LogUtil.info(JNI_LIBRARY + " library loading...", getClass());
		System.loadLibrary(JNI_LIBRARY);
		LogUtil.debug("bytecodeFile : " + byteCodeFile, getClass());
		LogUtil.info(JNI_LIBRARY + " library loaded", getClass());
		return executeByteCode(byteCodeFile, functionName, parameters);
	}
	
	public void setByteCode(byte[] byteCode) {
		this.byteCode = byteCode;
	}




}
