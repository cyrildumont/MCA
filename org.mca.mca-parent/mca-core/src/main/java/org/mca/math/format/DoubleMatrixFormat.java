package org.mca.math.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.mca.math.DataPart;
import org.mca.math.SubMatrixImpl;

@SuppressWarnings("serial")
public class DoubleMatrixFormat extends DataFormat<Double>{

	private static DecimalFormat format;
	
	public DoubleMatrixFormat() {
		format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(4);
		format.setMinimumFractionDigits(4);
		format.setGroupingUsed(false);
	}
	
	@Override
	public File format(Object data, File out) throws FormatException {
		Object[][] matrix = (Object[][])data;
		PrintWriter pw = null;
		try { 
			pw = new PrintWriter(out);
			int height = matrix.length;
		    int width = matrix[0].length;
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Double d = (Double)matrix[i][j];
					String s = format.format(d);
					int padding = Math.max(1,width-s.length());
					for (int k = 0; k < padding; k++)
						pw.print(' ');
					pw.print(s);
				}
				pw.println();
			}
			pw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new FormatException();
		}finally{
			pw.close();
		}
		return out;
	}

	@Override
	public DataPart<Double> parse(File in) throws FormatException {
		Double[][] values = null;
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
			List<Double[]> matrix = new ArrayList<Double[]>();
			int width = list.size();  
			Double row[] = new Double[width];
			for (int j=0; j<width; j++) 
				row[j]= list.get(j);
			list.clear();
			matrix.add(row);
			while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
				row = new Double[width];
				int j = 0;
				do {
					row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
				} while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
				if (j < width) 
					throw new java.io.IOException("Row " + list.size() + " is too short.");
				matrix.add(row);
			}
			int height = matrix.size();  			
			
			values = new Double[height][width];
			for (int i = 0; i < height; i++) {
				values[i] = matrix.get(i);
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
		SubMatrixImpl<Double> result = new SubMatrixImpl<Double>(values);
		return result;
	}

}
