package Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PERMetric extends EvaluationMetric{

	List<Integer> refWords;
	List<Integer> correctWords;
	int testWords;
	
	public PERMetric(){
		testWords = 0;
		refWords = new ArrayList<Integer>();
		correctWords = new ArrayList<Integer>();
	}
	
	@Override
	public double addSentence(Sequence hyp, List<Sequence> refs) {
		// TODO Auto-generated method stub
		if(refWords.size() == 0){
			for(int i = 0; i < refs.size(); i++){
				refWords.add(0);
				correctWords.add(0);
			}
		}
		
		Map<String, Integer> hypCounts = new HashMap<String, Integer>();
		int testcnt = 0;
		for(String h : hyp){
			if(hypCounts.containsKey(h))
				hypCounts.put(h, hypCounts.get(h) + 1);
			else hypCounts.put(h, 1);
			testcnt++;
		}
		testWords += testcnt;
		double maxScore = 0;
		for(int i = 0; i < refs.size(); i++){
			Map<String, Integer> refCounts = new HashMap<String, Integer>();
			
			for(String r : refs.get(i)){
				if(refCounts.containsKey(r))
					refCounts.put(r, refCounts.get(r) + 1);
				else refCounts.put(r, 1);
				
			}
			
			refWords.set(i, refWords.get(i) + refs.get(i).size());
		
			int cnt = 0;
			for(String key : hypCounts.keySet()){
				if(refCounts.containsKey(key)){
					cnt += Math.min(hypCounts.get(key), refCounts.get(key));
//					System.out.print(key + " " + cnt + " ||| ");
				}
			}
			correctWords.set(i, correctWords.get(i) + cnt);
			
			int refCnt = refs.get(i).size();
			int corrects = cnt;
//			System.out.println(corrects);
			double score = 1. - (corrects - Math.max(0, testcnt - refCnt)) / (double)refCnt;
			maxScore = Math.max(maxScore, score);
		}
			
		
		return maxScore;
	}


	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		double maxScore = 0;
		for(int i = 0; i < refWords.size(); i++){
			double score = 1. - (correctWords.get(i) - Math.max(0, testWords - refWords.get(i))) / (double)refWords.get(i);
			maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}

}
