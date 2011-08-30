package org.mca.math.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mca.math.DataPart;
import org.mca.math.SubVectorImpl;

@SuppressWarnings("serial")
public class DoubleVectorFormat extends DataFormat<Double>{

	private static DecimalFormat format;
	
	public DoubleVectorFormat() {
		format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(4);
		format.setMinimumFractionDigits(4);
		format.setGroupingUsed(false);
	}
	
	@Override
	public File format(Object data, File out) throws FormatException {
		Object[] vector = (Object[])data;
		PrintWriter pw = null;
		try { 
			pw = new PrintWriter(out);
			for (int i = 0; i < vector.length; i++) {
				String s = format.format(vector[i]);
				pw.println(s);
			}
		} catch (FileNotFoundException e) {
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
		Double[] values = (Double[])input.toArray();
		SubVectorImpl<Double> result = new SubVectorImpl<Double>(values);
		return result;
	}

}
