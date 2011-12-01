package org.mca.data.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mca.data.DataPart;
import org.mca.data.struct.SubMatrixLocal;

@SuppressWarnings("serial")
public class DoubleMatrixFormat extends DataFormat<Double>{

	private static DecimalFormat format;
	
	private static final double DEFAULT_VALUE = 0.0;
	
	private final static int MAX_INTEGER_DIGIT = 10;
	private final static int MAX_FRACTION_DIGIT = 10;
	
	public DoubleMatrixFormat() {
		format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(MAX_FRACTION_DIGIT);
		format.setMinimumFractionDigits(MAX_FRACTION_DIGIT);
		format.setGroupingUsed(false);
	}
	
	@Override
	public File format(Object data, File out) throws FormatException {
		double[][] matrix = (double[][])data;
		int maxFormatSize = MAX_INTEGER_DIGIT + MAX_FRACTION_DIGIT + 1;
		PrintWriter pw = null;
		int i = 0 ,j = 0;
		try { 
			pw = new PrintWriter(out);
			int height = matrix.length;
		    int width = matrix[0].length;
			for (i = 0; i < height; i++) {
				for (j = 0; j < width; j++) {
					Object value = matrix[i][j];
					double d;
					if (value == null) d = DEFAULT_VALUE;
					else d = (Double)value;
					String s = format.format(d);
					int padding = maxFormatSize - s.length();
					for (int k = 0; k < padding; k++)
						pw.print(' ');
					pw.print(s);
				}
				pw.println();
			}
			pw.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FormatException();
		}finally{
			pw.close();
		}
		return out;
	}

	@Override
	public DataPart parse(File in) throws FormatException {
		double[][] values = null;
		SubMatrixLocal result = null;
		FileReader fr = null;
		try {
			fr = new FileReader(in);
			StreamTokenizer tokenizer= new StreamTokenizer(fr);

			tokenizer.resetSyntax();
			tokenizer.wordChars(0,255);
			tokenizer.whitespaceChars(0, ' ');
			tokenizer.eolIsSignificant(true);
			List<Double> list = new ArrayList<Double>();

			// Ignore initial empty lines
			while (tokenizer.nextToken() == StreamTokenizer.TT_EOL);
			if (tokenizer.ttype == StreamTokenizer.TT_EOF)
				throw new java.io.IOException("Unexpected EOF on matrix read.");
			do {
				list.add(Double.valueOf(tokenizer.sval)); 
			} while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
			List<double[]> matrix = new ArrayList<double[]>();
			int width = list.size();  
			double row[] = new double[width];
			for (int j=0; j<width; j++) 
				row[j]= list.get(j);
			list.clear();
			matrix.add(row);
			while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
				row = new double[width];
				int j = 0;
				do {
					row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
				} while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
				if (j < width) 
					throw new java.io.IOException("row " + list.size() + " is too short.");
				matrix.add(row);
			}
			int height = matrix.size();  			
			
			values = new double[height][width];
			for (int i = 0; i < height; i++) {
				values[i] = matrix.get(i);
			}
			result = new SubMatrixLocal(values);
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
		return result;
	}

}
