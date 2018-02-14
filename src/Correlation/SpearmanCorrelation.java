package Correlation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class SpearmanCorrelation {

	ArrayList<pair> x, y;
	
	public SpearmanCorrelation(){
	}
	
	double getCorrelation(ArrayList<Double> x1, ArrayList<Double> x2){
		double res = 0;
		x = new ArrayList<pair>();
		y = new ArrayList<pair>();
		for(int i = 0; i < x1.size(); i++)
			x.add(new pair(x1.get(i), i));
		for(int i = 0; i < x2.size(); i++)
			y.add(new pair(x2.get(i), i));
		Collections.sort(x);
		Collections.sort(y);
		int n = x1.size();
		int rankX[] =new int[n];
		int rankY[] =new int[n];
		for(int i = 0; i < n; i++){
			pair p = x.get(i);
			rankX[p.second] = i;
			p = y.get(i);
			rankY[p.second] = i;
		}
		
		for(int i = 0; i < n; i++)
			res += (rankX[i]- rankY[i])* (rankX[i] - rankY[i]);
		res /= (n*(n * n - 1.));
		res *= 6;
		res = 1 - res;
		
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
				x2.add(1.-y);
			}else System.err.println(sys1 + " , " + sys2);
		}
		return getCorrelation(x1, x2);
		
	}
	
	public static void main(String args[]) throws IOException{
		
		String x = "humanAssessments/data_RNK_Spanish-English.csv.SYSRNK";
		String y = "outputs/ter.es.csv";
		SpearmanCorrelation spr = new SpearmanCorrelation();
		System.out.println(spr.getCorrelation(x, y));
	}
}
class pair implements Comparable{
    double first;
    int second;
	public pair(double a, int b){
		first = a;
		second = b;
	}
	public pair(){
		
	}
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		if(first < ((pair)arg0).first)
			return -1;
		else if(first > ((pair)arg0).first)
			return 1;
		return new Integer(second).compareTo(new Integer(((pair)arg0).second));
		
		
	}
	
}