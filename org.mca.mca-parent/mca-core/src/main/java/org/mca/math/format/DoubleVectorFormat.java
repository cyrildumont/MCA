package org.mca.math.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.mca.math.DataPart;
import org.mca.math.Iterator;
import org.mca.math.SubVector;
import org.mca.math.SubVectorImpl;

@SuppressWarnings("serial")
public class DoubleVectorFormat extends DataFormat<Double>{

	@Override
	public File format(DataPart<Double> subVector, File out) throws FormatException {
		SubVector<Double> vector = null;
		if (subVector instanceof SubVector)
			vector = (SubVector<Double>)subVector;
		else
			throw new FormatException();
		PrintWriter pw = null;
		try { 
			pw = new PrintWriter(out);
			Iterator<Double> iterator = vector.iterator();
			while (iterator.hasNext()) {
				Double e = iterator.next();
				pw.println(e);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new FormatException();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new FormatException();
		}finally{
			pw.close();
		}
		return null;
	}

	@Override
	public DataPart<Double> parse(File in) throws FormatException {
		List<Double> input = new ArrayList<Double>();
		FileReader fr = null;
		try {
			fr = new FileReader(in);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				input.add(Double.valueOf(s));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		SubVectorImpl<Double> result = new SubVectorImpl<Double>(input);
		return result;
	}

}
