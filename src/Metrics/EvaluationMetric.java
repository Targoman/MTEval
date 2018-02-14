package Metrics;


import java.util.List;


public abstract class EvaluationMetric {

//	public abstract void addSentence(Sequence hyp, List<Sequence> refs);
	public abstract double addSentence(Sequence hyp, List<Sequence> refs);
	public abstract double getScore();
}
