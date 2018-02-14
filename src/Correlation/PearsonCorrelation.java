package Correlation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PearsonCorrelation {

	/**
	 * @param args
	 */
	
	double getCorrelation(ArrayList<Double> x, ArrayList<Double> y){
		double res = 0;
		double x2 = 0;
		double y2 = 0;
		double xy = 0;
		
		double sx = 0, sy = 0, sx2 = 0, sy2 = 0, sxy = 0;
		
		int n = x.size();
		for(int i = 0; i < n; i++){
			x2 = x.get(i) * x.get(i);
			y2 = y.get(i) * y.get(i);
			xy = x.get(i) * y.get(i);
			sx2 += x2;
			sy2 += y2;
			sxy += xy;
			sx += x.get(i);
			sy += y.get(i);
		}
		
		double a = n * sxy - sx * sy;
		double b = Math.sqrt(n * sx2 - sx * sx) * Math.sqrt(n * sy2 - sy * sy);
		res = a / b;
		return res;
	}

	double getCorrelation(String file1, String file2) throws IOException{
		
		ArrayList<Double>x1 = new ArrayList<Double>();
		ArrayList<Double>x2 = new ArrayList<Double>();
		FileInputStream fis1 = new FileInputStream(file1);
		FileInputStream fis2 = new FileInputStream(file2);
		Scanner sc2 = new Scanner(fis2);
		Scanner sc1 = new Scanner(fis1);
		while(sc1.hasNext()){
			
			sc1.next();
			String sys1 = sc1.next();
			sc2.next();
			sc2.next();
			String sys2 = sc2.next();
			double x = sc1.nextDouble();
			double y = sc2.nextDouble();
			if(sys1.equals(sys2)){
				x1.add(x);
				x2.add(1-y);
			}else System.err.println(sys1 + " , " + sys2);
		}
		return getCorrelation(x1, x2);
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String x = "humanAssessments/data_RNK_Spanish-English.csv.SYSRNK";
		String y = "outputs/ter.es.csv";
		PearsonCorrelation spr = new PearsonCorrelation();
		System.out.println(spr.getCorrelation(x, y));
		

	}

}
