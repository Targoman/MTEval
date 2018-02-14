package Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WERMetric extends EvaluationMetric{

	List<Integer> refWords;
	List<Integer> distance;
	
	public WERMetric(){
		refWords = new ArrayList<Integer>();
		distance = new ArrayList<Integer>();
	}
	
	@Override
	public double addSentence(Sequence hyp, List<Sequence> refs) {
		// TODO Auto-generated method stub
		if(refWords.size() == 0){
			for(int i = 0; i < refs.size(); i++){
				refWords.add(0);
				distance.add(0);
			}
		}
		
		double maxScore = 0;
		
		for(int i = 0; i < refs.size(); i++){
			int d = computeEditDistance(hyp, refs.get(i));
			double score = (double)d / (double)refs.get(i).size();
			if(score > maxScore)
				maxScore = score;
			
			distance.set(i, distance.get(i) + d);
			refWords.set(i, refWords.get(i) + refs.get(i).size());
		}
		
		return maxScore;
	}

	public int computeEditDistance(Sequence hyp, Sequence ref){
		int d[][] = new int[hyp.size() + 1][ref.size() + 1];
		int n = hyp.size(), m = ref.size();
		
		for(int i = 0; i <= m; i++)
			d[0][i] = i;
		for(int i = 1; i <= n; i++){
			
			d[i][0] = i;
			for(int j = 1; j <= m; j++){
				int dif = 1;
				if(hyp.get(i - 1).equals(ref.get(j - 1)))
					dif = 0;
				d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + dif));

			}
		}
		
		return d[n][m];
		
	}

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		double maxScore = 0;
		for(int i = 0; i < refWords.size(); i++){
			double score = distance.get(i) / (double)refWords.get(i);
			maxScore = Math.max(maxScore, score);
		}
		return maxScore;
	}

}
