import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
/**
 * This class implements PageRank algorithm on simple graph structure.
 * Put your name(s), ID(s), and section here.
 *		Mr.Thanapon 	Jarukasetphon	5888057
 *		Mr.Papatsapong  Jiraroj-ungkun 	5888060
 *		Mr.Chatchanin 	Yimudom			5888234
 */
public class PageRanker {
	
	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into memory.
	 * Each line in the input file should have the following format:
	 * <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. 
	 * You can assume that a page ID is an integer.
	 */
	
	private double NewPerPlex, PerPlex;	// Perplexity Value
	private int NumOffense = 0;			// Time of offense converge condition
	private final double d = 0.85;		// d is the PageRank damping/teleportation factor; use d = 0.85 as is typical
	private int Size;					// number of pages	
	
	// InBounded-Links -> All In-link graph
	Map<Integer,ArrayList<Integer>> InGraph = new HashMap<Integer,ArrayList<Integer>>();
	// OutBounded-Links -> All Out-link graph
	Map<Integer,ArrayList<Integer>> OutGraph = new HashMap<Integer,ArrayList<Integer>>();
	
	Map<Integer,Integer> Page;											// P = All Pages
	Map<Integer,Integer> SinkPage = new HashMap<Integer,Integer>();		// S = Sink Nodes
	Map<Integer,Double> PR = new HashMap<Integer,Double>();				// PR = PageRank
	
	public void loadData(String inputLinkFilename){
		
		Map<Integer,Integer> TempPage = new HashMap<Integer,Integer>();
		int pageID;
		ArrayList<Integer> inNode;
		
		try {
		     String read;
		     BufferedReader buff = new BufferedReader(new FileReader(inputLinkFilename));
		     
		     while ((read = buff.readLine()) != null) {	 
		    	 String[] tok = read.trim().split(" ");
		    	 pageID = Integer.parseInt(tok[0]);
		    	 inNode = new ArrayList<Integer>();
		    	 TempPage.put(pageID,0);
	           
		    	 for(int i=1;i<tok.length;i++){
	        	   int temp = Integer.parseInt(tok[i]);
	        	   if(!inNode.contains(temp)) inNode.add(temp);
	        	   if(TempPage.get(temp)==null) TempPage.put(temp,0);
		    	 }
		    	 InGraph.put(pageID, inNode);
	         }
	         buff.close();
		} catch(Exception e) {
	         e.printStackTrace();
		}
		
		// Restructure pages from HashMap to TreeMap (in-order) 
		Page = new TreeMap<Integer,Integer>(TempPage);
		
		for(Integer I:InGraph.keySet()){
			ArrayList<Integer> inlist = InGraph.get(I);
			int size = inlist.size();
			for(int i=0;i<size;i++){
				int pagenum = inlist.get(i);
				if(OutGraph.get(pagenum)==null){
					ArrayList<Integer> outlist = new ArrayList<Integer>();
					outlist.add(I);
					OutGraph.put(pagenum, outlist);
				}
				else{
					ArrayList<Integer> outlist = OutGraph.get(pagenum);
					if(!outlist.contains(I)){
						outlist.add(I);
						OutGraph.put(pagenum, outlist);
					}
				}
			}
		}
		for(Integer I:Page.keySet()){
			if(OutGraph.get(I) == null) SinkPage.put(I, 0);
		}
	}
	
	/**
	 * This method will be called after the graph is loaded into the memory.
	 * This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize(){
		Size = Page.size();
		double x = 1/(Size*1.0);
		for(Integer i : Page.keySet()){
			PR.put(i,x);
		}
	}
	
	/**
	 * Computes the perplexity of the current state of the graph. The definition
	 * of perplexity is given in the project specs.
	 */
	public double getPerplexity(){
		double totalP = 0.0;
		for (Integer p : Page.keySet()){
			totalP += PR.get(p)*(Math.log(PR.get(p))/Math.log(2));
		}
		return Math.pow(2, totalP*-1);
	}
	
	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank algorithm).
	 * Returns false otherwise (and PageRank algorithm continue to update the page scores). 
	 */
	public boolean isConverge(){
		int diff = Math.abs((int) NewPerPlex - (int) PerPlex);
		if(diff<1) NumOffense++;
		else NumOffense = 1;
		if(NumOffense == 4) return true;
		return false;
	}
	
	/**
	 * The main method of PageRank algorithm. 
	 * Can assume that initialize() has been called before this method is invoked.
	 * While the algorithm is being run, this method should keep track of the perplexity
	 * after each iteration. 
	 * 
	 * Once the algorithm terminates, the method generates two output files.
	 * [1]	"perplexityOutFilename" lists the perplexity after each iteration on each line. 
	 * 		The output should look something like:
	 *  	
	 *  	183811
	 *  	79669.9
	 *  	86267.7
	 *  	72260.4
	 *  	75132.4
	 *  
	 *  Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate.
	 * 		The output should look something like:
	 * 		
	 * 		1	0.1235
	 * 		2	0.3542
	 * 		3 	0.236
	 * 		
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename){
		Map<Integer,Double> NewPR = new HashMap<Integer,Double>();
		StringBuilder StrP = new StringBuilder();
		do {
			double sinkPR = 0.0;
			PerPlex = getPerplexity();
			for (Integer key : SinkPage.keySet()){
				sinkPR += PR.get(key);
			}
			for (Integer p : Page.keySet()) {
				double newPRtemp = (1-d)/Size;
				newPRtemp += d*sinkPR/Size;
				if(InGraph.containsKey(p)){
					ArrayList<Integer> Mp = InGraph.get(p);
					for(Integer q : Mp){
						int Lq = 1;
						if(OutGraph.containsKey(q)) Lq = OutGraph.get(q).size();
						newPRtemp += d * PR.get(q)/Lq;
					}
				}
				NewPR.put(p, newPRtemp);
			}
			PR.putAll(NewPR);
			NewPerPlex = getPerplexity();
			StrP.append(NewPerPlex+"\n");
		} while(!isConverge());
		
		StringBuilder str = new StringBuilder();
		for(Integer i:PR.keySet()){
			str.append(i+" "+PR.get(i)+"\n");
		}
		
		try {
			BufferedWriter writePR = new BufferedWriter(new FileWriter(prOutFilename));
			writePR.write(str.toString());
			writePR.close();
			BufferedWriter writePerPlex = new BufferedWriter(new FileWriter(perplexityOutFilename));
			writePerPlex.write(StrP.toString());
			writePerPlex.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
			Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() {
				@Override
				public int compare(Entry<K,V> e1, Entry<K,V> e2) {
					return e2.getValue().compareTo(e1.getValue());
				}
			}
		);
		return sortedEntries;
	}
	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K){
		Integer[] rank = new Integer[K];
		int index =0;
		List<Entry<Integer,Double>> rankedPage=entriesSortedByValues(PR);
		for(Entry<Integer,Double> i:rankedPage){
			rank[index] = i.getKey();
			index++;
			if(index==K) break;
		}
		return rank;
	}
	
	public static void main(String args[]){
	
	long startTime = System.currentTimeMillis();
		PageRanker pageRanker =  new PageRanker();
		pageRanker.loadData("citeseer.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(100);
	double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}