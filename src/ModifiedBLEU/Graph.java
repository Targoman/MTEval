package ModifiedBLEU;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class Graph {

	/**
	 * @param args
	 */
	Vector<Integer> adjList[];
	Vector<Integer> weigths[];
	int n;
	int source, sink;
	int grMap[][];
	public Graph(int n){
//		System.out.println(n);
		adjList = new Vector[n];
		weigths = new Vector[n];
		for(int i = 0; i < n; i++){
			adjList[i] = new Vector<Integer>();
			weigths[i] = new Vector<Integer>();
			
		}
		grMap = new int[n][n];
		this.n = n;
			
		
	}
	
	void addEdge(int i, int j, int w){
		grMap[i][j] = adjList[i].size();
		grMap[j][i] = adjList[j].size();
//		grMap[i][j] = w;
		adjList[i].add(j);
		adjList[j].add(i);
		weigths[i].add(w);
		weigths[j].add(0);
	}
	
	public int maximumMatching(int source, int sink){
		int res = 0;
		while(true){
			int w = findPath(source, sink);
			if(w == 0)
				break;
			res += w;
		}
		return res;
	}
	
	public int findPath(int s, int t){
		Queue<Integer> q = new LinkedList<Integer>();
		int par[] = new int[n];
		int mark[] = new int[n];
		Arrays.fill(par, -1);
		Arrays.fill(mark, 0);
		mark[s] = 1;
		q.add(s);
		while(!q.isEmpty() && mark[t] == 0){
			int x = q.remove();
			int sz = adjList[x].size();
			for(int i = 0; i < sz; i++){
				int nxt = adjList[x].get(i);
				if(mark[nxt] == 0 && weigths[x].get(i) > 0){
					mark[nxt] = Math.min(mark[x], weigths[x].get(i));
					par[nxt] = x;
					q.add(nxt);
				}
			}
//			for(int i = 0; i < n; i++){
//				if(mark[i] == 0 && grMap[x][i] > 0){
//					mark[i] = Math.min(mark[x], grMap[x][i]);
//					par[i] = x;
//					q.add(i);
//				}
//			}
		}
			
		if(mark[t] == 0)
			return 0;
		int res = mark[t];
		int x = t;
		while(x != s){
			int p = par[x];
			int a = grMap[p][x];
			weigths[p].set(a, weigths[p].get(a) - res);
			a = grMap[x][p];
			weigths[x].set(a, weigths[x].get(a) + res);
//			grMap[p][x] -= res;
//			grMap[x][p] += res;
//			
			x = p;
		}
//		System.out.println(res);
		
		return res;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Graph g = new Graph(7);
		g.addEdge(0, 1, 3);
		g.addEdge(0, 2, 1);
		g.addEdge(1, 3, 3);
		g.addEdge(2, 3, 5);
		g.addEdge(2, 4, 4);
		g.addEdge(4, 5, 2);
		g.addEdge(3, 6, 2);
		g.addEdge(5, 6, 3);
		System.out.println(g.maximumMatching(0, 6));
		
		
	}

}
